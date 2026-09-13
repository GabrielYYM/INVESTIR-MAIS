package com.repositorio.investir_mais.domain.user.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

public record UserRequestDTO(
        String name,
        String email
) {
}
