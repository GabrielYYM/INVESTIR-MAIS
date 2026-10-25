package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.UserService;
import com.repositorio.investir_mais.infrastructure.config.AuthenticationRateLimitProperties;
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

// Real Spring Security filters and native JDBC token storage, without a running Docker/database server.
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SecurityConfig.class, AuthenticationController.class, SpringLoginFlowTest.Config.class})
class SpringLoginFlowTest {
    private static final String EMAIL = "user@investir.com";
    private static final String PASSWORD = "MinhaSenhaSegura123";

    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository users;
    @Autowired private UserService userService;
    @Autowired private AuthenticationEmailHandler emails;
    @Autowired private StringRedisTemplate rateLimits;
    @Autowired private OneTimeTokenService tokens;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbc;
    private MockMvc mvc;
    private User user;

    @BeforeEach
    void setUp() {
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        reset(users, userService, emails, rateLimits);
        jdbc.update("DELETE FROM one_time_tokens");
        when(rateLimits.execute(any(RedisScript.class), anyList(), anyString(), anyString())).thenReturn(0L);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Usuário");
        user.setEmail(EMAIL);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole(UserRole.STUDENT);
        user.setEmailVerified(true);
        when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userService.findByEmail(EMAIL)).thenReturn(user);
    }

    @Test
    void anonymousRequestsCannotReadCurrentUserOrGenerateTokens() throws Exception {
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/code").with(csrf()).param("username", EMAIL))
                .andExpect(status().isUnauthorized());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM one_time_tokens", Integer.class));
        verifyNoInteractions(emails, userService);
    }

    @Test
    void wrongPasswordAndUnverifiedEmailCannotStartLogin() throws Exception {
        mvc.perform(post("/auth/login").with(csrf()).param("email", EMAIL).param("password", "wrong"))
                .andExpect(status().isUnauthorized());
        user.setEmailVerified(false);
        mvc.perform(post("/auth/login").with(csrf()).param("email", EMAIL).param("password", PASSWORD))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(emails);
    }

    @Test
    void passwordAloneDoesNotGrantAccessEvenToCurrentUser() throws Exception {
        MockHttpSession session = login();
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/test/private").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/test/admin").session(session)).andExpect(status().isForbidden());
        verifyNoInteractions(userService);
    }

    @Test
    void generationUsesSessionAccountInsteadOfSubmittedEmail() throws Exception {
        MockHttpSession session = login();
        mvc.perform(post("/auth/code").session(session).with(csrf()).param("username", "other@investir.com"))
                .andExpect(status().isNoContent());
        verify(emails, times(2)).sendLoginCode(eq(EMAIL), anyString());
        assertEquals(EMAIL, jdbc.queryForObject("SELECT DISTINCT username FROM one_time_tokens", String.class));
    }

    @Test
    void passwordAndEmailTokenGrantAccessAndKeepRoleRestrictions() throws Exception {
        MockHttpSession session = login();
        String initialId = session.getId();
        String token = sentCode();
        session = verifyCode(session, token);
        assertNotEquals(initialId, session.getId());
        mvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(EMAIL));
        mvc.perform(get("/test/private").session(session)).andExpect(status().isOk());
        mvc.perform(get("/test/admin").session(session)).andExpect(status().isForbidden());
        SecurityContext security = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        var authorities = security.getAuthentication().getAuthorities().stream().map(a -> a.getAuthority()).toList();
        assertTrue(authorities.contains(FactorGrantedAuthority.PASSWORD_AUTHORITY));
        assertTrue(authorities.contains(FactorGrantedAuthority.OTT_AUTHORITY));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM one_time_tokens", Integer.class));
    }

    @Test
    void tokenAloneCannotGrantAccess() throws Exception {
        String token = tokens.generate(new GenerateOneTimeTokenRequest(EMAIL)).getTokenValue();
        MockHttpSession session = verifyCode(null, token);
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/test/private").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void factorsForDifferentUsersCannotBeCombined() throws Exception {
        User other = new User();
        other.setEmail("other@investir.com");
        other.setPassword(user.getPassword());
        other.setRole(UserRole.ADMIN);
        other.setEmailVerified(true);
        when(users.findByEmail(other.getEmail())).thenReturn(Optional.of(other));
        String otherToken = tokens.generate(new GenerateOneTimeTokenRequest(other.getEmail())).getTokenValue();
        MockHttpSession session = verifyCode(login(), otherToken);
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/test/admin").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void invalidExpiredAndReusedTokensAreRejected() throws Exception {
        mvc.perform(post("/auth/verify-2fa").session(login()).with(csrf()).param("token", "invalid"))
                .andExpect(status().isUnauthorized());
        String expired = tokens.generate(new GenerateOneTimeTokenRequest(EMAIL, Duration.ofMinutes(-1))).getTokenValue();
        mvc.perform(post("/auth/verify-2fa").session(login()).with(csrf()).param("token", expired))
                .andExpect(status().isUnauthorized());
        MockHttpSession session = login();
        String token = sentCode();
        session = verifyCode(session, token);
        mvc.perform(post("/auth/verify-2fa").session(session).with(csrf()).param("token", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void submittingPasswordAgainRequiresNewEmailVerification() throws Exception {
        MockHttpSession session = login();
        session = verifyCode(session, sentCode());
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isOk());
        mvc.perform(post("/auth/login").session(session).with(csrf()).param("email", EMAIL).param("password", PASSWORD))
                .andExpect(status().isNoContent());
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void nativeLogoutInvalidatesSessionAndDeletesCookie() throws Exception {
        MockHttpSession session = login();
        session = verifyCode(session, sentCode());
        mvc.perform(post("/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("SESSION", 0));
        assertTrue(session.isInvalid());
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void allAuthenticationWritesRequireCsrfAndCodeGenerationIsRateLimited() throws Exception {
        for (String endpoint : new String[]{"/auth/login", "/auth/code", "/auth/verify-2fa", "/auth/logout"}) {
            mvc.perform(post(endpoint)).andExpect(status().isForbidden());
        }
        MockHttpSession session = login();
        when(rateLimits.execute(any(RedisScript.class), argThat(keys -> keys.getFirst().endsWith(":/auth/code")), anyString(), anyString())).thenReturn(1000L);
        mvc.perform(post("/auth/code").session(session).with(csrf())).andExpect(status().isTooManyRequests());
        verify(emails).sendLoginCode(eq(EMAIL), anyString());
    }

    @Test
    void browserCanRefreshCsrfBetweenPasswordTokenAndLogout() throws Exception {
        // Exercise the actual cookie/header protocol used by Axios, without the csrf() test shortcut.
        Cookie csrfCookie = csrfCookie(null);
        var passwordResult = mvc.perform(post("/auth/login").cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue()).param("email", EMAIL).param("password", PASSWORD))
                .andExpect(status().isNoContent()).andReturn();
        MockHttpSession session = (MockHttpSession) passwordResult.getRequest().getSession(false);
        assertEquals(0, passwordResult.getResponse().getCookie("XSRF-TOKEN").getMaxAge());

        Cookie nextCookie = csrfCookie(session);
        assertNotEquals(csrfCookie.getValue(), nextCookie.getValue());
        mvc.perform(post("/auth/code").session(session).cookie(nextCookie).header("X-XSRF-TOKEN", nextCookie.getValue()))
                .andExpect(status().isNoContent());
        String token = sentCode();
        mvc.perform(post("/auth/verify-2fa").session(session).cookie(nextCookie)
                        .header("X-XSRF-TOKEN", nextCookie.getValue()).param("token", token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isOk());
        Cookie logoutCookie = csrfCookie(session);
        mvc.perform(post("/auth/logout").session(session).cookie(logoutCookie)
                        .header("X-XSRF-TOKEN", logoutCookie.getValue()))
                .andExpect(status().isNoContent());
        assertTrue(session.isInvalid());
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    private Cookie csrfCookie(MockHttpSession session) throws Exception {
        var request = get("/csrf");
        if (session != null) request.session(session);
        Cookie cookie = mvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        return cookie;
    }

    private RequestPostProcessor csrf() {
        return request -> {
            try {
                Cookie cookie = csrfCookie((MockHttpSession) request.getSession(false));
                request.setCookies(cookie);
                request.addHeader("X-XSRF-TOKEN", cookie.getValue());
                return request;
            } catch (Exception exception) {
                throw new IllegalStateException("Could not obtain CSRF cookie", exception);
            }
        };
    }

    private MockHttpSession login() throws Exception {
        return (MockHttpSession) mvc.perform(post("/auth/login").with(csrf())
                        .param("email", EMAIL).param("password", PASSWORD))
                .andExpect(status().isNoContent()).andReturn().getRequest().getSession(false);
    }

    private String sentCode() {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(emails, atLeastOnce()).sendLoginCode(eq(EMAIL), code.capture());
        return code.getValue();
    }

    private MockHttpSession verifyCode(MockHttpSession session, String token) throws Exception {
        var request = post("/auth/verify-2fa").with(csrf()).param("token", token);
        if (session != null) request.session(session);
        return (MockHttpSession) mvc.perform(request).andExpect(status().isNoContent())
                .andReturn().getRequest().getSession(false);
    }

    @Configuration
    @EnableWebMvc
    @EnableConfigurationProperties({AuthenticationRateLimitProperties.class, WebApplicationProperties.class})
    static class Config {
        @Bean UserMapper userMapper() {
            return Mappers.getMapper(UserMapper.class);
        }
        @Bean UserRepository users() { return mock(UserRepository.class); }
        @Bean UserService userService() { return mock(UserService.class); }
        @Bean AuthenticationEmailHandler emails() { return mock(AuthenticationEmailHandler.class); }
        @Bean StringRedisTemplate rateLimits() { return mock(StringRedisTemplate.class); }
        @Bean ProtectedEndpoints protectedEndpoints() { return new ProtectedEndpoints(); }
        @Bean(destroyMethod = "shutdown") DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2)
                    .addScript("classpath:org/springframework/security/core/ott/jdbc/one-time-tokens-schema.sql").build();
        }
        @Bean JdbcTemplate jdbcTemplate(DataSource dataSource) { return new JdbcTemplate(dataSource); }
    }

    @RestController
    static class ProtectedEndpoints {
        @GetMapping("/test/private") public String privatePage() { return "private"; }
        @GetMapping("/test/admin") @PreAuthorize("hasRole('ADMIN')") public String adminPage() { return "admin"; }
    }
}
