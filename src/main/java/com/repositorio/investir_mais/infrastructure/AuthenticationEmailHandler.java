package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.AccountEmailRequested;
import com.repositorio.investir_mais.domain.AccountSecurityService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class AuthenticationEmailHandler {
    private final JavaMailSender mailSender;

    private final WebApplicationProperties properties;
    private final AccountSecurityService accountSecurity;
    private final FailedEventPublications publications;
    private final Duration retryDelay;

    public AuthenticationEmailHandler(JavaMailSender mailSender, WebApplicationProperties properties,
            AccountSecurityService accountSecurity, FailedEventPublications publications,
            @Value("${app.mail.retry-delay:30s}") String retryDelay) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.accountSecurity = accountSecurity;
        this.publications = publications;
        this.retryDelay = DurationStyle.detectAndParse(retryDelay);
        if (this.retryDelay.compareTo(Duration.ofSeconds(1)) < 0) {
            throw new IllegalArgumentException("app.mail.retry-delay deve ser de pelo menos 1 segundo");
        }
    }

    @ApplicationModuleListener
    public void afterAccountCommit(AccountEmailRequested event) {
        if (!accountSecurity.isTokenActive(event)) {
            return;
        }
        switch (event.purpose()) {
            case VERIFY_EMAIL -> sendEmailVerificationLink(event.email(), event.token());
            case PASSWORD_RESET -> sendPasswordResetEmail(event.email(), event.token());
        }
    }

    @Scheduled(fixedDelayString = "${app.mail.retry-delay:30s}", initialDelayString = "${app.mail.retry-delay:30s}")
    public void retryFailed() {
        publications.resubmit(ResubmissionOptions.defaults().withBatchSize(50).withMaxInFlight(10)
                .withMinAge(retryDelay)
                .withFilter(publication -> publication.getEvent() instanceof AccountEmailRequested));
    }

    public void sendEmailVerificationLink(String email, String tokenValue) {
        log.info("Iniciando envio assíncrono de link de verificação de e-mail para: {}", email);
        String link = UriComponentsBuilder
            .fromUriString(properties.publicBaseUrl())
            .path("/api/users/verify-email")
            .queryParam("token", tokenValue)
            .build()
            .encode()
            .toUriString();

        sendSimpleEmail(email, "Verifique seu e-mail - Investir Mais", "Acesse o link abaixo para confirmar seu e-mail (expira em 5 minutos):\n" + link);
        log.info("Link de verificação enviado com sucesso para: {}", email);
    }

    public void sendPasswordResetEmail(String email, String tokenValue) {
        log.info("Iniciando envio assíncrono de código de redefinição de senha para: {}", email);
        sendSimpleEmail(email, "Redefinição de senha - Investir Mais", "Seu código para redefinir a senha é: " + tokenValue + "\nEle expira em 5 minutos.");
    }

    @Async
    public void sendLoginCode(String email, String code) {
        log.info("Enviando código de login para: {}", email);
        sendSimpleEmail(email, "Seu código de acesso - Investir Mais", "Seu código para entrar no Investir Mais é: " + code + "\nEle expira em 5 minutos.");
    }

    private void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(text);
        mailSender.send(mail);
    }
}
