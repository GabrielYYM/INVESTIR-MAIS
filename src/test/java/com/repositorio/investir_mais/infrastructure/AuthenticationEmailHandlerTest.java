package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import com.repositorio.investir_mais.infrastructure.token.AccountTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationEmailHandlerTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private AccountTokenRepository tokens;

    private AuthenticationEmailHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AuthenticationEmailHandler(mailSender, new WebApplicationProperties("http://localhost:8080", new WebApplicationProperties.Cors("http://localhost:5173")), tokens);
    }

    @Test
    void sendLoginCode_ShouldSendSixDigitCodeByEmail() {
        handler.sendLoginCode("dev@investir.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("dev@investir.com", message.getTo()[0]);
        assertEquals("Seu código de acesso - Investir Mais", message.getSubject());
        assertTrue(message.getText().contains("123456"));
        assertTrue(message.getText().contains("5 minutos"));
    }

    @Test
    void sendPasswordResetEmail_ShouldSendPurposeSpecificCode() {
        handler.sendPasswordResetEmail("dev@investir.com", "reset-code");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("Redefinição de senha - Investir Mais", message.getSubject());
        assertTrue(message.getText().contains("reset-code"));
        assertTrue(message.getText().contains("redefinir a senha"));
    }
}
