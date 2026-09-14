package com.repositorio.investir_mais.domain.question.DTO;

import java.util.UUID;

public record QuestionResponseDTO(
        UUID id,
        String text) {
}
