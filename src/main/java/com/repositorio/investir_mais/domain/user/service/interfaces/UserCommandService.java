package com.repositorio.investir_mais.domain.user.service.interfaces;

import java.util.UUID;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserUpdateRequestDTO;

public interface UserCommandService {
    ServiceResult<UserResponseDTO> createUser(UserRequestDTO userRequestDTO);

    ServiceResult<Void> deleteUserById(UUID id);

    ServiceResult<UserResponseDTO> updateUserById(UUID id, UserUpdateRequestDTO userUpdateRequestDTO);
}
