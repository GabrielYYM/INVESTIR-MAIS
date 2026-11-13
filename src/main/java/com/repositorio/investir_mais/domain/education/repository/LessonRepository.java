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

    @Query(value = """
            select l.id
            from tb_lesson_progress p
            join tb_lesson l on l.id = p.lesson_id
            where p.student_id = :studentId and l.course_id = :courseId
              and l.deleted_at is null and p.completed = true
              and not exists (
                  select 1 from tb_question q
                  where q.course_id = l.course_id and q.deleted_at is null and q.after_lesson < l.position
                    and not exists (select 1 from tb_question_pass a
                                    where a.question_id = q.id and a.student_id = p.student_id)
              )
            """, nativeQuery = true)
    List<UUID> findCompletedLessonIds(UUID studentId, UUID courseId);

    @Query(value = "select coalesce(max(position), 0) + 1 from tb_lesson where course_id = :courseId", nativeQuery = true)
    int nextPosition(UUID courseId);

    List<Lesson> findAllByCourseIdOrderByPositionAscIdAsc(UUID courseId);

    @Query(value = "select count(*) from tb_question where course_id = :courseId and deleted_at is null", nativeQuery = true)
    long countQuestions(UUID courseId);

    @Query(value = """
            select count(*) from tb_question q where q.course_id = :courseId and q.deleted_at is null
              and exists(select 1 from tb_question_pass a
                         where a.question_id = q.id and a.student_id = :studentId)
            """, nativeQuery = true)
    long countPassedQuestions(UUID courseId, UUID studentId);
}
