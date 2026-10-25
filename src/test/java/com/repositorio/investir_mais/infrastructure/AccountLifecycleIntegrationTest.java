package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.UserService;
import com.repositorio.investir_mais.infrastructure.config.AuthenticationRateLimitProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(properties = {"app.security.rate-limit.capacity=3", "app.mail.retry-delay=1s"})
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountLifecycleIntegrationTest extends PostgreSqlIntegrationTest {
    private static final String EMAIL = "account@investir.com";
    private static final String PASSWORD = "SenhaSegura123";
    @Autowired private MockMvc mvc;
    @Autowired private UserService service;
    @MockitoSpyBean private UserRepository users;
    @MockitoSpyBean private PasswordEncoder encoder;
    @MockitoSpyBean private JdbcTemplate jdbc;
    @MockitoBean private JavaMailSender mail;
    @Autowired private FailedEventPublications failedPublications;
    @Autowired private EventPublicationRegistry publications;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private CourseRepository courses;
    @Autowired private LessonRepository lessons;
    @Autowired private CourseService courseService;

    @Autowired private StringRedisTemplate redis;
    @Autowired private FindByIndexNameSessionRepository<?> sessions;
    @Autowired private AuthenticationRateLimitProperties rateLimitProperties;

    @BeforeEach
    void clean() {
        await().atMost(Duration.ofSeconds(5)).until(() -> publications.findIncompletePublications().isEmpty());
        reset(users, mail, jdbc);
        sessions.findByPrincipalName(EMAIL).keySet().forEach(sessions::deleteById);
        try (var keys = redis.scan(ScanOptions.scanOptions()
                .match(rateLimitProperties.namespace() + ":*").count(100).build())) {
            keys.forEachRemaining(redis::delete);
        }
        jdbc.update("DELETE FROM one_time_tokens");
        jdbc.update("DELETE FROM app_one_time_token");
        lessons.deleteAll();
        courses.deleteAll();
        users.deleteAll();
    }

    @Test
    void failedEmailIsPersistedAndCanBeDeliveredByNativeResubmission() {
        doThrow(new MailSendException("simulated SMTP failure")).when(mail).send(any(SimpleMailMessage.class));
        service.registerUser(new UserDTO.Request("Conta", 22, EMAIL, PASSWORD, UserRole.STUDENT));
        awaitFailedPublication();
        reset(mail);
        failedPublications.resubmit(ResubmissionOptions.defaults());
        verify(mail, timeout(3000)).send(any(SimpleMailMessage.class));
        await().atMost(Duration.ofSeconds(5)).until(() -> publications.findIncompletePublications().isEmpty());
    }

    @Test
    void failedEmailIsDeliveredByTheManagedRetryScheduler() {
        doThrow(new MailSendException("temporary SMTP failure")).doNothing()
                .when(mail).send(any(SimpleMailMessage.class));
        service.registerUser(new UserDTO.Request("Conta", 22, EMAIL, PASSWORD, UserRole.STUDENT));
        verify(mail, timeout(5000).times(2)).send(any(SimpleMailMessage.class));
        await().atMost(Duration.ofSeconds(5)).until(() -> publications.findIncompletePublications().isEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL,
            authorities = {"ROLE_TEACHER", "FACTOR_PASSWORD", "FACTOR_OTT"})
    void deletingCourseAlsoDeletesItsLessonsAndSupportsLongDescriptionsAndUrls() {
        User owner = createVerifiedUser();
        var course = new Course();
        course.setName("Curso de teste");
        course.setDescription("Descrição");
        course.setProfessor(owner);
        course = courses.saveAndFlush(course);
        var lesson = new Lesson();
        lesson.setTitle("Aula de teste");
        lesson.setDescription("x".repeat(1000));
        lesson.setMediaUrl("https://media.example.com/" + "v".repeat(500));
        lesson.setThumbnailUrl("https://media.example.com/" + "t".repeat(500));
        lesson.setCourse(course);
        lesson = lessons.saveAndFlush(lesson);
        assertEquals(1000, lessons.findById(lesson.getId()).orElseThrow().getDescription().length());

        courseService.deleteCourse(course.getId(), EMAIL);

        assertFalse(courses.existsById(course.getId()));
        assertFalse(lessons.existsById(lesson.getId()));
    }

    @Test
    void persistedEmailWithExpiredTokenIsDiscardedOnRetry() {
        doThrow(new MailSendException("simulated SMTP failure")).when(mail).send(any(SimpleMailMessage.class));
        service.registerUser(new UserDTO.Request("Conta", 22, EMAIL, PASSWORD, UserRole.STUDENT));
        awaitFailedPublication();
        jdbc.update("UPDATE app_one_time_token SET expires_at = ?", Timestamp.from(Instant.now().minusSeconds(60)));
        reset(mail);
        failedPublications.resubmit(ResubmissionOptions.defaults());
        await().atMost(Duration.ofSeconds(5)).until(() -> publications.findIncompletePublications().isEmpty());
        verifyNoInteractions(mail);
    }

    @Test
    void rollbackDoesNotPersistAccountTokenOrEmailPublication() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            service.registerUser(new UserDTO.Request("Conta", 22, EMAIL, PASSWORD, UserRole.STUDENT));
            status.setRollbackOnly();
        });
        assertTrue(users.findByEmail(EMAIL).isEmpty());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM app_one_time_token", Integer.class));
        assertTrue(publications.findIncompletePublications().isEmpty());
        verifyNoInteractions(mail);
    }

    private void awaitFailedPublication() {
        await().atMost(Duration.ofSeconds(5)).until(() -> publications.findIncompletePublications().stream()
                .anyMatch(publication -> publication.getStatus() == EventPublication.Status.FAILED));
    }

    @Test
    void registrationResendAndVerificationWorkAfterCommit() throws Exception {
        service.registerUser(new UserDTO.Request("Conta", 22, EMAIL, PASSWORD, UserRole.STUDENT));
        verify(mail, timeout(3000)).send(any(SimpleMailMessage.class));
        assertFalse(users.findByEmail(EMAIL).orElseThrow().isEmailVerified());
        String first = accountToken("EMAIL_VERIFICATION");
        Browser browser = new Browser();
        assertEquals(200, browser.write(post("/api/users/resend-verification")
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + EMAIL + "\"}")).getResponse().getStatus());
        String second = accountToken("EMAIL_VERIFICATION");
        assertNotEquals(first, second);
        assertEquals(400, browser.read(get("/api/users/verify-email").param("token", first)).getResponse().getStatus());
        assertEquals(200, browser.read(get("/api/users/verify-email").param("token", second)).getResponse().getStatus());
        assertTrue(users.findByEmail(EMAIL).orElseThrow().isEmailVerified());
    }

    @Test
    void changingEmailLogsOutCurrentSessionAndRevokesOldTokens() throws Exception {
        createVerifiedUser();
        Browser browser = authenticatedBrowser();
        service.forgotPassword(new AuthDTO.ForgotPasswordRequest(EMAIL));
        assertEquals(200, browser.write(put("/api/users/{id}", users.findByEmail(EMAIL).orElseThrow().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Conta\",\"email\":\"new@investir.com\",\"age\":22,\"currentPassword\":\"" + PASSWORD + "\"}")).getResponse().getStatus());
        assertEquals(401, browser.read(get("/auth/me")).getResponse().getStatus());
        assertTrue(users.findByEmail(EMAIL).isEmpty());
        assertFalse(users.findByEmail("new@investir.com").orElseThrow().isEmailVerified());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM app_one_time_token WHERE username = ?", Integer.class, EMAIL));
    }

    @Test
    void passwordRecoveryRevokesAllSessionsAndPreviouslyIssuedResetTokens() throws Exception {
        createVerifiedUser();
        Browser first = authenticatedBrowser();
        Browser second = authenticatedBrowser();
        service.forgotPassword(new AuthDTO.ForgotPasswordRequest(EMAIL));
        String old = accountToken("PASSWORD_RESET");
        service.forgotPassword(new AuthDTO.ForgotPasswordRequest(EMAIL));
        String current = accountToken("PASSWORD_RESET");
        assertNotEquals(old, current);
        assertThrows(Exception.class, () -> service.resetPassword(new AuthDTO.ResetPasswordRequest(old, "NovaSenha123")));
        service.resetPassword(new AuthDTO.ResetPasswordRequest(current, "NovaSenha123"));
        assertEquals(401, first.read(get("/auth/me")).getResponse().getStatus());
        assertEquals(401, second.read(get("/auth/me")).getResponse().getStatus());
        assertTrue(encoder.matches("NovaSenha123", users.findByEmail(EMAIL).orElseThrow().getPassword()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM app_one_time_token", Integer.class));
    }

    @Test
    void passwordChangeDoesNotRecreateTheRevokedSessionAtResponseCommit() throws Exception {
        createVerifiedUser();
        Browser browser = authenticatedBrowser();
        assertEquals(200, browser.write(put("/api/users/update-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"NovaSenha123\"}"))
                .getResponse().getStatus());
        assertEquals(401, browser.read(get("/auth/me")).getResponse().getStatus());
    }

    @Test
    void failureSavingPasswordRollsBackTokenConsumption() {
        createVerifiedUser();
        service.forgotPassword(new AuthDTO.ForgotPasswordRequest(EMAIL));
        String token = accountToken("PASSWORD_RESET");
        doReturn("x".repeat(256)).when(encoder).encode("NovaSenha123");
        assertThrows(DataIntegrityViolationException.class,
                () -> service.resetPassword(new AuthDTO.ResetPasswordRequest(token, "NovaSenha123")));
        assertEquals(token, accountToken("PASSWORD_RESET"));
        assertTrue(encoder.matches(PASSWORD, users.findByEmail(EMAIL).orElseThrow().getPassword()));
    }

    @Test
    void publicAccountWritesAreRateLimitedAndResendDoesNotDiscloseAccountExistence() throws Exception {
        Browser browser = new Browser();
        for (String endpoint : new String[]{"register", "forgot-password", "reset-password", "resend-verification"}) {
            for (int i = 0; i < 3; i++) {
                assertEquals(400, browser.write(post("/api/users/" + endpoint)
                        .contentType(MediaType.APPLICATION_JSON).content("{}")) .getResponse().getStatus());
            }
            assertEquals(429, browser.write(post("/api/users/" + endpoint)
                    .contentType(MediaType.APPLICATION_JSON).content("{}")) .getResponse().getStatus());
        }
    }

    private User createVerifiedUser() {
        User user = new User();
        user.setName("Conta"); user.setAge(22); user.setEmail(EMAIL);
        user.setPassword(encoder.encode(PASSWORD)); user.setRole(UserRole.STUDENT); user.setEmailVerified(true);
        return users.saveAndFlush(user);
    }

    private String accountToken(String purpose) {
        return jdbc.queryForObject("SELECT token_value FROM app_one_time_token WHERE username = ? AND purpose = ?",
                String.class, EMAIL, purpose);
    }

    private Browser authenticatedBrowser() throws Exception {
        Browser browser = new Browser();
        assertEquals(204, browser.write(post("/auth/login").param("email", EMAIL).param("password", PASSWORD)).getResponse().getStatus());
        assertEquals(403, browser.read(get("/auth/me")).getResponse().getStatus());
        String token = jdbc.queryForObject("SELECT token_value FROM one_time_tokens WHERE username = ?", String.class, EMAIL);
        assertEquals(204, browser.write(post("/auth/verify-2fa").param("token", token)).getResponse().getStatus());
        assertEquals(200, browser.read(get("/auth/me")).getResponse().getStatus());
        return browser;
    }

    private class Browser {
        private Cookie session;
        MvcResult read(MockHttpServletRequestBuilder request) throws Exception {
            if (session != null) request.cookie(session);
            MvcResult result = mvc.perform(request).andReturn();
            Cookie next = result.getResponse().getCookie("SESSION");
            if (next != null) session = next.getMaxAge() == 0 ? null : next;
            return result;
        }
        MvcResult write(MockHttpServletRequestBuilder request) throws Exception {
            Cookie csrf = read(get("/csrf")).getResponse().getCookie("XSRF-TOKEN");
            assertNotNull(csrf);
            return read(request.cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue()));
        }
    }
}
