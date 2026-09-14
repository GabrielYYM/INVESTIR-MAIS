package com.repositorio.investir_mais.domain.asset.repository.projection;

import java.util.UUID;

public interface AssetScoreProjection {
    UUID getId();
    String getTicker();
    // A nota bruta (+1, -1) calculada
    Integer getRawScore();
}