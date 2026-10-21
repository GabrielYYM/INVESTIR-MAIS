package com.repositorio.investir_mais.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app")
public record WebApplicationProperties(
        @DefaultValue("http://localhost:8080") @NotBlank @URL String publicBaseUrl,
        @DefaultValue @Valid Cors cors) {
    public record Cors(@DefaultValue("http://localhost:5173") @NotBlank @URL String allowedOrigin) {}
}
