package com.repositorio.investir_mais.domain.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.LogMessageConstants;
import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.common.security.CryptoService;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditAction;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditStatus;
import com.repositorio.investir_mais.domain.audit.service.interfaces.AuditLogService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CryptoService cryptoService;
    private final List<UserRegisterValidator> registerValidators;
    private final List<UserUpdateValidator> updateValidators;
    private final AuditLogService auditLogService;
    private final com.repositorio.investir_mais.domain.portfolio.service.interfaces.PortfolioCommandService portfolioCommandService;

    @Override
    @Transactional
    public ServiceResult<UserResponseDTO> createUser(
            @NonNull UserRequestDTO userRequestDTO) {
        try {
            registerValidators.forEach(v -> v.validate(userRequestDTO));

            User user = userMapper.toUser(userRequestDTO);
            UserSecurity security = UserSecurity.builder()
                    .password(passwordEncoder.encode(userRequestDTO.password()))
                    .emailHash(cryptoService.generateSha256Hash(userRequestDTO.email()))
                    .role(UserRole.USER)
                    .emailVerified(false)
                    .build();
            user.setSecurity(security);
            User savedUser = userRepository.save(user);

            log.info(LogMessageConstants.AUDIT.USER_CREATED, savedUser.getId(), userRequestDTO.email());
            auditLogService.log(AuditAction.USER_CREATED, savedUser.getId().toString(), userRequestDTO.email(),
                    "USER", savedUser.getId().toString(), null, AuditStatus.SUCCESS, "Novo usuário registrado com sucesso");
            user = userRepository.save(user);

            // Cria o portfólio (carteira) com categorias default para o novo usuário
            portfolioCommandService.createPortfolioForUser(user.getId());

            return ServiceResult.success(userMapper.toUserResponseDTO(savedUser));
        } catch (DataIntegrityViolationException e) {
            log.warn("Falha no cadastro (conflito de dados): {}", e.getMessage());
            auditLogService.log(AuditAction.USER_CREATED, null, userRequestDTO.email(),
                    "USER", null, null, AuditStatus.FAILURE, MessageConstants.User.EMAIL_ALREADY_IN_USE);
            return ServiceResult.error(MessageConstants.User.EMAIL_ALREADY_IN_USE);
        } catch (IllegalArgumentException e) {
            log.warn("Falha na validação do usuário: {}", e.getMessage());
            auditLogService.log(AuditAction.USER_CREATED, null, userRequestDTO.email(),
                    "USER", null, null, AuditStatus.FAILURE, e.getMessage());
            return ServiceResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao cadastrar usuário: {}", e.getMessage(), e);
            auditLogService.log(AuditAction.USER_CREATED, null, userRequestDTO.email(),
                    "USER", null, null, AuditStatus.FAILURE, e.getMessage());
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
        log.info(LogMessageConstants.AUDIT.USER_DELETED, id);
        auditLogService.log(AuditAction.USER_DELETED, "USER", id.toString(), AuditStatus.SUCCESS, "Usuário removido do sistema");
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

                        log.info(LogMessageConstants.AUDIT.USER_UPDATED, updatedUser.getId());
                        auditLogService.log(AuditAction.USER_UPDATED, "USER", updatedUser.getId().toString(),
                                AuditStatus.SUCCESS, "Perfil de usuário atualizado");

                        return ServiceResult.success(userMapper.toUserResponseDTO(updatedUser));
                    } catch (Exception e) {
                        return ServiceResult.<UserResponseDTO>error(e.getMessage());
                    }
                })
                .orElseGet(() -> ServiceResult.notFound(MessageConstants.User.NOT_FOUND_FOR_UPDATE));
    }
}