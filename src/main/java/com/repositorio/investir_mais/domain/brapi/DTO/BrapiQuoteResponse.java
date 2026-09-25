package com.repositorio.investir_mais.domain.brapi.dto;

public record BrapiQuoteResponse(Quote[] results) {
    public record Quote(
            String symbol,
            String shortName,
            double regularMarketPrice,
            double regularMarketChange,
            double regularMarketChangePercent,
            String currency
    ) {}
}
