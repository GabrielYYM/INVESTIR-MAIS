package com.repositorio.investir_mais.domain.user.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserRequestDTO(
                @NotBlank String name,

                @NotBlank @Email String email) {
}
