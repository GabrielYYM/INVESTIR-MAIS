package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.infrastructure.token.AccountToken;
import com.repositorio.investir_mais.infrastructure.token.AccountTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = JpaAccountTokenServiceTest.Config.class)
class JpaAccountTokenServiceTest {
    @Autowired JpaAccountTokenService service;
    @Autowired AccountTokenRepository tokens;
    @Autowired PlatformTransactionManager transactionManager;

    @BeforeEach void clean() { tokens.deleteAll(); }

    @Test void tokensAreSingleUseAndSeparatedByPurpose() {
        String token = service.generatePasswordResetToken("user@investir.com");
        assertTrue(service.consumeEmailVerificationToken(token).isEmpty());
        assertEquals(Optional.of("user@investir.com"), service.consumePasswordResetToken(token));
        assertTrue(service.consumePasswordResetToken(token).isEmpty());
    }

    @Test void expiredTokensAreRejectedAndResendInvalidatesPreviousToken() {
        tokens.saveAndFlush(new AccountToken("expired", "user@investir.com", Instant.now().minusSeconds(10), "EMAIL_VERIFICATION"));
        assertTrue(service.consumeEmailVerificationToken("expired").isEmpty());
        String first = service.generateEmailVerificationToken("user@investir.com").getTokenValue();
        String second = service.generateEmailVerificationToken("user@investir.com").getTokenValue();
        assertTrue(service.consumeEmailVerificationToken(first).isEmpty());
        assertTrue(service.consumeEmailVerificationToken(second).isPresent());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentConsumptionOnlySucceedsOnce() throws Exception {
        String token = service.generatePasswordResetToken("user@investir.com");
        var start = new CountDownLatch(1);
        Callable<Optional<String>> consume = () -> { start.await(); return service.consumePasswordResetToken(token); };
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(consume);
            var second = executor.submit(consume);
            start.countDown();
            assertEquals(1, List.of(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS))
                    .stream().filter(Optional::isPresent).count());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rollbackRestoresConsumedToken() {
        String token = service.generatePasswordResetToken("user@investir.com");
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertTrue(service.consumePasswordResetToken(token).isPresent());
            status.setRollbackOnly();
        });
        assertTrue(service.consumePasswordResetToken(token).isPresent());
    }

    @Configuration
    @EntityScan(basePackageClasses = AccountToken.class)
    @EnableJpaRepositories(basePackageClasses = AccountTokenRepository.class)
    @Import(JpaAccountTokenService.class)
    static class Config {}
}
