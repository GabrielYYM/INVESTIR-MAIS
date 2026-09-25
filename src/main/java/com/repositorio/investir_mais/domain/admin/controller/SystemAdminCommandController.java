package com.repositorio.investir_mais.domain.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.common.DTO.MessageResponseDTO;
import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.auth.token.service.TokenBlackListService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Comandos do Admin", description = "Endpoints de operações administrativas e infraestrutura")
public class SystemAdminCommandController {
    private final TokenBlackListService tokenBlackListService;

    @DeleteMapping("/tokens/expired")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Força a limpeza manual de tokens expirados",
            description = "Apaga imediatamente do banco de dados todos os tokens JWT que já passaram da validade.")
    public ResponseEntity<MessageResponseDTO> forceRemoveExpiredTokens() {
        ServiceResult<Void> result = tokenBlackListService.removeExpiredTokens();

        return switch (result) {
            case ServiceResult.Success<Void> _ -> ResponseEntity.ok(new MessageResponseDTO(MessageConstants.Admin.TOKEN_CLEANUP_SUCCESS));
            case ServiceResult.NotFound<Void> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Void> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }
}
