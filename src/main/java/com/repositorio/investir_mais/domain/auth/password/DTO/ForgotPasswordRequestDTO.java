package com.repositorio.investir_mais.domain.auth.password.DTO;

import com.repositorio.investir_mais.common.validation.user.ValidEmail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição de recuperação de senha")
public record ForgotPasswordRequestDTO(
        @Schema(description = "Email do usuário",example = "example@gmail.com")
        @ValidEmail
        String email
) {}
