package com.repositorio.investir_mais.infrastructure;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class BrapiQuoteController {
    private final AssetQuoteClient assetQuoteClient;

    @Retryable(includes = {ResourceAccessException.class, HttpServerErrorException.BadGateway.class,
            HttpServerErrorException.ServiceUnavailable.class, HttpServerErrorException.GatewayTimeout.class},
            maxRetries = 2, delay = 200)
    @GetMapping(value = "/{ticker}/quote", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getQuote(@PathVariable @Pattern(regexp = "[A-Za-z0-9.-]{1,16}", message = "Ticker inválido.") String ticker) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.APPLICATION_JSON)
                .body(assetQuoteClient.getQuote(ticker.toUpperCase(Locale.ROOT)));
    }
}
