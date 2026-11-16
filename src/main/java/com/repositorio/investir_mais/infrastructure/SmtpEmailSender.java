package com.repositorio.investir_mais.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "smtp")
class SmtpEmailSender implements AccountEmailSender {
    private final JavaMailSender mail;
    private final String from;
    SmtpEmailSender(JavaMailSender mail, @Value("${app.mail.smtp.from:teste@investir.local}") String from) {
        this.mail = mail; this.from = from;
    }
    public void send(String recipient, String subject, String text, String html, String idempotencyKey) {
        var message = new SimpleMailMessage();
        message.setFrom(from); message.setTo(recipient); message.setSubject(subject); message.setText(text);
        mail.send(message);
    }
}
