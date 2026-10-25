package com.repositorio.investir_mais.infrastructure.config;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@ConfigurationProperties("app.security.rate-limit")
public record AuthenticationRateLimitProperties(
        @DefaultValue("10") @Positive int capacity,
        @DefaultValue("1m") @DurationMin(millis = 1) Duration refillPeriod,
        @DefaultValue("investir-mais:rate-limit") @NotBlank String namespace) {
}
