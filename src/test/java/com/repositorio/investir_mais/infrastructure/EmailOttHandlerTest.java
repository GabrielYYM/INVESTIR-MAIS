package com.repositorio.investir_mais.infrastructure;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class EmailOttHandlerTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailOttHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EmailOttHandler(mailSender);
        ReflectionTestUtils.setField(handler, "publicBaseUrl", "http://localhost:8080");
    }

    @Test
    void sendOttEmail_ShouldBuildAndSendEmailSuccessfully() {
        String email = "dev@investir.com";
        String token = "abc-123-token";

        handler.sendOttEmail(email, token);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage mailEnviado = captor.getValue();
        assertEquals(email, mailEnviado.getTo()[0]);
        assertEquals("Seu link de acesso e ativação", mailEnviado.getSubject());
        assertTrue(mailEnviado.getText().contains("http://localhost:8080/login/ott?token=abc-123-token"));
    }

    @Test
    void handle_ShouldSendEmailAndConfigureHttpResponseCorrectly() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);

        OneTimeToken ott = new DefaultOneTimeToken("token-valido", "user@investir.com", Instant.now().plusSeconds(300));

        handler.handle(request, response, ott);

        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");

        printWriter.flush();
        assertEquals("{\"message\": \"Link de acesso enviado para o seu e-mail!\"}", stringWriter.toString());
    }

    @Test
    void handle_WhenEmailSendingFails_ShouldThrowException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        OneTimeToken ott = new DefaultOneTimeToken("token-123", "user@investir.com", Instant.now().plusSeconds(300));

        doThrow(new MailSendException("Erro SMTP")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(MailSendException.class, () -> handler.handle(request, response, ott));
    }
}