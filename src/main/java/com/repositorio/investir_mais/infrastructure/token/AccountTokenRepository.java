package com.repositorio.investir_mais.infrastructure.token;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AccountToken> findByTokenValueAndPurposeAndExpiresAtAfter(String token, String purpose, Instant now);

    @Modifying(flushAutomatically = true)
    @Query("delete from AccountToken t where t.username = :email and t.purpose = :purpose")
    int invalidatePurpose(@Param("email") String email, @Param("purpose") String purpose);

    @Modifying(flushAutomatically = true)
    @Query("delete from AccountToken t where t.username = :email")
    int invalidateAll(@Param("email") String email);

    @Modifying(flushAutomatically = true)
    @Query("delete from AccountToken t where t.expiresAt <= :now")
    int deleteExpired(@Param("now") Instant now);

    boolean existsByTokenValueAndUsernameAndPurposeAndExpiresAtAfter(String tokenValue, String username, String purpose, Instant now);
}
