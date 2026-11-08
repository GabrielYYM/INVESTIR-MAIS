package com.repositorio.investir_mais.domain.user.controller;

import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.security.Principal;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de perfis, cadastros e redefinição de senhas")
public class UserController {

    private final UserService userService;
    private final String frontendOrigin;

    public UserController(UserService userService,
            @Value("${app.cors.allowed-origin:http://localhost:5173}") String frontendOrigin) {
        this.userService = userService;
        this.frontendOrigin = frontendOrigin;
    }

    @Operation(summary = "Cadastrar novo usuário", description = "Registra um novo usuário no sistema e envia um e-mail com o link de ativação.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Solicitação recebida"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida ou perfil diferente de STUDENT")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> registerUser(@Valid @RequestBody UserDTO.Request dto) {
        try {
            userService.registerUser(dto);
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() != HttpStatus.CONFLICT) throw exception;
        } catch (DataIntegrityViolationException exception) {
            if (!(exception.getMostSpecificCause() instanceof SQLException sql) || !"23505".equals(sql.getSQLState())) {
                throw exception;
            }
        }
        return Map.of("message", "Se o cadastro puder ser realizado, um link de confirmação será enviado.");
    }

    @Operation(summary = "Abrir a confirmação de e-mail")
    @GetMapping(value = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> emailVerificationForm(@RequestParam String token, CsrfToken csrf,
            HttpServletRequest request) {
        if (token.length() != 36) {
            return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body("Link inválido.");
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("Referrer-Policy", "strict-origin")
                .header("Content-Security-Policy", "default-src 'none'; form-action 'self'; frame-ancestors 'none'; base-uri 'none'")
                .body("""
                    <!doctype html>
                    <html lang="pt-BR">
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Confirmação de e-mail · Investir Mais</title>
                    <main>
                      <h1>Confirme seu e-mail</h1>
                      <p>Selecione o botão para ativar sua conta no Investir Mais.</p>
                      <form method="post" action="%s/api/users/verify-email">
                        <input type="hidden" name="token" value="%s">
                        <input type="hidden" name="%s" value="%s">
                        <button>Confirmar e-mail</button>
                      </form>
                    </main>
                    </html>
                    """.formatted(HtmlUtils.htmlEscape(request.getContextPath()), HtmlUtils.htmlEscape(token),
                            HtmlUtils.htmlEscape(csrf.getParameterName()), HtmlUtils.htmlEscape(csrf.getToken())));
    }

    @Operation(summary = "Verificar e-mail", description = "Consome o token após a confirmação do usuário.")
    @PostMapping(value = "/verify-email", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> verifyEmail(@RequestParam String token,
            @RequestHeader(value = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String accept) {
        boolean browser = MediaType.parseMediaTypes(accept).stream()
                .anyMatch(type -> !type.isWildcardType() && !type.isWildcardSubtype()
                        && type.isCompatibleWith(MediaType.TEXT_HTML) && type.getQualityValue() > 0);
        try {
            userService.verifyEmail(token);
        } catch (ResponseStatusException exception) {
            if (!browser || exception.getStatusCode() != HttpStatus.BAD_REQUEST) throw exception;
            return emailVerificationResult(HttpStatus.BAD_REQUEST, "Link inválido ou expirado",
                    "Confira se você recebeu um link mais recente ou solicite uma nova confirmação pelo cadastro.");
        }
        if (browser) {
            return emailVerificationResult(HttpStatus.OK, "E-mail confirmado",
                    "Sua conta foi ativada. Entre com seu e-mail e senha para continuar.");
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(Map.of("message", "E-mail verificado com sucesso."));
    }

    private ResponseEntity<String> emailVerificationResult(HttpStatus status, String title, String message) {
        String loginUrl = UriComponentsBuilder.fromUriString(frontendOrigin)
                .path("/entrar").build().toUriString();
        return ResponseEntity.status(status).contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .cacheControl(CacheControl.noStore())
                .header("Referrer-Policy", "no-referrer")
                .header("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; base-uri 'none'")
                .body("""
                    <!doctype html>
                    <html lang="pt-BR">
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>%s · Investir Mais</title>
                    <main>
                      <h1>%s</h1>
                      <p>%s</p>
                      <a href="%s">Ir para entrar</a>
                    </main>
                    </html>
                    """.formatted(HtmlUtils.htmlEscape(title), HtmlUtils.htmlEscape(title),
                            HtmlUtils.htmlEscape(message), HtmlUtils.htmlEscape(loginUrl)));
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
        @ApiResponse(responseCode = "401", description = "Senha atual incorreta"),
        @ApiResponse(responseCode = "403", description = "Acesso negado para atualizar perfis de terceiros"),
        @ApiResponse(responseCode = "409", description = "Novo e-mail informado já está em uso")
    })
    @PutMapping("/{id}")
    public UserDTO.Response updateProfile(Principal principal, @PathVariable UUID id, @Valid @RequestBody UserDTO.UpdateProfileRequest dto) {
        return userService.updateProfile(principal.getName(), id, dto);
    }

    @Operation(summary = "Excluir a própria conta", description = "Exclui logicamente o usuário e seus cursos e aulas, preservando os registros. Exige a senha atual e encerra todas as sessões da conta.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Conta excluída logicamente"),
        @ApiResponse(responseCode = "400", description = "Senha de confirmação ausente"),
        @ApiResponse(responseCode = "401", description = "Senha atual incorreta ou usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado para excluir outra conta"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Principal principal, @PathVariable UUID id, @Valid @RequestBody UserDTO.DeleteAccountRequest dto) {
        userService.deleteAccount(principal.getName(), id, dto);
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
