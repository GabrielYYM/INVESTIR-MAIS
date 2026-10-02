package com.repositorio.investir_mais.domain.user.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.service.interfaces.UserQueryService;
import com.repositorio.investir_mais.infrastructure.security.UserDetailsImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Consultas de Usuário", description = "Operações de leitura para busca e listagem de usuarios")
public class UserQueryController {
    private final UserQueryService userQueryService;
    private final com.repositorio.investir_mais.domain.user.mapper.UserMapper userMapper;

    @GetMapping("/me")
    @Operation(summary = "Retorna o usuário autenticado", description = "Retorna os dados básicos e a role do usuário associado ao token JWT.")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(userMapper.toUserResponseDTO(principal.getUser()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista os usuarios com paginação", description = "Retorna uma página de usuários")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(@PageableDefault(size = 20) @NonNull Pageable pageable) {
        ServiceResult<Page<UserResponseDTO>> result = userQueryService.listAllUsers(pageable);

        return switch (result) {
            case ServiceResult.Success<Page<UserResponseDTO>> s -> ResponseEntity.ok(s.data());
            case ServiceResult.NotFound<Page<UserResponseDTO>> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Page<UserResponseDTO>> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.principal.user.id.toString()")
    @Operation(summary = "Busca um usuario pelo id", description = "Busca um usuário pelo ID")
    public ResponseEntity<UserResponseDTO> findUserByID(@PathVariable @NonNull UUID id) {
        ServiceResult<UserResponseDTO> result = userQueryService.findUserById(id);

        return switch (result) {
            case ServiceResult.Success<UserResponseDTO> s -> ResponseEntity.ok(s.data());
            case ServiceResult.NotFound<UserResponseDTO> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<UserResponseDTO> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }
}
