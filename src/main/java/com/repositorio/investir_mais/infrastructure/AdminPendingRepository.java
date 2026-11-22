package com.repositorio.investir_mais.infrastructure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.Instant;

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
                0 AS recoverable_accounts, 0 AS expiring_accounts, 0 AS expired_accounts,
                CAST(NULL AS timestamp) AS next_deadline
            """)
            .query((result, row) -> {
                Timestamp deadline = result.getTimestamp("next_deadline");
                return new Summary(result.getLong("open_reports"), result.getLong("pending_applications"),
                    result.getLong("recoverable_accounts"), result.getLong("expiring_accounts"),
                    result.getLong("expired_accounts"), deadline == null ? null : deadline.toInstant(),
                    EXPIRING_WITHIN_DAYS, now);
            }).single();
    }
}
