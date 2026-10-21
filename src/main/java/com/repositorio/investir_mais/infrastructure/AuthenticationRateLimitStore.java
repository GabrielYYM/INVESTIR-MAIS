package com.repositorio.investir_mais.infrastructure;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.repositorio.investir_mais.infrastructure.config.AuthenticationRateLimitProperties;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class AuthenticationRateLimitStore {
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final Duration refillPeriod;

    public AuthenticationRateLimitStore(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager, AuthenticationRateLimitProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.refillPeriod = properties.refillPeriod();
    }

    @Retryable(includes = DuplicateKeyException.class, maxRetries = 2, delay = 0)
    public boolean tryConsume(String bucketKey, int capacity) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> consumeInTransaction(bucketKey, capacity)));
    }

    private boolean consumeInTransaction(String bucketKey, int capacity) {
        Instant now = Instant.now();
        List<RateLimitWindow> windows = jdbcTemplate.query(
            "SELECT window_started_at, request_count FROM app_auth_rate_limit WHERE bucket_key = ? FOR UPDATE",
            (resultSet, rowNumber) -> new RateLimitWindow(
                resultSet.getTimestamp("window_started_at").toInstant(), resultSet.getInt("request_count")),
            bucketKey);

        if (windows.isEmpty()) {
            jdbcTemplate.update(
                "INSERT INTO app_auth_rate_limit (bucket_key, window_started_at, request_count) VALUES (?, ?, 1)",
                bucketKey, Timestamp.from(now));
            return true;
        }

        RateLimitWindow window = windows.getFirst();
        if (!now.isBefore(window.startedAt().plus(refillPeriod))) {
            jdbcTemplate.update(
                "UPDATE app_auth_rate_limit SET window_started_at = ?, request_count = 1 WHERE bucket_key = ?",
                Timestamp.from(now), bucketKey);
            return true;
        }
        if (window.requestCount() >= capacity) {
            return false;
        }
        jdbcTemplate.update(
            "UPDATE app_auth_rate_limit SET request_count = request_count + 1 WHERE bucket_key = ?", bucketKey);
        return true;
    }

    @Scheduled(fixedDelayString = "${app.security.rate-limit.cleanup-delay:900000}")
    public void cleanExpiredWindows() {
        jdbcTemplate.update("DELETE FROM app_auth_rate_limit WHERE window_started_at < ?",
            Timestamp.from(Instant.now().minus(refillPeriod)));
    }

    private record RateLimitWindow(Instant startedAt, int requestCount) {}
}
