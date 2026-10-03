package com.repositorio.investir_mais.domain.tools.DTO;

import java.util.List;
import java.util.UUID;

public record PortfolioResponseDTO(
    UUID id,
    UUID userId,
    List<UUID> assetIds
) {}
