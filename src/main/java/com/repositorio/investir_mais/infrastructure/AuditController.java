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
    private final com.repositorio.investir_mais.domain.AuditAccessService access;
    private static final Map<String, String> TABLES = Map.of("users", "tb_user_audit", "courses", "tb_course_aud", "lessons", "tb_lesson_aud", "questions", "tb_question_aud", "reports", "tb_content_report_aud", "restorations", "tb_content_restoration_aud", "accounts", "account_deletion_aud");

    @GetMapping @Transactional(readOnly = true)
    public Map<String, Object> searchAudit(@RequestParam(required = false) String entity,
            @RequestParam(required = false) UUID entityId, @RequestParam(required = false) Integer revision,
            @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String actor, @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        if (page < 0 || size < 1 || size > 100 || (entity != null && !TABLES.containsKey(entity)) || (from != null && to != null && from.isAfter(to))) {
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
        if (entityId != null) {
            access.record("SEARCH", entity, entityId, revision, rows.stream().map(row -> row.get("entity") + ":" + row.get("id") + ":" + row.get("revision")).toList());
        }
        return Map.of("items", rows, "totalItems", total, "page", page, "size", size);
    }

    @GetMapping("/{entity}/{entityId}/{revision}")
    @Transactional(readOnly = true)
    public Map<String, Object> auditDetail(@PathVariable String entity, @PathVariable UUID entityId, @PathVariable int revision) {
        String table = TABLES.get(entity);
        if (table == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entidade inválida.");
        var row = jdbc.sql("SELECT a.id, a.rev AS revision, a.revtype AS operation, r.revtstmp AS timestamp, r.actor, r.actor_role AS role, (to_jsonb(a)-'password')::text AS data FROM " + table + " a JOIN revinfo r ON r.rev=a.rev WHERE a.id=? AND a.rev=?")
            .params(entityId, revision).query().listOfRows().stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revisão não encontrada."));
        row.put("entity", entity);
        access.record("DETAIL", entity, entityId, revision, List.of(entity + ":" + entityId + ":" + revision));
        return row;
    }

    @GetMapping("/accesses")
    @Transactional(readOnly = true)
    public Map<String, Object> auditAccesses(@RequestParam(required = false) UUID viewerId,
            @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        if (page < 0 || size < 1 || size > 100 || (from != null && to != null && from.isAfter(to)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página ou período inválido.");
        String base = " FROM audit_access_log l LEFT JOIN tb_user u ON u.id=l.viewer_id WHERE (l.action IN ('DETAIL', 'USER_HISTORY') OR (l.entity IS NOT NULL AND l.entity_id IS NOT NULL))";
        var params = new ArrayList<Object>();
        if (viewerId != null) { base += " AND l.viewer_id=?"; params.add(viewerId); }
        if (from != null) { base += " AND l.viewed_at>=?"; params.add(java.sql.Timestamp.from(from)); }
        if (to != null) { base += " AND l.viewed_at<=?"; params.add(java.sql.Timestamp.from(to)); }
        long total = jdbc.sql("SELECT count(*)" + base).params(params).query(Long.class).single();
        var paged = new ArrayList<>(params); paged.add(size); paged.add((long) page * size);
        var rows = jdbc.sql("SELECT l.*,u.name AS viewer_name,CASE WHEN u.deleted_at IS NULL THEN u.email END AS viewer_email" + base + " ORDER BY l.viewed_at DESC,l.id LIMIT ? OFFSET ?")
            .params(paged).query().listOfRows();
        return Map.of("items", rows, "totalItems", total, "page", page, "size", size);
    }

}
