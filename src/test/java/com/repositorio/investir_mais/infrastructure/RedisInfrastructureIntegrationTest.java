package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties.RateLimit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = RedisInfrastructureIntegrationTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RedisInfrastructureIntegrationTest {
    @Container static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:8-alpine"))
            .withExposedPorts(6379);
    private static final String PREFIX = "investir-mais:test:" + UUID.randomUUID() + ":";
    @Autowired StringRedisTemplate redis;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry properties) {
        properties.add("spring.data.redis.host", REDIS::getHost);
        properties.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        properties.add("spring.data.redis.username", () -> "");
        properties.add("spring.data.redis.password", () -> "");
        properties.add("spring.data.redis.ssl.enabled", () -> false);
    }

    @Test
    void concurrentRequestsShareOneAtomicRateLimit() throws Exception {
        var filter = filter(10, Duration.ofMinutes(1));
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(16)) {
            var responses = new ArrayList<Future<MockHttpServletResponse>>();
            for (int index = 0; index < 32; index++) responses.add(executor.submit(() -> {
                start.await();
                return request(filter, "POST", "/auth/login", "127.0.0.1");
            }));
            start.countDown();
            int allowed = 0;
            int blocked = 0;
            for (var future : responses) {
                var response = future.get(10, TimeUnit.SECONDS);
                if (response.getStatus() == 204) allowed++;
                else {
                    assertEquals(429, response.getStatus());
                    assertNotNull(response.getHeader("Retry-After"));
                    blocked++;
                }
            }
            assertEquals(10, allowed);
            assertEquals(22, blocked);
        }
    }

    @Test
    void rateLimitExpiresAndSeparatesIpsAndEndpoints() throws Exception {
        var filter = filter(1, Duration.ofSeconds(1));
        assertEquals(204, request(filter, "POST", "/auth/login", "127.0.0.1").getStatus());
        assertEquals(429, request(filter, "POST", "/auth/login", "127.0.0.1").getStatus());
        assertEquals(204, request(filter, "POST", "/auth/code", "127.0.0.1").getStatus());
        assertEquals(204, request(filter, "POST", "/auth/login", "127.0.0.2").getStatus());
        await().atMost(Duration.ofSeconds(4)).untilAsserted(() ->
                assertEquals(204, request(filter, "POST", "/auth/login", "127.0.0.1").getStatus()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void redisFailureBlocksAuthenticationButDoesNotAffectUnrelatedRoutes() throws Exception {
        var unavailable = mock(StringRedisTemplate.class);
        when(unavailable.execute(any(RedisScript.class), anyList(), anyString(), anyString()))
                .thenThrow(new RedisConnectionFailureException("simulated failure"));
        var filter = new SecurityFilter(unavailable, new RateLimit(10, Duration.ofMinutes(1), PREFIX));
        var response = request(filter, "POST", "/auth/login", "127.0.0.1");
        assertEquals(503, response.getStatus());
        assertEquals("1", response.getHeader("Retry-After"));
        assertEquals(204, request(filter, "GET", "/api/courses", "127.0.0.1").getStatus());
        verify(unavailable).execute(any(RedisScript.class), anyList(), anyString(), anyString());
    }

    private SecurityFilter filter(int capacity, Duration period) {
        return new SecurityFilter(redis, new RateLimit(capacity, period, PREFIX + UUID.randomUUID()));
    }

    private MockHttpServletResponse request(SecurityFilter filter, String method, String endpoint, String ip) throws Exception {
        var request = new MockHttpServletRequest(method, endpoint);
        request.setRemoteAddr(ip);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse) res).setStatus(204));
        return response;
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(DataRedisAutoConfiguration.class)
    static class Config {}
}
