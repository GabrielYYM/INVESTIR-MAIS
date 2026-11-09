package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.Lesson;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    Page<Lesson> findAllByCourseId(UUID courseId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Lesson> findForUpdateById(UUID lessonId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Lesson> findByIdAndCourse_Professor_Id(UUID lessonId, UUID professorId);

    long countByCourseId(UUID courseId);

    long countByCourseProfessorId(UUID professorId);
}
