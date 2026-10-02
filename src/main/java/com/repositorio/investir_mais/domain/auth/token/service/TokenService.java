package com.repositorio.investir_mais.domain.auth.token.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import lombok.NonNull;


@Service
public class TokenService implements TokenProviderService {

    @Value("${api.security.token.secret}")
    private String secret;

    private static final long EXPIRATION_MINUTES = 60;

    @Override
    public String generateToken(@NonNull UUID userId) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Instant now = Instant.now();

        return JWT.create()
                .withIssuer(MessageConstants.Auth.TOKEN_ISSUER)
                .withSubject(userId.toString())
                .withIssuedAt(now)
                .withExpiresAt(now.plus(
                                Duration.ofMinutes(EXPIRATION_MINUTES)
                        )
                )
                .sign(algorithm);
    }

    @Override
    public String validateToken(@NonNull String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer(MessageConstants.Auth.TOKEN_ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException _) {
            throw new IllegalArgumentException(MessageConstants.Auth.ERR_INVALID_JWT);
        }
    }


    @Override
    public Instant getExpiration(@NonNull String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer(MessageConstants.Auth.TOKEN_ISSUER)
                    .build()
                    .verify(token)
                    .getExpiresAt()
                    .toInstant();
        } catch (JWTVerificationException exception) {
            throw new IllegalArgumentException(MessageConstants.Auth.ERR_INVALID_JWT);
        }
    }
}
