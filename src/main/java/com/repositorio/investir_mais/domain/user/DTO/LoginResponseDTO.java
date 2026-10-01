package com.repositorio.investir_mais.domain.user.DTO;

public record LoginResponseDTO(
    String accessToken,
    String tokenType,
    long expiresInSeconds
) {}
