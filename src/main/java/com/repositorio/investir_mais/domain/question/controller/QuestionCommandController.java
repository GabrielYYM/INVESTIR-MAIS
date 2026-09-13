package com.repositorio.investir_mais.domain.question.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.question.DTO.EvaluationRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.question.service.interfaces.QuestionCommandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionCommandController {

    private final QuestionCommandService commandService;

    @PostMapping("/categories/{categoryId}")
    public ResponseEntity<QuestionResponseDTO> createQuestion(
            @PathVariable UUID categoryId,
            @Valid @RequestBody QuestionRequestDTO request) {
        ServiceResult<QuestionResponseDTO> result = commandService.createQuestion(categoryId, request);

        return switch (result) {
            case ServiceResult.Success<QuestionResponseDTO> s ->
                ResponseEntity.status(HttpStatus.CREATED).body(s.data());
            case ServiceResult.NotFound<QuestionResponseDTO> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND,
                    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<QuestionResponseDTO> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponseDTO> updateQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody QuestionRequestDTO request) {
        ServiceResult<QuestionResponseDTO> result = commandService.updateQuestion(id, request);

        return switch (result) {
            case ServiceResult.Success<QuestionResponseDTO> s -> ResponseEntity.ok(s.data());
            case ServiceResult.NotFound<QuestionResponseDTO> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND,
                    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<QuestionResponseDTO> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) {
        ServiceResult<Void> result = commandService.deleteQuestion(id);

        return switch (result) {
            case ServiceResult.Success<Void> s -> ResponseEntity.noContent().build();
            case ServiceResult.NotFound<Void> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND,
                    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Void> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @PostMapping("/assets/{assetId}/evaluations")
    public ResponseEntity<Void> saveEvaluations(
            @PathVariable UUID assetId,
            @Valid @RequestBody List<EvaluationRequestDTO> evaluations) {
        ServiceResult<Void> result = commandService.saveEvaluations(assetId, evaluations);

        return switch (result) {
            case ServiceResult.Success<Void> s -> ResponseEntity.noContent().build();
            case ServiceResult.NotFound<Void> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND,
                    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Void> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }
}
