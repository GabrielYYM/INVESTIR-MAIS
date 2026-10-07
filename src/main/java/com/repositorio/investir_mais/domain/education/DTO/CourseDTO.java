package com.repositorio.investir_mais.domain.education.DTO;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import lombok.experimental.UtilityClass;

@UtilityClass 
public class CourseDTO {
    public record Request(
        @NotBlank String name,
        @NotBlank String description
    ) {}

    public record Response(
        UUID id,
        String name,
        String description,
        UUID professorId
    ) {}
}