package com.repositorio.investir_mais.domain.user.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthDTO {

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}

    public record ForgotPasswordRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        String email
    ) {}

    public record ResetPasswordRequest(
        @NotBlank(message = "O token é obrigatório.")
        String token,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 6, max = 20, message = "A nova senha deve ter entre 6 e 20 caracteres.")
        String newPassword
    ) {}

    public record UpdatePasswordRequest(
        @NotBlank(message = "A senha atual é obrigatória.")
        String currentPassword,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        String newPassword
    ) {}

    public record VerifyTwoFactorRequest(
        @NotBlank @Email String email,
        @NotBlank String code
    ) {}
}