package com.repositorio.investir_mais.domain.auth.token.service;

import java.time.Instant;

import com.repositorio.investir_mais.common.security.CryptoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.auth.token.model.InvalidToken;
import com.repositorio.investir_mais.domain.auth.token.repository.InvalidTokenRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class TokenBlackListService {
    private final InvalidTokenRepository invalidTokenRepository;
    private final CryptoService cryptoService;

    @Transactional
    public void invalidateToken(String token, Instant expiresAt) {
        InvalidToken invalidToken = new InvalidToken(cryptoService.generateSha256Hash(token), expiresAt);
        invalidTokenRepository.save(invalidToken);
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return invalidTokenRepository.existsById(cryptoService.generateSha256Hash(token));
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public ServiceResult<Void> removeExpiredTokens() {
        invalidTokenRepository.deleteByExpiresAtBefore(Instant.now());
        return ServiceResult.success(null);
    }
}
