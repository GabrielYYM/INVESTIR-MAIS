package com.repositorio.investir_mais.domain.user.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.interfaces.UserQueryService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<UserResponseDTO> findUserById(
            @NonNull UUID id) {
        return userRepository.findById(id)
                .map(user -> ServiceResult.success(userMapper.toUserResponseDTO(user)))
                .orElse(ServiceResult.notFound(MessageConstants.User.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<Page<UserResponseDTO>> listAllUsers(
            @NonNull Pageable pageable) {
        Page<UserResponseDTO> users = userRepository.findAll(pageable)
                .map(userMapper::toUserResponseDTO);
        return ServiceResult.success(users);
    }
}
