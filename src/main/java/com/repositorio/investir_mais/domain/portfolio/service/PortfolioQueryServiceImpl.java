package com.repositorio.investir_mais.domain.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.engine.AssetScoreCalculator;
import com.repositorio.investir_mais.domain.asset.model.Asset;
import com.repositorio.investir_mais.domain.asset.model.AssetCategory;
import com.repositorio.investir_mais.domain.portfolio.DTO.DashboardResponseDTO;
import com.repositorio.investir_mais.domain.portfolio.DTO.RebalanceResponseDTO;
import com.repositorio.investir_mais.domain.portfolio.engine.RebalanceEngine;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.domain.portfolio.service.interfaces.PortfolioQueryService;
import com.repositorio.investir_mais.infrastructure.security.UserContextService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Implementação do serviço de consultas para Carteiras (Portfolios).
 * Provê funcionalidades de análise da carteira, incluindo o cálculo de
 * rebalanceamento
 * e a geração de resumos para o Dashboard do usuário.
 */
@Service
@RequiredArgsConstructor
public class PortfolioQueryServiceImpl implements PortfolioQueryService {
    private final UserContextService userContextService;
    private final AssetScoreCalculator assetScoreCalculator;
    private final RebalanceEngine rebalanceEngine;

    /**
     * Calcula a sugestão de aporte (rebalanceamento) baseada no valor disponível.
     * Utiliza o motor de rebalanceamento para distribuir o aporte entre os ativos
     * que mais precisam de capital para atingir os alvos definidos.
     * 
     * @param aporteAmount Valor em dinheiro disponível para novos investimentos.
     * @return DTO contendo a lista de ativos sugeridos para compra e as
     *         quantidades.
     */
    @Override
    @Transactional(readOnly = true)
    public ServiceResult<RebalanceResponseDTO> calculateRebalance(@NonNull BigDecimal aporteAmount) {
        try {
            Portfolio portfolio = userContextService.getCurrentUserPortfolioWithCategoriesAndAssets();

            return ServiceResult.success(rebalanceEngine.calculate(portfolio,
                    aporteAmount));
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        } catch (Exception e) {
            return ServiceResult.error(e.getMessage());
        }
    }

    /**
     * Gera um resumo consolidado da carteira para visualização no Dashboard.
     * Consolida valores totais, distribuição por categoria e alvos redistribuídos.
     * 
     * @return DashboardResponseDTO com os dados agregados da carteira.
     */
    @Override
    @Transactional(readOnly = true)
    public ServiceResult<DashboardResponseDTO> getPortfolioSummary() {
        try {
            Portfolio portfolio = userContextService.getCurrentUserPortfolioWithCategoriesAndAssets();

            BigDecimal totalValue = calculateTotalValue(portfolio);

            List<AssetCategory> activeCategories = portfolio.getCategories().stream()
                    .filter(c -> c.getAssets()
                            .stream()
                            .anyMatch(a -> assetScoreCalculator.calculateScore(a) > 0))
                    .toList();

            BigDecimal sumActiveOriginalTargets = activeCategories.stream()
                    .map(AssetCategory::getTargetPercentage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalAssets = (int) portfolio.getCategories()
                    .stream()
                    .flatMap(c -> c.getAssets().stream())
                    .count();

            List<DashboardResponseDTO.CategorySummaryDTO> summaries = new ArrayList<>();

            for (AssetCategory category : portfolio.getCategories()) {
                BigDecimal currentCategoryValue = category.getAssets()
                        .stream()
                        .map(Asset::getCurrentPositionValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal currentPercentage = totalValue.compareTo(BigDecimal.ZERO) > 0
                        ? currentCategoryValue.multiply(new BigDecimal("100"))
                                .divide(totalValue,
                                        2,
                                        RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                BigDecimal redistributedTarget = BigDecimal.ZERO;
                if (activeCategories.contains(category) && sumActiveOriginalTargets.compareTo(BigDecimal.ZERO) > 0) {
                    redistributedTarget = category.getTargetPercentage()
                            .multiply(new BigDecimal("100"))
                            .divide(sumActiveOriginalTargets, 2, RoundingMode.HALF_UP);
                }

                summaries.add(new DashboardResponseDTO.CategorySummaryDTO(
                        category.getId(),
                        category.getName(),
                        currentPercentage,
                        redistributedTarget,
                        currentCategoryValue,
                        category.getAssets()
                                .size()));
            }

            return ServiceResult.success(new DashboardResponseDTO(totalValue, summaries, totalAssets));
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        } catch (Exception e) {
            return ServiceResult.error(e.getMessage());
        }
    }

    private BigDecimal calculateTotalValue(Portfolio portfolio) {
        return portfolio.getCategories().stream()
                .flatMap(c -> c.getAssets().stream())
                .map(Asset::getCurrentPositionValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
