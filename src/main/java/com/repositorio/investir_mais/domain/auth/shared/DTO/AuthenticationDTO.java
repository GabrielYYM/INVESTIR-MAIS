package com.repositorio.investir_mais.domain.auth.shared.DTO;

import com.repositorio.investir_mais.common.validation.user.ValidEmail;
import com.repositorio.investir_mais.common.validation.user.ValidPassword;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição de autenticação")
public record AuthenticationDTO(
        @Schema(description = "Email do usuário",example = "example@gmail.com")
        @ValidEmail
        String email,

        @Schema(description = "Senha do usuário", example = "Password@123")
        @ValidPassword
        String password
) {}
