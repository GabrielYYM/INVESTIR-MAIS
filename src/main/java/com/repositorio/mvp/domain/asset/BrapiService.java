package com.repositorio.mvp.domain.asset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BrapiService {

    @Value("${brapi.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://brapi.dev/api";

    public QuoteResponse getQuote(String ticker) {
        String url = String.format("%s/quote/%s", BASE_URL, ticker);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, QuoteResponse.class).getBody();
    }
}