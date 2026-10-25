package com.repositorio.investir_mais.infrastructure;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class PostgreSqlIntegrationTest {

    @Container
    protected static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @Container
    protected static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:8-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configurePostgreSql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.username", () -> "");
        registry.add("spring.data.redis.password", () -> "");
        registry.add("spring.data.redis.ssl.enabled", () -> false);
        registry.add("spring.session.data.redis.namespace", () -> "investir-mais:test:session:" + REDIS.getMappedPort(6379));
        registry.add("spring.cache.redis.key-prefix", () -> "investir-mais:test:cache:" + REDIS.getMappedPort(6379) + ":");
        registry.add("app.security.rate-limit.namespace", () -> "investir-mais:test:rate:" + REDIS.getMappedPort(6379));
    }
}
