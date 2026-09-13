package com.repositorio.investirMais.domain.question.DTO;

import java.util.UUID;

import com.repositorio.investirMais.common.validation.question.ValidIsPositive;
import com.repositorio.investirMais.common.validation.question.ValidQuestionId;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição de avaliação")
public record EvaluationRequestDTO(
                @ValidQuestionId @Schema(description = "ID único da pergunta", example = "123e4567-e89b-12d3-a456-426614174000") UUID questionId,
                @ValidIsPositive @Schema(description = "Avaliação da pergunta", example = "true") Boolean isPositive) {
}
