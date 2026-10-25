package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.AccountEmailRequested;
import com.repositorio.investir_mais.domain.AccountSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(AccountSecurityServiceTest.Config.class)
class AccountSecurityServiceTest {
    private static final String EMAIL = "user@investir.com";
    @Autowired AccountSecurityService service;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired FindByIndexNameSessionRepository<MapSession> sessions;

    @BeforeEach
    void clean() {
        reset(sessions);
        jdbc.update("DELETE FROM app_one_time_token");
        jdbc.update("DELETE FROM one_time_tokens");
        jdbc.update("DELETE FROM tb_user");
        jdbc.update("INSERT INTO tb_user (id, email) VALUES (?, ?)", UUID.randomUUID(), EMAIL);
    }

    @Test
    void tokensAreSingleUseAndSeparatedByPurpose() {
        String token = service.generatePasswordResetToken(EMAIL);
        assertTrue(service.consumeEmailVerificationToken(token).isEmpty());
        assertEquals(Optional.of(EMAIL), service.consumePasswordResetToken(token));
        assertTrue(service.consumePasswordResetToken(token).isEmpty());
    }

    @Test
    void expiredTokensAreRejectedAndResendInvalidatesOnlyTheSamePurpose() {
        String reset = service.generatePasswordResetToken(EMAIL);
        String first = service.generateEmailVerificationToken(EMAIL).getTokenValue();
        String second = service.generateEmailVerificationToken(EMAIL).getTokenValue();
        assertTrue(service.consumeEmailVerificationToken(first).isEmpty());
        assertTrue(service.consumePasswordResetToken(reset).isPresent());
        jdbc.update("UPDATE app_one_time_token SET expires_at = ? WHERE token_value = ?",
                Timestamp.from(Instant.now().minusSeconds(10)), second);
        assertTrue(service.consumeEmailVerificationToken(second).isEmpty());
        assertFalse(service.isTokenActive(new AccountEmailRequested(EMAIL, second, AccountEmailRequested.Purpose.VERIFY_EMAIL)));
    }

    @Test
    void emailDeliveryRequiresAnActiveTokenForTheRightRecipientAndPurpose() {
        String token = service.generatePasswordResetToken(EMAIL);
        assertTrue(service.isTokenActive(new AccountEmailRequested(EMAIL, token, AccountEmailRequested.Purpose.PASSWORD_RESET)));
        assertFalse(service.isTokenActive(new AccountEmailRequested("other@investir.com", token, AccountEmailRequested.Purpose.PASSWORD_RESET)));
        assertFalse(service.isTokenActive(new AccountEmailRequested(EMAIL, token, AccountEmailRequested.Purpose.VERIFY_EMAIL)));
        service.invalidateAll(EMAIL);
        assertFalse(service.isTokenActive(new AccountEmailRequested(EMAIL, token, AccountEmailRequested.Purpose.PASSWORD_RESET)));
    }

    @Test
    void concurrentConsumptionOnlySucceedsOnce() throws Exception {
        String token = service.generatePasswordResetToken(EMAIL);
        var start = new CountDownLatch(1);
        Callable<Optional<String>> consume = () -> { start.await(); return service.consumePasswordResetToken(token); };
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(consume);
            var second = executor.submit(consume);
            start.countDown();
            assertEquals(1, List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS))
                    .stream().filter(Optional::isPresent).count());
        }
    }

    @Test
    void concurrentIssuanceLeavesOneTokenPerPurpose() throws Exception {
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var futures = new ArrayList<Future<String>>();
            for (int index = 0; index < 8; index++) futures.add(executor.submit(() -> {
                start.await();
                return service.generatePasswordResetToken(EMAIL);
            }));
            start.countDown();
            for (var future : futures) future.get(10, TimeUnit.SECONDS);
        }
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM app_one_time_token WHERE username = ? AND purpose = 'PASSWORD_RESET'", Integer.class, EMAIL));
    }

    @Test
    void rollbackRestoresConsumedTokenAndDiscardsNewTokens() {
        String token = service.generatePasswordResetToken(EMAIL);
        var transaction = new TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            assertTrue(service.consumePasswordResetToken(token).isPresent());
            service.generateEmailVerificationToken(EMAIL);
            status.setRollbackOnly();
        });
        assertTrue(service.consumePasswordResetToken(token).isPresent());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM app_one_time_token", Integer.class));
    }

    @Test
    void revocationDeletesIndexedSessionsAndOnlyThatUsersLoginCodes() {
        var session = new MapSession();
        when(sessions.findByPrincipalName(EMAIL)).thenReturn(Map.of(session.getId(), session));
        jdbc.update("INSERT INTO one_time_tokens VALUES (?, ?, ?)", "own", EMAIL, Timestamp.from(Instant.now().plusSeconds(300)));
        jdbc.update("INSERT INTO one_time_tokens VALUES (?, ?, ?)", "other", "other@investir.com", Timestamp.from(Instant.now().plusSeconds(300)));
        service.revokeAllSessions(EMAIL);
        verify(sessions).deleteById(session.getId());
        assertEquals(List.of("other"), jdbc.queryForList("SELECT token_value FROM one_time_tokens", String.class));
    }

    @Test
    void cleanupRemovesExpiredTokensWithoutRemovingValidOnes() {
        String valid = service.generatePasswordResetToken(EMAIL);
        jdbc.update("INSERT INTO app_one_time_token VALUES (?, ?, ?, ?)", "expired", EMAIL,
                Timestamp.from(Instant.now().minusSeconds(10)), "EMAIL_VERIFICATION");
        jdbc.update("INSERT INTO one_time_tokens VALUES (?, ?, ?)", "login-expired", EMAIL, Timestamp.from(Instant.now().minusSeconds(10)));
        service.cleanupExpiredTokens();
        assertEquals(List.of(valid), jdbc.queryForList("SELECT token_value FROM app_one_time_token", String.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM one_time_tokens", Integer.class));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @Import(AccountSecurityService.class)
    static class Config {
        @Bean(destroyMethod = "shutdown") DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2)
                    .addScript("classpath:account-security-schema.sql").addScript("classpath:schema-postgresql.sql").build();
        }
        @Bean JdbcTemplate jdbc(DataSource source) { return new JdbcTemplate(source); }
        @Bean JdbcClient jdbcClient(JdbcTemplate jdbc) { return JdbcClient.create(jdbc); }
        @Bean PlatformTransactionManager transactionManager(DataSource source) { return new JdbcTransactionManager(source); }
        @Bean @SuppressWarnings("unchecked") FindByIndexNameSessionRepository<MapSession> sessions() {
            return mock(FindByIndexNameSessionRepository.class);
        }
    }
}
