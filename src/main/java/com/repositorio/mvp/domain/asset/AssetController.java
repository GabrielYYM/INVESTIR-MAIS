package com.repositorio.mvp.domain.asset;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final BrapiService brapiService;

    public AssetController(BrapiService brapiService) {
        this.brapiService = brapiService;
    }

    @GetMapping("/{ticker}")
    public QuoteResponse getAsset(@PathVariable String ticker) {
        return brapiService.getQuote(ticker);
    }
}
