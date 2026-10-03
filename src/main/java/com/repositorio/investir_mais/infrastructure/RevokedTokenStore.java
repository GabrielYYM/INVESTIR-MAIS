package com.repositorio.investir_mais.infrastructure;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class RevokedTokenStore {
    private final ConcurrentHashMap<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String tokenId, Instant expiresAt) {
        if (tokenId != null && expiresAt != null && expiresAt.isAfter(Instant.now())) {
            revokedTokens.put(tokenId, expiresAt);
        }
    }

    public boolean isRevoked(String tokenId) {
        if (tokenId == null) return false;
        Instant expiresAt = revokedTokens.get(tokenId);
        if (expiresAt == null) return false;
        if (!expiresAt.isAfter(Instant.now())) {
            revokedTokens.remove(tokenId, expiresAt);
            return false;
        }
        return true;
    }
}
