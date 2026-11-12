package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Tag(name = "Aulas", description = "Endpoints para gerenciamento de aulas associadas a cursos")
public class LessonController {

    private final LessonService lessonService;

    @Operation(summary = "Cadastrar nova aula em um curso", description = "Cria uma nova aula para o curso informado. Exige permissão de proprietário do curso ou ADMIN.")
    @PostMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonDTO.Response createLesson(@PathVariable UUID courseId, @Valid @RequestBody LessonDTO.Request dto) {
        return lessonService.createLesson(courseId, dto);
    }

    @Operation(summary = "Listar aulas de um curso", description = "Retorna uma lista paginada com todas as aulas de um determinado curso.")
    @GetMapping("/course/{courseId}")
    public Page<LessonDTO.Response> getLessonsByCourse(@PathVariable UUID courseId, @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return lessonService.getLessonsByCourseId(courseId, pageable);
    }

    @Operation(summary = "Obter aula por ID", description = "Retorna os detalhes de uma aula específica.")
    @GetMapping("/{lessonId}")
    public LessonDTO.Response getLessonById(@PathVariable UUID lessonId) {
        return lessonService.getLessonById(lessonId);
    }

    @Operation(summary = "Atualizar aula", description = "Atualiza os dados de uma aula. Exige que o usuário seja proprietário do curso associado ou ADMIN.")
    @PutMapping("/{lessonId}")
    public LessonDTO.Response updateLesson(@PathVariable UUID lessonId, @Valid @RequestBody LessonDTO.Request dto) {
        return lessonService.updateLesson(lessonId, dto);
    }

    @Operation(summary = "Atualizar progresso de uma aula", description = "Marca uma videoaula como assistida/concluída ou pendente.")
    @PostMapping("/{courseId}/{lessonId}/progress")
    public CourseDTO.CourseProgressResponse updateLessonProgress(
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @RequestBody LessonDTO.ProgressRequest request,
            Principal principal) {
        return lessonService.updateLessonProgress(courseId, lessonId, request.completed(), principal.getName());
    }

    @Operation(summary = "Excluir aula", description = "Exclui logicamente a aula, preservando o registro no banco. Exige que o usuário seja proprietário do curso associado ou ADMIN.")
    @DeleteMapping("/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable UUID lessonId) {
        lessonService.deleteLesson(lessonId);
    }
}
