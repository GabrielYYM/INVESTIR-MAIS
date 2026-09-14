package com.repositorio.investir_mais.domain.brapi.service;

import com.repositorio.investir_mais.domain.brapi.dto.BrapiQuoteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class BrapiService {

    @Value("${brapi.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://brapi.dev/api";

    public BrapiQuoteResponse getQuotes(List<String> tickers) {
        List<BrapiQuoteResponse.Quote> todosOsResultados = new ArrayList<>();

        for (String ticker : tickers) {
            BrapiQuoteResponse resposta = getQuote(ticker.trim());
            if (resposta != null && resposta.results() != null) {
                todosOsResultados.addAll(List.of(resposta.results()));
            }
        }

        return new BrapiQuoteResponse(todosOsResultados.toArray(new BrapiQuoteResponse.Quote[0]));
    }

    public BrapiQuoteResponse getQuote(String ticker) {
        String url = String.format("%s/quote/%s", BASE_URL, ticker);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, BrapiQuoteResponse.class).getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ResponseStatusException(
                    HttpStatus.valueOf(e.getStatusCode().value()),
                    "Erro ao consultar a Brapi para '" + ticker + "': " + e.getResponseBodyAsString()
            );
        }
    }
}