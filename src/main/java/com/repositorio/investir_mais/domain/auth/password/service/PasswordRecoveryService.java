package com.repositorio.investir_mais.domain.auth.password.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.repositorio.investir_mais.common.constants.LogMessageConstants;
import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.common.security.CryptoService;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditAction;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditStatus;
import com.repositorio.investir_mais.domain.audit.service.interfaces.AuditLogService;
import com.repositorio.investir_mais.domain.auth.login.service.LoginAttemptService;
import com.repositorio.investir_mais.domain.auth.password.model.PasswordResetToken;
import com.repositorio.investir_mais.domain.auth.password.repository.PasswordResetTokenRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;
    private final LoginAttemptService loginAttemptService;
    private final CryptoService cryptoService;
    private final RecoveryEmailService authEmailService;
    private final AuditLogService auditLogService;

    public ServiceResult<Void> initiatePasswordRecovery(@NonNull String email, @NonNull String ip) {
        if (!loginAttemptService.isBlocked(ip)) {
            createPasswordResetTokenForUser(email);
            auditLogService.log(AuditAction.PASSWORD_RESET_REQUESTED, null, email,
                    "AUTH", null, ip, AuditStatus.SUCCESS, "Solicitação de recuperação de senha iniciada");
        } else {
            log.warn(LogMessageConstants.SECURITY.PASSWORD_RECOVERY_BLOCKED_RATE_LIMIT, ip);
            auditLogService.log(AuditAction.SECURITY_BLOCK, null, email,
                    "AUTH", null, ip, AuditStatus.WARNING, "Solicitação de recuperação bloqueada por Rate Limit");
        }

        log.info(LogMessageConstants.AUTH.PASSWORD_RECOVERY_INITIATED, email, ip);

        // Previne Timing Attack: Equalizar tempo de resposta
        try {
            Thread.sleep(50);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }

        return ServiceResult.success(null);
    }

    @Async
    public void createPasswordResetTokenForUser(@NonNull String email) {
        String emailHash = cryptoService.generateSha256Hash(email);

        userRepository.findBySecurityEmailHash(emailHash)
                .ifPresent(user -> {
                    String token = cryptoService.generateSecureToken();
                    String hashedToken = cryptoService.generateHmacTokenHash(token);

                    transactionTemplate.execute(status -> {
                        PasswordResetToken myToken = new PasswordResetToken(hashedToken, "hmac-v1", user);
                        return passwordResetTokenRepository.save(myToken);
                    });

                    authEmailService.sendPasswordRecoveryEmail(email, user.getName(), token);
                });
    }

    @Transactional
    public ServiceResult<Void> resetPassword(@NonNull String token, @NonNull String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(cryptoService.generateHmacTokenHash(token))
                .orElse(null);

        if (resetToken == null) {
            auditLogService.log(AuditAction.PASSWORD_RESET_SUCCESS, null, null,
                    "AUTH", null, null, AuditStatus.FAILURE, "Tentativa de redefinição com token inexistente ou inválido");
            return ServiceResult.error(MessageConstants.Auth.ERR_INVALID_TOKEN);
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            auditLogService.log(AuditAction.PASSWORD_RESET_SUCCESS, resetToken.getUser() != null ? resetToken.getUser().getId().toString() : null,
                    resetToken.getUser() != null ? resetToken.getUser().getEmail() : null,
                    "AUTH", null, null, AuditStatus.FAILURE, "Tentativa de redefinição com token expirado");
            return ServiceResult.error(MessageConstants.Auth.ERR_EXPIRED_TOKEN);
        }

        User user = resetToken.getUser();
        user.getSecurity().setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        auditLogService.log(AuditAction.PASSWORD_RESET_SUCCESS, user.getId().toString(), user.getEmail(),
                "AUTH", user.getId().toString(), null, AuditStatus.SUCCESS, "Senha redefinida com sucesso via token de recuperação");

        return ServiceResult.success(null);
    }
}
