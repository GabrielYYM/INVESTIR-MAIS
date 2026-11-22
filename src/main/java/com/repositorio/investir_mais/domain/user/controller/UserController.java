package com.repositorio.investir_mais.domain.user.controller;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de perfis, cadastros e redefinição de senhas")
public class UserController {
    private final UserService userService;
    private final CourseService courseService;

    @Operation(summary = "Obter perfil do usuário por ID", description = "Retorna os detalhes do perfil do usuário autenticado.")
    @GetMapping("/users/{id}")
    public UserDTO.Response getProfile(Principal principal, @PathVariable UUID id) {
        return userService.getProfile(principal.getName(), id);
    }

    @Operation(summary = "Atualizar dados do perfil", description = "Atualiza apelido ou e-mail do usuário autenticado.")
    @PutMapping("/users/{id}")
    public UserDTO.Response updateProfile(Principal principal, @PathVariable UUID id, @Valid @RequestBody UserDTO.UpdateProfileRequest dto) {
        return userService.updateProfile(principal.getName(), id, dto);
    }

    @Operation(summary = "Excluir logicamente a própria conta", description = "Marca a conta como excluída (soft delete) e suprime os identificadores do perfil imediatamente, libera o e-mail, encerra sessões e preserva o histórico de ações com acesso restrito. Sem recuperação. Exige senha atual.")
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Principal principal, @PathVariable UUID id, @Valid @RequestBody UserDTO.DeleteAccountRequest dto, HttpServletRequest request) {
        userService.deleteAccount(principal.getName(), id, dto);
        endCurrentSession(request);
    }

    private void endCurrentSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
    }

    @Operation(summary = "Atualizar senha do usuário logado", description = "Permite alterar a senha mediante confirmação da senha atual.")
    @PutMapping("/users/update-password")
    public Map<String, String> updatePassword(Principal principal, @Valid @RequestBody AuthDTO.UpdatePasswordRequest dto) {
        userService.updatePassword(principal.getName(), dto);
        return Map.of("message", "Senha atualizada com sucesso!");
    }

    @Operation(summary = "Listar todos os usuários", description = "Retorna a lista completa de usuários cadastrados no sistema. Exige perfil ADMIN.")
    @GetMapping("/users")
    public List<UserDTO.Response> listAllUsers() {
        return userService.listAllUsers();
    }

    @Operation(summary = "Alterar perfil (role) do usuário", description = "Altera o perfil para TEACHER ou STUDENT. Exige ADMIN.")
    @PatchMapping("/users/{id}/role")
    public UserDTO.Response updateUserRole(Principal principal, @PathVariable UUID id, @Valid @RequestBody UserDTO.UpdateRoleRequest dto) {
        return userService.updateUserRole(principal.getName(), id, dto.role());
    }

    @Operation(summary = "Consultar histórico de auditoria do usuário", description = "Retorna todas as revisões históricas do usuário registradas pelo Hibernate Envers. Exige perfil ADMIN.")
    @GetMapping("/users/{id}/audit")
    public List<UserDTO.AuditResponse> getUserAuditHistory(@PathVariable UUID id) {
        return userService.getUserAuditHistory(id);
    }

    @Operation(
        summary = "Obter estatísticas do painel do professor",
        description = "Retorna métricas consolidadas sobre cursos, alunos matriculados, videoaulas assistidas e taxas de conclusão no período informado (padrão últimos 6 meses, máximo de 6 meses de intervalo). Exige perfil TEACHER ou ADMIN."
    )
    @GetMapping("/teacher/dashboard")
    public CourseDTO.Summary getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal) {
        return courseService.getTeacherDashboard(principal.getName(), startDate, endDate);
    }
}
