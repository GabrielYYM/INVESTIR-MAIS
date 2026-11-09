package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.DTO.TeacherDashboardDTO.CourseStats;
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


    @Query("""
            with visibleCourses as (
                select c.id as courseId, c.name as courseName
                from Course c
                where :allCourses = true or fk(c.professor) = :professorId
            ), lessonCounts as (
                select c.courseId as courseId, count(l) as total
                from Lesson l join visibleCourses c on c.courseId = fk(l.course)
                group by c.courseId
            ), enrollmentCounts as (
                select c.courseId as courseId, count(e) as total
                from CourseEnrollment e join visibleCourses c on c.courseId = fk(e.course)
                where e.enrolledAt between :start and :end
                group by c.courseId
            ), completionCounts as (
                select c.courseId as courseId, count(p) as total
                from LessonProgress p join p.lesson l join visibleCourses c on c.courseId = fk(l.course)
                where p.completed = true and p.completedAt between :start and :end
                group by c.courseId
            )
            select new com.repositorio.investir_mais.domain.education.DTO.TeacherDashboardDTO$CourseStats(
                c.courseId, c.courseName, coalesce(l.total, 0L), coalesce(e.total, 0L), coalesce(p.total, 0L))
            from visibleCourses c
            left join lessonCounts l on l.courseId = c.courseId
            left join enrollmentCounts e on e.courseId = c.courseId
            left join completionCounts p on p.courseId = c.courseId
            """)
    List<CourseStats> findDashboardStats(UUID professorId, boolean allCourses, LocalDateTime start, LocalDateTime end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Course> findForUpdateById(UUID courseId);
}
