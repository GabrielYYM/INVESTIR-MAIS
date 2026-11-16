package com.repositorio.investir_mais.infrastructure;

interface AccountEmailSender {
    void send(String recipient, String subject, String text, String html, String idempotencyKey);
}
