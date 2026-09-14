package com.repositorio.investir_mais.domain.portfolio.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.repositorio.investir_mais.domain.asset.model.Asset;
import com.repositorio.investir_mais.domain.portfolio.DTO.RebalanceResponseDTO;
import com.repositorio.investir_mais.domain.portfolio.engine.calculator.InvestmentAllocator;
import com.repositorio.investir_mais.domain.portfolio.engine.calculator.AssetTargetCalculator;
import com.repositorio.investir_mais.domain.portfolio.engine.calculator.CategoryRedistributionCalculator;
import com.repositorio.investir_mais.domain.portfolio.engine.calculator.RebalanceResponseFactory;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JavaRebalanceEngineImpl implements RebalanceEngine {
    private final CategoryRedistributionCalculator categoryCalculator;
    private final AssetTargetCalculator assetCalculator;
    private final InvestmentAllocator aporteAllocator;
    private final RebalanceResponseFactory responseFactory;

    @Override
    public RebalanceResponseDTO calculate(Portfolio portfolio, BigDecimal aporteAmount) {
        BigDecimal totalCurrentValue = calculateTotalValue(portfolio);
        BigDecimal newTotalValue = totalCurrentValue.add(aporteAmount);
        Map<UUID, BigDecimal> categoryTargets = categoryCalculator.calculate(portfolio);
        Map<UUID, BigDecimal> assetTargets = assetCalculator.calculate(portfolio, categoryTargets);
        List<Asset> allAssets = new ArrayList<>();
        Map<UUID, BigDecimal> suggestedAportes = aporteAllocator.allocate(allAssets, assetTargets, newTotalValue,
                aporteAmount);

        portfolio.getCategories().forEach(c -> allAssets.addAll(c.getAssets()));

        return responseFactory.build(portfolio, totalCurrentValue, newTotalValue, categoryTargets, assetTargets,
                suggestedAportes);
    }

    private BigDecimal calculateTotalValue(Portfolio portfolio) {
        return portfolio.getCategories().stream()
                .flatMap(c -> c.getAssets().stream())
                .map(Asset::getCurrentPositionValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
