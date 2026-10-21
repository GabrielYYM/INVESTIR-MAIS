package com.repositorio.investir_mais.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
@EnableEnversRepositories(basePackages = {"com.repositorio.investir_mais.domain", "com.repositorio.investir_mais.infrastructure.token"})
public class PersistenceConfiguration {
}
