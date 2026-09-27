package com.repositorio.investir_mais.domain.auth.login.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.repositorio.investir_mais.common.security.CryptoService;
import com.repositorio.investir_mais.domain.auth.login.service.TwoFactorNotificationService;
import com.repositorio.investir_mais.domain.user.model.User;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class TwoFactorService {
    private final CryptoService cryptoService;
    private final TwoFactorNotificationService twoFactorNotification;

    public void prepareAndSendTwoFactor(@NonNull User user) {
        String code = cryptoService.generateNumericCode(6);
        user.getSecurity().generateTwoFactorCode(
                code,
                LocalDateTime.now()
                        .plusMinutes(5)
        );
        twoFactorNotification.sendTwoFactorCode(user, code);
    }
}
