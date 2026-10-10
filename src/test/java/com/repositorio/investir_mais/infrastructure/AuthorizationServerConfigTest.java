package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthorizationServerConfigTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
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
        assertTrue(client.getRedirectUris().contains("http://127.0.0.1:8080/login/oauth2/code/investir-mais-client"));
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