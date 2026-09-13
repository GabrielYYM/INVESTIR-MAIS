package com.repositorio.investirMais.domain.portfolio.engine.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.repositorio.investirMais.domain.asset.engine.AssetScoreCalculator;
import com.repositorio.investirMais.domain.asset.model.AssetCategory;
import com.repositorio.investirMais.domain.portfolio.model.Portfolio;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryRedistributionCalculator {

    private final AssetScoreCalculator assetScoreCalculator;

    public Map<UUID, BigDecimal> calculate(Portfolio portfolio) {
        List<AssetCategory> activeCategories = portfolio.getCategories().stream()
                .filter(c -> c.getAssets().stream().anyMatch(a -> assetScoreCalculator.calculateScore(a) > 0))
                .toList();

        BigDecimal sumActiveOriginalTargets = activeCategories.stream()
                .map(AssetCategory::getTargetPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, BigDecimal> redistributedTargets = new HashMap<>();

        for (AssetCategory category : portfolio.getCategories()) {
            boolean isActive = activeCategories.contains(category);
            BigDecimal redistributedTargetPercentage = BigDecimal.ZERO;

            if (isActive && sumActiveOriginalTargets.compareTo(BigDecimal.ZERO) > 0) {
                redistributedTargetPercentage = category.getTargetPercentage()
                        .multiply(new BigDecimal("100"))
                        .divide(sumActiveOriginalTargets, 2, RoundingMode.HALF_UP);
            }
            redistributedTargets.put(category.getId(), redistributedTargetPercentage);
        }

        return redistributedTargets;
    }
}
