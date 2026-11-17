package com.repositorio.investir_mais.config;

import java.time.Duration;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.constraints.time.DurationMax;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app")
public record WebApplicationProperties(
        @DefaultValue("http://localhost:8080") @NotBlank @URL String publicBaseUrl,
        @DefaultValue @Valid Cors cors,
        @DefaultValue @Valid Security security,
        @DefaultValue @Valid Teacher teacher,
        @DefaultValue @Valid Mail mail) {
    public record Cors(@DefaultValue("http://localhost:5173") @NotBlank @URL String allowedOrigin) {}

    public record Security(
            @DefaultValue @Valid RateLimit rateLimit,
            @DefaultValue("30s") @NotNull @DurationMin @DurationMax(minutes = 5) Duration emailCooldown) {}

    public record Teacher(
            @DefaultValue("comprovante-de-qualificacao") @NotNull @Size(min = 1, max = 4)
            List<@Pattern(regexp = "[a-z0-9-]{1,100}") String> requiredDocuments) {
        public Teacher {
            if (requiredDocuments != null) {
                requiredDocuments = requiredDocuments.stream().map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
            }
        }
    }

    public record Mail(@DefaultValue("30s") @NotNull @DurationMin(seconds = 1) Duration retryDelay) {}

    public record RateLimit(
            @DefaultValue("10") @Positive int capacity,
            @DefaultValue("1m") @DurationMin(millis = 1) Duration refillPeriod,
            @DefaultValue("investir-mais:rate-limit") @NotBlank String namespace) {}

    public String frontendUrl() {
        return cors.allowedOrigin();
    }
}
