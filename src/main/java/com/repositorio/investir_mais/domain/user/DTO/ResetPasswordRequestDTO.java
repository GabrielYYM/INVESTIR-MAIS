package com.repositorio.investir_mais.domain.user.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDTO(
    @NotBlank(message = "O token é obrigatório.")
    String token,

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 6, max = 20, message = "A nova senha deve ter entre 6 e 20 caracteres.")
    String newPassword
) {
}
