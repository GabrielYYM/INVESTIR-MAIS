package com.repositorio.investir_mais.domain.question.DTO;

import java.util.UUID;

import com.repositorio.investir_mais.common.validation.question.ValidQuestionText;

public record QuestionRequestDTO(
        UUID id,
        @ValidQuestionText String text) {
}
