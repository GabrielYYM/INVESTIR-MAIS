package com.repositorio.investir_mais.domain.user.validation.interfaces;

import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;

public interface UserRegisterValidator {
    void validate(UserRequestDTO request);
}
