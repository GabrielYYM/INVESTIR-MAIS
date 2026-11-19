package com.repositorio.investir_mais.infrastructure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Repository @RequiredArgsConstructor
public class AdminPendingRepository {
    private static final int EXPIRING_WITHIN_DAYS = 7;
    private final JdbcClient jdbc;

    @Schema(name = "AdminPendingSummary")
    public record Summary(long openReports, long pendingTeacherApplications, long recoverableAccounts,
                          long expiringAccounts, long expiredAccounts, Instant nextRecoveryDeadline,
                          int expiringWithinDays, Instant checkedAt) {}

    @Transactional(readOnly = true)
    public Summary summarize(Instant now) {
        return jdbc.sql("""
            SELECT
                (SELECT count(*) FROM tb_content_report WHERE status='OPEN') AS open_reports,
                (SELECT count(*) FROM teacher_application a JOIN tb_user u ON u.id=a.user_id
                    WHERE a.status='PENDING' AND u.deleted_at IS NULL) AS pending_applications,
                count(*) FILTER (WHERE d.purge_after>:now) AS recoverable_accounts,
                count(*) FILTER (WHERE d.purge_after>:now AND d.purge_after<=:soon) AS expiring_accounts,
                count(*) FILTER (WHERE d.purge_after<=:now) AS expired_accounts,
                min(d.purge_after) FILTER (WHERE d.purge_after>:now) AS next_deadline
            FROM account_deletion d JOIN tb_user u ON u.id=d.user_id
            WHERE d.status='PENDING' AND u.deleted_at IS NOT NULL
            """)
            .param("now", Timestamp.from(now))
            .param("soon", Timestamp.from(now.plus(EXPIRING_WITHIN_DAYS, ChronoUnit.DAYS)))
            .query((result, row) -> {
                Timestamp deadline = result.getTimestamp("next_deadline");
                return new Summary(result.getLong("open_reports"), result.getLong("pending_applications"),
                    result.getLong("recoverable_accounts"), result.getLong("expiring_accounts"),
                    result.getLong("expired_accounts"), deadline == null ? null : deadline.toInstant(),
                    EXPIRING_WITHIN_DAYS, now);
            }).single();
    }
}
