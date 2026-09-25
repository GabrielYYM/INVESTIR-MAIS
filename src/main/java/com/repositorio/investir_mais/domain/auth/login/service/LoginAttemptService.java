package com.repositorio.investir_mais.domain.auth.login.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.NonNull;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private final Cache<String, Integer> attemptsCache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();


    public void loginSucceeded(@NonNull String key) {
        attemptsCache.invalidate(key);
    }


    public void loginFailed(@NonNull String key) {
        int attempts = attemptsCache.get(key, k -> 0) + 1;
        attemptsCache.put(key, attempts);
    }


    public boolean isBlocked(@NonNull String key) {
        return attemptsCache.get(key, k -> 0) >= MAX_ATTEMPTS;
    }


    public int getAttempts(@NonNull String key) {
        Integer attempts = attemptsCache.getIfPresent(key);
        return attempts != null ? attempts : 0;
    }
}
