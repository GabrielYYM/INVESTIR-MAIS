package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private UserRepository userRepository;

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void passwordEncoder_ShouldEncryptAndValidatePasswordWithArgon2() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof Argon2PasswordEncoder);

        String rawPassword = "MinhaSenhaSegura123";
        String encoded = encoder.encode(rawPassword);

        assertTrue(encoder.matches(rawPassword, encoded));
    }

    @Test
    void userDetailsService_WhenUserExists_ShouldReturnUserDetails() {
        User user = new User();
        user.setEmail("felipe@investir.com");
        user.setPassword("hashed_password");
        user.setRole(UserRole.STUDENT);

        when(userRepository.findByEmail("felipe@investir.com")).thenReturn(Optional.of(user));

        UserDetailsService userDetailsService = securityConfig.userDetailsService(userRepository);
        UserDetails userDetails = userDetailsService.loadUserByUsername("felipe@investir.com");

        assertEquals("felipe@investir.com", userDetails.getUsername());
        assertEquals("hashed_password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
    }

    @Test
    void userDetailsService_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findByEmail("inexistente@investir.com")).thenReturn(Optional.empty());

        UserDetailsService userDetailsService = securityConfig.userDetailsService(userRepository);

        assertThrows(UsernameNotFoundException.class, () -> 
            userDetailsService.loadUserByUsername("inexistente@investir.com")
        );
    }
}