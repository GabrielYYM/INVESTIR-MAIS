package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO.CourseStats;
import com.repositorio.investir_mais.domain.education.model.Course;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    Page<Course> findAllByProfessorId(UUID professorId, Pageable pageable);


    @Query(value = """
            with visible_courses as (
                select id, name
                from tb_course
                where deleted_at is null and (:allCourses = true or professor_id = :professorId)
            ), lesson_counts as (
                select l.course_id, count(*) as total
                from tb_lesson l join visible_courses c on c.id = l.course_id
                where l.deleted_at is null
                group by l.course_id
            ), enrollment_counts as (
                select e.course_id, count(*) as total
                from tb_course_enrollment e join visible_courses c on c.id = e.course_id
                join tb_user u on u.id = e.student_id and u.deleted_at is null
                where e.enrolled_at between :start and :end
                group by e.course_id
            ), completion_counts as (
                select l.course_id, count(*) as total
                from tb_lesson_progress p
                join tb_lesson l on l.id = p.lesson_id
                join visible_courses c on c.id = l.course_id
                join tb_user u on u.id = p.student_id and u.deleted_at is null
                where l.deleted_at is null and p.completed = true and p.completed_at between :start and :end
                  and not exists (
                      select 1 from tb_question q
                      where q.course_id = l.course_id and q.deleted_at is null and q.after_lesson < l.position
                        and not exists (select 1 from tb_question_pass a
                                        where a.question_id = q.id and a.student_id = p.student_id)
                  )
                group by l.course_id
            )
            select c.id as "courseId", c.name as "courseName", coalesce(l.total, 0) as "totalLessons",
                   coalesce(e.total, 0) as "enrolledStudents", coalesce(p.total, 0) as "lessonsWatched"
            from visible_courses c
            left join lesson_counts l on l.course_id = c.id
            left join enrollment_counts e on e.course_id = c.id
            left join completion_counts p on p.course_id = c.id
            """, nativeQuery = true)
    List<CourseStats> findDashboardStats(UUID professorId, boolean allCourses, LocalDateTime start, LocalDateTime end);

    @Query(value = """
            select count(distinct e.student_id)
            from tb_course_enrollment e
            join tb_course c on c.id = e.course_id
            join tb_user u on u.id = e.student_id and u.deleted_at is null
            where e.enrolled_at between :start and :end
              and c.deleted_at is null
              and (:allCourses = true or c.professor_id = :professorId)
            """, nativeQuery = true)
    long countDashboardStudents(UUID professorId, boolean allCourses, LocalDateTime start, LocalDateTime end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Course> findForUpdateById(UUID courseId);
}
