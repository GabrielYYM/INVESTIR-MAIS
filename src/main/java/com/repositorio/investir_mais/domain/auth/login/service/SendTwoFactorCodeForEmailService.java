package com.repositorio.investir_mais.domain.auth.login.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.domain.user.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class SendTwoFactorCodeForEmailService implements TwoFactorNotificationService{
    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendTwoFactorCode(User user, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(user.getEmail());
            message.setSubject(MessageConstants.Auth.EMAIL_2FA_SUBJECT);
            message.setText(String.format(MessageConstants.Auth.EMAIL_2FA_BODY, user.getName(), code));

            mailSender.send(message);
            log.info("E-mail de 2FA enviado com sucesso para: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de 2FA para: {}", user.getEmail(), e);
        }
    }

}
