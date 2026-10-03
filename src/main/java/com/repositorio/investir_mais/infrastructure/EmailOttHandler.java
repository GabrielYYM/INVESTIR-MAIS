package com.repositorio.investir_mais.infrastructure;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailOttHandler implements OneTimeTokenGenerationSuccessHandler {
    private final JavaMailSender mailSender;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    public void sendOttEmail(String email, String tokenValue) {
        String link = UriComponentsBuilder
                .fromUriString(publicBaseUrl)
                .path("/login/ott")
                .queryParam("token", tokenValue)
                .build()
                .encode()
                .toUriString();

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(email);
        mail.setSubject("Seu link de acesso e ativação");
        mail.setText("Acesse o link abaixo para entrar e ativar sua conta (expira em 5 minutos):\n" + link);
        mailSender.send(mail);
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            OneTimeToken oneTimeToken) throws IOException, ServletException {

        sendOttEmail(oneTimeToken.getUsername(), oneTimeToken.getTokenValue());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\": \"Link de acesso enviado para o seu e-mail!\"}");
    }
}
