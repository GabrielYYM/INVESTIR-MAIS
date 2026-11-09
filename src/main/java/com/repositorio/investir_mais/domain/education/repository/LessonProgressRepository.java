package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    Optional<LessonProgress> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);

    List<LessonProgress> findByStudentId(UUID studentId);

    List<LessonProgress> findByStudentIdAndLessonCourseIdAndCompletedTrue(UUID studentId, UUID courseId);

    long countByLessonCourseIdAndCompletedTrue(UUID courseId);

    long countByLessonCourseIdAndCompletedTrueAndCompletedAtBetween(UUID courseId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(p) FROM LessonProgress p WHERE p.lesson.course.professor.id = :professorId AND p.completed = true")
    long countCompletedByProfessorId(@Param("professorId") UUID professorId);

    @Query("SELECT COUNT(p) FROM LessonProgress p WHERE p.lesson.course.professor.id = :professorId AND p.completed = true AND p.completedAt BETWEEN :start AND :end")
    long countCompletedByProfessorIdAndCompletedAtBetween(
            @Param("professorId") UUID professorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(p) FROM LessonProgress p WHERE p.completed = true")
    long countTotalCompleted();

    @Query("SELECT COUNT(p) FROM LessonProgress p WHERE p.completed = true AND p.completedAt BETWEEN :start AND :end")
    long countTotalCompletedAndCompletedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}

