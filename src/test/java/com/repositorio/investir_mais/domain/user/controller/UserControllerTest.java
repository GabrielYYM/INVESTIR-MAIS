package com.repositorio.investir_mais.domain.user.controller;

import tools.jackson.databind.ObjectMapper;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.UserService;
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import com.repositorio.investir_mais.infrastructure.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {UserController.class, GlobalExceptionHandler.class})
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private CourseService courseService;
    @MockitoBean
    private CsrfTokenRepository csrfTokens;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private WebApplicationProperties properties;

    @Test
    void registerUser_WithValidData_ShouldReturnGenericAcceptance() throws Exception {
        UserDTO.Request requestDTO = new UserDTO.Request("Felipe", 22, "felipe@investir.com", "senha12345", UserRole.STUDENT);
        when(userService.registerUser(any(UserDTO.Request.class)))
                .thenReturn(new DefaultOneTimeToken("registration-token", requestDTO.email(), Instant.now().plusSeconds(300)));

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void deleteAccount_WithConfirmation_ShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        var dto = new UserDTO.DeleteAccountRequest("current-password");

        mockMvc.perform(delete("/api/users/{id}", id)
                .principal(() -> "felipe@investir.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        verify(userService).deleteAccount("felipe@investir.com", id, dto);
    }

    @Test
    void deleteAccount_WithoutPassword_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", UUID.randomUUID())
                .principal(() -> "felipe@investir.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
        org.mockito.Mockito.verifyNoInteractions(userService);
    }

    @Test
    void updatePassword_WithValidData_ShouldReturnOk() throws Exception {
        AuthDTO.UpdatePasswordRequest dto = new AuthDTO.UpdatePasswordRequest("senhaAntiga123", "novaSenha123");

        mockMvc.perform(put("/api/users/update-password")
            .principal(() -> "felipe@investir.com")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Senha atualizada com sucesso!"));

        verify(userService).updatePassword(eq("felipe@investir.com"), any(AuthDTO.UpdatePasswordRequest.class));
    }

    @Test
    void forgotPassword_WithValidEmail_ShouldReturnOk() throws Exception {
        AuthDTO.ForgotPasswordRequest dto = new AuthDTO.ForgotPasswordRequest("felipe@investir.com");

        mockMvc.perform(post("/api/users/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Se o e-mail existir na nossa base")));

        verify(userService).forgotPassword(any(AuthDTO.ForgotPasswordRequest.class));
    }

    @Test
    void resetPassword_WithValidData_ShouldReturnOk() throws Exception {
        AuthDTO.ResetPasswordRequest dto = new AuthDTO.ResetPasswordRequest("token-123", "novaSenha123");

        mockMvc.perform(post("/api/users/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso!"));

        verify(userService).resetPassword(any(AuthDTO.ResetPasswordRequest.class));
    }

    @Test
    void resetPassword_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        AuthDTO.ResetPasswordRequest dto = new AuthDTO.ResetPasswordRequest("", "123");

        mockMvc.perform(post("/api/users/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listAllUsers_ShouldReturnUsersList() throws Exception {
        UUID id = UUID.randomUUID();
        UserDTO.Response responseDTO = new UserDTO.Response(
            id, "Admin", 30, "admin@investir.com", UserRole.ADMIN, true, LocalDateTime.now()
        );
        when(userService.listAllUsers()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/users")
                .principal(() -> "admin@investir.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@investir.com"));

        verify(userService).listAllUsers();
    }

    @Test
    void updateUserRole_WithValidData_ShouldReturnUpdatedUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserDTO.UpdateRoleRequest dto = new UserDTO.UpdateRoleRequest(UserRole.TEACHER);
        UserDTO.Response responseDTO = new UserDTO.Response(
            id, "Felipe", 25, "felipe@investir.com", UserRole.TEACHER, true, LocalDateTime.now()
        );
        when(userService.updateUserRole(eq("admin@investir.com"), eq(id), eq(UserRole.TEACHER))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/users/{id}/role", id)
                .principal(() -> "admin@investir.com")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TEACHER"));

        verify(userService).updateUserRole("admin@investir.com", id, UserRole.TEACHER);
    }

    @Test
    void getUserAuditHistory_ShouldReturnAuditList() throws Exception {
        UUID id = UUID.randomUUID();
        UserDTO.Response userSnapshot = new UserDTO.Response(
            id, "Felipe", 25, "felipe@investir.com", UserRole.STUDENT, true, LocalDateTime.now()
        );
        UserDTO.AuditResponse auditDTO = new UserDTO.AuditResponse(1, Instant.now(), "INSERT", userSnapshot);
        when(userService.getUserAuditHistory(id)).thenReturn(List.of(auditDTO));

        mockMvc.perform(get("/api/users/{id}/audit", id)
                .principal(() -> "admin@investir.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].revisionNumber").value(1))
                .andExpect(jsonPath("$[0].operation").value("INSERT"));

        verify(userService).getUserAuditHistory(id);
    }
}
