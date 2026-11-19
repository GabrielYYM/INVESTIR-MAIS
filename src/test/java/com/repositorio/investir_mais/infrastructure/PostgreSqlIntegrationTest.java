package com.repositorio.investir_mais.infrastructure;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@MockitoBean(types = ResendEmailSender.class)
public abstract class PostgreSqlIntegrationTest {

    @Container
    protected static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @Container
    protected static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:8-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configurePostgreSql(DynamicPropertyRegistry registry) {
        String external = System.getenv("INVESTIR_TEST_DATABASE_URL");
        if (external != null) {
            if (!external.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/investir_test_[a-z0-9_]+")) {
                throw new IllegalArgumentException("External integration tests require an isolated investir_test_* database on loopback.");
            }
            registry.add("spring.datasource.url", () -> external);
            registry.add("spring.datasource.username", () -> "postgres");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
            return;
        }
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
        registry.add("app.security.rate-limit.namespace", () -> "investir-mais:test:rate:" + REDIS.getMappedPort(6379));
    }
}
