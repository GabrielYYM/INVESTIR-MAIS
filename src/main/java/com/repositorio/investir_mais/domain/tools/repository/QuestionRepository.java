package com.repositorio.investir_mais.domain.tools.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.repositorio.investir_mais.domain.tools.model.Question;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByCategoryId(UUID categoryId);

    Page<Question> findByCategoryId(UUID categoryId, Pageable pageable);
}