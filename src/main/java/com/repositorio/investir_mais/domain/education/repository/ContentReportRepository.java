package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.ContentReport;
import com.repositorio.investir_mais.domain.education.model.ContentReport.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ContentReportRepository extends JpaRepository<ContentReport, UUID> {
    boolean existsByReporterIdAndTargetTypeAndTargetIdAndStatus(UUID reporterId, Target targetType, UUID targetId, Status status);
    long countByReporterIdAndCreatedAtAfter(UUID reporterId, Instant since);
    Page<ContentReport> findByStatus(Status status, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ContentReport> findForUpdateById(UUID id);
}
