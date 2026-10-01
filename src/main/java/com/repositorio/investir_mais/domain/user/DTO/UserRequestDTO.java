package com.repositorio.investir_mais.domain.user.DTO;

import com.repositorio.investir_mais.domain.user.model.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name,

    @NotNull(message = "A idade é obrigatória")
    Integer age,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    String password,

    UserRole role) {
    
}
