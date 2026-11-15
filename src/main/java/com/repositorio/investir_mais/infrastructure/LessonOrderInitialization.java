package com.repositorio.investir_mais.infrastructure;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class LessonOrderInitialization {
    @Bean
    @DependsOn("entityManagerFactory")
    SmartInitializingSingleton initializeLegacyLessonOrder(DataSource dataSource) {
        return () -> new ResourceDatabasePopulator(
                new ClassPathResource("lesson-order-postgresql.sql")).execute(dataSource);
    }
}
