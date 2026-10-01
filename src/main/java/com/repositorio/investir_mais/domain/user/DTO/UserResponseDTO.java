package com.repositorio.investir_mais.domain.user.DTO;

import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
    UUID id,
    String name,
    Integer age,
    String email,
    UserRole role,
    Boolean emailVerified,
    LocalDateTime createdAt
) {}