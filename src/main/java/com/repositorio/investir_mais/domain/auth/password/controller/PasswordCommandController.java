package com.repositorio.investir_mais.domain.auth.password.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;

import com.repositorio.investir_mais.common.DTO.MessageResponseDTO;
import com.repositorio.investir_mais.common.constants.LogMessageConstants;
import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.auth.password.DTO.ForgotPasswordRequestDTO;
import com.repositorio.investir_mais.domain.auth.password.DTO.ResetPasswordRequestDTO;
import com.repositorio.investir_mais.domain.auth.password.service.PasswordRecoveryService;
import com.repositorio.investir_mais.infrastructure.security.util.ClientIp;

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
@Tag(name = "Autenticação - Senha", description = "Recuperação e redefinição de senha")
public class PasswordCommandController {
    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicita recuperação de senha", description = "Gera token de redefinição e envia um link por e-mail.")
    @ApiResponse(responseCode = "200", description = "Token de recuperação enviado")
    public ResponseEntity<MessageResponseDTO> forgotPassword(@Valid @RequestBody @NonNull ForgotPasswordRequestDTO forgotPasswordRequestDTO,
                                                             @NonNull HttpServletRequest request) {
        String ip = ClientIp.getClientIp(request);

        ServiceResult<Void> result = passwordRecoveryService.initiatePasswordRecovery(
                forgotPasswordRequestDTO.email(),
                ip
        );

        return switch (result) {
            case ServiceResult.Success<Void> _ -> ResponseEntity.ok(new MessageResponseDTO(MessageConstants.Auth.FORGOT_PASSWORD_SENT));
            case ServiceResult.NotFound<Void> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Void> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefine senha", description = "Recebe token de redefinição e nova senha.")
    @ApiResponse(responseCode = "200", description = "Senha redefinida")
    public ResponseEntity<MessageResponseDTO> resetPassword(@Valid @RequestBody @NonNull ResetPasswordRequestDTO request,
                                                            @NonNull HttpServletRequest httpRequest) {
        String ip = ClientIp.getClientIp(httpRequest);

        ServiceResult<Void> result = passwordRecoveryService.resetPassword(
                request.token(),
                request.newPassword());

        return switch (result) {
            case ServiceResult.Success<Void> _ -> {
                log.info(LogMessageConstants.AUTH.PASSWORD_RESET_SUCCESS, ip);
                yield ResponseEntity.ok(new MessageResponseDTO(MessageConstants.Auth.PASSWORD_RESET_SUCCESS));
            }
            case ServiceResult.NotFound<Void> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Void> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }
}
