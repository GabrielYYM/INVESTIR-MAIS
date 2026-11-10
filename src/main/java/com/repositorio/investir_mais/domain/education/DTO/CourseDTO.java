package com.repositorio.investir_mais.domain.education.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CourseDTO {
    @Schema(name = "CourseRequest", description = "Dados para criação ou atualização de um curso")
    public record Request(
        @Schema(description = "Nome do curso", example = "Introdução aos Investimentos")
        @NotBlank(message = "O nome é obrigatório") 
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String name,

        @Schema(description = "Descrição detalhada sobre o conteúdo do curso", example = "Aprenda os conceitos básicos de renda fixa e variável.")
        @NotBlank(message = "A descrição é obrigatória") 
        @Size(max = 10000)
        String description
    ) {}

    @Schema(name = "CourseResponse", description = "Resposta com dados do curso")
    public record Response(
        @Schema(description = "Identificador único do curso (UUID)", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
        UUID id,

        @Schema(description = "Nome do curso", example = "Introdução aos Investimentos")
        String name,

        @Schema(description = "Descrição do curso", example = "Aprenda os conceitos básicos de renda fixa e variável.")
        String description,

        @Schema(description = "ID do professor responsável pelo curso", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID professorId
    ) {}

    @Schema(description = "Estatísticas gerais consolidadas do professor no período")
    public record Summary(
        @Schema(description = "Data inicial do período de consulta (ISO-8601)", example = "2026-05-08")
        LocalDate startDate,

        @Schema(description = "Data final do período de consulta (ISO-8601)", example = "2026-11-08")
        LocalDate endDate,

        @Schema(description = "Total de cursos criados pelo professor", example = "4")
        long totalCourses,

        @Schema(description = "Total de videoaulas publicadas", example = "24")
        long totalLessons,

        @Schema(description = "Total de alunos distintos matriculados no período", example = "85")
        long totalStudents,

        @Schema(description = "Total de aulas assistidas pelos alunos no período", example = "310")
        long totalLessonsWatched,

        @Schema(description = "Taxa média geral de conclusão (%) no período", example = "72.4")
        double averageCompletionRate,

        @Schema(description = "Métricas detalhadas por curso no período")
        List<CourseStats> courses
    ) {}

    @Schema(description = "Métricas de engajamento de um curso")
    public record CourseStats(
        @Schema(description = "ID do curso")
        UUID courseId,
        @Schema(description = "Nome do curso")
        String courseName,
        @Schema(description = "Total de aulas neste curso")
        long totalLessons,
        @Schema(description = "Alunos matriculados no curso")
        long enrolledStudents,
        @Schema(description = "Aulas concluídas/assistidas pelos alunos")
        long lessonsWatched,
        @Schema(description = "Taxa de conclusão do curso (%)", example = "65.0")
        double completionRate
    ) {
        public CourseStats(UUID courseId, String courseName, long totalLessons, long enrolledStudents, long lessonsWatched) {
            this(courseId, courseName, totalLessons, enrolledStudents, lessonsWatched,
                    completionPercentage(lessonsWatched, totalLessons * enrolledStudents));
        }

        public static double completionPercentage(long lessonsWatched, long possibleWatches) {
            if (possibleWatches == 0) {
                return 0.0;
            }
            double rawPercentage = ((double) lessonsWatched / possibleWatches) * 100.0;
            return Math.min(100.0, Math.round(rawPercentage * 10.0) / 10.0);
        }
    }

    @Schema(description = "Status de progresso de um curso para o aluno atual")
    public record CourseProgressResponse(
        UUID courseId,
        boolean enrolled,
        long totalLessons,
        long completedLessons,
        double progressPercentage,
        List<UUID> completedLessonIds
    ) {}
}
