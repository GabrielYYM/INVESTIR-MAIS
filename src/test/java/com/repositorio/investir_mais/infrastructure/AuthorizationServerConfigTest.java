package com.repositorio.investir_mais.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.context.ActiveProfiles;

import com.repositorio.investir_mais.domain.user.repository.UserRepository;

@SpringBootTest(classes = {
    AuthorizationServerConfig.class, 
    SecurityConfig.class, 
    EmailOttHandler.class,
    WebMvcAutoConfiguration.class
})
@ActiveProfiles("test")
class AuthorizationServerConfigTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JavaMailSender mailSender;

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void registeredClientRepository_ShouldConfigureInvestirMaisAppClient() {
        RegisteredClient client = registeredClientRepository.findByClientId("investir-mais-app");
        assertNotNull(client);
        assertEquals("investir-mais-app", client.getClientId());
        assertTrue(client.getRedirectUris().contains("http://localhost:3000/callback"));
    }

    @Test
    void jwtEncoderAndDecoder_ShouldEncodeAndDecodeTokenSuccessfully() {
        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8080")
                .subject("usuario-teste")
                .build();

        Jwt encodedJwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));
        assertNotNull(encodedJwt);

        Jwt decodedJwt = jwtDecoder.decode(encodedJwt.getTokenValue());
        assertEquals("usuario-teste", decodedJwt.getSubject());
    }
}