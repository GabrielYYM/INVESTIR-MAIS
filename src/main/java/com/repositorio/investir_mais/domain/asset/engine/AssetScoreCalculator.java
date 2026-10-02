package com.repositorio.investir_mais.domain.asset.engine;

import com.repositorio.investir_mais.domain.asset.model.Asset;

public interface AssetScoreCalculator {
    int calculateScore(Asset asset);
}
