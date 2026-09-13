package com.repositorio.investirMais.domain.portfolio.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do rebalanceamento")
public record RebalanceResponseDTO(
        @Schema(description = "Valor total financeiro pós-simulação", example = "105000.00") BigDecimal totalValue,

        @Schema(description = "Resumo das categorias") List<CategoryRebalanceDTO> categories) {
    @Schema(description = "Resumo da categoria")
    public record CategoryRebalanceDTO(
            @Schema(description = "ID único da categoria", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,

            @Schema(description = "Nome da categoria", example = "Renda Fixa") String name,

            @Schema(description = "Peso percentual atual da categoria na carteira", example = "30.0") BigDecimal currentPercentage,

            @Schema(description = "Peso percentual alvo da categoria na carteira", example = "35.0") BigDecimal targetPercentage,

            @Schema(description = "Detalhamento e diretrizes de ação para cada ativo individual dentro desta categoria") List<AssetRebalanceDTO> assets) {
    }

    @Schema(description = "Recomendação para um ativo específico")
    public record AssetRebalanceDTO(
            @Schema(description = "ID único do ativo", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,

            @Schema(description = "Ticker ou nome de mercado", example = "PETR4") String ticker,

            @Schema(description = "Peso percentual atual do ativo na carteira", example = "2.1") BigDecimal currentPercentage,

            @Schema(description = "Peso percentual alvo do ativo na carteira", example = "3.5") BigDecimal targetPercentage,

            @Schema(description = "Valor em dinheiro (R$) a ser comprado agora", example = "1400.00") BigDecimal suggestedAporte,

            @Schema(description = "Instrução para o Front-end: 'MANTER', 'COMPRAR'", example = "COMPRAR") String action) {
    }
}
