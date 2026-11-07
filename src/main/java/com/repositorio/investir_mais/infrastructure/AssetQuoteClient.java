package com.repositorio.investir_mais.infrastructure;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/quote")
public interface AssetQuoteClient {
    @GetExchange("/{ticker}")
    String getQuote(@PathVariable String ticker);
}
