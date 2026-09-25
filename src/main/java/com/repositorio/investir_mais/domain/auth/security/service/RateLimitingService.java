package com.repositorio.investir_mais.domain.auth.security.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.NonNull;


@Service
public class RateLimitingService {
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(10000)
            .build();

    private final Cache<String, Boolean> bannedIps = Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(5000)
            .build();


    public Bucket resolveBucket(@NonNull String ip) {
        return cache.get(ip, this::newBucket);
    }


    public Bucket resolveRegistrationBucket(@NonNull String ip) {
        return cache.get("REG_" + ip, _ -> Bucket.builder()
                .addLimit(Bandwidth.classic(3, Refill.greedy(3, Duration.ofHours(1))))
                .build());
    }


    public Bucket resolveLoginBucket(@NonNull String ip) {
        return cache.get("LOGIN_" + ip, _ -> Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(15))))
                .build());
    }


    public void banIp(@NonNull String ip) {
        bannedIps.put(ip, Boolean.TRUE);
    }


    public boolean isBanned(@NonNull String ip) {
        return bannedIps.getIfPresent(ip) != null;
    }


    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(20,
                Refill.greedy(
                        20,
                        Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
