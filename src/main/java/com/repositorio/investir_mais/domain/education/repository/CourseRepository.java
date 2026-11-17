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

public interface CourseRepository extends JpaRepository<Course, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Course> {
    Page<Course> findAllByProfessorId(UUID professorId, Pageable pageable);


    @Query("""
            select c.id as courseId, c.name as courseName,
                (select count(l) from Lesson l where l.course.id = c.id) as totalLessons,
                (select count(u) from User u join u.enrollments e
                    where key(e).id = c.id and e.enrolledAt between :start and :end) as enrolledStudents,
                (select count(u) from User u join u.lessonProgress p join Lesson l on l.id = key(p).id
                    where l.course.id = c.id and p.completed = true
                      and p.completedAt between :start and :end) as lessonsWatched
            from Course c where (:allCourses = true or c.professor.id = :professorId)
            """)
    List<CourseStats> findDashboardStats(UUID professorId, boolean allCourses, LocalDateTime start, LocalDateTime end);

    @Query("""
            select count(distinct u.id) from User u join u.enrollments e join Course c on c.id = key(e).id
            where e.enrolledAt between :start and :end
              and (:allCourses = true or c.professor.id = :professorId)
            """)
    long countDashboardStudents(UUID professorId, boolean allCourses, LocalDateTime start, LocalDateTime end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Course> findForUpdateById(UUID courseId);
}
