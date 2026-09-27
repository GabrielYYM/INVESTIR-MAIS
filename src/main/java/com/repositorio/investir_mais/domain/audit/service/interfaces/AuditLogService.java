package com.repositorio.investir_mais.domain.audit.service.interfaces;

import com.repositorio.investir_mais.domain.audit.model.enums.AuditAction;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditStatus;

public interface AuditLogService {

    /**
     * Registra um evento de auditoria obtendo ator, IP e traceId automaticamente do contexto atual.
     */
    void log(AuditAction action, String resourceType, String resourceId, AuditStatus status, String details);

    /**
     * Registra um evento de auditoria especificando explicitamente os dados do ator e IP (ideal para login, registro e recuperação de senha).
     */
    void log(AuditAction action, String actorId, String actorEmail, String resourceType, String resourceId, String ipAddress, AuditStatus status, String details);
}
