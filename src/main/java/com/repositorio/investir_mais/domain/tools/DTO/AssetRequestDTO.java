package com.repositorio.investir_mais.domain.tools.DTO;

import java.math.BigDecimal;
import java.util.UUID;

public record AssetRequestDTO(
    UUID id,
    String ticker,
    BigDecimal currentPositionValue,
    BigDecimal quantity,
    BigDecimal averagePrice,
    int rawScore
) {
}
