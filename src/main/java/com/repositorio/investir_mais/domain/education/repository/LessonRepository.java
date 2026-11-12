package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import jakarta.persistence.LockModeType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
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

    @Query("select l.id, l.title, l.description, l.mediaUrl, l.thumbnailUrl, l.course.id from Lesson l where l.id = :lessonId")
    Optional<LessonDTO.Response> findResponseById(UUID lessonId);

    @Query("select l.id, l.title, l.description, l.mediaUrl, l.thumbnailUrl, l.course.id from Lesson l where l.course.id = :courseId")
    Page<LessonDTO.Response> findResponsesByCourseId(UUID courseId, Pageable pageable);


    // Authorize the locked resource before returning it to the transactional service.
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostAuthorize("hasRole('ADMIN') or (returnObject.isPresent() and returnObject.get().course.professor.email == authentication.name)")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Lesson> findForUpdateById(UUID lessonId);

    long countByCourseId(UUID courseId);

    @Query(value = """
            select l.id
            from tb_lesson_progress p
            join tb_lesson l on l.id = p.lesson_id
            where p.student_id = :studentId and l.course_id = :courseId
              and l.deleted_at is null and p.completed = true
            """, nativeQuery = true)
    List<UUID> findCompletedLessonIds(UUID studentId, UUID courseId);
}
