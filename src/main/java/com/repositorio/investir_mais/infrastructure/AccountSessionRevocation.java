package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.AccountSessions;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountSessionRevocation implements AccountSessions {
    private final FindByIndexNameSessionRepository<?> sessions;
    private final JdbcTemplate jdbc;

    @Override
    public void revokeAll(String email) {
        sessions.findByPrincipalName(email).keySet().forEach(sessions::deleteById);
        jdbc.update("DELETE FROM one_time_tokens WHERE username = ?", email);
    }
}
