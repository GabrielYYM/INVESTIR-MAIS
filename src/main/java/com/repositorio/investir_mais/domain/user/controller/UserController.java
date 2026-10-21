package com.repositorio.investir_mais.domain.user.controller;

import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de perfis, cadastros e redefinição de senhas")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Cadastrar novo usuário", description = "Registra um novo usuário no sistema e envia um e-mail com o link de ativação.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida ou perfil diferente de STUDENT"),
        @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO.Response registerUser(@Valid @RequestBody UserDTO.Request dto) {
        return userService.registerUser(dto);
    }

    @Operation(summary = "Verificar e-mail", description = "Consome o link de verificação enviado ao endereço cadastrado.")
    @GetMapping("/verify-email")
    public Map<String, String> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return Map.of("message", "E-mail verificado com sucesso.");
    }

    @PostMapping("/resend-verification")
    public Map<String, String> resendVerification(@Valid @RequestBody AuthDTO.ForgotPasswordRequest dto) {
        userService.resendVerification(dto.email());
        return Map.of("message", "Se houver uma conta pendente para este e-mail, uma nova confirmação será enviada.");
    }

    @Operation(summary = "Obter perfil do usuário por ID", description = "Retorna os detalhes do perfil do usuário autenticado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado para perfis de outros usuários"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public UserDTO.Response getProfile(Principal principal, @PathVariable UUID id) {
        return userService.getProfile(principal.getName(), id);
    }

    @Operation(summary = "Atualizar dados do perfil", description = "Atualiza nome, idade ou e-mail do usuário autenticado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado para atualizar perfis de terceiros"),
        @ApiResponse(responseCode = "409", description = "Novo e-mail informado já está em uso")
    })
    @PutMapping("/{id}")
    public UserDTO.Response updateProfile(Principal principal, @PathVariable UUID id, @Valid @RequestBody UserDTO.UpdateProfileRequest dto) {
        return userService.updateProfile(principal.getName(), id, dto);
    }

    @Operation(summary = "Atualizar senha do usuário logado", description = "Permite alterar a senha mediante confirmação da senha atual.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Senha atual incorreta")
    })
    @PutMapping("/update-password")
    public Map<String, String> updatePassword(Principal principal, @Valid @RequestBody AuthDTO.UpdatePasswordRequest dto) {
        userService.updatePassword(principal.getName(), dto);
        return Map.of("message", "Senha atualizada com sucesso!");
    }

    @Operation(summary = "Solicitar link de recuperação de senha", description = "Envia um link/token de acesso único caso o e-mail exista no sistema.")
    @ApiResponse(responseCode = "200", description = "Solicitação processada com sucesso")
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody AuthDTO.ForgotPasswordRequest dto) {
        userService.forgotPassword(dto);
        return Map.of("message", "Se o e-mail existir na nossa base, um link de recuperação será enviado.");
    }

    @Operation(summary = "Redefinir senha via token OTT", description = "Consome o token de acesso único recebido por e-mail para cadastrar uma nova senha.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Senha redefinida e e-mail validado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody AuthDTO.ResetPasswordRequest dto) {
        userService.resetPassword(dto);
        return Map.of("message", "Senha redefinida com sucesso!");
    }
}
