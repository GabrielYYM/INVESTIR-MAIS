package com.repositorio.investir_mais.domain.audit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.domain.audit.DTO.AuditLogResponseDTO;
import com.repositorio.investir_mais.domain.audit.mapper.AuditLogMapper;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditAction;
import com.repositorio.investir_mais.domain.audit.repository.AuditLogRepository;
import com.repositorio.investir_mais.domain.audit.service.interfaces.AuditQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditQueryServiceImpl implements AuditQueryService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> findAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable)
                .map(auditLogMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> findByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable)
                .map(auditLogMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> findByActorEmail(String actorEmail, Pageable pageable) {
        return auditLogRepository.findByActorEmailContainingIgnoreCaseOrderByTimestampDesc(actorEmail, pageable)
                .map(auditLogMapper::toDTO);
    }
}
