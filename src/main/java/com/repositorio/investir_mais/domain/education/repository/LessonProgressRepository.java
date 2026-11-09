package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    Optional<LessonProgress> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);

    @Query("""
            select l.id from LessonProgress p join p.lesson l
            where p.student.id = :studentId and l.course.id = :courseId and p.completed = true
            """)
    List<UUID> findCompletedLessonIds(UUID studentId, UUID courseId);
}

