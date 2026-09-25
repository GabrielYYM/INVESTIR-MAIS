package com.repositorio.investir_mais.domain.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.common.security.CryptoService;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserUpdateRequestDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.UserSecurity;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.interfaces.UserCommandService;
import com.repositorio.investir_mais.domain.user.validation.interfaces.UserRegisterValidator;
import com.repositorio.investir_mais.domain.user.validation.interfaces.UserUpdateValidator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CryptoService cryptoService;
    private final List<UserRegisterValidator> registerValidators;
    private final List<UserUpdateValidator> updateValidators;

    @Override
    @Transactional
    public ServiceResult<UserResponseDTO> createUser(
            @NonNull UserRequestDTO userRequestDTO) {
        try {
            User user = userMapper.toUser(userRequestDTO);
            UserSecurity security = UserSecurity.builder()
                    .password(passwordEncoder.encode(userRequestDTO.password()))
                    .emailHash(cryptoService.generateSha256Hash(userRequestDTO.email()))
                    .role(UserRole.USER)
                    .emailVerified(false)
                    .build();
            user.setSecurity(security);
            userRepository.save(user);
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
    public ServiceResult<UserResponseDTO> updateUserById(@NonNull UUID id, @NonNull UserUpdateRequestDTO userUpdateRequestDTO) {
        return userRepository.findById(id)
                .map(user -> {
                    if (!passwordEncoder.matches(userUpdateRequestDTO.currentPassword(), user.getSecurity().getPassword())) {
                        return ServiceResult.<UserResponseDTO>error(MessageConstants.User.INVALID_PASSWORD);
                    }

                    try {
                        updateValidators.forEach(v -> v.validate(userUpdateRequestDTO, user));
                        String emailHash = cryptoService.generateSha256Hash(userUpdateRequestDTO.email());
                        user.updateProfile(userUpdateRequestDTO.name(), userUpdateRequestDTO.email(), emailHash);
                        User updatedUser = userRepository.save(user);
                        return ServiceResult.success(userMapper.toUserResponseDTO(updatedUser));
                    } catch (Exception e) {
                        return ServiceResult.<UserResponseDTO>error(e.getMessage());
                    }
                })
                .orElseGet(() -> ServiceResult.notFound(MessageConstants.User.NOT_FOUND_FOR_UPDATE));
    }
}