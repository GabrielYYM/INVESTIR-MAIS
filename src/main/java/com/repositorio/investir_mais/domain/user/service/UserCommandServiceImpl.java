package com.repositorio.investir_mais.domain.user.service;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserUpdateRequestDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.interfaces.UserCommandService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ServiceResult<UserResponseDTO> createUser(
            @NonNull UserRequestDTO userRequestDTO) {
        try {
            User user = userMapper.toUser(userRequestDTO);
            userRepository.save(user);

            return ServiceResult.success(userMapper.toUserResponseDTO(user));
        } catch (DataIntegrityViolationException e) {
            return ServiceResult.error(MessageConstants.User.EMAIL_ALREADY_IN_USE);
        } catch (IllegalArgumentException e) {
            return ServiceResult.error(e.getMessage());
        } catch (Exception e) {
            return ServiceResult.error("Ocorreu um erro ao processar seu cadastro. Tente novamente mais tarde.");
        }
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteUserById(
            @NonNull UUID id) {
        if (!userRepository.existsById(id)) {
            return ServiceResult.notFound(MessageConstants.User.NOT_FOUND_WITH_ID + id);
        }
        userRepository.deleteById(id);
        return ServiceResult.success(null);
    }

    @Override
    @Transactional
    public ServiceResult<UserResponseDTO> updateUserById(
            @NonNull UUID id,
            @NonNull UserUpdateRequestDTO userUpdateRequestDTO) {
        return userRepository.findById(id)
                .map(user -> {
                    try {
                        user.updateProfile(userUpdateRequestDTO.name(), userUpdateRequestDTO.email());
                        User updatedUser = userRepository.save(user);
                        return ServiceResult.success(userMapper.toUserResponseDTO(updatedUser));
                    } catch (Exception e) {
                        return ServiceResult.<UserResponseDTO>error(e.getMessage());
                    }
                })
                .orElseGet(() -> ServiceResult.notFound(MessageConstants.User.NOT_FOUND_FOR_UPDATE));
    }
}