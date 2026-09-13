package com.repositorio.investirMais.common.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

public record MessageResponseDTO(
        @Schema(description = "Mensagem de status da operação", example = "Operação realizada com sucesso.") String message) {
}