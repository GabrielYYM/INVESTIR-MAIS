package com.repositorio.investir_mais.domain.tools.DTO;

import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;
import jakarta.validation.constraints.NotBlank;

public record QuestionRequestDTO(
    @NotBlank(message = "O texto da pergunta é obrigatório")
    String text,
    
    AssetRole role
) {}
