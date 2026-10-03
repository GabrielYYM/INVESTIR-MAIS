package com.repositorio.investir_mais.domain.user.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.user.DTO.ForgotPasswordRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.ResetPasswordRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UpdatePasswordDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.UserSecurity;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.infrastructure.EmailOttHandler;

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
    private UserSecurity userSecurity;

    @BeforeEach
    void setUp() {
        userSecurity = new UserSecurity("felipe@investir.com", "encoded_password", UserRole.STUDENT, false);
        user = new User();
        user.setUserSecurity(userSecurity);
    }

    @Test
    void findByEmail_QuandoExiste_DeveRetornarUsuario() {
        when(userRepository.findByUserSecurityEmail("felipe@investir.com")).thenReturn(user);

        User result = userService.findByEmail("felipe@investir.com");

        assertNotNull(result);
        assertEquals("felipe@investir.com", result.getUserSecurity().getEmail());
    }

    @Test
    void findByEmail_QuandoNaoExiste_DeveLancarNotFound() {
        when(userRepository.findByUserSecurityEmail("inexistente@investir.com")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> userService.findByEmail("inexistente@investir.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void registerUser_QuandoPerfilForAdmin_DeveLancarBadRequest() {
        UserRequestDTO dto = new UserRequestDTO("Admin", 30, "admin@investir.com", "password123", UserRole.ADMIN);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> userService.registerUser(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_ComSucesso_DeveSalvarEEnviarOtt() {
        UserRequestDTO dto = new UserRequestDTO("Felipe", 22, "felipe@investir.com", "password123", UserRole.STUDENT);
        UserResponseDTO responseDTO = new UserResponseDTO(
            UUID.randomUUID(), 
            "Felipe", 
            22, 
            "felipe@investir.com", 
            UserRole.STUDENT, 
            false, 
            LocalDateTime.now()
        );
        
        OneTimeToken ott = new DefaultOneTimeToken("token-123", "felipe@investir.com", Instant.now().plusSeconds(300));

        when(userMapper.toEntity(dto)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(user)).thenReturn(user);
        when(oneTimeTokenService.generate(any(GenerateOneTimeTokenRequest.class))).thenReturn(ott);
        when(userMapper.toDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.registerUser(dto);

        assertNotNull(result);
        verify(emailOttHandler).sendOttEmail("felipe@investir.com", "token-123");
        verify(userRepository).save(user);
    }

    @Test
    void updatePassword_QuandoSenhaAtualInvalida_DeveLancarUnauthorized() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO("senhaIncorreta", "novaSenha123");
        when(userRepository.findByUserSecurityEmail("felipe@investir.com")).thenReturn(user);
        when(passwordEncoder.matches("senhaIncorreta", "encoded_password")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> userService.updatePassword("felipe@investir.com", dto));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void updatePassword_ComSucesso_DeveAtualizarEGuardar() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO("encoded_password", "novaSenha123");
        when(userRepository.findByUserSecurityEmail("felipe@investir.com")).thenReturn(user);
        when(passwordEncoder.matches("encoded_password", "encoded_password")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("new_encoded_password");

        userService.updatePassword("felipe@investir.com", dto);

        assertEquals("new_encoded_password", user.getUserSecurity().getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void forgotPassword_QuandoUsuarioNaoExiste_NaoDeveEnviarEmail() {
        ForgotPasswordRequestDTO dto = new ForgotPasswordRequestDTO("inexistente@investir.com");
        when(userRepository.findByUserSecurityEmail("inexistente@investir.com")).thenReturn(null);

        userService.forgotPassword(dto);

        verify(emailOttHandler, never()).sendOttEmail(any(), any());
    }

    @Test
    void forgotPassword_QuandoUsuarioExiste_DeveGerarEEnviarOtt() {
        ForgotPasswordRequestDTO dto = new ForgotPasswordRequestDTO("felipe@investir.com");
        OneTimeToken ott = new DefaultOneTimeToken("token-ott", "felipe@investir.com", Instant.now().plusSeconds(300));

        when(userRepository.findByUserSecurityEmail("felipe@investir.com")).thenReturn(user);
        when(oneTimeTokenService.generate(any(GenerateOneTimeTokenRequest.class))).thenReturn(ott);

        userService.forgotPassword(dto);

        verify(emailOttHandler).sendOttEmail("felipe@investir.com", "token-ott");
    }

    @Test
    void resetPassword_QuandoTokenInvalido_DeveLancarBadRequest() {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO("token-invalido", "novaSenha123");
        when(oneTimeTokenService.consume(any(OneTimeTokenAuthenticationToken.class))).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> userService.resetPassword(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void resetPassword_ComSucesso_DeveAtualizarSenhaEAtivarEmail() {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO("token-valido", "novaSenha123");
        OneTimeToken ott = new DefaultOneTimeToken("token-valido", "felipe@investir.com", Instant.now().plusSeconds(300));

        when(oneTimeTokenService.consume(any(OneTimeTokenAuthenticationToken.class))).thenReturn(ott);
        when(userRepository.findByUserSecurityEmail("felipe@investir.com")).thenReturn(user);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("new_encoded_password");

        userService.resetPassword(dto);

        assertEquals("new_encoded_password", user.getUserSecurity().getPassword());
        assertTrue(user.getUserSecurity().isEmailVerified());
        verify(userRepository).save(user);
    }
}