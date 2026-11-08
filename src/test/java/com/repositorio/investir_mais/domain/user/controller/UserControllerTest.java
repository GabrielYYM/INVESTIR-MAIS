package com.repositorio.investir_mais.domain.user.controller;

import tools.jackson.databind.ObjectMapper;
import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.UserService;
import com.repositorio.investir_mais.infrastructure.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

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
    private UserRepository userRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void verifyEmail_InBrowser_ShouldShowConfirmationAndLoginLink() throws Exception {
        mockMvc.perform(post("/api/users/verify-email")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.TEXT_HTML).param("token", "verification-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("E-mail confirmado")))
                .andExpect(content().string(containsString("href=\"http://localhost:5173/entrar\"")))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
        verify(userService).verifyEmail("verification-token");
    }

    @Test
    void verifyEmail_WithExpiredBrowserLink_ShouldOfferReturnToLogin() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(userService).verifyEmail("expired-token");
        mockMvc.perform(post("/api/users/verify-email")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.TEXT_HTML).param("token", "expired-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString(HtmlUtils.htmlEscape("Link inválido ou expirado"))))
                .andExpect(content().string(containsString("Ir para entrar")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "*/*", "text/html;q=0,application/json"})
    void verifyEmail_ApiClients_ShouldKeepJsonResponse(String accept) throws Exception {
        mockMvc.perform(post("/api/users/verify-email")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Accept", accept).param("token", "verification-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("E-mail verificado com sucesso."));
    }

    @Test
    void registerUser_WithValidData_ShouldReturnGenericAcceptance() throws Exception {
        UserDTO.Request requestDTO = new UserDTO.Request("Felipe", 22, "felipe@investir.com", "senha12345", UserRole.STUDENT);
        UserDTO.Response responseDTO = new UserDTO.Response(
            UUID.randomUUID(), "Felipe", 22, "felipe@investir.com", UserRole.STUDENT, false, LocalDateTime.now()
        );

        when(userService.registerUser(any(UserDTO.Request.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.email").doesNotExist());
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
}
