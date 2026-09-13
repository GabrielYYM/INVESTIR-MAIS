package com.repositorio.investir_mais.domain.asset.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CategoryRequestDTO(
        UUID id,
        String name,
        BigDecimal targetPercentage,
        List<AssetRequestDTO> assets) {
}
