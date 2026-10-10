package com.repositorio.investir_mais.domain.user.DTO;

import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserDTO {

    @Schema(description = "Dados para cadastro de um novo usuário")
    public record Request(
        @Schema(description = "Nome completo do usuário", example = "Usuario da Silva")
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String name,

        @Schema(description = "Idade do usuário", example = "22")
        @NotNull(message = "A idade é obrigatória")
        Integer age,

        @Schema(description = "Endereço de e-mail único", example = "usuario@gmail.com")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @Schema(description = "Senha de acesso do usuário", example = "SenhaSegura123")
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String password,

        @Schema(description = "Perfil de acesso no sistema (não é permitido passar ADMIN no cadastro público)", example = "STUDENT")
        UserRole role
    ) {}

    @Schema(description = "Dados para atualização de perfil")
    public record UpdateProfileRequest(
        @Schema(description = "Nome do usuário", example = "Usuario da Silva")
        @NotBlank(message = "O nome é obrigatório") 
        String name,
        
        @Schema(description = "Novo endereço de e-mail", example = "usuario@gmail.com")
        @NotBlank(message = "O e-mail é obrigatório") 
        @Email(message = "E-mail inválido") 
        String email,
        
        @Schema(description = "Idade do usuário", example = "23")
        Integer age
    ) {}

    @Schema(description = "Resposta com dados do usuário")
    public record Response(
        @Schema(description = "Identificador único (UUID)", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID id,

        @Schema(description = "Nome do usuário", example = "Usuario da Silva")
        String name,

        @Schema(description = "Idade do usuário", example = "22")
        Integer age,

        @Schema(description = "E-mail cadastrado", example = "usuario@gmail.com")
        String email,

        @Schema(description = "Perfil de acesso", example = "STUDENT")
        UserRole role,

        @Schema(description = "Status de validação do e-mail", example = "false")
        Boolean emailVerified,

        @Schema(description = "Data e hora do cadastro")
        LocalDateTime createdAt
    ) {}
}