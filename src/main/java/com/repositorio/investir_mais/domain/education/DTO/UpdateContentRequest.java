package com.repositorio.investir_mais.domain.education.DTO;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Usado na tela "Detalhes do vídeo" para preencher/editar as informações
 * do conteúdo, incluindo o vínculo opcional com um módulo (playlist).
 *
 * moduleId nulo = conteúdo sem módulo (aparece só na listagem do professor).
 */
public record UpdateContentRequest(
        @NotBlank String title,
        String description,
        String thumbnailUrl,
        UUID moduleId,
        Integer orderInModule
) {
}
