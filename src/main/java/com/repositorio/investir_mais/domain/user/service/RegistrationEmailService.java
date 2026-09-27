package com.repositorio.investir_mais.domain.user.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.domain.user.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationEmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmails(User user, String userCode, String guardianCode) {
        try {
            // Envia e-mail para a criança / usuário
            SimpleMailMessage userMessage = new SimpleMailMessage();
            userMessage.setTo(user.getEmail());
            userMessage.setSubject(MessageConstants.Auth.EMAIL_VERIFICATION_CHILD_SUBJECT);

            if (user.isUnder12()) {
                userMessage.setText(String.format(
                        MessageConstants.Auth.EMAIL_VERIFICATION_CHILD_MINOR_BODY,
                        user.getName(),
                        userCode
                ));
            } else {
                userMessage.setText(String.format(
                        MessageConstants.Auth.EMAIL_VERIFICATION_CHILD_BODY,
                        user.getName(),
                        userCode
                ));
            }

            mailSender.send(userMessage);
            log.info("E-mail de verificação de cadastro enviado com sucesso para o usuário: {}", user.getEmail());

            // Se for menor de 12 anos, envia também para o responsável
            if (user.isUnder12() && user.getGuardianEmail() != null && !user.getGuardianEmail().isBlank()) {
                SimpleMailMessage guardianMessage = new SimpleMailMessage();
                guardianMessage.setTo(user.getGuardianEmail());
                guardianMessage.setSubject(MessageConstants.Auth.EMAIL_VERIFICATION_GUARDIAN_SUBJECT);
                guardianMessage.setText(String.format(
                        MessageConstants.Auth.EMAIL_VERIFICATION_GUARDIAN_BODY,
                        user.getName(),
                        guardianCode
                ));

                mailSender.send(guardianMessage);
                log.info("E-mail de autorização de cadastro enviado com sucesso para o responsável: {}", user.getGuardianEmail());
            }
        } catch (Exception e) {
            log.error("Falha ao enviar e-mails de verificação de cadastro para o usuário {}", user.getEmail(), e);
        }
    }
}
