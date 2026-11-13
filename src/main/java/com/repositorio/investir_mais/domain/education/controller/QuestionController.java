package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.QuestionDTO;
import com.repositorio.investir_mais.domain.education.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses/{courseId}/questions")
@RequiredArgsConstructor
@Tag(name = "Perguntas", description = "CRUD de perguntas e aprovação do aluno")
public class QuestionController {
    private final QuestionService questions;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar pergunta imediatamente disponível, sem rascunho")
    public QuestionDTO.TeacherQuestion create(@PathVariable UUID courseId, @Valid @RequestBody QuestionDTO.Request request, Principal principal) {
        return questions.create(courseId, request, principal.getName());
    }
    @PutMapping("/{questionId}")
    @Operation(summary = "Editar pergunta e invalidar suas aprovações anteriores")
    public QuestionDTO.TeacherQuestion update(@PathVariable UUID courseId, @PathVariable UUID questionId,
                                               @Valid @RequestBody QuestionDTO.Request request, Principal principal) {
        return questions.update(courseId, questionId, request, principal.getName());
    }
    @DeleteMapping("/{questionId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir logicamente pergunta")
    public void delete(@PathVariable UUID courseId, @PathVariable UUID questionId, Principal principal) {
        questions.delete(courseId, questionId, principal.getName());
    }
    @GetMapping("/manage")
    @Operation(summary = "Listar perguntas com gabarito para professor responsável ou ADMIN")
    public List<QuestionDTO.TeacherQuestion> manage(@PathVariable UUID courseId, Principal principal) {
        return questions.manage(courseId, principal.getName());
    }
    @GetMapping
    @Operation(summary = "Listar perguntas sem gabarito e informar os bloqueios de progressão")
    public List<QuestionDTO.StudentQuestion> list(@PathVariable UUID courseId, Principal principal) {
        return questions.list(courseId, principal.getName());
    }
    @PostMapping("/{questionId}/answers")
    @Operation(summary = "Validar resposta e guardar somente que o aluno passou")
    public QuestionDTO.AnswerResponse answer(@PathVariable UUID courseId, @PathVariable UUID questionId,
                                             @Valid @RequestBody QuestionDTO.AnswerRequest request, Principal principal) {
        return questions.answer(courseId, questionId, request, principal.getName());
    }
}
