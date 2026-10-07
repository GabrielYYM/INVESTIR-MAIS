package com.repositorio.investir_mais.infrastructure;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "brapiClient", url = "https://brapi.dev/api")
public interface AssetQuoteClient {

    @GetMapping("/quote/{ticker}")
    String getQuote(@PathVariable("ticker") String ticker, @RequestHeader("Authorization") String token);
}