package com.repositorio.investir_mais.domain.portfolio.service.interfaces;

import java.math.BigDecimal;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.portfolio.DTO.DashboardResponseDTO;
import com.repositorio.investir_mais.domain.portfolio.DTO.RebalanceResponseDTO;

import lombok.NonNull;

/**
 * Interface de consulta para análise de carteira e dashboard.
 * Fornece métodos para cálculo de rebalanceamento e visualização consolidada de
 * ativos.
 */
public interface PortfolioQueryService {
    /**
     * Calcula a sugestão de rebalanceamento com base em um novo aporte.
     * Utiliza as porcentagens alvo definidas nas categorias para sugerir a
     * distribuição ideal.
     * 
     * @param aporteAmount Valor total que o usuário deseja investir.
     * @return Resultado contendo os ativos sugeridos para compra e os novos pesos.
     */
    ServiceResult<RebalanceResponseDTO> calculateRebalance(@NonNull BigDecimal aporteAmount);

    /**
     * Recupera o resumo consolidado da carteira para exibição no dashboard.
     * Inclui totais por categoria, percentuais atuais vs alvo e desempenho geral.
     * 
     * @return Resultado contendo o DTO consolidado do dashboard.
     */
    ServiceResult<DashboardResponseDTO> getPortfolioSummary();
}
