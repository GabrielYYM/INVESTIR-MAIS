package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Set;
import java.util.UUID;

public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, UUID> {
    Page<QuestionAttempt> findByStudentIdOrderByAnsweredAtDesc(UUID studentId, Pageable pageable);
    void deleteByStudentId(UUID studentId);

    @Query("select distinct a.questionId from QuestionAttempt a where a.studentId = :studentId and a.courseId = :courseId")
    Set<UUID> findAttemptedQuestionIds(UUID studentId, UUID courseId);
}
