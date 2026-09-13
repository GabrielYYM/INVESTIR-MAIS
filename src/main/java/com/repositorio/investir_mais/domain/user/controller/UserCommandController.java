package com.repositorio.investir_mais.domain.user.controller;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserUpdateRequestDTO;
import com.repositorio.investir_mais.domain.user.service.interfaces.UserCommandService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCommandController {
    private final UserCommandService userCommandService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody @NonNull UserRequestDTO userRequestDTO,
            HttpServletRequest request) {
        ServiceResult<UserResponseDTO> result = userCommandService.createUser(userRequestDTO);

        return switch (result) {
            case ServiceResult.Success<UserResponseDTO> s -> ResponseEntity.status(HttpStatus.CREATED).body(s.data());
            case ServiceResult.NotFound<UserResponseDTO> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND,
                    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<UserResponseDTO> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @NonNull UUID id) {
        ServiceResult<Void> result = userCommandService.deleteUserById(id);

        return switch (result) {
            case ServiceResult.Success<Void> _ -> ResponseEntity.noContent().build();
            case ServiceResult.NotFound<Void> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND,
                    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<Void> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody @NonNull UserUpdateRequestDTO userUpdateRequestDTO) {
        ServiceResult<UserResponseDTO> result = userCommandService.updateUserById(id, userUpdateRequestDTO);

        return switch (result) {
            case ServiceResult.Success<UserResponseDTO> s -> ResponseEntity.ok(s.data());
            case ServiceResult.NotFound<UserResponseDTO> n -> throw new ErrorResponseException(HttpStatus.NOT_FOUND,
                    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<UserResponseDTO> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }
}
