package com.repositorio.investir_mais.domain.education.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.repositorio.investir_mais.domain.education.model.Lession;

public interface LessionRepository extends JpaRepository<Lession, UUID> {
    Page<Lession> findAllByCourseId(UUID courseId, Pageable pageable);
    
    Optional<Lession> findByIdAndCourseProfessorIdId(UUID lessionId, UUID professorId);
}
