package com.repositorio.investir_mais.domain.user.controller;

import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.service.UserService;
import com.repositorio.investir_mais.infrastructure.EmailOttHandler;
import com.repositorio.investir_mais.infrastructure.RevokedTokenStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticação", description = "Endpoints para Login, 2FA e Logout")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final OneTimeTokenService oneTimeTokenService;
    private final EmailOttHandler emailOttHandler;
    private final UserService userService;
    private final JwtEncoder jwtEncoder;
    private final RevokedTokenStore revokedTokenStore;

    @Operation(summary = "Informa credenciais de login e solicita código 2FA", description = "Valida e-mail e senha, gerando um código 2FA enviado por e-mail.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Código 2FA gerado e enviado por e-mail"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        log.info("Tentativa de login iniciada para o e-mail: {}", request.email());
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        OneTimeToken token = oneTimeTokenService.generate(new GenerateOneTimeTokenRequest(request.email()));
        emailOttHandler.sendTwoFactorCode(request.email(), token.getTokenValue());
        
        log.info("Autenticação primária concluída com sucesso. Código 2FA enviado para: {}", request.email());
        return Map.of("message", "Código de verificação enviado para o e-mail cadastrado.");
    }

    @Operation(summary = "Valida código 2FA e gera o Token JWT", description = "Consome o código temporário recebido no e-mail e gera o Bearer JWT para autenticação das requisições.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticação 2FA bem-sucedida, retorna o JWT"),
        @ApiResponse(responseCode = "401", description = "Código 2FA inválido ou expirado")
    })
    @PostMapping("/verify-2fa")
    public Map<String, String> verifyTwoFactor(@Valid @RequestBody AuthDTO.VerifyTwoFactorRequest request) {
        log.info("Verificando código 2FA para o e-mail: {}", request.email());
        OneTimeToken token = oneTimeTokenService.consume(new OneTimeTokenAuthenticationToken(request.code()));
        
        if (token == null || !token.getUsername().equalsIgnoreCase(request.email())) {
            log.warn("Falha na validação do código 2FA para o e-mail: {}", request.email());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código inválido ou expirado.");
        }

        User user = userService.findByEmail(token.getUsername());
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("investir-mais")
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("roles", user.getRole().name())
            .id(UUID.randomUUID().toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .build();

        String jwt = jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();

        log.info("Token JWT gerado com sucesso para o usuário: {}", user.getId());
        return Map.of("token", jwt);
    }

    @Operation(summary = "Encerra a sessão e revoga o Token JWT", description = "Adiciona o ID do JWT atual na lista de tokens revogados.")
    @ApiResponse(responseCode = "24", description = "Logout efetuado com sucesso")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        log.info("Revogando token JWT ID: {} do usuário: {}", jwt.getId(), jwt.getSubject());
        revokedTokenStore.revoke(jwt.getId());
        return ResponseEntity.noContent().build();
    }
}