package com.repositorio.investir_mais.domain.user.DTO;

import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.Range;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
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
        @Range(min = 0, max = 120, message = "A idade deve estar entre 0 e 120 anos")
        Integer age,

        @Schema(description = "Endereço de e-mail único", example = "usuario@gmail.com")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 254)
        String email,

        @Schema(description = "Senha de acesso do usuário", example = "SenhaSegura123")
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 4096, message = "A senha deve ter entre 8 e 4096 caracteres")
        String password,

        @Schema(description = "Perfil de acesso no sistema. O cadastro público aceita apenas STUDENT.", example = "STUDENT")
        UserRole role
    ) {}

    @Schema(description = "Dados para atualização de perfil")
    public record UpdateProfileRequest(
        @Schema(description = "Nome do usuário", example = "Usuario da Silva")
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String name,
        
        @Schema(description = "Novo endereço de e-mail", example = "usuario@gmail.com")
        @NotBlank(message = "O e-mail é obrigatório") 
        @Email(message = "E-mail inválido") 
        @Size(max = 254)
        String email,
        
        @Schema(description = "Idade do usuário", example = "23")
        @Range(min = 0, max = 120, message = "A idade deve estar entre 0 e 120 anos")
        Integer age,

        @Schema(description = "Senha atual para confirmar a alteração do perfil")
        @NotBlank(message = "A senha atual é obrigatória")
        @Size(max = 4096)
        String currentPassword
    ) {}

    @Schema(description = "Confirmação para exclusão da própria conta")
    public record DeleteAccountRequest(
        @Schema(description = "Senha atual do usuário")
        @NotBlank(message = "A senha atual é obrigatória")
        @Size(max = 4096)
        String currentPassword
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

    @Schema(description = "Requisição para alteração do perfil (role) do usuário pelo administrador")
    public record UpdateRoleRequest(
        @NotNull(message = "O perfil (role) é obrigatório")
        @Schema(description = "Novo perfil do usuário (apenas TEACHER ou STUDENT)", example = "TEACHER")
        UserRole role
    ) {}

    @Schema(description = "Registro de auditoria histórica de um usuário")
    public record AuditResponse(
        @Schema(description = "Número sequencial da revisão", example = "1")
        Integer revisionNumber,

        @Schema(description = "Data e hora exata da alteração")
        Instant timestamp,

        @Schema(description = "Tipo de operação: INSERT, UPDATE ou DELETE", example = "UPDATE")
        String operation,

        @Schema(description = "Estado do usuário no momento desta revisão")
        UserDTO.Response user
    ) {}
}
