package com.repositorio.investir_mais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
@EnableEnversRepositories(basePackages = "com.repositorio.investir_mais.domain")
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
public class InvestirMaisApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvestirMaisApplication.class, args);
    }
}
