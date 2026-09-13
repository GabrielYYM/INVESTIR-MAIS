package com.repositorio.investir_mais.domain.question.DTO;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta da pergunta")
public record QuestionResponseDTO(
        @Schema(description = "ID único da pergunta", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,
        @Schema(description = "Texto da pergunta", example = "A empresa tem boa governança corporativa?") String text) {
}
