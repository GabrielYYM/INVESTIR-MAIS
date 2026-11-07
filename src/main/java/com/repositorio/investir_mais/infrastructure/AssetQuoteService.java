package com.repositorio.investir_mais.infrastructure;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetQuoteService {
    private final AssetQuoteClient assetQuoteClient;

    @Cacheable(cacheNames = "cotacoes", key = "#p0.toUpperCase(T(java.util.Locale).ROOT)",
            unless = "#result == null || #result.isBlank()")
    public String getQuote(String ticker) {
        return assetQuoteClient.getQuote(ticker.toUpperCase(Locale.ROOT));
    }
}
