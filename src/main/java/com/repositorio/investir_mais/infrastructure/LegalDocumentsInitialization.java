package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.legal.service.LegalDocumentsService;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class LegalDocumentsInitialization {
    @Bean
    @DependsOnDatabaseInitialization
    SmartInitializingSingleton publishLegalDocuments(DataSource dataSource, LegalDocumentsService legal) {
        return () -> {
            new ResourceDatabasePopulator(new ClassPathResource("legal-history-postgresql.sql")).execute(dataSource);
            legal.publishBundledReleases();
        };
    }
}
