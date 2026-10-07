package com.repositorio.investir_mais.domain.tools.DTO;

import java.util.UUID;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;
import jakarta.validation.constraints.NotBlank;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QuestionDTO {

    public record Request(
        @NotBlank(message = "O texto da pergunta é obrigatório")
        String text,
        
        AssetRole role
    ) {}

    public record Response(
        UUID id,
        String text,
        AssetRole role
    ) {}
}