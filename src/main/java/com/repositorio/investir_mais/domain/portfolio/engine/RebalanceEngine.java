package com.repositorio.investir_mais.domain.portfolio.engine;

import java.math.BigDecimal;

import com.repositorio.investir_mais.domain.portfolio.DTO.RebalanceResponseDTO;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;

public interface RebalanceEngine {
    RebalanceResponseDTO calculate(Portfolio portfolio, BigDecimal aporteAmount);
}
