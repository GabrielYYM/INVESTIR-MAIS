package com.repositorio.investir_mais.domain.user.DTO;

import com.repositorio.investir_mais.common.validation.user.ValidEmail;
import com.repositorio.investir_mais.common.validation.user.ValidName;
import com.repositorio.investir_mais.common.validation.user.ValidPassword;


public record UserUpdateRequestDTO(
        @ValidName
        String name,

        @ValidEmail
        String email,

        @ValidPassword
        String currentPassword
) {
}
