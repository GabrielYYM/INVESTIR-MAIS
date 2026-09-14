package com.repositorio.investir_mais.domain.brapi.controller;
import java.util.List;

import com.repositorio.investir_mais.domain.brapi.dto.BrapiQuoteResponse;
import com.repositorio.investir_mais.domain.brapi.service.BrapiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
public class BrapiAssetController {

    private final BrapiService brapiService;

    public BrapiAssetController(BrapiService brapiService) {
        this.brapiService = brapiService;
    }

    @GetMapping("/{tickers}")
    public BrapiQuoteResponse getAssets(@PathVariable String tickers) {
        List<String> listaDeTickers = List.of(tickers.split(","));
        return brapiService.getQuotes(listaDeTickers);
    }
}
