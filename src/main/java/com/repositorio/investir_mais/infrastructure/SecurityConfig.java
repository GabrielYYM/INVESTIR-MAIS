package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.TokenDigest;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.infrastructure.config.AuthenticationRateLimitProperties;
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.JdbcOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.ott.GenerateOneTimeTokenFilter;
import org.springframework.security.web.authentication.ott.GenerateOneTimeTokenRequestResolver;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.util.List;


@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
@EnableMultiFactorAuthentication(authorities = {
    FactorGrantedAuthority.PASSWORD_AUTHORITY, FactorGrantedAuthority.OTT_AUTHORITY
})
public class SecurityConfig {
    private static final String PENDING_LOGIN_TOKEN = SecurityConfig.class.getName() + ".pendingLoginToken";

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> userRepository.findByEmail(email)
            .map(user -> User.withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .disabled(!user.isEmailVerified())
                    .build())
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2);
    }

    @Bean
    public OneTimeTokenService loginOneTimeTokenService(JdbcTemplate jdbcTemplate) throws Exception {
        var tokens = new JdbcOneTimeTokenService(jdbcTemplate);
        tokens.destroy();
        tokens.setCleanupCron(null);
        return tokens;
    }

    @Bean
    public GenerateOneTimeTokenRequestResolver loginTokenRequestResolver() {
        return request -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!hasPasswordFactor(authentication)) {
                return null;
            }
            return new GenerateOneTimeTokenRequest(authentication.getName());
        };
    }

    @Bean
    public OneTimeTokenGenerationSuccessHandler loginTokenDelivery(AuthenticationEmailHandler emailHandler) {
        return (request, response, token) -> {
            request.getSession().setAttribute(PENDING_LOGIN_TOKEN, loginTokenDigest(token.getUsername(), token.getTokenValue()));
            emailHandler.sendLoginLink(token.getUsername(), token.getTokenValue(), request.getParameter("retorno"));
            response.setStatus(HttpStatus.NO_CONTENT.value());
        };
    }

    @Bean
    public AuthenticationSuccessHandler passwordLoginSuccess(OneTimeTokenService tokens, OneTimeTokenGenerationSuccessHandler delivery) {
        return (request, response, authentication) -> delivery.handle(request, response,
            tokens.generate(new GenerateOneTimeTokenRequest(authentication.getName())));
    }

    private static boolean hasPasswordFactor(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() && authentication.getAuthorities().stream()
                .anyMatch(authority -> FactorGrantedAuthority.PASSWORD_AUTHORITY.equals(authority.getAuthority()));
    }

    private static String loginTokenDigest(String username, String token) {
        return TokenDigest.sha256(username + "\0" + token);
    }

    private static Authentication convertLoginToken(HttpServletRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var session = request.getSession(false);
        String token = request.getParameter("token");
        if (!hasPasswordFactor(authentication) || session == null || token == null || token.length() > 64
                || !loginTokenDigest(authentication.getName(), token).equals(session.getAttribute(PENDING_LOGIN_TOKEN))) {
            throw new BadCredentialsException("Link ou código inválido para esta sessão de login.");
        }
        return OneTimeTokenAuthenticationToken.unauthenticated(token);
    }

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository(@Value("${server.servlet.session.cookie.secure:false}") boolean secureCookie) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.secure(secureCookie).sameSite("Lax"));
        return repository;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(WebApplicationProperties properties, Environment environment) {
        if (environment.matchesProfiles("prod") && (!isHttpsOrigin(properties.publicBaseUrl())
                || !isHttpsOrigin(properties.cors().allowedOrigin()))) {
            throw new IllegalArgumentException("Produção exige origens HTTPS na URL pública e no CORS.");
        }
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(properties.cors().allowedOrigin()));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(List.of("Retry-After"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static boolean isHttpsOrigin(String value) {
        URI uri = URI.create(value);
        return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null
                && (uri.getPath().isEmpty() || "/".equals(uri.getPath())) && uri.getQuery() == null && uri.getFragment() == null;
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, StringRedisTemplate redis, CookieCsrfTokenRepository csrfRepository, AuthenticationSuccessHandler passwordLoginSuccess, AuthenticationRateLimitProperties rateLimitProperties) throws Exception {
        http.cors(Customizer.withDefaults())
            .addFilterBefore(new SecurityFilter(redis, rateLimitProperties), GenerateOneTimeTokenFilter.class)
            .requestCache(AbstractHttpConfigurer::disable)
            .csrf(csrf -> csrf.spa().csrfTokenRepository(csrfRepository))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN)))
            .formLogin(login -> login
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .successHandler(passwordLoginSuccess)
                .failureHandler((request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                .withObjectPostProcessor(new ObjectPostProcessor<UsernamePasswordAuthenticationFilter>() {
                    @Override
                    public <O extends UsernamePasswordAuthenticationFilter> O postProcess(O filter) {
                        filter.setMfaEnabled(false);
                        return filter;
                    }
                }))
                .oneTimeTokenLogin(ott -> ott
                    .tokenGeneratingUrl("/auth/code")
                    .loginProcessingUrl("/auth/verify-2fa")
                    .showDefaultSubmitPage(false)
                    .authenticationConverter(SecurityConfig::convertLoginToken)
                    .successHandler((request, response, authentication) -> {
                        var session = request.getSession(false);
                        if (session != null) session.removeAttribute(PENDING_LOGIN_TOKEN);
                        response.setStatus(HttpStatus.NO_CONTENT.value());
                    })
                    .failureHandler((request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value())))
                .logout(logout -> logout
                    .logoutUrl("/auth/logout")
                    .deleteCookies("SESSION")
                    .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/auth/login",
                        "/auth/verify-2fa",
                        "/csrf",
                        "/api/users/register",
                        "/api/users/verify-email",
                        "/api/users/resend-verification",
                        "/api/users/forgot-password",
                        "/api/users/reset-password",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                    )
                    .permitAll()
                    .anyRequest().authenticated()
                );

        return http.build();
    }
}
