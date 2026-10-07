package com.repositorio.investir_mais.domain.user.controller;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.service.UserService;
import com.repositorio.investir_mais.infrastructure.EmailOttHandler;
import com.repositorio.investir_mais.infrastructure.RevokedTokenStore;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final OneTimeTokenService oneTimeTokenService;
    private final EmailOttHandler emailOttHandler;
    private final UserService userService;
    private final JwtEncoder jwtEncoder;
    private final RevokedTokenStore revokedTokenStore;

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        OneTimeToken token = oneTimeTokenService.generate(new GenerateOneTimeTokenRequest(request.email()));
        emailOttHandler.sendTwoFactorCode(request.email(), token.getTokenValue());
        return Map.of("message", "Código de verificação enviado para o e-mail cadastrado.");
    }

    @PostMapping("/verify-2fa")
    public Map<String, String> verifyTwoFactor(@Valid @RequestBody AuthDTO.VerifyTwoFactorRequest request) {
        OneTimeToken token = oneTimeTokenService.consume(new OneTimeTokenAuthenticationToken(request.code()));
        if (token == null || !token.getUsername().equalsIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código inválido ou expirado.");
        }

        User user = userService.findByEmail(token.getUsername());
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("investir-mais")
            .subject(user.getId().toString())
            .claim("email", user.getUserSecurity().getEmail())
            .claim("roles", user.getUserSecurity().getRole().name())
            .id(UUID.randomUUID().toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .build();
        String jwt = jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
        return Map.of("token", jwt);
    }

    @PostMapping("/logout")
    public org.springframework.http.ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        revokedTokenStore.revoke(jwt.getId());
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}