package com.repositorio.investir_mais.domain.user.DTO;

import com.repositorio.investir_mais.common.validation.user.ValidEmail;
import com.repositorio.investir_mais.common.validation.user.ValidName;
import com.repositorio.investir_mais.common.validation.user.ValidPassword;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserRequestDTO(
        @ValidName
        String name,

        @ValidEmail
        String email,

        @ValidPassword
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,

        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
        java.time.LocalDate birthDate,

        String guardianEmail,

        @jakarta.validation.constraints.NotNull(message = "Você deve aceitar os termos de uso e política de privacidade.")
        @jakarta.validation.constraints.AssertTrue(message = "Você deve aceitar os termos de uso e política de privacidade.")
        @JsonProperty(required = true)
        Boolean termsAccepted
) {
}
