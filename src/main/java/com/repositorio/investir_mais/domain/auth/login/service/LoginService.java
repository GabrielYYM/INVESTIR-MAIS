package com.repositorio.investir_mais.domain.auth.login.service;

import java.time.LocalDateTime;

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
import com.repositorio.investir_mais.domain.auth.login.DTO.LoginRequestDTO;
import com.repositorio.investir_mais.domain.auth.login.DTO.Verify2FARequestDTO;
import com.repositorio.investir_mais.domain.auth.token.service.TokenProviderService;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProviderService tokenProvider;
    private final TwoFactorService twoFactorService;
    private final LoginAttemptService loginAttemptService;
    private final CryptoService cryptoService;
    private final AuditLogService auditLogService;

    @Transactional
    public ServiceResult<Void> initiateLogin(@NonNull LoginRequestDTO loginRequest, @NonNull String ip) {
        if (loginAttemptService.isBlocked(ip) || loginAttemptService.isBlocked(loginRequest.email())) {
            log.warn(LogMessageConstants.SECURITY.BRUTE_FORCE_LOGIN_BLOCKED, ip, loginRequest.email());
            auditLogService.log(AuditAction.SECURITY_BLOCK, null, loginRequest.email(),
                    "AUTH", null, ip, AuditStatus.WARNING, "Bloqueio por excesso de tentativas de login");
            return ServiceResult.error(MessageConstants.Auth.ERR_TOO_MANY_ATTEMPTS);
        }

        String searchHash = cryptoService.generateSha256Hash(loginRequest.email());
        User user = userRepository.findBySecurityEmailHash(searchHash).orElse(null);

        if (user == null) {
            loginAttemptService.loginFailed(ip);
            log.warn(LogMessageConstants.AUTH.LOGIN_FAILED_USER_NOT_FOUND, ip, maskEmail(loginRequest.email()));
            auditLogService.log(AuditAction.LOGIN_FAILED, null, loginRequest.email(),
                    "AUTH", null, ip, AuditStatus.FAILURE, "Usuário não encontrado");
            return ServiceResult.error(MessageConstants.Auth.ERR_INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(loginRequest.password(), user.getSecurity().getPassword())) {
            loginAttemptService.loginFailed(ip);
            loginAttemptService.loginFailed(user.getEmail());
            log.warn(LogMessageConstants.AUTH.LOGIN_FAILED_INVALID_PASSWORD, ip, maskEmail(user.getEmail()));
            auditLogService.log(AuditAction.LOGIN_FAILED, user.getId().toString(), user.getEmail(),
                    "AUTH", user.getId().toString(), ip, AuditStatus.FAILURE, "Senha incorreta");
            return ServiceResult.error(MessageConstants.Auth.ERR_INVALID_CREDENTIALS);
        }

        loginAttemptService.loginSucceeded(ip);
        loginAttemptService.loginSucceeded(user.getEmail());

        log.info(LogMessageConstants.AUTH.LOGIN_INITIATED, user.getId(), ip);
        auditLogService.log(AuditAction.TWO_FACTOR_GENERATED, user.getId().toString(), user.getEmail(),
                "AUTH", user.getId().toString(), ip, AuditStatus.SUCCESS, "Credenciais válidas, código 2FA emitido");

        twoFactorService.prepareAndSendTwoFactor(user);
        userRepository.save(user);

        return ServiceResult.success(null);
    }

    @Transactional
    public ServiceResult<String> verify2FAAndGenerateToken(@NonNull Verify2FARequestDTO verifyRequest, @NonNull String ip) {
        String attemptKey = MessageConstants.Auth.PREFIX_2FA + verifyRequest.email();

        if (loginAttemptService.isBlocked(ip)) {
            log.warn(LogMessageConstants.SECURITY.BRUTE_FORCE_2FA_BLOCKED, ip, verifyRequest.email());
            auditLogService.log(AuditAction.SECURITY_BLOCK, null, verifyRequest.email(),
                    "AUTH", null, ip, AuditStatus.WARNING, "Bloqueio por excesso de tentativas 2FA");
            return ServiceResult.error(MessageConstants.Auth.ERR_TOO_MANY_ATTEMPTS_2FA);
        }

        String searchHash = cryptoService.generateSha256Hash(verifyRequest.email());
        User user = userRepository.findBySecurityEmailHash(searchHash).orElse(null);

        if (user == null) {
            return ServiceResult.notFound(MessageConstants.User.NOT_FOUND);
        }

        if (user.getSecurity().getTwoFactorCode() == null ||
                !user.getSecurity().getTwoFactorCode().equals(verifyRequest.code())) {
            loginAttemptService.loginFailed(ip);
            log.warn(LogMessageConstants.AUTH.LOGIN_2FA_FAILED_INVALID_CODE, ip, maskEmail(user.getEmail()));
            auditLogService.log(AuditAction.TWO_FACTOR_FAILED, user.getId().toString(), user.getEmail(),
                    "AUTH", user.getId().toString(), ip, AuditStatus.FAILURE, "Código 2FA inválido fornecido");
            return ServiceResult.error(MessageConstants.Auth.ERR_INVALID_2FA);
        }

        if (user.getSecurity().getTwoFactorExpiry().isBefore(LocalDateTime.now())) {
            user.getSecurity().clearTwoFactorCode();
            userRepository.save(user);
            log.warn(LogMessageConstants.AUTH.LOGIN_2FA_FAILED_EXPIRED_CODE, ip, user.getEmail());
            auditLogService.log(AuditAction.TWO_FACTOR_FAILED, user.getId().toString(), user.getEmail(),
                    "AUTH", user.getId().toString(), ip, AuditStatus.FAILURE, "Código 2FA expirado");
            return ServiceResult.error(MessageConstants.Auth.ERR_EXPIRED_2FA);
        }

        loginAttemptService.loginSucceeded(ip);
        loginAttemptService.loginSucceeded(attemptKey);
        user.getSecurity().clearTwoFactorCode();
        userRepository.save(user);

        log.info(LogMessageConstants.AUTH.LOGIN_SUCCESS, user.getId(), ip);
        auditLogService.log(AuditAction.LOGIN_SUCCESS, user.getId().toString(), user.getEmail(),
                "AUTH", user.getId().toString(), ip, AuditStatus.SUCCESS, "Autenticação concluída via 2FA");

        return ServiceResult.success(tokenProvider.generateToken(user.getId()));
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@"))
            return "***";
        String[] parts = email.split("@");
        return parts[0].substring(0, Math.min(2, parts[0].length())) + "***@" + parts[1];
    }
}
