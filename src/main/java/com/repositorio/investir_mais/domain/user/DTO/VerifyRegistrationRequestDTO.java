package com.repositorio.investir_mais.domain.user.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.repositorio.investir_mais.common.validation.auth.Valid2FACode;
import com.repositorio.investir_mais.common.validation.user.ValidEmail;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VerifyRegistrationRequestDTO(
        @ValidEmail
        String email,

        @Valid2FACode
        String code,

        String guardianCode
) {
}
