package com.repositorio.investir_mais.domain.user.repository;

import com.repositorio.investir_mais.domain.user.model.AccountDeletion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AccountDeletionRepository extends JpaRepository<AccountDeletion, UUID> {
    Optional<AccountDeletion> findByUserIdAndStatus(UUID userId, String status);
}
