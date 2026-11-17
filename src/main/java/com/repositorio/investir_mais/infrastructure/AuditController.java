package com.repositorio.investir_mais.infrastructure;

import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditController {
    private final JdbcClient jdbc;
    private static final Map<String, String> TABLES = Map.of("users", "tb_user_audit", "courses", "tb_course_aud", "lessons", "tb_lesson_aud", "questions", "tb_question_aud");

    @GetMapping @Transactional(readOnly = true)
    public Map<String, Object> searchAudit(@RequestParam(required = false) String entity,
            @RequestParam(required = false) UUID entityId, @RequestParam(required = false) Integer revision,
            @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String actor, @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        if ((entity != null && !TABLES.containsKey(entity)) || (from != null && to != null && from.isAfter(to))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entidade ou período inválido.");
        }
        var branches = new ArrayList<String>();
        TABLES.forEach((name, table) -> {
            if (entity == null || name.equals(entity)) branches.add("SELECT '" + name + "' AS entity, a.id, a.rev, a.revtype, to_jsonb(a)-'password' AS data FROM " + table + " a");
        });
        String base = " FROM (" + String.join(" UNION ALL ", branches) + ") a JOIN revinfo r ON r.rev=a.rev WHERE 1=1";
        var params = new ArrayList<Object>();
        if (entityId != null) { base += " AND a.id=?"; params.add(entityId); }
        if (revision != null) { base += " AND a.rev=?"; params.add(revision); }
        if (from != null) { base += " AND r.revtstmp>=?"; params.add(from.toEpochMilli()); }
        if (to != null) { base += " AND r.revtstmp<=?"; params.add(to.toEpochMilli()); }
        if (actor != null && !actor.isBlank()) { base += " AND r.actor=?"; params.add(actor); }
        if (role != null && !role.isBlank()) { base += " AND r.actor_role=?"; params.add(role); }
        long total = jdbc.sql("SELECT count(*)" + base).params(params).query(Long.class).single();
        var paged = new ArrayList<>(params); paged.add(size); paged.add((long) page * size);
        var rows = jdbc.sql("SELECT a.entity, a.id, a.rev AS revision, a.revtype AS operation, r.revtstmp AS timestamp, r.actor, r.actor_role AS role, a.data::text AS data" + base + " ORDER BY a.rev DESC,a.entity,a.id LIMIT ? OFFSET ?")
                .params(paged).query().listOfRows();
        return Map.of("items", rows, "totalItems", total, "page", page, "size", size);
    }
}
