package com.repositorio.investir_mais.domain.auth.login.service;

import com.repositorio.investir_mais.domain.user.model.User;


public interface TwoFactorNotificationService {

    void sendTwoFactorCode(User user, String code);
}
