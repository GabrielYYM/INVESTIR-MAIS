package com.repositorio.investir_mais.domain.user.DTO;

import java.util.UUID;
public record UserResponseDTO (
        UUID id,
        String name,
        String email
) {}
