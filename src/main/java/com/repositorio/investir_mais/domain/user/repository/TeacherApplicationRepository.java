package com.repositorio.investir_mais.domain.user.repository;

import org.springframework.data.jpa.repository.Query;
import com.repositorio.investir_mais.domain.user.model.TeacherDocument;
import com.repositorio.investir_mais.domain.user.model.TeacherApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface TeacherApplicationRepository extends JpaRepository<TeacherApplication, UUID> {
    interface DocumentMetadata { UUID getId(); String getType(); String getMediaType(); }
    @Query("select d.id as id, d.type as type, d.mediaType as mediaType from TeacherApplication a join a.documents d where a.id=:applicationId")
    List<DocumentMetadata> documentMetadata(UUID applicationId);
    @Query("select d from TeacherApplication a join a.documents d where a.id=:applicationId and d.id=:documentId")
    Optional<TeacherDocument> document(UUID applicationId, UUID documentId);
    Optional<TeacherApplication> findByUserId(UUID userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TeacherApplication> findForUpdateById(UUID id);
    boolean existsByUserIdAndStatus(UUID userId, String status);
}
