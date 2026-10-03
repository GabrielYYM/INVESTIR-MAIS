package com.repositorio.investir_mais.domain.user.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repositorio.investir_mais.domain.user.DTO.ForgotPasswordRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.ResetPasswordRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UpdatePasswordDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void registerUser_WithValidData_ShouldReturnCreated() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Felipe", 22, "felipe@investir.com", "senha12345",
                UserRole.STUDENT);
        UserResponseDTO responseDTO = new UserResponseDTO(
                UUID.randomUUID(),
                "Felipe",
                22,
                "felipe@investir.com",
                UserRole.STUDENT,
                false,
                LocalDateTime.now());

        when(userService.registerUser(any(UserRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("felipe@investir.com"))
                .andExpect(jsonPath("$.emailVerified").value(false));
    }

    @Test
    void updatePassword_WithValidData_ShouldReturnOk() throws Exception {
        UpdatePasswordDTO dto = new UpdatePasswordDTO("senhaAntiga123", "novaSenha123");

        Principal mockPrincipal = () -> "felipe@investir.com";

        mockMvc.perform(put("/api/users/update-password")
                .principal(mockPrincipal)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha atualizada com sucesso!"));

        verify(userService).updatePassword(eq("felipe@investir.com"), any(UpdatePasswordDTO.class));
    }

    @Test
    void forgotPassword_WithValidEmail_ShouldReturnOk() throws Exception {
        ForgotPasswordRequestDTO dto = new ForgotPasswordRequestDTO("felipe@investir.com");

        mockMvc.perform(post("/api/users/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Se o e-mail existir na nossa base")));

        verify(userService).forgotPassword(any(ForgotPasswordRequestDTO.class));
    }

    @Test
    void resetPassword_WithValidData_ShouldReturnOk() throws Exception {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO("token-123", "novaSenha123");

        mockMvc.perform(post("/api/users/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso!"));

        verify(userService).resetPassword(any(ResetPasswordRequestDTO.class));
    }

    @Test
    void resetPassword_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO("", "123");

        mockMvc.perform(post("/api/users/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}