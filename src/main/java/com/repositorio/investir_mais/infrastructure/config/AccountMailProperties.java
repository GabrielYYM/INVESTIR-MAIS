package com.repositorio.investir_mais.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@ConfigurationProperties("app.mail")
public record AccountMailProperties(@DefaultValue("30s") @DurationMin(seconds = 1) Duration retryDelay) {
}
