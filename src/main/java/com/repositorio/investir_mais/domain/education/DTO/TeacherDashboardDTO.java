package com.repositorio.investir_mais.domain.education.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados consolidados do Dashboard do Professor")
public record TeacherDashboardDTO(
    @Schema(description = "Lista dos cursos do professor")
    List<CourseDTO.Response> courses,

    @Schema(description = "Total de visualizações em todos os cursos do professor", example = "Não sei")
    long totalViews,

    @Schema(description = "Média de vídeos assistidos por curso", example = "Não sei")
    double averageVideosWatchedPerCourse
) {}
