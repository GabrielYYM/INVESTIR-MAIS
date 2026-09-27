package com.repositorio.investir_mais.domain.audit.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

import com.repositorio.investir_mais.domain.audit.model.enums.AuditAction;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditStatus;

public record AuditLogResponseDTO(
        UUID id,
        LocalDateTime timestamp,
        String traceId,
        String actorId,
        String actorEmail,
        AuditAction action,
        String resourceType,
        String resourceId,
        String ipAddress,
        AuditStatus status,
        String details
) {
}
