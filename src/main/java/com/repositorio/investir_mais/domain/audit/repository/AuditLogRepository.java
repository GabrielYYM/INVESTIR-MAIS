package com.repositorio.investir_mais.domain.audit.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.repositorio.investir_mais.domain.audit.model.AuditLog;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditAction;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<AuditLog> findByActionOrderByTimestampDesc(AuditAction action, Pageable pageable);

    Page<AuditLog> findByActorEmailContainingIgnoreCaseOrderByTimestampDesc(String actorEmail, Pageable pageable);

    Page<AuditLog> findByResourceTypeOrderByTimestampDesc(String resourceType, Pageable pageable);

    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
