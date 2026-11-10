package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.AccountEmailRequested;
import com.repositorio.investir_mais.domain.AccountSecurityService;
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
<<<<<<< HEAD
=======
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
>>>>>>> origin/dev
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

@Component
public class AuthenticationEmailHandler {
    private static final String RETRY_DELAY_PROPERTY = "${app.mail.retry-delay:30s}";

    private final ResendEmailSender resend;
    private final WebApplicationProperties properties;
    private final AccountSecurityService accountSecurity;
    private final FailedEventPublications publications;
    private final Duration retryDelay;

    public AuthenticationEmailHandler(ResendEmailSender resend, WebApplicationProperties properties,
            AccountSecurityService accountSecurity, FailedEventPublications publications,
            @Value(RETRY_DELAY_PROPERTY) Duration retryDelay) {
        Assert.isTrue(retryDelay.compareTo(Duration.ofSeconds(1)) >= 0,
                "app.mail.retry-delay deve ser de pelo menos 1 segundo");
        this.resend = resend;
        this.properties = properties;
        this.accountSecurity = accountSecurity;
        this.publications = publications;
        this.retryDelay = retryDelay;
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

    @Scheduled(fixedDelayString = RETRY_DELAY_PROPERTY, initialDelayString = RETRY_DELAY_PROPERTY)
    public void retryFailed() {
        publications.resubmit(ResubmissionOptions.defaults().withBatchSize(50).withMaxInFlight(10)
                .withMinAge(retryDelay)
                .withFilter(publication -> publication.getEvent() instanceof AccountEmailRequested));
    }

    public void sendEmailVerificationLink(String email, String tokenValue) {
<<<<<<< HEAD
        String link = frontendLink("/confirmar-email")
                .queryParam("token", tokenValue)
                .build()
                .encode()
                .toUriString();
=======
        String link = UriComponentsBuilder
            .fromUriString(properties.frontendUrl())
            .replacePath("/confirmar-email")
            .replaceQuery(null)
            .queryParam("token", tokenValue)
            .build()
            .encode()
            .toUriString();
>>>>>>> origin/dev

        String text = "Acesse o link abaixo para confirmar seu e-mail (expira em 5 minutos):\n" + link;
        String html = """
                <p>Abra o link abaixo para confirmar seu e-mail.</p>
                <p><a href="%s">Confirmar e-mail</a></p>
                <p>O link expira em 5 minutos.</p>
                """.formatted(HtmlUtils.htmlEscape(link));
        resend.send(email, "Verifique seu e-mail - Investir Mais", text, html, idempotencyKey("verify-email", tokenValue));
    }

    public void sendPasswordResetEmail(String email, String tokenValue) {
        String text = "Seu código para redefinir a senha é: " + tokenValue + "\nEle expira em 5 minutos.";
        resend.send(email, "Redefinição de senha - Investir Mais", text, null, idempotencyKey("password-reset", tokenValue));
    }

    @Async
    public void sendLoginLink(String email, String code, String returnTo) {
        String link = frontendLink("/entrar/link")
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
<<<<<<< HEAD
        String html = """
                <p>Abra o link no mesmo navegador em que você digitou a senha.</p>
                <p><a href="%s">Concluir acesso</a></p>
                <p>Se preferir, digite este código na tela que já está aberta: <strong>%s</strong></p>
                <p>O acesso expira em 5 minutos.</p>
                """.formatted(HtmlUtils.htmlEscape(link), HtmlUtils.htmlEscape(code));
        resend.send(email, "Conclua seu acesso - Investir Mais", text, html, null);
    }

    private UriComponentsBuilder frontendLink(String path) {
        return UriComponentsBuilder.fromUriString(properties.frontendUrl()).replacePath(path).replaceQuery(null);
=======
        var message = mailSender.createMimeMessage();
        try {
            var mail = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED, StandardCharsets.UTF_8.name());
            if (senderEmail != null && !senderEmail.isBlank()) {
                mail.setFrom(senderEmail, "Investir Mais");
            }
            mail.setTo(email);
            mail.setSubject("Conclua seu acesso - Investir Mais");
            mail.setText(text, """
                    <p>Abra o link no mesmo navegador em que você digitou a senha.</p>
                    <p><a href="%s">Concluir acesso</a></p>
                    <p>Se preferir, digite este código na tela que já está aberta: <strong>%s</strong></p>
                    <p>O acesso expira em 5 minutos.</p>
                    """.formatted(HtmlUtils.htmlEscape(link), HtmlUtils.htmlEscape(code)));
        } catch (MessagingException | UnsupportedEncodingException exception) {
            throw new MailPreparationException(exception);
        }
        mailSender.send(message);
>>>>>>> origin/dev
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

    private static String idempotencyKey(String purpose, String token) {
        return purpose + "/" + DigestUtils.sha256Hex(token);
    }
}
