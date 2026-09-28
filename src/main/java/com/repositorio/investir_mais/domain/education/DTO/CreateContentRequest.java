package com.repositorio.investir_mais.domain.education.DTO;

import com.repositorio.investir_mais.domain.education.model.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateContentRequest(
        @NotBlank String title,
        @NotNull ContentType type,
        @NotBlank String mediaUrl
) {
}
