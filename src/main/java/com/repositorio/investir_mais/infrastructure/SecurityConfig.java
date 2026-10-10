package com.repositorio.investir_mais.infrastructure;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ott.InMemoryOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> userRepository.findByEmail(email)
            .map(user -> User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build())
            .orElseThrow(() -> {
                log.warn("Falha na autenticação: Usuário não encontrado para o e-mail {}", email);
                return new UsernameNotFoundException("Usuário não encontrado.");
            });
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
                log.warn("Acesso recusado: Tentativa de uso do token revogado ID {}", jwt.getId());
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

        AuthenticationSuccessHandler ottSuccessHandler = (request, response, authentication) -> {
            String email = authentication.getName();
            log.info("Sucesso na autenticação via One-Time Token (OTT) para o e-mail: {}", email);
            userRepository.findByEmail(email).ifPresent(user -> {
                if (!user.isEmailVerified()) {
                    user.setEmailVerified(true);
                    userRepository.save(user);
                    log.info("E-mail do usuário {} verificado com sucesso via OTT", user.getId());
                }
            });

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\": \"Login efetuado com sucesso e e-mail validado!\"}");
        };

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
                .authenticationSuccessHandler(ottSuccessHandler)
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter(revokedTokenStore))));

        return http.build();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("revokedTokens");
    }

    @Bean
    public KeyPair keyPair() {
        try {
            log.info("Gerando par de chaves RSA de 2048 bits para assinatura de JWTs");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            log.error("Erro crítico ao gerar par de chaves RSA", e);
            throw new IllegalStateException("Erro ao gerar par de chaves RSA", e);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair keyPair) {
        JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
    }
}