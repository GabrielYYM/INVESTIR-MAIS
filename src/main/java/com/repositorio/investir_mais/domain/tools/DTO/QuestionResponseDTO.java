package com.repositorio.investir_mais.domain.tools.DTO;

import java.util.UUID;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;

public record QuestionResponseDTO(
    UUID id,
    String text,
    AssetRole role
) {}
