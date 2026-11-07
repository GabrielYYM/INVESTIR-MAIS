package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.AccountEmailRequested;
import com.repositorio.investir_mais.domain.AccountSecurityService;
import jakarta.mail.MessagingException;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Component
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
        String link = UriComponentsBuilder
            .fromUriString(properties.publicBaseUrl())
            .path("/api/users/verify-email")
            .queryParam("token", tokenValue)
            .build()
            .encode()
            .toUriString();

        sendSimpleEmail(email, "Verifique seu e-mail - Investir Mais", "Acesse o link abaixo para confirmar seu e-mail (expira em 5 minutos):\n" + link);
    }

    public void sendPasswordResetEmail(String email, String tokenValue) {
        sendSimpleEmail(email, "Redefinição de senha - Investir Mais", "Seu código para redefinir a senha é: " + tokenValue + "\nEle expira em 5 minutos.");
    }

    @Async
    public void sendLoginLink(String email, String code, String returnTo) {
        String link = UriComponentsBuilder.fromUriString(properties.cors().allowedOrigin())
                .replacePath("/entrar/link")
                .replaceQuery(null)
                .queryParam("retorno", "{returnTo}")
                .fragment("ott={token}")
                .encode()
                .buildAndExpand(safeReturnPath(returnTo), code)
                .toUriString();

        String text = """
                Abra o link abaixo no mesmo navegador em que você digitou a senha.

                %s

                Se preferir, digite este código na tela que já está aberta: %s
                O acesso expira em 5 minutos.
                """.formatted(link, code);
        var message = mailSender.createMimeMessage();
        try {
            var mail = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED, StandardCharsets.UTF_8.name());
            mail.setTo(email);
            mail.setSubject("Conclua seu acesso - Investir Mais");
            mail.setText(text, """
                    <p>Abra o link no mesmo navegador em que você digitou a senha.</p>
                    <p><a href="%s">Concluir acesso</a></p>
                    <p>Se preferir, digite este código na tela que já está aberta: <strong>%s</strong></p>
                    <p>O acesso expira em 5 minutos.</p>
                    """.formatted(HtmlUtils.htmlEscape(link), HtmlUtils.htmlEscape(code)));
        } catch (MessagingException exception) {
            throw new MailPreparationException(exception);
        }
        mailSender.send(message);
    }

    private static String safeReturnPath(String value) {
        if (value == null || value.length() > 2048 || !value.startsWith("/") || value.startsWith("//")) {
            return "/";
        }
        try {
            URI uri = URI.create(value);
            return uri.getRawAuthority() == null && uri.getRawFragment() == null ? value : "/";
        } catch (IllegalArgumentException exception) {
            return "/";
        }
    }

    private void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(text);
        mailSender.send(mail);
    }
}
