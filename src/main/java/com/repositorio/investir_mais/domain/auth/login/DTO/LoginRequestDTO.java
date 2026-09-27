package com.repositorio.investir_mais.domain.auth.login.DTO;

import com.repositorio.investir_mais.common.validation.user.ValidEmail;
import com.repositorio.investir_mais.common.validation.user.ValidPassword;


public record LoginRequestDTO(
        @ValidEmail
        String email,


        @ValidPassword
        String password
) {}
