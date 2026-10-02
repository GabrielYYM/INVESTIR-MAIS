package com.repositorio.investir_mais.domain.user.DTO;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        String role,
        LocalDate birthDate,
        boolean requiresGuardianVerification
) {
}
