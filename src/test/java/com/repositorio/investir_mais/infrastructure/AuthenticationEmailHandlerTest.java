package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.AccountSecurityService;
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class AuthenticationEmailHandlerTest {
    private static final String EMAILS_URL = "https://api.resend.com/emails";
    private static final String RECIPIENT = "aluno@example.test";

    @Mock
    private AccountSecurityService accountSecurity;

    @Mock
    private FailedEventPublications publications;

    private MockRestServiceServer server;
    private AuthenticationEmailHandler handler;

    @BeforeEach
    void setUp() {
        var rateLimit = new WebApplicationProperties.RateLimit(10, Duration.ofMinutes(1), "investir-mais:rate-limit");
        var properties = new WebApplicationProperties("http://localhost:8080",
                new WebApplicationProperties.Cors("http://localhost:5173"), new WebApplicationProperties.Security(rateLimit));
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        var resend = new ResendEmailSender(builder, "test-key", "Investir Mais <conta@example.test>");
        handler = new AuthenticationEmailHandler(resend, properties, accountSecurity, publications, Duration.ofSeconds(30));
    }

    @Test
    void sendsConfirmationLinkWithConfiguredSender() {
        String link = "http://localhost:5173/confirmar-email?token=verify-token";
        server.expect(requestTo(EMAILS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(header("Idempotency-Key", "verify-email/" + DigestUtils.sha256Hex("verify-token")))
                .andExpect(jsonPath("$.from").value("Investir Mais <conta@example.test>"))
                .andExpect(jsonPath("$.to[0]").value(RECIPIENT))
                .andExpect(jsonPath("$.text").value(containsString(link)))
                .andExpect(jsonPath("$.html").value(containsString("href=\"" + link + "\">Confirmar e-mail</a>")))
                .andRespond(withSuccess());

        handler.sendEmailVerificationLink(RECIPIENT, "verify-token");

        server.verify();
    }

    @Test
    void loginEmailContainsLinkWithTokenInFragmentAndFallbackCode() {
        String link = "http://localhost:5173/entrar/link?retorno=%2Feducacao%3Fpagina%3D2#ott=123456";
        server.expect(requestTo(EMAILS_URL))
                .andExpect(headerDoesNotExist("Idempotency-Key"))
                .andExpect(jsonPath("$.to[0]").value(RECIPIENT))
                .andExpect(jsonPath("$.subject").value("Conclua seu acesso - Investir Mais"))
                .andExpect(jsonPath("$.text").value(containsString(link)))
                .andExpect(jsonPath("$.text").value(containsString("tela que já está aberta: 123456")))
                .andExpect(jsonPath("$.text").value(containsString("5 minutos")))
                .andExpect(jsonPath("$.html").value(containsString("href=\"" + link + "\">Concluir acesso</a>")))
                .andRespond(withSuccess());

        handler.sendLoginLink(RECIPIENT, "123456", "/educacao?pagina=2");

        server.verify();
    }

    @Test
    void passwordResetUsesPurposeSpecificCodeAndSanitizesProviderFailure() {
        server.expect(requestTo(EMAILS_URL))
                .andExpect(header("Idempotency-Key", "password-reset/" + DigestUtils.sha256Hex("reset-token")))
                .andExpect(jsonPath("$.subject").value("Redefinição de senha - Investir Mais"))
                .andExpect(jsonPath("$.text").value(containsString("redefinir a senha é: reset-token")))
                .andExpect(jsonPath("$.html").doesNotHaveJsonPath())
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("private provider details"));

        var failure = assertThrows(RestClientException.class,
                () -> handler.sendPasswordResetEmail(RECIPIENT, "reset-token"));

        assertEquals("Resend retornou HTTP 429", failure.getMessage());
        server.verify();
    }
}
