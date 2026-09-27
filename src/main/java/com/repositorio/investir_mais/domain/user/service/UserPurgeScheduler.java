package com.repositorio.investir_mais.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPurgeScheduler {

    private static final int RETENTION_MONTHS = 6;

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void purgeExpiredAccounts() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(RETENTION_MONTHS);
        List<User> expired = userRepository.findAllPendingPurge(threshold);

        if (expired.isEmpty()) {
            return;
        }

        userRepository.deleteAll(expired);
        log.info("[UserPurgeScheduler] {} conta(s) removida(s) definitivamente (deletedAt < {}).",
                expired.size(), threshold);
    }
}
