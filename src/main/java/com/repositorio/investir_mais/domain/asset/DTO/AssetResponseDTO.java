package com.repositorio.investir_mais.domain.asset.DTO;

import java.math.BigDecimal;
import java.util.UUID;

public record AssetResponseDTO(
        UUID id,
        String ticker,
        BigDecimal currentPositionValue,
        BigDecimal quantity,
        BigDecimal averagePrice,
        Integer rawScore) {
}
