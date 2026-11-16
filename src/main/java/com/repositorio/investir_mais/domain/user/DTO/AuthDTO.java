package com.repositorio.investir_mais.domain.user.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDTO {
    public static final String PASSWORD_PATTERN = "^(?=[\\s\\S]*[a-z])(?=[\\s\\S]*[A-Z])(?=[\\s\\S]*[0-9])(?=[\\s\\S]*[\\x21-\\x2F\\x3A-\\x40\\x5B-\\x60\\x7B-\\x7E])[\\s\\S]*$";
    public static final String PASSWORD_REQUIREMENTS = "Use pelo menos uma letra minúscula, uma maiúscula, um número e um caractere especial.";

    @Schema(description = "Dados para solicitação de recuperação de senha")
    public record ForgotPasswordRequest(
        @Schema(description = "E-mail do usuário que deseja redefinir a senha", example = "usuario@gmail.com")
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

        @Schema(description = "Nova senha com minúscula, maiúscula, número e caractere especial", example = "NovaSenha123!")
        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.")
        @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_REQUIREMENTS)
        String newPassword
    ) {}

    @Schema(description = "Dados para atualização de senha de usuário autenticado")
    public record UpdatePasswordRequest(
        @Schema(description = "Senha atual do usuário", example = "SenhaAntiga123")
        @NotBlank(message = "A senha atual é obrigatória.")
        @Size(max = 4096)
        String currentPassword,

        @Schema(description = "Nova senha com minúscula, maiúscula, número e caractere especial", example = "NovaSenhaSegura123!")
        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.")
        @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_REQUIREMENTS)
        String newPassword
    ) {}

}
