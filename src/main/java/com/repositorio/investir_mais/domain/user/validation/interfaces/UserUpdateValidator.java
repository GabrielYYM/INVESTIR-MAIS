package com.repositorio.investir_mais.domain.user.validation.interfaces;

import com.repositorio.investir_mais.domain.user.DTO.UserUpdateRequestDTO;
import com.repositorio.investir_mais.domain.user.model.User;

public interface UserUpdateValidator {
    void validate(UserUpdateRequestDTO request, User user);
}
