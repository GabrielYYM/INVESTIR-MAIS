package com.repositorio.investir_mais.domain.token.repository;

import com.repositorio.investir_mais.domain.token.model.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;

public interface TokenAccountRepository extends Repository<TokenAccount, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TokenAccount> findByEmail(String email);
}
