package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.AccountEmailRequested;
import com.repositorio.investir_mais.infrastructure.config.AccountMailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountEmailRecovery {
    private final FailedEventPublications publications;
    private final AccountMailProperties properties;

    @Scheduled(fixedDelayString = "${app.mail.retry-delay:30s}", initialDelayString = "${app.mail.retry-delay:30s}")
    public void retryFailed() {
        publications.resubmit(ResubmissionOptions.defaults().withBatchSize(50).withMaxInFlight(10)
                .withMinAge(properties.retryDelay())
                .withFilter(publication -> publication.getEvent() instanceof AccountEmailRequested));
    }
}
