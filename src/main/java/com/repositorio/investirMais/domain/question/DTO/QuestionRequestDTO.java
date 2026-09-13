package com.repositorio.investirMais.domain.question.DTO;

import java.util.UUID;

import com.repositorio.investirMais.common.validation.question.ValidQuestionText;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição da pergunta")
public record QuestionRequestDTO(
                @Schema(description = "ID único da pergunta", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,
                @ValidQuestionText @Schema(description = "Texto da pergunta", example = "A empresa tem boa governança corporativa?") String text) {
}
