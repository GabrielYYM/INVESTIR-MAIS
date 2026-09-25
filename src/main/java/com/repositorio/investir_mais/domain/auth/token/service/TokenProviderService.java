package com.repositorio.investir_mais.domain.auth.token.service;

import java.time.Instant;
import java.util.UUID;


public interface TokenProviderService {

    String generateToken(UUID userId);

    String validateToken(String token);

    Instant getExpiration(String token);
}
