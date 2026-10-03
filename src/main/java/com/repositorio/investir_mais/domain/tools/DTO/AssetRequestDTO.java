package com.repositorio.investir_mais.domain.tools.DTO;

import java.math.BigDecimal;
import java.util.UUID;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;

public record AssetRequestDTO(
    UUID id,
    String ticker,
    BigDecimal currentPositionValue,
    BigDecimal quantity,
    BigDecimal averagePrice,
    int rawScore,
    AssetRole role
) {
}
