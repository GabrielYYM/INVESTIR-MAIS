package com.repositorio.investir_mais.infrastructure;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class AuditRevisionInitialization {
    @Bean
    @DependsOn("entityManagerFactory")
    SmartInitializingSingleton initializeAuditRevisionSequence(DataSource dataSource) {
        return () -> {
            var script = new ResourceDatabasePopulator(new ClassPathResource("audit-revision-postgresql.sql"));
            script.setSeparator(ScriptUtils.EOF_STATEMENT_SEPARATOR);
            script.execute(dataSource);
        };
    }
}
