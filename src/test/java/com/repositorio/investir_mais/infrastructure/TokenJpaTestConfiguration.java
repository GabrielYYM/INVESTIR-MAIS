package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.token.mapper.TokenMapper;
import com.repositorio.investir_mais.domain.token.repository.AccountTokenRepository;
import com.repositorio.investir_mais.domain.token.service.LoginTokenService;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableJpaRepositories(basePackageClasses = AccountTokenRepository.class)
@Import(LoginTokenService.class)
class TokenJpaTestConfiguration {
    @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource source) {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(source);
        factory.setPackagesToScan("com.repositorio.investir_mais.domain.token.model");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return factory;
    }
    @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }
    @Bean TokenMapper tokenMapper() { return Mappers.getMapper(TokenMapper.class); }
}
