package com.repositorio.investir_mais.domain.brapi.service;

import com.repositorio.investir_mais.domain.brapi.DTO.BrapiQuoteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

/**
 * Integração com a Brapi.
 *
 * O plano atual da Brapi aceita apenas UMA requisição simultânea
 * (concurrencyLimit = 1). Por isso:
 *  - as chamadas passam por uma fila (lock justo): uma de cada vez;
 *  - cada cotação fica em cache por alguns segundos, evitando chamadas repetidas
 *    quando a tela recarrega a carteira;
 *  - se mesmo assim a Brapi responder 429, esperamos e tentamos de novo.
 */
@Slf4j
@Service
public class BrapiService {

    @Value("${brapi.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://brapi.dev/api";

    /** Tempo que uma cotação fica guardada em cache. */
    private static final long CACHE_TTL_MS = 60_000;
    /** Número de novas tentativas quando a Brapi responde 429. */
    private static final int MAX_RETRIES = 3;
    /** Espera base entre tentativas (dobra a cada tentativa). */
    private static final long RETRY_DELAY_MS = 700;

    /** Garante uma única requisição à Brapi por vez, na ordem de chegada. */
    private final ReentrantLock brapiLock = new ReentrantLock(true);

    private final Map<String, CachedQuote> cache = new ConcurrentHashMap<>();

    private record CachedQuote(BrapiQuoteResponse response, long timestamp) {
        boolean isValid() {
            return System.currentTimeMillis() - timestamp < CACHE_TTL_MS;
        }
    }

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
        String chave = ticker.trim().toUpperCase();

        CachedQuote emCache = cache.get(chave);
        if (emCache != null && emCache.isValid()) {
            return emCache.response();
        }

        brapiLock.lock();
        try {
            // Outra requisição pode ter buscado o mesmo ticker enquanto esperávamos na fila.
            emCache = cache.get(chave);
            if (emCache != null && emCache.isValid()) {
                return emCache.response();
            }

            BrapiQuoteResponse resposta = consultarComRetry(chave);
            cache.put(chave, new CachedQuote(resposta, System.currentTimeMillis()));
            return resposta;
        } finally {
            brapiLock.unlock();
        }
    }

    private BrapiQuoteResponse consultarComRetry(String ticker) {
        String url = String.format("%s/quote/%s", BASE_URL, ticker);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (int tentativa = 0; ; tentativa++) {
            try {
                return restTemplate.exchange(url, HttpMethod.GET, entity, BrapiQuoteResponse.class).getBody();
            } catch (HttpClientErrorException e) {
                boolean limiteAtingido = e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
                if (limiteAtingido && tentativa < MAX_RETRIES) {
                    long espera = RETRY_DELAY_MS * (1L << tentativa);
                    log.warn("Brapi: limite atingido para '{}'. Nova tentativa em {} ms.", ticker, espera);
                    aguardar(espera);
                    continue;
                }
                throw erroBrapi(ticker, e.getStatusCode().value(), e.getResponseBodyAsString());
            } catch (HttpServerErrorException e) {
                throw erroBrapi(ticker, e.getStatusCode().value(), e.getResponseBodyAsString());
            }
        }
    }

    private ResponseStatusException erroBrapi(String ticker, int status, String corpo) {
        return new ResponseStatusException(
                HttpStatus.valueOf(status),
                "Erro ao consultar a Brapi para '" + ticker + "': " + corpo);
    }

    private void aguardar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Consulta à Brapi interrompida.");
        }
    }
}