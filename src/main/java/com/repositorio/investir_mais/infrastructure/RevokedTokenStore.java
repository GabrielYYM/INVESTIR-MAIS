package com.repositorio.investir_mais.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RevokedTokenStore {

    @CachePut(value = "revokedTokens", key = "#tokenId", condition = "#tokenId != null")
    public boolean revoke(String tokenId) {
        log.info("Token adicionado à lista de revogados (blacklist). ID: {}", tokenId);
        return true;
    }

    @Cacheable(value = "revokedTokens", key = "#tokenId", condition = "#tokenId != null")
    public boolean isRevoked(String tokenId) {
        log.debug("Verificando status de revogação do token ID: {}", tokenId);
        return false;
    }
}