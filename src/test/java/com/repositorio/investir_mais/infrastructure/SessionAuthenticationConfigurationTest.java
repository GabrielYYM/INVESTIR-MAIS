package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class SessionAuthenticationConfigurationTest extends PostgreSqlIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void oauthAuthorizationServerBeansAreNotConfigured() {
        assertFalse(applicationContext.containsBean("registeredClientRepository"));
        assertFalse(applicationContext.containsBean("authorizationService"));
        assertInstanceOf(RedisCacheManager.class, applicationContext.getBean(CacheManager.class));
        assertInstanceOf(RedisIndexedSessionRepository.class, applicationContext.getBean(SessionRepository.class));
    }

    @Test
    void securityContextSurvivesRedisSerializationAndSessionsCanBeRevokedByPrincipal() {
        var sessions = applicationContext.getBean(RedisIndexedSessionRepository.class);
        var principal = User.withUsername("session-test@investir.com").password("unused").roles("STUDENT").build();
        var security = SecurityContextHolder.createEmptyContext();
        security.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
        var session = sessions.createSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", security);
        try {
            sessions.save(session);
            var restored = sessions.findById(session.getId());
            assertNotNull(restored);
            SecurityContext restoredSecurity = restored.getAttribute("SPRING_SECURITY_CONTEXT");
            assertEquals(principal.getUsername(), restoredSecurity.getAuthentication().getName());
            assertTrue(sessions.findByPrincipalName(principal.getUsername()).containsKey(session.getId()));
            assertEquals(Duration.ofMinutes(30), restored.getMaxInactiveInterval());
            sessions.findByPrincipalName(principal.getUsername()).keySet().forEach(sessions::deleteById);
            assertNull(sessions.findById(session.getId()));
        } finally {
            sessions.deleteById(session.getId());
        }
    }

    @Test
    void sessionIsRejectedAfterInactivityTimeout() {
        var sessions = applicationContext.getBean(RedisIndexedSessionRepository.class);
        var session = sessions.createSession();
        session.setMaxInactiveInterval(Duration.ofSeconds(1));
        try {
            sessions.save(session);
            assertNotNull(sessions.findById(session.getId()));
            await().atMost(Duration.ofSeconds(4)).until(() -> sessions.findById(session.getId()) == null);
        } finally {
            sessions.deleteById(session.getId());
        }
    }
}
