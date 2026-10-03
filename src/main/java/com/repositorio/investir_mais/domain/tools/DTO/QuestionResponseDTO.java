package com.repositorio.investir_mais.domain.tools.DTO;

import java.util.UUID;

public record QuestionResponseDTO(
    UUID id,
    String statement,
    String answer,
    UUID lessionId
) {}