package com.repositorio.investir_mais.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.UserSecurity;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private UserRepository userRepository;

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void passwordEncoder_DeveCriptografarEValidarSenhaComArgon2() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof Argon2PasswordEncoder);

        String rawPassword = "MinhaSenhaSegura123";
        String encoded = encoder.encode(rawPassword);

        assertTrue(encoder.matches(rawPassword, encoded));
    }

    @Test
    void userDetailsService_QuandoUsuarioExiste_DeveRetornarUserDetails() {
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setEmail("felipe@investir.com");
        userSecurity.setPassword("hashed_password");
        userSecurity.setRole(UserRole.STUDENT);

        User user = new User();
        user.setUserSecurity(userSecurity);

        when(userRepository.findByUserSecurityEmail("felipe@investir.com")).thenReturn(user);

        UserDetailsService userDetailsService = securityConfig.userDetailsService(userRepository);
        UserDetails userDetails = userDetailsService.loadUserByUsername("felipe@investir.com");

        assertEquals("felipe@investir.com", userDetails.getUsername());
        assertEquals("hashed_password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
    }

    @Test
    void userDetailsService_QuandoUsuarioNaoExiste_DeveLancarExcecao() {
        when(userRepository.findByUserSecurityEmail("inexistente@investir.com")).thenReturn(null);

        UserDetailsService userDetailsService = securityConfig.userDetailsService(userRepository);

        assertThrows(UsernameNotFoundException.class, () -> 
            userDetailsService.loadUserByUsername("inexistente@investir.com")
        );
    }
}