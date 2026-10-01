package com.repositorio.investir_mais.domain.education.DTO;

import java.util.UUID;

public record LessionResponseDTO(
    UUID id,
    String title,
    String description,
    String mediaUrl,
    String thumbnailUrl,
    UUID courseId
) {}
