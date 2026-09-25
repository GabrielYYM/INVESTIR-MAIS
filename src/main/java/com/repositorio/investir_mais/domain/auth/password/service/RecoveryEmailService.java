package com.repositorio.investir_mais.domain.auth.password.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.repositorio.investir_mais.common.constants.LogMessageConstants;
import com.repositorio.investir_mais.common.constants.MessageConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável por envio de e-mails transacionais do domínio de autenticação.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryEmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordRecoveryEmail(String email, String userName, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(MessageConstants.Auth.EMAIL_RECOVERY_SUBJECT);
            message.setText(
                    String.format(
                            MessageConstants.Auth.EMAIL_RECOVERY_BODY,
                            userName,
                            token
                    )
            );
            mailSender.send(message);
            log.info(LogMessageConstants.AUTH.PASSWORD_RECOVERY_EMAIL_SENT, email);
        } catch (Exception e) {
            log.error(LogMessageConstants.AUTH.PASSWORD_RECOVERY_EMAIL_ERROR, e);
        }
    }
}
