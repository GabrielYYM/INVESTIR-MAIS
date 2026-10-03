package com.repositorio.investir_mais.infrastructure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.repositorio.investir_mais.domain.tools.model.Asset;
import com.repositorio.investir_mais.domain.tools.model.Category;
import com.repositorio.investir_mais.domain.tools.model.Portfolio;

@Component
public class PortfolioRebalanceEngine {

    public Map<String, Object> rebalance(Portfolio portfolio, BigDecimal totalCurrentValue, BigDecimal aporteAmount, Map<UUID, BigDecimal> categoryTargets) {
        BigDecimal newTotalValue = totalCurrentValue.add(aporteAmount);
        Map<UUID, BigDecimal> targetPercentageMap = new HashMap<>();
        Map<UUID, BigDecimal> gaps = new HashMap<>();

        List<Category> categories = portfolio.getListCategory() != null ? portfolio.getListCategory() : Collections.emptyList();

        for (Category category : categories) {
            BigDecimal categoryTarget = categoryTargets.getOrDefault(category.getId(), BigDecimal.ZERO);
            List<Asset> assets = category.getListAssets() != null ? category.getListAssets() : Collections.emptyList();

            int totalScore = assets.stream()
                .mapToInt(Asset::getRawScore)
                .filter(s -> s > 0)
                .sum();

            for (Asset asset : assets) {
                BigDecimal targetPercent = BigDecimal.ZERO;

                if (categoryTarget.compareTo(BigDecimal.ZERO) > 0 && asset.getRawScore() > 0 && totalScore > 0) {
                    targetPercent = categoryTarget.multiply(new BigDecimal(asset.getRawScore()))
                        .divide(new BigDecimal(totalScore), 4, RoundingMode.HALF_UP);
                }

                targetPercentageMap.put(asset.getId(), targetPercent);

                BigDecimal targetVal = newTotalValue.multiply(targetPercent)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                
                BigDecimal currentPosition = asset.getCurrentPositionValue() != null ? asset.getCurrentPositionValue() : BigDecimal.ZERO;
                BigDecimal gap = targetVal.subtract(currentPosition);

                if (gap.compareTo(BigDecimal.ZERO) > 0) {
                    gaps.put(asset.getId(), gap);
                } else {
                    gaps.put(asset.getId(), BigDecimal.ZERO);
                }
            }
        }

        List<Asset> sortedAssets = categories.stream()
            .flatMap(c -> (c.getListAssets() != null ? c.getListAssets() : Collections.<Asset>emptyList()).stream())
            .sorted((a1, a2) -> {
                BigDecimal g1 = gaps.getOrDefault(a1.getId(), BigDecimal.ZERO);
                BigDecimal g2 = gaps.getOrDefault(a2.getId(), BigDecimal.ZERO);
                return g2.compareTo(g1);
            })
            .toList();

        Map<UUID, BigDecimal> aportes = new HashMap<>();
        BigDecimal remaining = aporteAmount;

        for (Asset asset : sortedAssets) {
            BigDecimal gap = gaps.getOrDefault(asset.getId(), BigDecimal.ZERO);
            BigDecimal buyAmount = BigDecimal.ZERO;

            if (remaining.compareTo(BigDecimal.ZERO) > 0 && gap.compareTo(BigDecimal.ZERO) > 0) {
                buyAmount = gap.min(remaining);
            }

            aportes.put(asset.getId(), buyAmount.setScale(2, RoundingMode.HALF_UP));
            remaining = remaining.subtract(buyAmount);
        }

        return Map.of(
            "totalValue", newTotalValue.setScale(2, RoundingMode.HALF_UP),
            "categories", categories.stream().map(cat -> {
                List<Asset> assets = cat.getListAssets() != null ? cat.getListAssets() : Collections.emptyList();
                return Map.of(
                    "id", cat.getId(),
                    "name", cat.getName() != null ? cat.getName() : "",
                    "assets", assets.stream().map(asset -> {
                        BigDecimal aporte = aportes.getOrDefault(asset.getId(), BigDecimal.ZERO);
                        String action = aporte.compareTo(BigDecimal.ZERO) > 0 ? "COMPRAR" : "AGUARDAR";

                        return Map.of(
                            "id", asset.getId(),
                            "ticker", asset.getTicker() != null ? asset.getTicker() : "",
                            "targetPercentage", targetPercentageMap.getOrDefault(asset.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                            "suggestedAporte", aporte,
                            "action", action
                        );
                    }).toList()
                );
            }).toList()
        );
    }
}