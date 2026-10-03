package com.repositorio.investir_mais.domain.user.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyTwoFactorRequestDTO(@NotBlank @Email String email, @NotBlank String code) {}
