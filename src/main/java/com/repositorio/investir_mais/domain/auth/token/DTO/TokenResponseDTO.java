package com.repositorio.investir_mais.domain.auth.token.DTO;

import jakarta.validation.constraints.NotBlank;

public record TokenResponseDTO(
        @NotBlank(message = "O token é obrigatório.")
        String token
) {}
