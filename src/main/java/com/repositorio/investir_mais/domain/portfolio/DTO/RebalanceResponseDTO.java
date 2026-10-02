package com.repositorio.investir_mais.domain.portfolio.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RebalanceResponseDTO(
        BigDecimal totalValue,
        List<CategoryRebalanceDTO> categories
){
    public record CategoryRebalanceDTO(
        UUID id,
        String name,
        BigDecimal currentPercentage,
        BigDecimal targetPercentage,
        List<AssetRebalanceDTO> assets) {
    }
    public record AssetRebalanceDTO(
        UUID id,
        String ticker,
        BigDecimal currentPercentage,
        BigDecimal targetPercentage,
        BigDecimal suggestedAporte,
        String action) {
    }
}
