package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.user.service.AccountDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component @RequiredArgsConstructor @Slf4j
public class AccountErasureJob {
    private final JdbcClient jdbc;
    private final AccountDeletionService deletions;
    @Scheduled(cron = "${app.privacy.account-erasure-cron:0 0 * * * *}")
    public void eraseExpired() {
        var ids = jdbc.sql("SELECT user_id FROM account_deletion WHERE status='PENDING' AND purge_after<=CURRENT_TIMESTAMP ORDER BY purge_after LIMIT 100")
            .query(UUID.class).list();
        for (UUID id : ids) {
            try { deletions.erase(id, false); }
            catch (RuntimeException exception) { log.error("Falha na eliminação de conta expirada {}", id, exception); }
        }
    }
}
