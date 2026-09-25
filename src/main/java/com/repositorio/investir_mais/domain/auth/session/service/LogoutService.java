package com.repositorio.investir_mais.domain.auth.session.service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.auth.token.service.TokenProviderService;
import com.repositorio.investir_mais.domain.auth.token.service.TokenBlackListService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class LogoutService {

    private final TokenBlackListService tokenBlackListService;
    private final TokenProviderService tokenProvider;

    private static final String BEARER_PREFIX = "Bearer ";

    @Transactional
    public ServiceResult<Void> logout(@NonNull String token) {
        String tokenJWT = token.replace(BEARER_PREFIX, "");
        tokenBlackListService.invalidateToken(
                tokenJWT,
                tokenProvider.getExpiration(tokenJWT)
        );
        return ServiceResult.success(null);
    }
}
