package com.repositorio.investir_mais.domain.user.DTO;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDTO(
    @NotBlank(message = "O e-mail é obrigatório.")
    String email
) {
}
