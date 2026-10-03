package com.repositorio.investir_mais.domain.tools.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.repositorio.investir_mais.domain.tools.model.Question;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByRole(AssetRole role);

    Page<Question> findByRole(AssetRole role, Pageable pageable);
}
