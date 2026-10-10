package com.repositorio.investir_mais.domain.education.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class LessonDTO {
    @Schema(description = "Dados para criação ou atualização de uma aula")
    public record Request(
        @Schema(description = "Título da aula", example = "Aula 1 - O que é Renda Fixa?")
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 3, max = 100, message = "O título deve ter entre 3 e 100 caracteres")
        String title,

        @Schema(description = "Descrição do conteúdo da aula", example = "Conceitos fundamentais sobre Tesouro Direto e CDBs.")
        @NotBlank(message = "A descrição é obrigatória")
        String description,

        @Schema(description = "URL do vídeo/mídia hospedada", example = "https://media.investirmais.com/videos/aula-1.mp4")
        @NotBlank(message = "A URL da mídia é obrigatória")
        String mediaUrl,

        @Schema(description = "URL da imagem de capa/thumbnail", example = "https://media.investirmais.com/thumbs/aula-1.jpg")
        @NotBlank(message = "A URL da thumbnail é obrigatória")
        String thumbnailUrl
    ) {}

    @Schema(description = "Resposta com dados da aula")
    public record Response(
        @Schema(description = "Identificador único da aula (UUID)", example = "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f")
        UUID id,

        @Schema(description = "Título da aula", example = "Aula 1 - O que é Renda Fixa?")
        String title,

        @Schema(description = "Descrição da aula", example = "Conceitos fundamentais sobre Tesouro Direto e CDBs.")
        String description,

        @Schema(description = "URL da mídia", example = "https://media.investirmais.com/videos/aula-1.mp4")
        String mediaUrl,

        @Schema(description = "URL da thumbnail", example = "https://media.investirmais.com/thumbs/aula-1.jpg")
        String thumbnailUrl,

        @Schema(description = "ID do curso ao qual esta aula pertence", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
        UUID courseId
    ) {}
}