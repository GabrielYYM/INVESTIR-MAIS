package com.repositorio.investir_mais.domain.user.service;

import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.infrastructure.EmailOttHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private OneTimeTokenService oneTimeTokenService;
    @Mock
    private EmailOttHandler emailOttHandler;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("felipe@investir.com");
        user.setPassword("encoded_password");
        user.setRole(UserRole.STUDENT);
        user.setEmailVerified(false);
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByEmail("felipe@investir.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("felipe@investir.com");

        assertNotNull(result);
        assertEquals("felipe@investir.com", result.getEmail());
    }

    @Test
    void findByEmail_WhenUserDoesNotExist_ShouldThrowNotFound() {
        when(userRepository.findByEmail("inexistente@investir.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> userService.findByEmail("inexistente@investir.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void registerUser_WhenRoleIsAdmin_ShouldThrowBadRequest() {
        UserDTO.Request dto = new UserDTO.Request("Admin", 30, "admin@investir.com", "password123", UserRole.ADMIN);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> userService.registerUser(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_OnSuccess_ShouldSaveAndSendOtt() {
        UserDTO.Request dto = new UserDTO.Request("Felipe", 22, "felipe@investir.com", "password123", UserRole.STUDENT);
        UserDTO.Response responseDTO = new UserDTO.Response(
            UUID.randomUUID(), "Felipe", 22, "felipe@investir.com", UserRole.STUDENT, false, LocalDateTime.now()
        );
        
        OneTimeToken ott = new DefaultOneTimeToken("token-123", "felipe@investir.com", Instant.now().plusSeconds(300));

        when(userRepository.findByEmail("felipe@investir.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(user)).thenReturn(user);
        when(oneTimeTokenService.generate(any(GenerateOneTimeTokenRequest.class))).thenReturn(ott);
        when(userMapper.toDTO(user)).thenReturn(responseDTO);

        UserDTO.Response result = userService.registerUser(dto);

        assertNotNull(result);
        verify(emailOttHandler).sendOttEmail("felipe@investir.com", "token-123");
        verify(userRepository).save(user);
    }

    @Test
    void updatePassword_WhenCurrentPasswordIsInvalid_ShouldThrowUnauthorized() {
        AuthDTO.UpdatePasswordRequest dto = new AuthDTO.UpdatePasswordRequest("senhaIncorreta", "novaSenha123");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaIncorreta", "encoded_password")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> userService.updatePassword(user.getId().toString(), dto));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void updatePassword_OnSuccess_ShouldUpdateAndSave() {
        AuthDTO.UpdatePasswordRequest dto = new AuthDTO.UpdatePasswordRequest("encoded_password", "novaSenha123");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("encoded_password", "encoded_password")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("new_encoded_password");

        userService.updatePassword(user.getId().toString(), dto);

        assertEquals("new_encoded_password", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void forgotPassword_WhenUserDoesNotExist_ShouldNotSendEmail() {
        AuthDTO.ForgotPasswordRequest dto = new AuthDTO.ForgotPasswordRequest("inexistente@investir.com");
        when(userRepository.findByEmail("inexistente@investir.com")).thenReturn(Optional.empty());

        userService.forgotPassword(dto);

        verify(emailOttHandler, never()).sendOttEmail(any(), any());
    }

    @Test
    void forgotPassword_WhenUserExists_ShouldGenerateAndSendOtt() {
        AuthDTO.ForgotPasswordRequest dto = new AuthDTO.ForgotPasswordRequest("felipe@investir.com");
        OneTimeToken ott = new DefaultOneTimeToken("token-ott", "felipe@investir.com", Instant.now().plusSeconds(300));

        when(userRepository.findByEmail("felipe@investir.com")).thenReturn(Optional.of(user));
        when(oneTimeTokenService.generate(any(GenerateOneTimeTokenRequest.class))).thenReturn(ott);

        userService.forgotPassword(dto);

        verify(emailOttHandler).sendOttEmail("felipe@investir.com", "token-ott");
    }

    @Test
    void resetPassword_WhenTokenIsInvalid_ShouldThrowBadRequest() {
        AuthDTO.ResetPasswordRequest dto = new AuthDTO.ResetPasswordRequest("token-invalido", "novaSenha123");
        when(oneTimeTokenService.consume(any(OneTimeTokenAuthenticationToken.class))).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> userService.resetPassword(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void resetPassword_OnSuccess_ShouldUpdatePasswordAndVerifyEmail() {
        AuthDTO.ResetPasswordRequest dto = new AuthDTO.ResetPasswordRequest("token-valido", "novaSenha123");
        OneTimeToken ott = new DefaultOneTimeToken("token-valido", "felipe@investir.com", Instant.now().plusSeconds(300));

        when(oneTimeTokenService.consume(any(OneTimeTokenAuthenticationToken.class))).thenReturn(ott);
        when(userRepository.findByEmail("felipe@investir.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("new_encoded_password");

        userService.resetPassword(dto);

        assertEquals("new_encoded_password", user.getPassword());
        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
    }
}