package com.repositorio.investir_mais.domain.token.DTO;

import java.time.Instant;

public class TokenDTO {
    /** Internal issuance data; never returned by the HTTP controllers. */
    public record Issued(String tokenValue, String username, Instant expiresAt) {}
    public record MessageResponse(String message) {}
    public record VerificationResponse(String message, boolean signedIn) {}
    public record CsrfResponse(String token) {}
}
