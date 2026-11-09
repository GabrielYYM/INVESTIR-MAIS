package com.repositorio.investir_mais.infrastructure;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
class ResendEmailSender {
    private final RestClient client;
    private final String sender;

    ResendEmailSender(RestClient.Builder builder, @Value("${app.mail.resend.api-key}") String apiKey,
            @Value("${app.mail.resend.from}") String sender) {
        Assert.hasText(apiKey, "RESEND_API_KEY é obrigatório para enviar e-mails pelo Resend.");
        Assert.hasText(sender, "RESEND_FROM é obrigatório para enviar e-mails pelo Resend.");
        this.sender = sender;
        this.client = builder.baseUrl("https://api.resend.com")
                .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
                .build();
    }

    void send(String recipient, String subject, String text, String html, String idempotencyKey) {
        var request = client.post().uri("/emails").contentType(MediaType.APPLICATION_JSON);
        if (idempotencyKey != null) {
            request.header("Idempotency-Key", idempotencyKey);
        }
        request.body(new EmailRequest(sender, List.of(recipient), subject, text, html)).retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), (ignored, response) -> {
                    throw new RestClientException("Resend retornou HTTP " + response.getStatusCode().value());
                })
                .toBodilessEntity();
    }

    private record EmailRequest(String from, List<String> to, String subject, String text,
            @JsonInclude(JsonInclude.Include.NON_NULL) String html) {}
}
