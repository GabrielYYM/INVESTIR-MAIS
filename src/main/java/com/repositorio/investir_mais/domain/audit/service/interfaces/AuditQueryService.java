package com.repositorio.investir_mais.domain.audit.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.repositorio.investir_mais.domain.audit.DTO.AuditLogResponseDTO;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditAction;

public interface AuditQueryService {

    Page<AuditLogResponseDTO> findAll(Pageable pageable);

    Page<AuditLogResponseDTO> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLogResponseDTO> findByActorEmail(String actorEmail, Pageable pageable);
}
