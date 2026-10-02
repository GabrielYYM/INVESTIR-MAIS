package com.repositorio.investir_mais.domain.auth.login.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;

import com.repositorio.investir_mais.infrastructure.exception.RateLimitExceededException;

import com.repositorio.investir_mais.common.DTO.MessageResponseDTO;
import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.auth.login.DTO.LoginRequestDTO;
import com.repositorio.investir_mais.domain.auth.token.DTO.TokenResponseDTO;
import com.repositorio.investir_mais.domain.auth.login.DTO.Verify2FARequestDTO;
import com.repositorio.investir_mais.domain.auth.login.service.LoginService;
import com.repositorio.investir_mais.domain.auth.security.service.RateLimitingService;
import com.repositorio.investir_mais.infrastructure.security.util.ClientIp;

import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação - Login", description = "Operações de login e 2FA")
public class LoginCommandController {
    private final LoginService loginService;
    private final RateLimitingService rateLimitingService;

    @PostMapping("/login")
    @Operation(summary = "Inicia o login e envia código 2FA", description = "Valida e-mail e senha e envia um código por e-mail.")
    @ApiResponse(responseCode = "200", description = "Código 2FA enviado")
    public ResponseEntity<MessageResponseDTO> login(@Valid @RequestBody @NonNull LoginRequestDTO loginRequest, @NonNull HttpServletRequest request) {
        String ip = ClientIp.getClientIp(request);
        Bucket bucket = rateLimitingService.resolveLoginBucket(ip);

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException(MessageConstants.Auth.ERR_RATELIMIT_EXCEEDED);
        }

        ServiceResult<Void> result = loginService.initiateLogin(loginRequest, ip);

        return switch (result) {
            case ServiceResult.Success<Void> _ -> ResponseEntity.ok(new MessageResponseDTO(MessageConstants.Auth.LOGIN_2FA_SENT));
            case ServiceResult.NotFound<Void> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Void> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @PostMapping("/verify-2fa")
    @ApiResponse(responseCode = "200", description = "Token JWT gerado com sucesso")
    public ResponseEntity<TokenResponseDTO> verify2FA(@Valid @RequestBody @NonNull Verify2FARequestDTO verifyRequest,
                                                      @NonNull HttpServletRequest request) {
        String ip = ClientIp.getClientIp(request);
        ServiceResult<String> result = loginService.verify2FAAndGenerateToken(verifyRequest, ip);

        return switch (result) {
            case ServiceResult.Success<String> s -> ResponseEntity.ok(new TokenResponseDTO(s.data()));
            case ServiceResult.NotFound<String> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<String> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }
}
