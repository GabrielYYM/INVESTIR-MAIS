package com.repositorio.investir_mais.domain.auth.token.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.repositorio.investir_mais.domain.auth.token.model.InvalidToken;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken, String>{
    void deleteByExpiresAtBefore(Instant now);
}
