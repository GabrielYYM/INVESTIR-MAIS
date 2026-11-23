package com.repositorio.investir_mais.domain.user.service;

import com.repositorio.investir_mais.domain.user.model.AccountDeletion;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.repository.AccountDeletionRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service @RequiredArgsConstructor @Transactional
public class AccountDeletionService {
    private final JdbcClient jdbc;
    private final AccountDeletionRepository deletions;
    private final EntityManager entities;

    /** Suppress profile identifiers while retaining relationships and action history. */
    public void anonymize(User user) {
        UUID id = user.getId();
        String email = user.getEmail();
        String replacement = "deleted-" + UUID.randomUUID() + "@deleted.invalid";
        user.setName("Usuário excluído");
        user.setEmail(replacement);
        user.setPassword(null);
        user.setEmailVerified(false);
        if (entities.contains(user)) entities.flush();
        else jdbc.sql("UPDATE tb_user SET name=?,email=?,password=NULL,email_verified=false WHERE id=? AND deleted_at IS NOT NULL")
            .params(user.getName(), replacement, id).update();
        // Keep revisions, operations, timestamps and IDs; suppress only direct profile identifiers.
        jdbc.sql("UPDATE tb_user_audit SET name=?, email=? WHERE id=?")
            .params(user.getName(), replacement, id).update();
        // The current address still belongs to this locked account. Do not rewrite events for former addresses.
        jdbc.sql("UPDATE revinfo SET actor=? WHERE actor=?").params("deleted:" + id, email).update();
        jdbc.sql("UPDATE tb_user SET deleted_at=CURRENT_TIMESTAMP WHERE id=? AND deleted_at IS NULL").param(id).update();
        jdbc.sql("UPDATE tb_question SET deleted_at=CURRENT_TIMESTAMP WHERE deleted_at IS NULL AND course_id IN (SELECT id FROM tb_course WHERE professor_id=?)").param(id).update();
        jdbc.sql("UPDATE tb_lesson SET deleted_at=CURRENT_TIMESTAMP WHERE deleted_at IS NULL AND course_id IN (SELECT id FROM tb_course WHERE professor_id=?)").param(id).update();
        jdbc.sql("UPDATE tb_course SET deleted_at=CURRENT_TIMESTAMP WHERE professor_id=? AND deleted_at IS NULL").param(id).update();
        // Uploaded identity/qualification documents have no continuing product purpose.
        jdbc.sql("DELETE FROM teacher_document WHERE application_id IN (SELECT id FROM teacher_application WHERE user_id=?)").param(id).update();
        jdbc.sql("UPDATE teacher_application SET review_reason=NULL WHERE user_id=?").param(id).update();
        jdbc.sql("DELETE FROM app_one_time_token WHERE username=?").param(email).update();
        jdbc.sql("DELETE FROM one_time_tokens WHERE username=?").param(email).update();
        jdbc.sql("DELETE FROM event_publication WHERE event_type=? AND serialized_event LIKE ? ESCAPE '!'")
            .params("com.repositorio.investir_mais.domain.AccountEmailRequested", "%\"email\":\"" + email.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "\"%").update();
        var request = deletions.findByUserIdAndStatus(id, "PENDING").orElseGet(AccountDeletion::new);
        request.setUserId(id);
        if (request.getRequestedAt() == null) request.setRequestedAt(Instant.now());
        request.setPurgeAfter(request.getRequestedAt());
        request.setStatus("SOFT_DELETED");
        deletions.saveAndFlush(request);
        entities.clear();
    }

    /** Upgrade a legacy soft deletion only when its former address is requested again. */
    public void anonymizeDeletedByEmail(String email) {
        var ids = jdbc.sql("SELECT id FROM tb_user WHERE email=? AND deleted_at IS NOT NULL FOR UPDATE")
            .param(email).query(UUID.class).list();
        for (UUID id : ids) {
            var deleted = new User(); deleted.setId(id); deleted.setEmail(email);
            anonymize(deleted);
        }
    }

}
