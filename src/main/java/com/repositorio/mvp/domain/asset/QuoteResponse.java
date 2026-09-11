package com.repositorio.mvp.domain.asset;

public class QuoteResponse {
    public Quote[] results;

    public static class Quote {
        public String symbol;
        public String shortName;
        public double regularMarketPrice;
        public double regularMarketChange;
        public double regularMarketChangePercent;
        public String currency;
    }
}
