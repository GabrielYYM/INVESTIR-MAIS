package com.repositorio.investir_mais.domain.token.service;

import com.repositorio.investir_mais.domain.token.DTO.TokenDTO;
import com.repositorio.investir_mais.domain.token.mapper.TokenMapper;
import com.repositorio.investir_mais.domain.token.repository.LoginTokenRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ott.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginTokenService implements OneTimeTokenService {
    private final LoginTokenRepository tokens;
    private final TokenMapper mapper;

    @Override
    public OneTimeToken generate(GenerateOneTimeTokenRequest request) {
        Assert.notNull(request, "generateOneTimeTokenRequest cannot be null");
        var token = new TokenDTO.Issued(UUID.randomUUID().toString(), request.getUsername(),
                Instant.now().plus(request.getExpiresIn()));
        tokens.saveAndFlush(mapper.toLoginEntity(token));
        return mapper.toSecurityToken(token);
    }

    @Override
    public OneTimeToken consume(OneTimeTokenAuthenticationToken authenticationToken) {
        Assert.notNull(authenticationToken, "authenticationToken cannot be null");
        var token = tokens.findById(authenticationToken.getTokenValue());
        if (token.isEmpty() || tokens.consume(token.get().getTokenValue()) == 0) return null;
        if (Instant.now().isAfter(token.get().getExpiresAt())) return null;
        return mapper.toSecurityToken(mapper.toDTO(token.get()));
    }

    public void invalidateAll(String email) { tokens.deleteForUser(email); }
    public void cleanupExpired(Instant now) { tokens.deleteExpired(now); }
}
