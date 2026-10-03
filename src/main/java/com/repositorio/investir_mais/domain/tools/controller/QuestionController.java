package com.repositorio.investir_mais.domain.tools.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.domain.tools.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;
import com.repositorio.investir_mais.domain.tools.service.QuestionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/roles/{role}")
    public Page<QuestionResponseDTO> getByRole(@PathVariable AssetRole role, Pageable pageable) {
        return questionService.getQuestionsByRole(role, pageable);
    }

    @GetMapping("/{id}")
    public QuestionResponseDTO getById(@PathVariable UUID id) {
        return questionService.getQuestionById(id);
    }

    @PostMapping
    public QuestionResponseDTO create(@Valid @RequestBody QuestionRequestDTO request) {
        return questionService.createQuestion(request);
    }

    @PutMapping("/{id}")
    public QuestionResponseDTO update(@PathVariable UUID id, @Valid @RequestBody QuestionRequestDTO request) {
        return questionService.updateQuestion(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        questionService.deleteQuestion(id);
    }
}
