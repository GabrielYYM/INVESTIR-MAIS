package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.TeacherDashboardDTO;
import com.repositorio.investir_mais.domain.education.service.TeacherDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Painel do Professor e Progresso", description = "Endpoints para métricas de ensino, progresso de aulas e matrículas")
public class TeacherDashboardController {

    private final TeacherDashboardService dashboardService;

    @Operation(
        summary = "Obter estatísticas do painel do professor",
        description = "Retorna métricas consolidadas sobre cursos, alunos matriculados, videoaulas assistidas e taxas de conclusão no período informado (padrão últimos 6 meses, máximo de 6 meses de intervalo). Exige perfil TEACHER ou ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Métricas retornadas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Período inválido ou superior a 6 meses"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/api/teacher/dashboard")
    public TeacherDashboardDTO.Summary getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal) {
        return dashboardService.getTeacherDashboard(principal.getName(), startDate, endDate);
    }

    @Operation(summary = "Matricular aluno em um curso", description = "Registra a matrícula do usuário autenticado no curso especificado.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Matrícula realizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    @PostMapping("/api/courses/{courseId}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherDashboardDTO.CourseProgressResponse enrollStudent(@PathVariable UUID courseId, Principal principal) {
        return dashboardService.enrollStudent(courseId, principal.getName());
    }

    @Operation(summary = "Atualizar progresso de uma aula", description = "Marca uma videoaula como assistida/concluída ou pendente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Progresso atualizado"),
        @ApiResponse(responseCode = "404", description = "Curso ou aula não encontrada")
    })
    @PostMapping("/api/lessons/{courseId}/{lessonId}/progress")
    public TeacherDashboardDTO.CourseProgressResponse updateLessonProgress(
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @RequestBody TeacherDashboardDTO.ProgressRequest request,
            Principal principal) {
        return dashboardService.updateLessonProgress(courseId, lessonId, request.completed(), principal.getName());
    }

    @Operation(summary = "Consultar progresso do aluno no curso", description = "Retorna o percentual de conclusão e lista de aulas já concluídas pelo aluno atual.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Progresso retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    @GetMapping("/api/courses/{courseId}/progress")
    public TeacherDashboardDTO.CourseProgressResponse getCourseProgress(@PathVariable UUID courseId, Principal principal) {
        return dashboardService.getCourseProgress(courseId, principal.getName());
    }
}

