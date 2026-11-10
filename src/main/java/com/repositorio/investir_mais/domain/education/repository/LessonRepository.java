package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.Lesson;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
=======
>>>>>>> origin/dev

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

<<<<<<< HEAD
    @Query(value = """
            select l.id
            from tb_lesson_progress p
            join tb_lesson l on l.id = p.lesson_id
            where p.student_id = :studentId and l.course_id = :courseId
              and l.deleted_at is null and p.completed = true
            """, nativeQuery = true)
    List<UUID> findCompletedLessonIds(UUID studentId, UUID courseId);
=======
    long countByCourseProfessorId(UUID professorId);
>>>>>>> origin/dev
}
