package com.repositorio.investir_mais.domain.education.DTO;

import com.repositorio.investir_mais.domain.education.model.enums.ContentStatus;
import com.repositorio.investir_mais.domain.education.model.enums.ContentType;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representação de EducationalContent exposta pela API.
 * Nunca expõe a entidade JPA diretamente — ver EducationalContentMapper.
 */
public record ContentResponse(
        UUID id,
        String title,
        String description,
        ContentType type,
        ContentStatus status,
        String mediaUrl,
        String thumbnailUrl,
        UUID moduleId,
        String moduleName,
        Integer orderInModule,
        UUID professorId,
        String professorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
