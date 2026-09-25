package com.repositorio.investir_mais.domain.auth.login.DTO;

import com.repositorio.investir_mais.common.validation.auth.Valid2FACode;
import com.repositorio.investir_mais.common.validation.user.ValidEmail;

public record Verify2FARequestDTO(
        @ValidEmail
        String email,

        @Valid2FACode
        String code
) {}
