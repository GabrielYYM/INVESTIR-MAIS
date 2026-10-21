package com.repositorio.investir_mais.domain.user.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDTO {

    @Schema(description = "Dados para solicitação de recuperação de senha")
    public record ForgotPasswordRequest(
        @Schema(description = "E-mail do usuário que deseja redefinir a senha", example = "usuario@gmail.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "E-mail inválido")
        String email
    ) {}

    @Schema(description = "Dados para redefinição de senha com token OTT")
    public record ResetPasswordRequest(
        @Schema(description = "Token de verificação recebido por e-mail", example = "123456")
        @NotBlank(message = "O token é obrigatório.")
        String token,

        @Schema(description = "Nova senha de acesso", example = "NovaSenha123")
        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        String newPassword
    ) {}

    @Schema(description = "Dados para atualização de senha de usuário autenticado")
    public record UpdatePasswordRequest(
        @Schema(description = "Senha atual do usuário", example = "SenhaAntiga123")
        @NotBlank(message = "A senha atual é obrigatória.")
        String currentPassword,

        @Schema(description = "Nova senha desejada", example = "NovaSenhaSegura123")
        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        String newPassword
    ) {}

}
