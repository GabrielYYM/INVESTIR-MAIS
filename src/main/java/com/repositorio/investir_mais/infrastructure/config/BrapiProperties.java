package com.repositorio.investir_mais.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("brapi")
public record BrapiProperties(@DefaultValue("") String token) {
    public String authorization() {
        return token.isBlank() ? "" : token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
