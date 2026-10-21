package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.AccountTokens;
import com.repositorio.investir_mais.infrastructure.token.AccountToken;
import com.repositorio.investir_mais.infrastructure.token.AccountTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class JpaAccountTokenService implements AccountTokens {
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    private static final String PASSWORD_RESET = "PASSWORD_RESET";
    private final AccountTokenRepository tokens;

    public OneTimeToken generateEmailVerificationToken(String email) {
        return generate(email, EMAIL_VERIFICATION);
    }

    public String generatePasswordResetToken(String email) {
        return generate(email, PASSWORD_RESET).getTokenValue();
    }

    public Optional<String> consumeEmailVerificationToken(String token) {
        return consume(token, EMAIL_VERIFICATION);
    }

    public Optional<String> consumePasswordResetToken(String token) {
        return consume(token, PASSWORD_RESET);
    }

    public void invalidateAll(String email) {
        tokens.invalidateAll(email);
    }

    private OneTimeToken generate(String email, String purpose) {
        tokens.deleteExpired(Instant.now());
        tokens.invalidatePurpose(email, purpose);
        var token = tokens.saveAndFlush(new AccountToken(UUID.randomUUID().toString(), email, Instant.now().plus(TTL), purpose));
        return new DefaultOneTimeToken(token.getTokenValue(), token.getUsername(), token.getExpiresAt());
    }

    private Optional<String> consume(String value, String purpose) {
        return tokens.findByTokenValueAndPurposeAndExpiresAtAfter(value, purpose, Instant.now()).map(token -> {
            tokens.delete(token);
            tokens.flush();
            return token.getUsername();
        });
    }
}
