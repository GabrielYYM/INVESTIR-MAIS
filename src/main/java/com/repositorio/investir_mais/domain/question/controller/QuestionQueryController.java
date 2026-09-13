package com.repositorio.investir_mais.domain.question.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.question.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.question.service.interfaces.QuestionQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Consultas de Perguntas", description = "Operações de leitura para perguntas qualitativas")
public class QuestionQueryController {
    private final QuestionQueryService queryService;

    @GetMapping("/categories/{categoryId}")
    @Operation(summary = "Lista perguntas de uma categoria")
    @ApiResponse(responseCode = "200", description = "Lista de perguntas retornada com sucesso")
    public ResponseEntity<List<QuestionResponseDTO>> listByCategoryId(@PathVariable UUID categoryId) {
        ServiceResult<List<QuestionResponseDTO>> result = queryService.listByCategoryId(categoryId);

        return switch (result) {
            case ServiceResult.Success<List<QuestionResponseDTO>> s -> ResponseEntity.ok(s.data());
            case ServiceResult.NotFound<List<QuestionResponseDTO>> n -> throw new ErrorResponseException(
                    HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<List<QuestionResponseDTO>> e ->
                throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }
}
