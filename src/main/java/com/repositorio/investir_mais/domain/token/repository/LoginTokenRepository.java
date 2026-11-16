package com.repositorio.investir_mais.domain.token.repository;

import com.repositorio.investir_mais.domain.token.model.*;
import java.time.Instant;
import org.springframework.data.jpa.repository.*;

public interface LoginTokenRepository extends JpaRepository<LoginToken,String> {
    @Modifying(flushAutomatically=true)
    @Query("delete from LoginToken t where t.tokenValue=:tokenValue")
    int consume(String tokenValue);

    @Modifying
    @Query("delete from LoginToken t where t.username=:username")
    void deleteForUser(String username);

    @Modifying
    @Query("delete from LoginToken t where t.expiresAt<=:now")
    void deleteExpired(Instant now);
}
