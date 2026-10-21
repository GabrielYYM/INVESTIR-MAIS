package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.AccountEmailRequested;
import org.springframework.modulith.events.ApplicationModuleListener;
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import com.repositorio.investir_mais.infrastructure.token.AccountTokenRepository;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationEmailHandler {
    private final JavaMailSender mailSender;

    private final WebApplicationProperties properties;
    private final AccountTokenRepository tokens;

    @ApplicationModuleListener
    public void afterAccountCommit(AccountEmailRequested event) {
        String purpose = event.purpose() == AccountEmailRequested.Purpose.VERIFY_EMAIL ? "EMAIL_VERIFICATION" : "PASSWORD_RESET";
        if (!tokens.existsByTokenValueAndUsernameAndPurposeAndExpiresAtAfter(
                event.token(), event.email(), purpose, Instant.now())) {
            return;
        }
        switch (event.purpose()) {
            case VERIFY_EMAIL -> sendEmailVerificationLink(event.email(), event.token());
            case PASSWORD_RESET -> sendPasswordResetEmail(event.email(), event.token());
        }
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
