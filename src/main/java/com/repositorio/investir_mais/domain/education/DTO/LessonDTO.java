package com.repositorio.investir_mais.domain.education.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

public class LessonDTO {
    @Schema(name = "LessonRequest", description = "Dados para criação ou atualização de uma aula")
    public record Request(
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 3, max = 100, message = "O título deve ter entre 3 e 100 caracteres")
        String title,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 10000)
        String description,

        @NotBlank(message = "A URL da mídia é obrigatória")
        @Size(max = 4096)
        @URL(message = "A URL da mídia é inválida")
        String mediaUrl,

        @NotBlank(message = "A URL da thumbnail é obrigatória")
        @Size(max = 4096)
        @URL(message = "A URL da thumbnail é inválida")
        String thumbnailUrl
    ) {}

    @Schema(name = "LessonResponse", description = "Resposta com dados da aula")
    public record Response(
        UUID id,

        String title,

        String description,

        String mediaUrl,

        String thumbnailUrl,

        UUID courseId
    ) {}

    @Schema(description = "Requisição para atualizar o progresso de uma aula")
    public record ProgressRequest(
        boolean completed
    ) {}
}
