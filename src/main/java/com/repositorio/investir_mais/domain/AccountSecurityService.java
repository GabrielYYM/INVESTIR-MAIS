package com.repositorio.investir_mais.domain;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountSecurityService {
    private static final Duration TOKEN_TTL = Duration.ofMinutes(5);
    private static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    private static final String PASSWORD_RESET = "PASSWORD_RESET";
    private static final Pattern TOKEN_FORMAT = Pattern.compile("[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}");

    private final JdbcClient jdbc;
    private final FindByIndexNameSessionRepository<?> sessions;
    private Duration emailCooldown;

    @Value("${app.security.email-cooldown:30s}")
    void setEmailCooldown(String value) {
        emailCooldown = DurationStyle.detectAndParse(value);
        if (emailCooldown.isNegative() || emailCooldown.compareTo(TOKEN_TTL) > 0) {
            throw new IllegalArgumentException("O intervalo de reenvio deve estar entre zero e cinco minutos.");
        }
    }

    public OneTimeToken generateEmailVerificationToken(String email) {
        return generate(email, EMAIL_VERIFICATION, Duration.ZERO).orElseThrow();
    }

    public String generatePasswordResetToken(String email) {
        return generate(email, PASSWORD_RESET, Duration.ZERO).orElseThrow().getTokenValue();
    }

    public Optional<OneTimeToken> requestEmailVerificationToken(String email) {
        return generate(email, EMAIL_VERIFICATION, emailCooldown);
    }

    public Optional<String> requestPasswordResetToken(String email) {
        return generate(email, PASSWORD_RESET, emailCooldown).map(OneTimeToken::getTokenValue);
    }

    public Optional<String> consumeEmailVerificationToken(String token) {
        return consume(token, EMAIL_VERIFICATION);
    }

    public Optional<String> consumePasswordResetToken(String token) {
        return consume(token, PASSWORD_RESET);
    }

    public void invalidateAll(String email) {
        jdbc.sql("DELETE FROM app_one_time_token WHERE username = ?").param(email).update();
    }

    public void revokeAllSessions(String email) {
        jdbc.sql("DELETE FROM one_time_tokens WHERE username = ?").param(email).update();
        sessions.findByPrincipalName(email).keySet().forEach(sessions::deleteById);
    }

    @Scheduled(cron = "${app.security.token-cleanup-cron:0 0 * * * *}")
    public void cleanupExpiredTokens() {
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.sql("DELETE FROM app_one_time_token WHERE expires_at <= ?").param(now).update();
        jdbc.sql("DELETE FROM one_time_tokens WHERE expires_at <= ?").param(now).update();
    }

    @Transactional(readOnly = true)
    public boolean isTokenActive(AccountEmailRequested event) {
        String purpose = switch (event.purpose()) {
            case VERIFY_EMAIL -> EMAIL_VERIFICATION;
            case PASSWORD_RESET -> PASSWORD_RESET;
        };
        return jdbc.sql("""
            SELECT EXISTS (
                SELECT 1 FROM app_one_time_token
                WHERE token_value IN (?, ?) AND username = ? AND purpose = ? AND expires_at > ?
            )
            """).params(TokenDigest.sha256(event.token()), event.token(), event.email(), purpose, Timestamp.from(Instant.now()))
                .query(Boolean.class).single();
    }

    private Optional<OneTimeToken> generate(String email, String purpose, Duration cooldown) {
        lockAccount(email);
        Instant now = Instant.now();
        if (!cooldown.isZero() && jdbc.sql("""
                SELECT EXISTS (SELECT 1 FROM app_one_time_token WHERE username = ? AND purpose = ? AND expires_at > ?)
                """).params(email, purpose, Timestamp.from(now.plus(TOKEN_TTL).minus(cooldown))).query(Boolean.class).single()) {
            return Optional.empty();
        }
        jdbc.sql("DELETE FROM app_one_time_token WHERE username = ? AND purpose = ?").params(email, purpose).update();
        String value = UUID.randomUUID().toString();
        Instant expiresAt = now.plus(TOKEN_TTL);
        jdbc.sql("""
            INSERT INTO app_one_time_token (token_value, username, expires_at, purpose)
            VALUES (?, ?, ?, ?)
            """).params(TokenDigest.sha256(value), email, Timestamp.from(expiresAt), purpose).update();
        return Optional.of(new DefaultOneTimeToken(value, email, expiresAt));
    }

    private Optional<String> consume(String token, String purpose) {
        if (!TOKEN_FORMAT.matcher(token).matches()) {
            return Optional.empty();
        }
        // Raw UUIDs from before the migration remain usable until their original expiry.
        String digest = TokenDigest.sha256(token);
        var owner = jdbc.sql("""
            SELECT username FROM app_one_time_token
            WHERE token_value IN (?, ?) AND purpose = ?
            """).params(digest, token, purpose).query(String.class).optional();
        if (owner.isEmpty()) {
            return Optional.empty();
        }
        lockAccount(owner.get());
        var email = jdbc.sql("""
            SELECT username FROM app_one_time_token
            WHERE token_value IN (?, ?) AND purpose = ? AND expires_at > ?
            FOR UPDATE
            """).params(digest, token, purpose, Timestamp.from(Instant.now())).query(String.class).optional();
        if (email.isPresent()) {
            jdbc.sql("DELETE FROM app_one_time_token WHERE token_value IN (?, ?) AND purpose = ?").params(digest, token, purpose).update();
        }
        return email;
    }

    private void lockAccount(String email) {
        jdbc.sql("SELECT id FROM tb_user WHERE email = ? FOR UPDATE").param(email).query(UUID.class).list();
    }
}
