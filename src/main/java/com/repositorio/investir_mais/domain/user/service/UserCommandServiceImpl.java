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
    private final RegistrationEmailService registrationEmailService;

    @Override
    @Transactional
    public ServiceResult<UserResponseDTO> createUser(
            @NonNull UserRequestDTO userRequestDTO) {
        try {
            registerValidators.forEach(v -> v.validate(userRequestDTO));

            User user = userMapper.toUser(userRequestDTO);
            String userCode = cryptoService.generateNumericCode(6);
            String guardianCode = user.isUnder12() ? cryptoService.generateNumericCode(6) : null;

            UserSecurity security = UserSecurity.builder()
                    .password(passwordEncoder.encode(userRequestDTO.password()))
                    .emailHash(cryptoService.generateSha256Hash(userRequestDTO.email()))
                    .role(UserRole.ALUNO)
                    .emailVerified(false)
                    .verificationCode(userCode)
                    .guardianVerificationCode(guardianCode)
                    .verificationExpiry(java.time.LocalDateTime.now().plusMinutes(15))
                    .build();

            user.setSecurity(security);
            userRepository.save(user);

            registrationEmailService.sendVerificationEmails(user, userCode, guardianCode);

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
    public ServiceResult<Void> verifyRegistration(@NonNull com.repositorio.investir_mais.domain.user.DTO.VerifyRegistrationRequestDTO verifyRequest) {
        String emailHash = cryptoService.generateSha256Hash(verifyRequest.email());
        User user = userRepository.findBySecurityEmailHash(emailHash).orElse(null);

        if (user == null) {
            return ServiceResult.notFound(MessageConstants.User.NOT_FOUND);
        }

        if (user.getSecurity().isEmailVerified()) {
            return ServiceResult.error(MessageConstants.Auth.ERR_ALREADY_VERIFIED);
        }

        if (user.getSecurity().getVerificationExpiry() == null ||
                user.getSecurity().getVerificationExpiry().isBefore(java.time.LocalDateTime.now())) {
            return ServiceResult.error(MessageConstants.Auth.ERR_EXPIRED_VERIFICATION);
        }

        if (user.getSecurity().getVerificationCode() == null ||
                !user.getSecurity().getVerificationCode().equals(verifyRequest.code())) {
            return ServiceResult.error(MessageConstants.Auth.ERR_INVALID_VERIFICATION_CODE);
        }

        if (user.isUnder12()) {
            if (verifyRequest.guardianCode() == null || verifyRequest.guardianCode().trim().isBlank()) {
                return ServiceResult.error(MessageConstants.Auth.ERR_GUARDIAN_CODE_REQUIRED);
            }

            if (user.getSecurity().getGuardianVerificationCode() == null ||
                    !user.getSecurity().getGuardianVerificationCode().equals(verifyRequest.guardianCode().trim())) {
                return ServiceResult.error(MessageConstants.Auth.ERR_INVALID_GUARDIAN_CODE);
            }
        }

        user.getSecurity().setEmailVerified(true);
        user.getSecurity().clearVerificationCodes();
        userRepository.save(user);

        return ServiceResult.success(null);
    }

    @Override
    @Transactional
    public ServiceResult<Void> resendRegistrationVerification(@NonNull com.repositorio.investir_mais.domain.user.DTO.ResendVerificationRequestDTO resendRequest) {
        String emailHash = cryptoService.generateSha256Hash(resendRequest.email());
        User user = userRepository.findBySecurityEmailHash(emailHash).orElse(null);

        if (user == null) {
            return ServiceResult.notFound(MessageConstants.User.NOT_FOUND);
        }

        if (user.getSecurity().isEmailVerified()) {
            return ServiceResult.error(MessageConstants.Auth.ERR_ALREADY_VERIFIED);
        }

        String userCode = cryptoService.generateNumericCode(6);
        String guardianCode = user.isUnder12() ? cryptoService.generateNumericCode(6) : null;

        user.getSecurity().generateVerificationCodes(userCode, guardianCode, java.time.LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        registrationEmailService.sendVerificationEmails(user, userCode, guardianCode);

        return ServiceResult.success(null);
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