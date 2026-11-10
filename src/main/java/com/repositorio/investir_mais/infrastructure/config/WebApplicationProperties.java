package com.repositorio.investir_mais.infrastructure.config;

import java.time.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app")
public record WebApplicationProperties(
        @DefaultValue("http://localhost:8080") @NotBlank @URL String publicBaseUrl,
        @DefaultValue @Valid Cors cors,
        @DefaultValue @Valid Security security) {
    public record Cors(@DefaultValue("http://localhost:5173") @NotBlank @URL String allowedOrigin) {}

    public record Security(@DefaultValue @Valid RateLimit rateLimit) {}

    public record RateLimit(
            @DefaultValue("10") @Positive int capacity,
            @DefaultValue("1m") @DurationMin(millis = 1) Duration refillPeriod,
            @DefaultValue("investir-mais:rate-limit") @NotBlank String namespace) {}

    public String frontendUrl() {
        return cors.allowedOrigin();
    }
}
