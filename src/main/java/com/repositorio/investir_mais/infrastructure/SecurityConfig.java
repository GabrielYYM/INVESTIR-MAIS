package com.repositorio.investir_mais.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ott.InMemoryOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public RevokedTokenStore revokedTokenStore() {
        return new RevokedTokenStore();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> {
            var user = userRepository.findByUserSecurityEmail(email);

            if (user == null) {
                throw new UsernameNotFoundException("Usuário não encontrado.");
            }

            return User.withUsername(user.getUserSecurity().getEmail())
                .password(user.getUserSecurity().getPassword())
                .roles(user.getUserSecurity().getRole().name())
                .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(RevokedTokenStore revokedTokenStore) {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            if (revokedTokenStore.isRevoked(jwt.getId())) {
                throw new BadCredentialsException("Token revogado.");
            }
            Object roleClaim = jwt.getClaims().get("roles");
            Stream<SimpleGrantedAuthority> roles = roleClaim instanceof Collection<?> values
                ? values.stream().map(Object::toString)
                    .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                : roleClaim instanceof String role
                    ? Stream.of(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                    : Stream.empty();
            return Stream.concat(scopes.convert(jwt).stream(), roles).toList();
        });
        converter.setPrincipalClaimName("sub");
        return converter;
    }

    @Bean
    public OneTimeTokenService oneTimeTokenService() {
        return new InMemoryOneTimeTokenService();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http, 
            EmailOttHandler emailOttHandler,
            UserRepository userRepository,
            RevokedTokenStore revokedTokenStore) throws Exception {

        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**", "/ott/generate", "/login/ott"))
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/users/register",
                    "/api/users",
                    "/api/users/forgot-password",
                    "/api/users/reset-password",
                    "/auth/login",
                    "/auth/verify-2fa",
                    "/api/questions/roles/**",
                    "/ott/**",
                    "/login/ott",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/h2-console/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .oneTimeTokenLogin(ott -> ott
                .tokenGenerationSuccessHandler(emailOttHandler)
                .successHandler((request, response, authentication) -> {
                    String email = authentication.getName();
                    var user = userRepository.findByUserSecurityEmail(email);
                    if (user != null && !user.getUserSecurity().isEmailVerified()) {
                        user.getUserSecurity().setEmailVerified(true);
                        userRepository.save(user);
                    }

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"message\": \"Login efetuado com sucesso e e-mail validado!\"}");
                })
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter(revokedTokenStore))));

        return http.build();
    }
}
