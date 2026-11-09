package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, UUID> {

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);

    Optional<CourseEnrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<CourseEnrollment> findByStudentId(UUID studentId);

    Optional<CourseEnrollment> findFirstByOrderByEnrolledAtAsc();

    long countByCourseId(UUID courseId);

    long countByCourseIdAndEnrolledAtBetween(UUID courseId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT e.student.id) FROM CourseEnrollment e WHERE e.course.professor.id = :professorId")
    long countDistinctStudentsByProfessorId(@Param("professorId") UUID professorId);

    @Query("SELECT COUNT(DISTINCT e.student.id) FROM CourseEnrollment e WHERE e.course.professor.id = :professorId AND e.enrolledAt BETWEEN :start AND :end")
    long countDistinctStudentsByProfessorIdAndEnrolledAtBetween(
            @Param("professorId") UUID professorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(DISTINCT e.student.id) FROM CourseEnrollment e")
    long countDistinctStudents();

    @Query("SELECT COUNT(DISTINCT e.student.id) FROM CourseEnrollment e WHERE e.enrolledAt BETWEEN :start AND :end")
    long countDistinctStudentsAndEnrolledAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}

