package com.repositorio.investir_mais.infrastructure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.repositorio.investir_mais.domain.tools.model.Asset;
import com.repositorio.investir_mais.domain.tools.model.Portfolio;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;

@Component
public class RebalanceEngine {

    public Map<String, Object> rebalance(Portfolio portfolio, BigDecimal totalCurrentValue, BigDecimal aporteAmount, Map<AssetRole, BigDecimal> roleTargets) {
        BigDecimal newTotalValue = totalCurrentValue.add(aporteAmount);
        Map<UUID, BigDecimal> targetPercentageMap = new HashMap<>();
        Map<UUID, BigDecimal> gaps = new HashMap<>();
        List<Asset> assets = portfolio.getAssets() != null ? portfolio.getAssets() : List.of();
        Map<AssetRole, List<Asset>> assetsByRole = assets.stream()
            .filter(asset -> asset.getRole() != null)
            .collect(java.util.stream.Collectors.groupingBy(Asset::getRole));

        for (Map.Entry<AssetRole, List<Asset>> entry : assetsByRole.entrySet()) {
            BigDecimal roleTarget = roleTargets.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            int totalScore = entry.getValue().stream().mapToInt(Asset::getRawScore).filter(score -> score > 0).sum();
            for (Asset asset : entry.getValue()) {
                BigDecimal targetPercent = BigDecimal.ZERO;
                if (roleTarget.signum() > 0 && asset.getRawScore() > 0 && totalScore > 0) {
                    targetPercent = roleTarget.multiply(BigDecimal.valueOf(asset.getRawScore()))
                        .divide(BigDecimal.valueOf(totalScore), 4, RoundingMode.HALF_UP);
                }
                targetPercentageMap.put(asset.getId(), targetPercent);
                BigDecimal targetValue = newTotalValue.multiply(targetPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal currentValue = asset.getCurrentPositionValue() != null ? asset.getCurrentPositionValue() : BigDecimal.ZERO;
                gaps.put(asset.getId(), targetValue.subtract(currentValue).max(BigDecimal.ZERO));
            }
        }

        List<Asset> sortedAssets = assets.stream()
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
            "roles", assetsByRole.entrySet().stream().map(entry -> {
                List<Asset> roleAssets = entry.getValue();
                return Map.of(
                    "role", entry.getKey().name(),
                    "assets", roleAssets.stream().map(asset -> {
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
