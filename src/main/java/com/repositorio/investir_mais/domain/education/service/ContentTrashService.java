package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.model.ContentRestoration;
import com.repositorio.investir_mais.domain.user.service.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Timestamp;
import java.util.*;

@Service @RequiredArgsConstructor @PreAuthorize("hasRole('ADMIN')")
public class ContentTrashService {
    private static final Map<String, String> TABLES = Map.of("courses", "tb_course", "lessons", "tb_lesson", "questions", "tb_question");
    private final JdbcClient jdbc;
    private final EntityManager entities;
    private final UserService users;

    @Transactional(readOnly = true)
    public Map<String, Object> list(int page, int size) {
        String union = """
            FROM (SELECT 'courses' AS entity, id, name AS title, id AS course_id, deleted_at FROM tb_course WHERE deleted_at IS NOT NULL
            UNION ALL SELECT 'lessons', id, title, course_id, deleted_at FROM tb_lesson WHERE deleted_at IS NOT NULL
            UNION ALL SELECT 'questions', id, prompt, course_id, deleted_at FROM tb_question WHERE deleted_at IS NOT NULL) removed
            """;
        long total = jdbc.sql("SELECT count(*) " + union).query(Long.class).single();
        var rows = jdbc.sql("SELECT * " + union + " ORDER BY deleted_at DESC, entity, id LIMIT ? OFFSET ?")
            .params(size, (long) page * size).query().listOfRows();
        return Map.of("items", rows, "totalItems", total, "page", page, "size", size);
    }

    @Transactional
    public void restore(String entity, UUID id, String email) {
        String table = TABLES.get(entity);
        if (table == null) throw error(HttpStatus.BAD_REQUEST, "Tipo de conteúdo inválido.");
        UUID courseId = entity.equals("courses") ? id : jdbc.sql("SELECT course_id FROM " + table + " WHERE id=?")
            .param(id).query(UUID.class).optional().orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Conteúdo não encontrado."));
        UUID professorId = jdbc.sql("SELECT professor_id FROM tb_course WHERE id=?").param(courseId).query(UUID.class)
            .optional().orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Curso não encontrado."));
        // Match account/course deletion lock order, preventing restore into a concurrently deleted parent.
        if (jdbc.sql("SELECT id FROM tb_user WHERE id=? AND deleted_at IS NULL FOR UPDATE").param(professorId).query(UUID.class).optional().isEmpty()) {
            throw error(HttpStatus.CONFLICT, "A conta responsável foi excluída. Este conteúdo não pode ser restaurado.");
        }
        var course = jdbc.sql("SELECT id, deleted_at FROM tb_course WHERE id=? FOR UPDATE").param(courseId).query().singleRow();
        if (!entity.equals("courses") && course.get("deleted_at") != null) {
            throw error(HttpStatus.CONFLICT, "Restaure o curso antes de restaurar seu conteúdo.");
        }
        var row = jdbc.sql("SELECT * FROM " + table + " WHERE id=? FOR UPDATE").param(id).query().singleRow();
        if (row.get("deleted_at") == null) throw error(HttpStatus.CONFLICT, "Este conteúdo já está ativo.");
        if (entity.equals("lessons") && jdbc.sql("SELECT count(*) FROM tb_lesson WHERE course_id=? AND position=? AND deleted_at IS NULL")
                .params(courseId, row.get("position")).query(Long.class).single() > 0) {
            throw error(HttpStatus.CONFLICT, "Outra aula ocupa esta posição. Reordene as aulas antes de restaurar.");
        }
        if (entity.equals("questions")) {
            int afterLesson = ((Number) row.get("after_lesson")).intValue();
            if (afterLesson > 0 && jdbc.sql("SELECT count(*) FROM tb_lesson WHERE course_id=? AND position=? AND deleted_at IS NULL")
                    .params(courseId, afterLesson).query(Long.class).single() == 0) {
                throw error(HttpStatus.CONFLICT, "Restaure a aula vinculada antes desta questão.");
            }
            if (jdbc.sql("SELECT count(*) FROM tb_question WHERE course_id=? AND after_lesson=? AND position=? AND deleted_at IS NULL")
                    .params(courseId, afterLesson, row.get("position")).query(Long.class).single() > 0) {
                throw error(HttpStatus.CONFLICT, "Outra questão ocupa esta posição. Reordene as questões antes de restaurar.");
            }
        }
        jdbc.sql("UPDATE " + table + " SET deleted_at=NULL WHERE id=? AND deleted_at IS NOT NULL").param(id).update();
        entities.persist(new ContentRestoration(entity, id, users.findByEmail(email).getId(), ((Timestamp) row.get("deleted_at")).toInstant()));
    }

    private ResponseStatusException error(HttpStatus status, String message) { return new ResponseStatusException(status, message); }
}
