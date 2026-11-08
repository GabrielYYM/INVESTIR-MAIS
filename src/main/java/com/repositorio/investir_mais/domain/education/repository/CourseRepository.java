package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    Page<Course> findAllByProfessorId(UUID professorId, Pageable pageable);

    List<Course> findAllByProfessorId(UUID professorId);

    long countByProfessorId(UUID professorId);
}
