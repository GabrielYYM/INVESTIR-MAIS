package com.repositorio.investir_mais.domain.token.repository;

import com.repositorio.investir_mais.domain.token.model.*;
import java.time.Instant;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;

public interface AccountTokenRepository extends JpaRepository<AccountToken,String> {
    boolean existsByTokenValueAndUsernameAndPurposeAndExpiresAtAfter(String tokenValue, String username, String purpose, Instant now);
    boolean existsByUsernameAndPurposeAndExpiresAtAfter(String username, String purpose, Instant threshold);

    @Query("select t.username from AccountToken t where t.tokenValue=:digest and t.purpose=:purpose")
    Optional<String> findOwner(String digest, String purpose);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AccountToken> findByTokenValueAndPurposeAndExpiresAtAfter(String tokenValue, String purpose, Instant now);

    @Modifying(flushAutomatically=true)
    @Query("delete from AccountToken t where t.username=:username")
    void deleteForUser(String username);

    @Modifying(flushAutomatically=true)
    @Query("delete from AccountToken t where t.username=:username and t.purpose=:purpose")
    void deleteForPurpose(String username, String purpose);

    @Modifying(flushAutomatically=true)
    @Query("delete from AccountToken t where t.expiresAt<=:now")
    void deleteExpired(Instant now);
}
