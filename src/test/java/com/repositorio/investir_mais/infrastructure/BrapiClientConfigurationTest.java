package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.infrastructure.config.BrapiProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.autoconfigure.imperative.ImperativeHttpClientAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.service.HttpServiceClientPropertiesAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.service.HttpServiceClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.service.registry.HttpServiceProxyRegistry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = BrapiClientConfiguration.class)
@Import({BrapiClientConfigurationTest.BreakerConfig.class, BrapiQuoteController.class, AssetQuoteService.class})
@ImportAutoConfiguration({RestClientAutoConfiguration.class, HttpServiceClientAutoConfiguration.class,
        HttpServiceClientPropertiesAutoConfiguration.class, ImperativeHttpClientAutoConfiguration.class})
class BrapiClientConfigurationTest {
    private static final AtomicReference<String> authorization = new AtomicReference<>();
    private static final HttpServer server = startServer();
    @Autowired AssetQuoteClient client;
    @Autowired BrapiQuoteController controller;
    private static final AtomicInteger attempts = new AtomicInteger();

    @BeforeEach void resetAttempts() { attempts.set(0); }
    @Autowired HttpServiceProxyRegistry registry;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry properties) {
        properties.add("spring.http.serviceclient.brapi.base-url", () -> "http://127.0.0.1:" + server.getAddress().getPort());
        properties.add("spring.http.serviceclient.brapi.connect-timeout", () -> "1s");
        properties.add("spring.http.serviceclient.brapi.read-timeout", () -> "200ms");
        properties.add("brapi.token", () -> "test-token");
    }

    @TestConfiguration
    @EnableResilientMethods
    @EnableConfigurationProperties(BrapiProperties.class)
    static class BreakerConfig {
        @Bean
        @SuppressWarnings({"unchecked", "rawtypes"})
        CircuitBreakerFactory<?, ?> circuitBreakerFactory() {
            CircuitBreakerFactory<?, ?> factory = mock(CircuitBreakerFactory.class);
            CircuitBreaker breaker = mock(CircuitBreaker.class);
            when(factory.create("brapi")).thenReturn(breaker);
            when(breaker.run(any(Supplier.class), any(Function.class))).thenAnswer(invocation -> {
                try { return ((Supplier<?>) invocation.getArgument(0)).get(); }
                catch (RuntimeException failure) { return ((Function) invocation.getArgument(1)).apply(failure); }
            });
            return factory;
        }
    }

    @Test void nativeGroupAppliesBaseUrlAndAuthorization() {
        assertEquals("{\"ok\":true}", client.getQuote("PETR4"));
        assertEquals("Bearer test-token", authorization.get());
    }

    @Test void nativeGroupAppliesReadTimeout() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () ->
                assertThrows(Exception.class, () -> registry.getClient("brapi", AssetQuoteClient.class).getQuote("SLOW")));
    }

    @Test void circuitBreakerStillMapsFailureToServiceUnavailable() {
        ResponseStatusException failure = assertThrows(ResponseStatusException.class, () -> client.getQuote("SLOW"));
        assertEquals(503, failure.getStatusCode().value());
    }

    @Test void transientFailureIsRetriedUntilSuccess() {
        assertEquals("{\"ok\":true}", controller.getQuote("RECOVER").getBody());
        assertEquals(3, attempts.get());
    }

    @Test void persistentServiceUnavailableStopsAfterThreeAttempts() {
        var failure = assertThrows(ResponseStatusException.class, () -> controller.getQuote("UNAVAILABLE"));
        assertEquals(503, failure.getStatusCode().value());
        assertEquals(3, attempts.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BADREQUEST", "UNAUTHORIZED", "NOTFOUND"})
    void clientErrorsAreNotRetried(String ticker) {
        assertThrows(ResponseStatusException.class, () -> controller.getQuote(ticker));
        assertEquals(1, attempts.get());
    }

    @AfterAll static void stop() { server.stop(0); }

    private static HttpServer startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.createContext("/quote/", exchange -> {
                authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                try {
                    String ticker = exchange.getRequestURI().getPath().substring("/quote/".length());
                    int attempt = attempts.incrementAndGet();
                    int status = switch (ticker) {
                        case "RECOVER" -> attempt < 3 ? 503 : 200;
                        case "UNAVAILABLE" -> 503;
                        case "BADREQUEST" -> 400;
                        case "UNAUTHORIZED" -> 401;
                        case "NOTFOUND" -> 404;
                        default -> 200;
                    };
                    if (exchange.getRequestURI().getPath().endsWith("SLOW")) Thread.sleep(1000);
                    byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(status, body.length);
                    exchange.getResponseBody().write(body);
                } catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
                finally { exchange.close(); }
            });
            server.start();
            return server;
        } catch (IOException exception) { throw new IllegalStateException(exception); }
    }
}
