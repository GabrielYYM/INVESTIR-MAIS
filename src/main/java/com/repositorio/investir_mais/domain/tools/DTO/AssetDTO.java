package com.repositorio.investir_mais.domain.tools.DTO;

import java.math.BigDecimal;
import java.util.UUID;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AssetDTO {

    public record Request(
        UUID id,
        String ticker,
        BigDecimal currentPositionValue,
        BigDecimal quantity,
        BigDecimal averagePrice,
        int rawScore,
        AssetRole role
    ) {}

    public record Response(
        UUID id,
        String ticker,
        BigDecimal currentPositionValue,
        BigDecimal quantity,
        BigDecimal averagePrice,
        Integer rawScore,
        AssetRole role
    ) {}
}