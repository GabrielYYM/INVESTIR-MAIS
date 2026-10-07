package com.repositorio.investir_mais.domain.education.DTO;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LessonDTO {

    public record Request(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String mediaUrl,
        @NotBlank String thumbnailUrl
    ) {}

    public record Response(
        UUID id,
        String title,
        String description,
        String mediaUrl,
        String thumbnailUrl,
        UUID courseId
    ) {}
}