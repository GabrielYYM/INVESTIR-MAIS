package com.repositorio.investir_mais.domain.user.service;

import com.repositorio.investir_mais.domain.user.model.AccountDeletion;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.repository.AccountDeletionRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class AccountDeletionService {
    public static final int RECOVERY_DAYS = 30;
    private final JdbcClient jdbc;
    private final AccountDeletionRepository deletions;
    private final EntityManager entities;

    public void deactivate(User user) {
        entities.flush();
        Instant now = Instant.now();
        var request = new AccountDeletion();
        request.setUserId(user.getId()); request.setRequestedAt(now); request.setPurgeAfter(now.plus(RECOVERY_DAYS, ChronoUnit.DAYS));
        // Preserve collections and capture only active content. Older deletions must stay deleted after recovery.
        request.setCourseIds(jdbc.sql("SELECT id FROM tb_course WHERE professor_id=? AND deleted_at IS NULL ORDER BY id FOR UPDATE")
            .param(user.getId()).query(UUID.class).list());
        request.setLessonIds(jdbc.sql("SELECT l.id FROM tb_lesson l JOIN tb_course c ON c.id=l.course_id WHERE c.professor_id=? AND c.deleted_at IS NULL AND l.deleted_at IS NULL ORDER BY l.id FOR UPDATE")
            .param(user.getId()).query(UUID.class).list());
        request.setQuestionIds(jdbc.sql("SELECT q.id FROM tb_question q JOIN tb_course c ON c.id=q.course_id WHERE c.professor_id=? AND c.deleted_at IS NULL AND q.deleted_at IS NULL ORDER BY q.id FOR UPDATE")
            .param(user.getId()).query(UUID.class).list());
        mark("tb_question", request.getQuestionIds(), now); mark("tb_lesson", request.getLessonIds(), now); mark("tb_course", request.getCourseIds(), now);
        jdbc.sql("UPDATE tb_user SET deleted_at=? WHERE id=? AND deleted_at IS NULL").params(Timestamp.from(now), user.getId()).update();
        entities.clear();
        deletions.saveAndFlush(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void restore(UUID id, UUID administratorId, String reason) {
        var row = lockAccount(id);
        var request = pending(id);
        if (!Instant.now().isBefore(request.getPurgeAfter())) throw error(HttpStatus.CONFLICT, "O prazo de 30 dias para recuperação terminou.");
        if (row.get("password") == null || row.get("deleted_at") == null) throw error(HttpStatus.CONFLICT, "Esta conta não pode ser restaurada.");
        jdbc.sql("UPDATE tb_user SET deleted_at=NULL WHERE id=?").param(id).update();
        // Content hidden by the account request is restored together. Previously removed items are untouched.
        mark("tb_course", request.getCourseIds(), null); mark("tb_lesson", request.getLessonIds(), null); mark("tb_question", request.getQuestionIds(), null);
        request.setStatus("RESTORED"); request.setRestoredAt(Instant.now()); request.setRestoredBy(administratorId);
        request.setRestorationReason(reason.trim());
    }

    /** Called after password verification for immediate erasure, or by the expiry worker. */
    public boolean erase(UUID id, boolean immediately) {
        entities.flush();
        var rows = jdbc.sql("SELECT * FROM tb_user WHERE id=? FOR UPDATE").param(id).query().listOfRows();
        if (rows.isEmpty()) return false;
        if (!immediately && Instant.now().isBefore(pending(id).getPurgeAfter())) return false;
        String email = (String) rows.getFirst().get("email");
        var emails = new HashSet<>(jdbc.sql("SELECT DISTINCT email FROM tb_user_audit WHERE id=? AND email IS NOT NULL")
            .param(id).query(String.class).list());
        emails.add(email);
        // Remove dependencies explicitly, including snapshots; ORM soft-delete must not intercept final erasure.
        String courses = "SELECT id FROM tb_course WHERE professor_id=?";
        String lessons = "SELECT id FROM tb_lesson WHERE course_id IN (" + courses + ")";
        String questions = "SELECT id FROM tb_question WHERE course_id IN (" + courses + ")";
        var reportIds = jdbc.sql("SELECT id FROM tb_content_report WHERE reporter_id=? OR (target_type='TEACHER' AND target_id=?) OR course_id IN (" + courses + ")")
            .params(id, id, id).query(UUID.class).list();
        for (UUID report : reportIds) {
            jdbc.sql("DELETE FROM tb_content_report_aud WHERE id=?").param(report).update();
            jdbc.sql("DELETE FROM tb_content_report WHERE id=?").param(report).update();
        }
        jdbc.sql("UPDATE tb_content_report SET reviewer_id=NULL WHERE reviewer_id=?").param(id).update();
        jdbc.sql("UPDATE tb_content_report_aud SET reviewer_id=NULL WHERE reviewer_id=?").param(id).update();
        jdbc.sql("DELETE FROM teacher_document WHERE application_id IN (SELECT id FROM teacher_application WHERE user_id=?)").param(id).update();
        jdbc.sql("DELETE FROM teacher_application WHERE user_id=?").param(id).update();
        jdbc.sql("UPDATE teacher_application SET reviewed_by=NULL WHERE reviewed_by=?").param(id).update();
        jdbc.sql("DELETE FROM question_attempt WHERE student_id=? OR course_id IN (" + courses + ")").params(id, id).update();
        jdbc.sql("DELETE FROM tb_question_pass WHERE student_id=? OR question_id IN (" + questions + ")").params(id, id).update();
        jdbc.sql("DELETE FROM tb_lesson_progress WHERE student_id=? OR lesson_id IN (" + lessons + ")").params(id, id).update();
        jdbc.sql("DELETE FROM tb_course_enrollment WHERE student_id=? OR course_id IN (" + courses + ")").params(id, id).update();
        for (String table : List.of("tb_content_restoration_aud", "tb_content_restoration")) {
            jdbc.sql("DELETE FROM " + table + " WHERE administrator_id=? OR entity_id IN (" + courses + ") OR entity_id IN (" + lessons + ") OR entity_id IN (" + questions + ")")
                .params(id, id, id, id).update();
        }
        jdbc.sql("DELETE FROM tb_question_aud WHERE id IN (" + questions + ")").param(id).update();
        jdbc.sql("DELETE FROM tb_question WHERE course_id IN (" + courses + ")").param(id).update();
        jdbc.sql("DELETE FROM tb_lesson_aud WHERE id IN (" + lessons + ")").param(id).update();
        jdbc.sql("DELETE FROM tb_lesson WHERE course_id IN (" + courses + ")").param(id).update();
        jdbc.sql("DELETE FROM tb_course_aud WHERE id IN (" + courses + ")").param(id).update();
        jdbc.sql("DELETE FROM tb_course WHERE professor_id=?").param(id).update();
        jdbc.sql("DELETE FROM tb_user_audit WHERE id=?").param(id).update();
        jdbc.sql("DELETE FROM account_deletion_aud WHERE user_id=?").param(id).update();
        jdbc.sql("DELETE FROM account_deletion WHERE user_id=?").param(id).update();
        jdbc.sql("UPDATE account_deletion SET restored_by=NULL WHERE restored_by=?").param(id).update();
        jdbc.sql("UPDATE account_deletion_aud SET restored_by=NULL WHERE restored_by=?").param(id).update();
        for (String address : emails) {
            // An old address may now belong to a different active account; do not touch that person's tokens/events.
            if (jdbc.sql("SELECT count(*) FROM tb_user WHERE email=? AND id<>?").params(address, id).query(Long.class).single() == 0) {
                jdbc.sql("UPDATE revinfo SET actor=NULL,actor_role=NULL WHERE actor=?").param(address).update();
                jdbc.sql("DELETE FROM app_one_time_token WHERE username=?").param(address).update();
                jdbc.sql("DELETE FROM one_time_tokens WHERE username=?").param(address).update();
                jdbc.sql("DELETE FROM event_publication WHERE event_type=? AND serialized_event::jsonb->>'email'=?")
                    .params("com.repositorio.investir_mais.domain.AccountEmailRequested", address).update();
            }
        }
        jdbc.sql("DELETE FROM tb_user WHERE id=?").param(id).update();
        // The database permits subject-specific evidence erasure only after the account is physically gone.
        jdbc.sql("SELECT set_config('app.erasure_user_id', ?, true)").param(id.toString()).query(String.class).single();
        jdbc.sql("DELETE FROM legal_document_acceptance WHERE user_id=?").param(id).update();
        jdbc.sql("SELECT set_config('app.erasure_user_id', '', true)").query(String.class).single();
        entities.clear();
        return true;
    }

    private Map<String, Object> lockAccount(UUID id) {
        return jdbc.sql("SELECT * FROM tb_user WHERE id=? FOR UPDATE").param(id).query().listOfRows().stream().findFirst()
            .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Conta não encontrada."));
    }
    private AccountDeletion pending(UUID id) {
        return deletions.findByUserIdAndStatus(id, "PENDING")
            .orElseThrow(() -> error(HttpStatus.CONFLICT, "A conta não tem uma solicitação de exclusão recuperável."));
    }
    private void mark(String table, List<UUID> ids, Instant timestamp) {
        for (UUID id : ids) jdbc.sql("UPDATE " + table + " SET deleted_at=? WHERE id=?")
            .params(Arrays.asList(timestamp == null ? null : Timestamp.from(timestamp), id)).update();
    }
    private ResponseStatusException error(HttpStatus status, String message) { return new ResponseStatusException(status, message); }
}
