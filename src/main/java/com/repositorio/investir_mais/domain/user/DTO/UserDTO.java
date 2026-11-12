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
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String name,

        @NotNull(message = "A idade é obrigatória")
        @Range(min = 0, max = 120, message = "A idade deve estar entre 0 e 120 anos")
        Integer age,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 254)
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 4096, message = "A senha deve ter entre 8 e 4096 caracteres")
        String password,

        @Schema(description = "Perfil de acesso no sistema. O cadastro público aceita apenas STUDENT.", example = "STUDENT")
        UserRole role
    ) {}

    @Schema(description = "Dados para atualização de perfil")
    public record UpdateProfileRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String name,
        
        @NotBlank(message = "O e-mail é obrigatório") 
        @Email(message = "E-mail inválido") 
        @Size(max = 254)
        String email,
        
        @Range(min = 0, max = 120, message = "A idade deve estar entre 0 e 120 anos")
        Integer age,

        @Schema(description = "Senha atual para confirmar a alteração do perfil")
        @NotBlank(message = "A senha atual é obrigatória")
        @Size(max = 4096)
        String currentPassword
    ) {}

    @Schema(description = "Confirmação para exclusão da própria conta")
    public record DeleteAccountRequest(
        @NotBlank(message = "A senha atual é obrigatória")
        @Size(max = 4096)
        String currentPassword
    ) {}

    @Schema(description = "Resposta com dados do usuário")
    public record Response(
        UUID id,

        String name,

        Integer age,

        String email,

        UserRole role,

        Boolean emailVerified,

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
        Integer revisionNumber,

        Instant timestamp,

        @Schema(description = "Tipo de operação: INSERT, UPDATE ou DELETE", example = "UPDATE")
        String operation,

        @Schema(description = "Estado do usuário no momento desta revisão")
        UserDTO.Response user
    ) {}
}
