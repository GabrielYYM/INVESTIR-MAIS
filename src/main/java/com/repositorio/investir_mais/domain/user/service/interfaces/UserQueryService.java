package com.repositorio.investir_mais.domain.user.service.interfaces;

import java.util.UUID;

import com.repositorio.investir_mais.common.result.ServiceResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;

public interface UserQueryService {
    ServiceResult<UserResponseDTO> findUserById(UUID id);

    ServiceResult<Page<UserResponseDTO>> listAllUsers(Pageable pageable);

}
