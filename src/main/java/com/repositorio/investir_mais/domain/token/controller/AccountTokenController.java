package com.repositorio.investir_mais.domain.token.controller;

import com.repositorio.investir_mais.domain.token.DTO.TokenDTO;
import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de perfis, cadastros e redefinição de senhas")
public class AccountTokenController {
    // Preserve the session attribute used by registrations already in progress.
    private static final String PENDING_REGISTRATION_TOKEN = "com.repositorio.investir_mais.domain.user.controller.UserController.pendingRegistrationToken";
    private final UserService userService;
    private final CsrfTokenRepository csrfTokens;
    private final HttpSessionSecurityContextRepository securityContexts = new HttpSessionSecurityContextRepository();

    @Operation(summary = "Cadastrar novo usuário", description = "Registra um novo usuário no sistema e envia um e-mail com o link de ativação.")
    @PostMapping("/users/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TokenDTO.MessageResponse registerUser(@Valid @RequestBody UserDTO.Request dto, HttpServletRequest request) {

        var session = request.getSession();
        try {
            var token = userService.registerUser(dto);
            session.setAttribute(PENDING_REGISTRATION_TOKEN, DigestUtils.sha256Hex(token.getTokenValue()));
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() != HttpStatus.CONFLICT) throw exception;
        } catch (DataIntegrityViolationException exception) {
            if (!(exception.getMostSpecificCause() instanceof SQLException sql) || !"23505".equals(sql.getSQLState())) {
                throw exception;
            }
        }
        return new TokenDTO.MessageResponse("Se o cadastro puder ser realizado, um link de confirmação será enviado.");
    }

    @Operation(summary = "Verificar e-mail", description = "Consome o token após a confirmação do usuário.")
    @PostMapping(value = "/users/verify-email", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenDTO.VerificationResponse verifyEmail(@RequestParam String token, HttpServletRequest request, HttpServletResponse response) {
        var user = userService.verifyEmail(token);
        boolean signedIn = completeRegistrationLogin(user, token, request, response);
        return new TokenDTO.VerificationResponse("E-mail verificado com sucesso.", signedIn);
    }

    private boolean completeRegistrationLogin(UserDTO.Response user, String token, HttpServletRequest request,
            HttpServletResponse response) {
        var session = request.getSession(false);
        if (session == null || !DigestUtils.sha256Hex(token).equals(session.getAttribute(PENDING_REGISTRATION_TOKEN))) {
            return false;
        }
        session.removeAttribute(PENDING_REGISTRATION_TOKEN);
        if (request.getUserPrincipal() != null || user.role() != UserRole.STUDENT) {
            return false;
        }

        var authentication = UsernamePasswordAuthenticationToken.authenticated(user.email(), null, List.of(
                new SimpleGrantedAuthority("ROLE_STUDENT"),
                FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY),
                FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.OTT_AUTHORITY)));
        request.changeSessionId();
        new CsrfAuthenticationStrategy(csrfTokens).onAuthentication(authentication, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContexts.saveContext(context, request, response);
        return true;
    }

    @PostMapping("/users/resend-verification")
    public TokenDTO.MessageResponse resendVerification(@Valid @RequestBody AuthDTO.ForgotPasswordRequest dto) {
        userService.resendVerification(dto.email());
        return new TokenDTO.MessageResponse("Se houver uma conta pendente para este e-mail, uma nova confirmação será enviada.");
    }

    @Operation(summary = "Solicitar link de recuperação de senha", description = "Envia um link/token de acesso único caso o e-mail exista no sistema.")
    @PostMapping("/users/forgot-password")
    public TokenDTO.MessageResponse forgotPassword(@Valid @RequestBody AuthDTO.ForgotPasswordRequest dto) {
        userService.forgotPassword(dto);
        return new TokenDTO.MessageResponse("Se o e-mail existir na nossa base, um link de recuperação será enviado.");
    }

    @Operation(summary = "Redefinir senha via token OTT", description = "Consome o token de acesso único recebido por e-mail para cadastrar uma nova senha.")
    @PostMapping("/users/reset-password")
    public TokenDTO.MessageResponse resetPassword(@Valid @RequestBody AuthDTO.ResetPasswordRequest dto) {
        userService.resetPassword(dto);
        return new TokenDTO.MessageResponse("Senha redefinida com sucesso!");
    }

}
