package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    Page<Lesson> findAllByCourseId(UUID courseId, Pageable pageable);

    Optional<Lesson> findByIdAndCourse_Professor_Id(UUID lessonId, UUID professorId);
}