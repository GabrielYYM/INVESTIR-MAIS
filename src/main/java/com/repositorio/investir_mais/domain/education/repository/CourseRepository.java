package com.repositorio.investir_mais.domain.education.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.repositorio.investir_mais.domain.education.model.Course;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    Page<Course> findAllByProfessorId(UUID professorId, Pageable pageable);

    Optional<Course> findByIdAndProfessorId(UUID courseId, UUID professorId);
}