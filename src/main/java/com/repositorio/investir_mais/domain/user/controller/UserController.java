package com.repositorio.investir_mais.domain.user.controller;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.service.UserService;
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
<<<<<<< HEAD
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
=======
>>>>>>> origin/dev
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
<<<<<<< HEAD
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

=======
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
>>>>>>> origin/dev
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.security.Principal;
import java.sql.SQLException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de perfis, cadastros e redefinição de senhas")
public class UserController {
    private static final String PENDING_REGISTRATION_TOKEN = UserController.class.getName() + ".pendingRegistrationToken";

    private final UserService userService;
<<<<<<< HEAD
    private final CourseService courseService;
    private final CsrfTokenRepository csrfTokens;
    private final HttpSessionSecurityContextRepository securityContexts = new HttpSessionSecurityContextRepository();
=======
    private final WebApplicationProperties properties;
>>>>>>> origin/dev

    @Operation(summary = "Cadastrar novo usuário", description = "Registra um novo usuário no sistema e envia um e-mail com o link de ativação.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Solicitação recebida"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida ou perfil diferente de STUDENT")
    })
    @PostMapping("/users/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> registerUser(@Valid @RequestBody UserDTO.Request dto, HttpServletRequest request) {
        // Create a session even on a duplicate so the cookie does not reveal which accounts exist.
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
        return Map.of("message", "Se o cadastro puder ser realizado, um link de confirmação será enviado.");
    }

<<<<<<< HEAD
=======
    @Operation(summary = "Redirecionar para confirmação no frontend", description = "Redireciona para a página React de confirmação de e-mail.")
    @GetMapping("/verify-email")
    public ResponseEntity<Void> redirectToFrontendVerification(@RequestParam(required = false) String token) {
        String destination = properties.frontendUrl() + "/confirmar-email"
                + (token != null && !token.isBlank() ? "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8) : "");
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(destination))
                .build();
    }

>>>>>>> origin/dev
    @Operation(summary = "Verificar e-mail", description = "Consome o token após a confirmação do usuário.")
    @PostMapping(value = "/users/verify-email", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Map<String, Object> verifyEmail(@RequestParam String token, HttpServletRequest request, HttpServletResponse response) {
        var user = userService.verifyEmail(token);
        boolean signedIn = completeRegistrationLogin(user, token, request, response);
        return Map.of("message", "E-mail verificado com sucesso.", "signedIn", signedIn);
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
    @GetMapping("/users/{id}")
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
    @PutMapping("/users/{id}")
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
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Principal principal, @PathVariable UUID id, @Valid @RequestBody UserDTO.DeleteAccountRequest dto) {
        userService.deleteAccount(principal.getName(), id, dto);
    }

    @Operation(summary = "Atualizar senha do usuário logado", description = "Permite alterar a senha mediante confirmação da senha atual.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Senha atual incorreta")
    })
    @PutMapping("/users/update-password")
    public Map<String, String> updatePassword(Principal principal, @Valid @RequestBody AuthDTO.UpdatePasswordRequest dto) {
        userService.updatePassword(principal.getName(), dto);
        return Map.of("message", "Senha atualizada com sucesso!");
    }

    @Operation(summary = "Solicitar link de recuperação de senha", description = "Envia um link/token de acesso único caso o e-mail exista no sistema.")
    @ApiResponse(responseCode = "200", description = "Solicitação processada com sucesso")
    @PostMapping("/users/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody AuthDTO.ForgotPasswordRequest dto) {
        userService.forgotPassword(dto);
        return Map.of("message", "Se o e-mail existir na nossa base, um link de recuperação será enviado.");
    }

    @Operation(summary = "Redefinir senha via token OTT", description = "Consome o token de acesso único recebido por e-mail para cadastrar uma nova senha.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Senha redefinida e e-mail validado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    @PostMapping("/users/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody AuthDTO.ResetPasswordRequest dto) {
        userService.resetPassword(dto);
        return Map.of("message", "Senha redefinida com sucesso!");
    }

    @Operation(summary = "Listar todos os usuários", description = "Retorna a lista completa de usuários cadastrados no sistema. Exige perfil ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado para perfis diferentes de ADMIN")
    })
<<<<<<< HEAD
    @GetMapping("/users")
=======
    @GetMapping
>>>>>>> origin/dev
    public List<UserDTO.Response> listAllUsers() {
        return userService.listAllUsers();
    }

    @Operation(summary = "Alterar perfil (role) do usuário", description = "Atualiza o perfil de acesso de um usuário para ADMIN, TEACHER ou STUDENT. Exige perfil ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida ou tentativa do admin remover seu próprio perfil"),
        @ApiResponse(responseCode = "403", description = "Acesso negado para perfis diferentes de ADMIN"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
<<<<<<< HEAD
    @PatchMapping("/users/{id}/role")
=======
    @PatchMapping("/{id}/role")
>>>>>>> origin/dev
    public UserDTO.Response updateUserRole(Principal principal, @PathVariable UUID id, @Valid @RequestBody UserDTO.UpdateRoleRequest dto) {
        return userService.updateUserRole(principal.getName(), id, dto.role());
    }

    @Operation(summary = "Consultar histórico de auditoria do usuário", description = "Retorna todas as revisões históricas do usuário registradas pelo Hibernate Envers. Exige perfil ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico de auditoria retornado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado para perfis diferentes de ADMIN"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
<<<<<<< HEAD
    @GetMapping("/users/{id}/audit")
    public List<UserDTO.AuditResponse> getUserAuditHistory(@PathVariable UUID id) {
        return userService.getUserAuditHistory(id);
    }

    @Operation(
        summary = "Obter estatísticas do painel do professor",
        description = "Retorna métricas consolidadas sobre cursos, alunos matriculados, videoaulas assistidas e taxas de conclusão no período informado (padrão últimos 6 meses, máximo de 6 meses de intervalo). Exige perfil TEACHER ou ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Métricas retornadas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Período inválido ou superior a 6 meses"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/teacher/dashboard")
    public CourseDTO.Summary getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal) {
        return courseService.getTeacherDashboard(principal.getName(), startDate, endDate);
    }
=======
    @GetMapping("/{id}/audit")
    public List<UserDTO.AuditResponse> getUserAuditHistory(@PathVariable UUID id) {
        return userService.getUserAuditHistory(id);
    }
>>>>>>> origin/dev
}
