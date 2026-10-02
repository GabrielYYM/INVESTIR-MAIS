package com.repositorio.investir_mais.domain.portfolio.engine.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.repositorio.investir_mais.domain.asset.engine.AssetScoreCalculator;
import com.repositorio.investir_mais.domain.asset.model.Asset;
import com.repositorio.investir_mais.domain.asset.model.AssetCategory;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AssetTargetCalculator {

    private final AssetScoreCalculator assetScoreCalculator;

    public Map<UUID, BigDecimal> calculate(Portfolio portfolio, Map<UUID, BigDecimal> redistributedCategoryTargets) {
        Map<UUID, BigDecimal> assetTargetPercentages = new HashMap<>();

        for (AssetCategory category : portfolio.getCategories()) {
            BigDecimal redistributedCategoryTarget = redistributedCategoryTargets.getOrDefault(category.getId(),
                    BigDecimal.ZERO);

            int totalCategoryScore = category.getAssets().stream()
                    .map(assetScoreCalculator::calculateScore)
                    .filter(score -> score > 0)
                    .mapToInt(Integer::intValue)
                    .sum();

            for (Asset asset : category.getAssets()) {
                int score = assetScoreCalculator.calculateScore(asset);
                BigDecimal assetTargetPercentage = BigDecimal.ZERO;

                if (redistributedCategoryTarget.compareTo(BigDecimal.ZERO) > 0 && score > 0 && totalCategoryScore > 0) {
                    assetTargetPercentage = redistributedCategoryTarget
                            .multiply(new BigDecimal(score))
                            .divide(new BigDecimal(totalCategoryScore), 2, RoundingMode.HALF_UP);
                }
                assetTargetPercentages.put(asset.getId(), assetTargetPercentage);
            }
        }

        return assetTargetPercentages;
    }
}
