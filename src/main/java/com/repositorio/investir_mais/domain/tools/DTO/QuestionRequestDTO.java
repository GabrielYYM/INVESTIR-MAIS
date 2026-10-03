package com.repositorio.investir_mais.domain.tools.DTO;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public record QuestionRequestDTO(
    @NotBlank(message = "O texto da pergunta é obrigatório")
    String text,
    
    UUID categoryId
) {}