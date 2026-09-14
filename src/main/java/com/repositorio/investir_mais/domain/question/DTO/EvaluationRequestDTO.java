package com.repositorio.investir_mais.domain.question.DTO;

import java.util.UUID;

import com.repositorio.investir_mais.common.validation.question.ValidIsPositive;
import com.repositorio.investir_mais.common.validation.question.ValidQuestionId;

public record EvaluationRequestDTO(
        @ValidQuestionId UUID questionId,
        @ValidIsPositive Boolean isPositive) {
}