package com.repositorio.investir_mais.domain.tools.DTO;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PortfolioDTO {

    public record Request(
        @NotNull(message = "O ID do utilizador é obrigatório")
        UUID userId
    ) {}

    public record Response(
        UUID id,
        UUID userId,
        List<UUID> assetIds
    ) {}
}