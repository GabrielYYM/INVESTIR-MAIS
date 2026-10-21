package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.infrastructure.config.BrapiProperties;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.service.registry.HttpServiceProxyRegistry;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "brapi", types = AssetQuoteClient.class)
public class BrapiClientConfiguration {
    @Bean
    RestClientHttpServiceGroupConfigurer brapiAuthorization(BrapiProperties properties) {
        return groups -> groups.forEachClient((group, builder) -> {
            if (group.name().equals("brapi") && !properties.authorization().isBlank()) {
                builder.defaultHeader(HttpHeaders.AUTHORIZATION, properties.authorization());
            }
        });
    }

    @Bean
    @Primary
    public AssetQuoteClient resilientAssetQuoteClient(HttpServiceProxyRegistry registry, CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        var delegate = registry.getClient("brapi", AssetQuoteClient.class);
        var circuitBreaker = circuitBreakerFactory.create("brapi");
        return ticker -> circuitBreaker.run(() -> delegate.getQuote(ticker), failure -> {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Serviço de cotação temporariamente indisponível.", failure);
        });
    }
}
