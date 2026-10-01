package com.repositorio.investir_mais.domain.education.DTO;

import jakarta.validation.constraints.NotBlank;

public record LessionRequestDTO(
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String mediaUrl,
    @NotBlank String thumbnailUrl
) {}
