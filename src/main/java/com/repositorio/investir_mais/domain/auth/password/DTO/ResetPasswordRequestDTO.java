package com.repositorio.investir_mais.domain.auth.password.DTO;

import com.repositorio.investir_mais.common.validation.auth.ValidToken;
import com.repositorio.investir_mais.common.validation.user.ValidPassword;


public record ResetPasswordRequestDTO (
        @ValidToken
        String token,

        @ValidPassword
        String newPassword
){}
