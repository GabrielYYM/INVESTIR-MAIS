package com.repositorio.investir_mais.domain.portfolio.engine;

import java.math.BigDecimal;

import com.repositorio.investir_mais.domain.portfolio.DTO.RebalanceResponseDTO;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;

public interface RebalanceEngine {

    /**
     * Calcula as necessidades de balanceamento financeiro de um Portfólio.
     * 
     * @param portfolio    Entidade principal com categorias e ativos carregados.
     * @param aporteAmount Dinheiro disponível para alocação neste rebalanceamento.
     * @return O resultado do rebalanceamento encapsulado no DTO oficial
     */
    RebalanceResponseDTO calculate(Portfolio portfolio, BigDecimal aporteAmount);
}
