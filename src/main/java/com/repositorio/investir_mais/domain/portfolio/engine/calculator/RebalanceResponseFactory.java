package com.repositorio.investir_mais.domain.portfolio.engine.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.repositorio.investir_mais.domain.asset.model.Asset;
import com.repositorio.investir_mais.domain.asset.model.AssetCategory;
import com.repositorio.investir_mais.domain.portfolio.DTO.RebalanceResponseDTO;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;

@Component
public class RebalanceResponseFactory {

        public RebalanceResponseDTO build(Portfolio portfolio,
                        BigDecimal totalCurrentValue,
                        BigDecimal newTotalValue,
                        Map<UUID, BigDecimal> redistributedCategoryTargets,
                        Map<UUID, BigDecimal> assetTargetPercentages,
                        Map<UUID, BigDecimal> suggestedAportes) {

                List<RebalanceResponseDTO.CategoryRebalanceDTO> categoryDTOs = new ArrayList<>();

                for (AssetCategory category : portfolio.getCategories()) {
                        BigDecimal redistributedTargetPercentage = redistributedCategoryTargets
                                        .getOrDefault(category.getId(), BigDecimal.ZERO);

                        BigDecimal currentCategoryValue = category.getAssets().stream()
                                        .map(Asset::getCurrentPositionValue)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal categoryCurrentPercentage = totalCurrentValue.compareTo(BigDecimal.ZERO) > 0
                                        ? currentCategoryValue.multiply(new BigDecimal("100")).divide(totalCurrentValue,
                                                        2, RoundingMode.HALF_UP)
                                        : BigDecimal.ZERO;

                        List<RebalanceResponseDTO.AssetRebalanceDTO> assetDTOs = new ArrayList<>();

                        for (Asset asset : category.getAssets()) {
                                BigDecimal assetCurrentPercentage = totalCurrentValue.compareTo(BigDecimal.ZERO) > 0
                                                ? asset.getCurrentPositionValue().multiply(new BigDecimal("100"))
                                                                .divide(totalCurrentValue, 2, RoundingMode.HALF_UP)
                                                : BigDecimal.ZERO;

                                BigDecimal suggestedAporte = suggestedAportes.getOrDefault(asset.getId(),
                                                BigDecimal.ZERO);

                                assetDTOs.add(new RebalanceResponseDTO.AssetRebalanceDTO(
                                                asset.getId(),
                                                asset.getTicker(),
                                                assetCurrentPercentage,
                                                assetTargetPercentages.getOrDefault(asset.getId(), BigDecimal.ZERO),
                                                suggestedAporte,
                                                suggestedAporte.compareTo(BigDecimal.ZERO) > 0 ? "COMPRAR"
                                                                : "AGUARDAR"));
                        }

                        categoryDTOs.add(new RebalanceResponseDTO.CategoryRebalanceDTO(
                                        category.getId(),
                                        category.getName(),
                                        categoryCurrentPercentage,
                                        redistributedTargetPercentage,
                                        assetDTOs));
                }

                return new RebalanceResponseDTO(newTotalValue.setScale(2, RoundingMode.HALF_UP), categoryDTOs);
        }
}
