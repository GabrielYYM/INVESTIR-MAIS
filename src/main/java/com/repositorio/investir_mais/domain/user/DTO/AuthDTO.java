package com.repositorio.investir_mais.domain.user.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDTO {

    @Schema(description = "Dados para solicitação de recuperação de senha")
    public record ForgotPasswordRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "E-mail inválido")
        @Size(max = 254)
        String email
    ) {}

    @Schema(description = "Dados para redefinição de senha com token OTT")
    public record ResetPasswordRequest(
        @Schema(description = "Token de verificação recebido por e-mail", example = "123456")
        @NotBlank(message = "O token é obrigatório.")
        @Size(max = 64)
        String token,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 8, max = 4096, message = "A senha deve ter entre 8 e 4096 caracteres.")
        String newPassword
    ) {}

    @Schema(description = "Dados para atualização de senha de usuário autenticado")
    public record UpdatePasswordRequest(
        @NotBlank(message = "A senha atual é obrigatória.")
        @Size(max = 4096)
        String currentPassword,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 8, max = 4096, message = "A senha deve ter entre 8 e 4096 caracteres.")
        String newPassword
    ) {}

}
