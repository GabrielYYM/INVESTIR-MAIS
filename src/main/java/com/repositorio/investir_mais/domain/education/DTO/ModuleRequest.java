package com.repositorio.investir_mais.domain.education.DTO;

import jakarta.validation.constraints.NotBlank;

/**
 * Usado tanto para criar quanto para editar um módulo (playlist).
 * Também é o payload da opção "criar novo módulo" no dropdown da
 * tela de Detalhes do vídeo.
 */
public record ModuleRequest(
        @NotBlank String name,
        String description,
        int orderIndex
) {
}
