package com.repositorio.investir_mais.domain.portfolio.service.interfaces;

import java.math.BigDecimal;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.portfolio.DTO.DashboardResponseDTO;
import com.repositorio.investir_mais.domain.portfolio.DTO.RebalanceResponseDTO;

import lombok.NonNull;

public interface PortfolioQueryService {
    ServiceResult<RebalanceResponseDTO> calculateRebalance(@NonNull BigDecimal aporteAmount);

    ServiceResult<DashboardResponseDTO> getPortfolioSummary();
}
