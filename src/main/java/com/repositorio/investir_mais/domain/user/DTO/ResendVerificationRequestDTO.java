package com.repositorio.investir_mais.domain.user.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.repositorio.investir_mais.common.validation.user.ValidEmail;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResendVerificationRequestDTO(
        @ValidEmail
        String email
) {
}
