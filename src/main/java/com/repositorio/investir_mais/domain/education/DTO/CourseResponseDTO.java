package com.repositorio.investir_mais.domain.education.DTO;

import java.util.UUID;

public record CourseResponseDTO(
    UUID id,
    String name,
    String description,
    String professorId
) {   
}
