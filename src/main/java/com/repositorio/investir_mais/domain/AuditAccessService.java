package com.repositorio.investir_mais.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

/** Only server-derived identity and returned record references enter this append-only ledger. */
@Service @RequiredArgsConstructor
public class AuditAccessService {
    private final JdbcClient jdbc;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String entity, UUID entityId, Integer revision, List<String> references) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("Leitura de auditoria sem autenticação.");
        UUID viewer = jdbc.sql("SELECT id FROM tb_user WHERE email=? AND deleted_at IS NULL")
            .param(auth.getName()).query(UUID.class).single();
        String role = auth.getAuthorities().stream().map(Object::toString)
            .filter(value -> value.startsWith("ROLE_")).sorted().findFirst().orElse("ROLE_UNKNOWN").substring(5);
        jdbc.sql("INSERT INTO audit_access_log(id,viewer_id,viewer_role,action,entity,entity_id,revision,record_references) VALUES (?,?,?,?,?,?,?,?)")
            .params(java.util.Arrays.asList(UUID.randomUUID(), viewer, role, action, entity, entityId, revision, String.join(",", references))).update();
    }
}
