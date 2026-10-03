package com.repositorio.investir_mais.domain.tools.DTO;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record PortfolioRequestDTO(
    @NotNull(message = "O ID do utilizador é obrigatório")
    UUID userId
) {}