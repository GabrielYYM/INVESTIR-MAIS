package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.Lesson;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    Page<Lesson> findAllByCourseId(UUID courseId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Lesson> findForUpdateById(UUID lessonId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Lesson> findByIdAndCourse_Professor_Id(UUID lessonId, UUID professorId);

    long countByCourseId(UUID courseId);

    @Query("select l.id from User u join u.lessonProgress p join Lesson l on l.id = key(p).id where u.id = :studentId and l.course.id = :courseId and p.completed = true")
    List<UUID> findCompletedLessonIds(UUID studentId, UUID courseId);

    // Includes deleted lessons to avoid reusing their positions.
    @Query(value = "select coalesce(max(position), 0) + 1 from tb_lesson where course_id = :courseId", nativeQuery = true)
    int nextPosition(UUID courseId);

    List<Lesson> findAllByCourseIdOrderByPositionAscIdAsc(UUID courseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Lesson> findForUpdateByCourseIdOrderByPositionAscIdAsc(UUID courseId);

    @Query("select count(q) from Question q where q.course.id = :courseId")
    long countQuestions(UUID courseId);

    @Query("select count(q) from Question q where q.course.id = :courseId and exists (select u.id from User u join u.passedQuestionIds p where u.id = :studentId and p = q.id)")
    long countPassedQuestions(UUID courseId, UUID studentId);
}
