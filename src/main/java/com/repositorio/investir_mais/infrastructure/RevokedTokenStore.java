package com.repositorio.investir_mais.infrastructure;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class RevokedTokenStore {
    @CachePut(value = "revokedTokens", key = "#tokenId", condition = "#tokenId != null")
    public boolean revoke(String tokenId) {
        return true;
    }

    @Cacheable(value = "revokedTokens", key = "#tokenId", condition = "#tokenId != null")
    public boolean isRevoked(String tokenId) {
        return false;
    }
}