package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, UUID> {
    Page<QuestionAttempt> findByStudentIdOrderByAnsweredAtDesc(UUID studentId, Pageable pageable);
    void deleteByStudentId(UUID studentId);
}
