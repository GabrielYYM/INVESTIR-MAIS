package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    Page<Lesson> findAllByCourseId(UUID courseId, Pageable pageable);

    Optional<Lesson> findByIdAndCourse_Professor_Id(UUID lessonId, UUID professorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select lesson from Lesson lesson where lesson.id = :id")
    Optional<Lesson> findByIdForUpdate(UUID id);
}
