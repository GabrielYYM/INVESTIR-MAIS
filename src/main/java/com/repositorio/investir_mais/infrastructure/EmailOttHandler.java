package com.repositorio.investir_mais.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailOttHandler implements OneTimeTokenGenerationSuccessHandler {

    private final JavaMailSender mailSender;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @Async
    public void sendOttEmail(String email, String tokenValue) {
        log.info("Iniciando envio assíncrono de e-mail com link OTT para: {}", email);
        String link = UriComponentsBuilder
                .fromUriString(publicBaseUrl)
                .path("/login/ott")
                .queryParam("token", tokenValue)
                .build()
                .encode()
                .toUriString();

        sendSimpleEmail(email, "Seu link de acesso e ativação", "Acesse o link abaixo para entrar e ativar sua conta (expira em 5 minutos):\n" + link);
        log.info("E-mail OTT enviado com sucesso para: {}", email);
    }

    @Async
    public void sendTwoFactorCode(String email, String tokenValue) {
        log.info("Iniciando envio assíncrono do código 2FA para: {}", email);
        sendSimpleEmail(email, "Código de verificação - Investir Mais", "Seu código de acesso é: " + tokenValue + "\nEle expira em 5 minutos.");
        log.info("Código 2FA enviado com sucesso para: {}", email);
    }

    private void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(text);
            mailSender.send(mail);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail simples para o destinatário: {}. Erro: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken) throws IOException {
        log.info("Processando geração de token OTT para o usuário: {}", oneTimeToken.getUsername());
        sendOttEmail(oneTimeToken.getUsername(), oneTimeToken.getTokenValue());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\": 200, \"message\": \"Link de acesso enviado para o seu e-mail!\"}");
    }
}