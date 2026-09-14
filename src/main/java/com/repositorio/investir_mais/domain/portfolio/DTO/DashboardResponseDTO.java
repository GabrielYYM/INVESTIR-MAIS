package com.repositorio.investir_mais.domain.portfolio.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public record DashboardResponseDTO(
        BigDecimal totalValue,
        List<CategorySummaryDTO> categories,
        int totalAssets) {
    public record CategorySummaryDTO(
        UUID id,
        String name,
        BigDecimal currentPercentage,
        BigDecimal targetPercentage,
        BigDecimal totalValue,
        int assetsCount) {
    }
}
