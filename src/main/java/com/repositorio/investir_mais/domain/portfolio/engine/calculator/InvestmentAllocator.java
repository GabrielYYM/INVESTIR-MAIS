package com.repositorio.investir_mais.domain.portfolio.engine.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.repositorio.investir_mais.domain.asset.model.Asset;

@Component
public class InvestmentAllocator {

    public Map<UUID, BigDecimal> allocate(
            List<Asset> allAssets,
            Map<UUID, BigDecimal> assetTargetPercentages,
            BigDecimal newTotalValue,
            BigDecimal aporteAmount) {
        Map<UUID, BigDecimal> rawGaps = new HashMap<>();

        for (Asset asset : allAssets) {
            BigDecimal targetPercentage = assetTargetPercentages.getOrDefault(asset.getId(), BigDecimal.ZERO);
            BigDecimal targetValue = newTotalValue.multiply(targetPercentage)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal gap = targetValue.subtract(asset.getCurrentPositionValue());
            if (gap.compareTo(BigDecimal.ZERO) < 0) {
                gap = BigDecimal.ZERO;
            }
            rawGaps.put(asset.getId(), gap);
        }

        Map<UUID, BigDecimal> suggestedAportes = new HashMap<>();
        BigDecimal remainingAporte = aporteAmount;
        List<Asset> assetsToBuy = allAssets.stream()
                .filter(a -> rawGaps.get(a.getId()).compareTo(BigDecimal.ZERO) > 0)
                .sorted((a1, a2) -> rawGaps.get(a2.getId()).compareTo(rawGaps.get(a1.getId())))
                .toList();

        for (Asset asset : assetsToBuy) {
            if (remainingAporte.compareTo(BigDecimal.ZERO) <= 0) {
                suggestedAportes.put(asset.getId(), BigDecimal.ZERO);
                continue;
            }

            BigDecimal gap = rawGaps.get(asset.getId());
            BigDecimal buyAmount = gap.min(remainingAporte);
            suggestedAportes.put(asset.getId(), buyAmount);
            remainingAporte = remainingAporte.subtract(buyAmount);
        }

        return suggestedAportes;
    }
}
