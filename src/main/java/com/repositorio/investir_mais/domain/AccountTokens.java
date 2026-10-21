package com.repositorio.investir_mais.domain;

import org.springframework.security.authentication.ott.OneTimeToken;
import java.util.Optional;

public interface AccountTokens {
    OneTimeToken generateEmailVerificationToken(String email);
    Optional<String> consumeEmailVerificationToken(String token);
    String generatePasswordResetToken(String email);
    Optional<String> consumePasswordResetToken(String token);
    void invalidateAll(String email);
}
