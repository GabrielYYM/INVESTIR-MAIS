package com.repositorio.investir_mais.infrastructure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/quote")
@Tag(name = "Brapi Client", description = "Integração declarativa HTTP com a API da Brapi para cotação de ativos")
public interface AssetQuoteClient {

    @Operation(summary = "Consultar cotação de ativo", description = "Busca a cotação de um ativo na Brapi informando o ticker e o token Bearer")
    @GetExchange("/{ticker}")
    String getQuote(
        @Parameter(description = "Ticker do ativo (ex: PETR4, VALE3)", example = "PETR4")
        @PathVariable String ticker, 
        
        @Parameter(description = "Token de autorização para a API Brapi", example = "Bearer SEU_TOKEN_BRAPI")
        @RequestHeader("Authorization") String token
    );
}