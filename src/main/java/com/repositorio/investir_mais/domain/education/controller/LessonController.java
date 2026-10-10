package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Aulas", description = "Endpoints para gerenciamento de aulas associadas a cursos")
public class LessonController {

    private final LessonService lessonService;

    @Operation(summary = "Cadastrar nova aula em um curso", description = "Cria uma nova aula para o curso informado. Exige permissão de proprietário do curso ou ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Aula cadastrada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para adicionar aulas a este curso"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    @PostMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonDTO.Response createLesson(@PathVariable UUID courseId, @Valid @RequestBody LessonDTO.Request dto, @AuthenticationPrincipal Jwt jwt) {
        log.info("Solicitação de criação de aula no curso ID: {} pelo usuário: {}", courseId, jwt.getSubject());
        return lessonService.createLesson(courseId, dto, jwt.getSubject());
    }

    @Operation(summary = "Listar aulas de um curso", description = "Retorna uma lista paginada com todas as aulas de um determinado curso.")
    @ApiResponse(responseCode = "200", description = "Página de aulas retornada com sucesso")
    @GetMapping("/course/{courseId}")
    public Page<LessonDTO.Response> getLessonsByCourse(@PathVariable UUID courseId, @PageableDefault(size = 20) Pageable pageable) {
        return lessonService.getLessonsByCourseId(courseId, pageable);
    }

    @Operation(summary = "Obter aula por ID", description = "Retorna os detalhes de uma aula específica.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aula encontrada"),
        @ApiResponse(responseCode = "404", description = "Aula não encontrada")
    })
    @GetMapping("/{lessonId}")
    public LessonDTO.Response getLessonById(@PathVariable UUID lessonId) {
        return lessonService.getLessonById(lessonId);
    }

    @Operation(summary = "Atualizar aula", description = "Atualiza os dados de uma aula. Exige que o usuário seja proprietário do curso associado ou ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aula atualizada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar esta aula"),
        @ApiResponse(responseCode = "404", description = "Aula não encontrada")
    })
    @PutMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonDTO.Response updateLesson(@PathVariable UUID lessonId, @Valid @RequestBody LessonDTO.Request dto, @AuthenticationPrincipal Jwt jwt) {
        log.info("Solicitação de atualização da aula ID: {} pelo usuário: {}", lessonId, jwt.getSubject());
        return lessonService.updateLesson(lessonId, dto, jwt.getSubject());
    }

    @Operation(summary = "Excluir aula", description = "Remove uma aula existente. Exige que o usuário seja proprietário do curso associado ou ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Aula removida com sucesso"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para remover esta aula"),
        @ApiResponse(responseCode = "404", description = "Aula não encontrada")
    })
    @DeleteMapping("/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void deleteLesson(@PathVariable UUID lessonId, @AuthenticationPrincipal Jwt jwt) {
        log.info("Solicitação de remoção da aula ID: {} pelo usuário: {}", lessonId, jwt.getSubject());
        lessonService.deleteLesson(lessonId, jwt.getSubject());
    }
}