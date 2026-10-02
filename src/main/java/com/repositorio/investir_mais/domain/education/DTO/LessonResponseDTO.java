package com.repositorio.investir_mais.domain.education.DTO;

import java.util.UUID;

public record LessonResponseDTO(
    UUID id,
    String title,
    String description,
    String mediaUrl,
    String thumbnailUrl,
    UUID courseId
) {}
