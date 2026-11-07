package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import com.repositorio.investir_mais.domain.AccountSecurityService;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.modulith.events.FailedEventPublications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationEmailHandlerTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private AccountSecurityService accountSecurity;

    @Mock
    private FailedEventPublications publications;

    private AuthenticationEmailHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AuthenticationEmailHandler(mailSender, new WebApplicationProperties("http://localhost:8080", new WebApplicationProperties.Cors("http://localhost:5173")), accountSecurity, publications, "30s");
    }

    @Test
    void loginEmailContainsMagicLinkWithTokenInFragmentAndFallbackCode() throws Exception {
        var message = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(message);
        handler.sendLoginLink("dev@investir.com", "123456", "/educacao?pagina=2");
        verify(mailSender).send(message);
        message.saveChanges();

        assertEquals("dev@investir.com", message.getAllRecipients()[0].toString());
        assertEquals("Conclua seu acesso - Investir Mais", message.getSubject());
        var body = (Multipart) message.getContent();
        var alternatives = (Multipart) body.getBodyPart(0).getContent();
        String text = (String) alternatives.getBodyPart(0).getContent();
        String html = (String) alternatives.getBodyPart(1).getContent();
        URI link = text.lines().filter(line -> line.startsWith("http")).findFirst().map(URI::create).orElseThrow();
        assertEquals("localhost:5173", link.getAuthority());
        assertEquals("/entrar/link", link.getPath());
        assertEquals("retorno=/educacao?pagina=2", link.getQuery());
        assertEquals("ott=123456", link.getFragment());
        assertTrue(text.contains("tela que já está aberta: 123456"));
        assertTrue(text.contains("5 minutos"));
        assertTrue(html.contains("href=\"" + link + "\">Concluir acesso</a>"));
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
