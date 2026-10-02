package com.repositorio.investir_mais.domain.asset.engine;

import org.springframework.stereotype.Component;

import com.repositorio.investir_mais.domain.asset.model.Asset;
import com.repositorio.investir_mais.domain.asset.model.AssetEvaluation;

/**
 * Estratégia padrão para cálculo de Score.
 * Positivos valem +1, negativos valem -1.
 */
@Component
public class BasicAssetScoreStrategy implements AssetScoreCalculator {

    @Override
    public int calculateScore(Asset asset) {
        if (asset == null || asset.getEvaluations() == null || asset.getEvaluations().isEmpty()) {
            return 0;
        }

        int positives = (int) asset.getEvaluations().stream().filter(AssetEvaluation::isPositive).count();
        int negatives = (int) asset.getEvaluations().stream().filter(e -> !e.isPositive()).count();

        return positives - negatives;
    }
}
