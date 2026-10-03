package com.repositorio.investir_mais.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BrapiConfig {
    @Bean
    RestClient brapiClient(RestClient.Builder builder, @Value("${brapi.token}") String token) {
        return builder
                .baseUrl("https://brapi.dev/api")
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }
}
