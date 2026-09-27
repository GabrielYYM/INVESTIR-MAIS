package com.repositorio.investir_mais.domain.education.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representação de Module exposta pela API.
 * contentCount ajuda a tela de gestão de módulos ("8 vídeos") sem precisar
 * que o front carregue a lista inteira de conteúdos só para contar.
 */
public record ModuleResponse(
        UUID id,
        String name,
        String description,
        int orderIndex,
        UUID professorId,
        String professorName,
        long contentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
