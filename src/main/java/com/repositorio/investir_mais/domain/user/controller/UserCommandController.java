package com.repositorio.investir_mais.domain.user.controller;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.common.DTO.MessageResponseDTO;
import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.auth.security.service.RateLimitingService;
import com.repositorio.investir_mais.domain.user.DTO.ResendVerificationRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserUpdateRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.VerifyRegistrationRequestDTO;
import com.repositorio.investir_mais.domain.user.service.interfaces.UserCommandService;
import com.repositorio.investir_mais.infrastructure.exception.RateLimitExceededException;
import com.repositorio.investir_mais.infrastructure.security.util.ClientIp;

import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Comandos de Usuário", description = "Operações de escrita para cadastro e gerenciamento de perfis de usuário")
public class UserCommandController {
    private final UserCommandService userCommandService;
    private final RateLimitingService rateLimitingService;

    @PostMapping
    @Operation(summary = "Cria um novo usuário", description = "Cria um novo usuário")
    @ApiResponse(responseCode = "201", description = "Usuário criado")
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody @NonNull UserRequestDTO userRequestDTO,
            HttpServletRequest request) {

        String ip = ClientIp.getClientIp(request);
        Bucket bucket = rateLimitingService.resolveRegistrationBucket(ip);

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException(MessageConstants.Auth.ERR_RATELIMIT_EXCEEDED);
        }

        ServiceResult<UserResponseDTO> result = userCommandService.createUser(userRequestDTO);

        return switch (result) {
            case ServiceResult.Success<UserResponseDTO> s -> ResponseEntity.status(HttpStatus.CREATED).body(s.data());
            case ServiceResult.NotFound<UserResponseDTO> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<UserResponseDTO> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @PostMapping("/verify-registration")
    @Operation(summary = "Verifica código de ativação do cadastro", description = "Valida os códigos enviados por e-mail (usuário e responsável se <12 anos) para ativar a conta")
    @ApiResponse(responseCode = "200", description = "Conta ativada com sucesso")
    public ResponseEntity<MessageResponseDTO> verifyRegistration(
            @Valid @RequestBody @NonNull VerifyRegistrationRequestDTO verifyRequest) {
        ServiceResult<Void> result = userCommandService.verifyRegistration(verifyRequest);

        return switch (result) {
            case ServiceResult.Success<Void> _ -> ResponseEntity.ok(new MessageResponseDTO(MessageConstants.Auth.REGISTRATION_ACTIVATED));
            case ServiceResult.NotFound<Void> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Void> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Reenvia códigos de ativação do cadastro", description = "Gera e reenvia novos códigos de verificação para o e-mail do usuário e do responsável")
    @ApiResponse(responseCode = "200", description = "Códigos reenviados com sucesso")
    public ResponseEntity<MessageResponseDTO> resendVerification(
            @Valid @RequestBody @NonNull ResendVerificationRequestDTO resendRequest,
            HttpServletRequest request) {
        String ip = ClientIp.getClientIp(request);
        Bucket bucket = rateLimitingService.resolveRegistrationBucket(ip);

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException(MessageConstants.Auth.ERR_RATELIMIT_EXCEEDED);
        }

        ServiceResult<Void> result = userCommandService.resendRegistrationVerification(resendRequest);

        return switch (result) {
            case ServiceResult.Success<Void> _ -> ResponseEntity.ok(new MessageResponseDTO(MessageConstants.Auth.REGISTRATION_VERIFICATION_SENT));
            case ServiceResult.NotFound<Void> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Void> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui permanentemente um usuário", description = "Exclui permanentemente um usuário pelo ID")
    @ApiResponse(responseCode = "204", description = "Usuário excluído")
    public ResponseEntity<Void> deleteUser(@PathVariable @NonNull UUID id) {
        ServiceResult<Void> result = userCommandService.deleteUserById(id);

        return switch (result) {
            case ServiceResult.Success<Void> _ -> ResponseEntity.noContent().build();
            case ServiceResult.NotFound<Void> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Void> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.principal.user.id.toString()")
    @Operation(summary = "Atualiza um usuário", description = "Atualiza um usuário pelo ID")
    @ApiResponse(responseCode = "200", description = "Usuário atualizado")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable @NonNull UUID id,
                                                      @Valid @RequestBody @NonNull UserUpdateRequestDTO userUpdateRequestDTO) {
        ServiceResult<UserResponseDTO> result = userCommandService.updateUserById(id, userUpdateRequestDTO);

        return switch (result) {
            case ServiceResult.Success<UserResponseDTO> s -> ResponseEntity.ok(s.data());
            case ServiceResult.NotFound<UserResponseDTO> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<UserResponseDTO> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }
}
