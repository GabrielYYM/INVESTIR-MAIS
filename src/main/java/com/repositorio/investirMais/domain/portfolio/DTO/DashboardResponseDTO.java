package com.repositorio.investirMais.domain.portfolio.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do dashboard")
public record DashboardResponseDTO(
        @Schema(description = "Valor total financeiro posicionado atualmente em todos os ativos da carteira", example = "150000.00") BigDecimal totalValue,

        @Schema(description = "Resumo das categorias") List<CategorySummaryDTO> categories,

        @Schema(description = "Quantidade de ativos únicos registrados na carteira", example = "14") int totalAssets) {
    @Schema(description = "Resumo da categoria")
    public record CategorySummaryDTO(
            @Schema(description = "ID único da categoria", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,

            @Schema(description = "Nome da categoria", example = "Fundos Imobiliários") String name,

            @Schema(description = "Percentual atual da categoria na carteira", example = "22.5") BigDecimal currentPercentage,

            @Schema(description = "Percentual alvo da categoria na carteira", example = "25.0") BigDecimal targetPercentage,

            @Schema(description = "Valor total investido nesta categoria", example = "33750.00") BigDecimal totalValue,

            @Schema(description = "Quantidade de ativos diferentes nesta categoria", example = "5") int assetsCount) {
    }
}
