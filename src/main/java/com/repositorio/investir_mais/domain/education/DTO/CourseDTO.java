package com.repositorio.investir_mais.domain.education.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CourseDTO {
    @Schema(description = "Dados para criação ou atualização de um curso")
    public record Request(
        @Schema(description = "Nome do curso", example = "Introdução aos Investimentos")
        @NotBlank(message = "O nome é obrigatório") 
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String name,

        @Schema(description = "Descrição detalhada sobre o conteúdo do curso", example = "Aprenda os conceitos básicos de renda fixa e variável.")
        @NotBlank(message = "A descrição é obrigatória") 
        String description
    ) {}

    @Schema(description = "Resposta com dados do curso")
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
}