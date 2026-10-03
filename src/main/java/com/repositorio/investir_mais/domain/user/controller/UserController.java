package com.repositorio.investir_mais.domain.user.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.domain.user.DTO.ForgotPasswordRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.ResetPasswordRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UpdatePasswordDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO registerUser(@Valid @RequestBody UserRequestDTO dto) {
        return userService.registerUser(dto);
    }

    @PutMapping("/update-password")
    public Map<String, String> updatePassword(Principal principal, @Valid @RequestBody UpdatePasswordDTO dto) {
        userService.updatePassword(principal.getName(), dto);
        return Map.of("message", "Senha atualizada com sucesso!");
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        userService.forgotPassword(dto);
        return Map.of("message", "Se o e-mail existir na nossa base, um link de recuperação será enviado.");
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO dto) {
        userService.resetPassword(dto);
        return Map.of("message", "Senha redefinida com sucesso!");
    }
}