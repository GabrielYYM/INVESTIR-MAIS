package com.repositorio.investir_mais.domain.user.controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO.Request;
import com.repositorio.investir_mais.domain.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping({"", "/register"})
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO.Response registerUser(@Valid @RequestBody Request dto) {
        return userService.registerUser(dto);
    }

    @GetMapping("/{id}")
    public UserDTO.Response getProfile(Principal principal, @PathVariable UUID id) {
        return userService.getProfile(principal.getName(), id);
    }

    @PutMapping("/{id}")
    public UserDTO.Response updateProfile(Principal principal, @PathVariable UUID id,
            @Valid @RequestBody UserDTO.UpdateProfileRequest dto) {
        return userService.updateProfile(principal.getName(), id, dto);
    }

    @PutMapping("/update-password")
    public Map<String, String> updatePassword(Principal principal, @Valid @RequestBody AuthDTO.UpdatePasswordRequest dto) {
        userService.updatePassword(principal.getName(), dto);
        return Map.of("message", "Senha atualizada com sucesso!");
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> forgotPassword(@Valid @RequestBody AuthDTO.ForgotPasswordRequest dto) {
        userService.forgotPassword(dto);
        return Map.of("message", "Se o e-mail existir na nossa base, um link de recuperação será enviado.");
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> resetPassword(@Valid @RequestBody AuthDTO.ResetPasswordRequest dto) {
        userService.resetPassword(dto);
        return Map.of("message", "Senha redefinida com sucesso!");
    }
}