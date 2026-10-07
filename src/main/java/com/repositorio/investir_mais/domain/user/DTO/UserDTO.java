package com.repositorio.investir_mais.domain.user.DTO;

import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class UserDTO {

    public record Request(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        Integer age,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String password,

        UserRole role
    ) {}

    public record UpdateProfileRequest(
        @NotBlank(message = "O nome é obrigatório") 
        String name,
        
        @NotBlank(message = "O e-mail é obrigatório") 
        @Email(message = "E-mail inválido") 
        String email,
        
        Integer age
    ) {}

    public record Response(
        UUID id,
        String name,
        Integer age,
        String email,
        UserRole role,
        Boolean emailVerified,
        LocalDateTime createdAt
    ) {}
}