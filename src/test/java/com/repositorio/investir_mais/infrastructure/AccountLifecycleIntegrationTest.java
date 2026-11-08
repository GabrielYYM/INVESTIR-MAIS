package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.mapper.CourseMapper;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.AccountSecurityService;
import com.repositorio.investir_mais.domain.TokenDigest;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.service.registry.HttpServiceProxyRegistry;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.security.rate-limit.capacity=3", "app.mail.retry-delay=1s", "app.security.email-cooldown=0s",
                "springdoc.group-configs[0].group=education",
                "springdoc.group-configs[0].paths-to-match=/api/courses/**,/api/lessons/**"})
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountLifecycleIntegrationTest extends PostgreSqlIntegrationTest {
    private static final String EMAIL = "account@investir.com";
    private static final String PASSWORD = "SenhaSegura123";
    @Autowired private MockMvc mvc;
    @Autowired private UserService service;
    @MockitoSpyBean private AccountSecurityService accountSecurity;
    @Autowired private CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    @LocalServerPort private int port;
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
    @MockitoSpyBean private CourseMapper courseMapper;

    @Autowired private StringRedisTemplate redis;
    @MockitoSpyBean private FindByIndexNameSessionRepository<?> sessions;
    @Autowired private AuthenticationRateLimitProperties rateLimitProperties;

    @BeforeEach
    void clean() {
        await().atMost(Duration.ofSeconds(5)).until(() -> publications.findIncompletePublications().isEmpty());
        reset(users, mail, jdbc, sessions, courseMapper, accountSecurity);
        when(mail.createMimeMessage()).thenAnswer(invocation -> new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null));
        sessions.findByPrincipalName(EMAIL).keySet().forEach(sessions::deleteById);
        try (var keys = redis.scan(ScanOptions.scanOptions()
                .match(rateLimitProperties.namespace() + ":*").count(100).build())) {
            keys.forEachRemaining(redis::delete);
        }
        jdbc.update("DELETE FROM one_time_tokens");
        jdbc.update("DELETE FROM app_one_time_token");
        jdbc.update("DELETE FROM tb_lesson");
        jdbc.update("DELETE FROM tb_course");
        jdbc.update("DELETE FROM tb_user");
    }

    @Test
    void passwordRecoveryRevokesAPartialLoginAndItsPendingSecondFactor() throws Exception {
        createVerifiedUser();
        Browser pending = new Browser();
        assertEquals(204, pending.write(post("/auth/login").param("email", EMAIL).param("password", PASSWORD))
                .getResponse().getStatus());
        String code = jdbc.queryForObject("SELECT token_value FROM one_time_tokens WHERE username = ?", String.class, EMAIL);
        Browser recovery = new Browser();
        assertEquals(200, recovery.write(post("/api/users/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\"}")).getResponse().getStatus());
        String token = accountToken("PASSWORD_RESET");
        assertEquals(200, recovery.write(post("/api/users/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\",\"newPassword\":\"NovaSenhaLogin123!\"}"))
                .getResponse().getStatus());

        assertEquals(401, pending.read(get("/auth/me")).getResponse().getStatus());
        assertEquals(401, pending.write(post("/auth/verify-2fa").param("token", code)).getResponse().getStatus());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM one_time_tokens WHERE username = ?", Integer.class, EMAIL));
        assertEquals(401, recovery.write(post("/auth/login").param("email", EMAIL).param("password", PASSWORD))
                .getResponse().getStatus());
        assertEquals(204, recovery.write(post("/auth/login").param("email", EMAIL).param("password", "NovaSenhaLogin123!"))
                .getResponse().getStatus());
        assertEquals(403, recovery.read(get("/auth/me")).getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ROLE_STUDENT", "FACTOR_PASSWORD", "FACTOR_OTT"})
    void passwordRecoveryCannotIssueATokenAfterConcurrentAccountDeletion() throws Exception {
        User owner = createVerifiedUser();
        var recoveryStarted = new CountDownLatch(1);
        var resumeRecovery = new CountDownLatch(1);
        doAnswer(invocation -> {
            recoveryStarted.countDown();
            assertTrue(resumeRecovery.await(10, TimeUnit.SECONDS));
            return invocation.callRealMethod();
        }).when(accountSecurity).requestPasswordResetToken(EMAIL);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var recovery = executor.submit(new DelegatingSecurityContextCallable<>(() ->
                    new Browser().write(post("/api/users/forgot-password").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + EMAIL + "\"}"))));
            assertTrue(recoveryStarted.await(5, TimeUnit.SECONDS));
            var deletion = executor.submit(new DelegatingSecurityContextCallable<>(() ->
                    new Browser().write(delete("/api/users/{id}", owner.getId()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"currentPassword\":\"" + PASSWORD + "\"}"))));
            boolean waitedForRecovery;
            try {
                deletion.get(2, TimeUnit.SECONDS);
                waitedForRecovery = false;
            } catch (TimeoutException expected) {
                waitedForRecovery = true;
            } finally {
                resumeRecovery.countDown();
            }
            assertEquals(200, recovery.get(5, TimeUnit.SECONDS).getResponse().getStatus());
            assertEquals(204, deletion.get(5, TimeUnit.SECONDS).getResponse().getStatus());
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM app_one_time_token WHERE username = ?",
                    Integer.class, EMAIL), "A recuperação não pode recriar tokens de uma conta excluída");
            assertTrue(waitedForRecovery, "A exclusão deve aguardar a emissão para invalidar o token");
        }
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ROLE_TEACHER", "FACTOR_PASSWORD", "FACTOR_OTT"})
    void accountDeletionCannotLeaveACourseCreatedByAnInFlightRequest() throws Exception {
        User owner = createVerifiedUser();
        owner.setRole(UserRole.TEACHER);
        users.saveAndFlush(owner);
        var creationStarted = new CountDownLatch(1);
        var resumeCreation = new CountDownLatch(1);
        doAnswer(invocation -> {
            creationStarted.countDown();
            assertTrue(resumeCreation.await(10, TimeUnit.SECONDS));
            return invocation.callRealMethod();
        }).when(courseMapper).toEntity(any(CourseDTO.Request.class));

        try (var executor = Executors.newFixedThreadPool(2)) {
            var creation = executor.submit(new DelegatingSecurityContextCallable<>(() ->
                    new Browser().write(post("/api/courses").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Curso concorrente\",\"description\":\"Descrição\"}"))));
            assertTrue(creationStarted.await(5, TimeUnit.SECONDS));
            var deletion = executor.submit(new DelegatingSecurityContextCallable<>(() ->
                    new Browser().write(delete("/api/users/{id}", owner.getId()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"currentPassword\":\"" + PASSWORD + "\"}"))));
            boolean waitedForCreation;
            try {
                deletion.get(2, TimeUnit.SECONDS);
                waitedForCreation = false;
            } catch (TimeoutException expected) {
                waitedForCreation = true;
            } finally {
                resumeCreation.countDown();
            }
            assertEquals(201, creation.get(5, TimeUnit.SECONDS).getResponse().getStatus());
            assertEquals(204, deletion.get(5, TimeUnit.SECONDS).getResponse().getStatus());
            assertEquals(0, jdbc.queryForObject("""
                    SELECT count(*) FROM tb_course
                    WHERE professor_id = ? AND deleted_at IS NULL
                    """, Integer.class, owner.getId()), "A conta excluída não pode manter um curso ativo");
            assertTrue(waitedForCreation, "A exclusão deve esperar a criação terminar antes de carregar a cascata");
        }
    }

    @Test
    void encodedRequestPathsShareTheOriginalRateLimitBucket() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            for (int i = 0; i < 3; i++) {
                assertEquals(401, sendInvalidLogin(client, "/auth/login"));
            }
            assertEquals(429, sendInvalidLogin(client, "/auth/login"));
            assertEquals(429, sendInvalidLogin(client, "/%61uth/login"));
        }
    }

    private int sendInvalidLogin(HttpClient client, String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "XSRF-TOKEN=rate-limit-test")
                .header("X-XSRF-TOKEN", "rate-limit-test")
                .POST(HttpRequest.BodyPublishers.ofString("email=unknown%40investir.com&password=invalid"))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    @Test
    void concurrentProfileUpdateCannotRestorePasswordFromBeforeReset() throws Exception {
        User account = createVerifiedUser();
        String token = accountSecurity.generatePasswordResetToken(EMAIL);
        String newPassword = "SenhaNova123456!";
        var started = new CountDownLatch(1);
        try (var executor = Executors.newSingleThreadExecutor()) {
            var result = new TransactionTemplate(transactionManager).execute(status -> {
                service.resetPassword(new AuthDTO.ResetPasswordRequest(token, newPassword));
                var update = executor.submit(() -> {
                    started.countDown();
                    return assertThrows(ResponseStatusException.class, () -> service.updateProfile(EMAIL, account.getId(),
                            new UserDTO.UpdateProfileRequest("Novo Nome", EMAIL, null, PASSWORD)));
                });
                assertDoesNotThrow(() -> assertTrue(started.await(5, TimeUnit.SECONDS)));
                assertThrows(TimeoutException.class, () -> update.get(500, TimeUnit.MILLISECONDS));
                return update;
            });
            assertEquals(401, result.get(5, TimeUnit.SECONDS).getStatusCode().value());
        }
        assertTrue(encoder.matches(newPassword, users.findByEmail(EMAIL).orElseThrow().getPassword()));
        assertEquals(0, jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.columns
                WHERE table_name = 'tb_user_audit' AND column_name = 'password'
                """, Integer.class));
    }

    @Test
    void educationOpenApiIncludesBothResources() throws Exception {
        var response = mvc.perform(get("/v3/api-docs/education")).andReturn().getResponse();
        assertEquals(200, response.getStatus());
        String contract = response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(contract.contains("\"/api/courses\""));
        assertTrue(contract.contains("\"/api/lessons/course/{courseId}\""));
        assertTrue(contract.contains("#/components/schemas/CourseRequest"));
        assertTrue(contract.contains("#/components/schemas/LessonRequest"));
        assertFalse(contract.contains("\"name\":\"pageable\""));
        String exportPath = System.getProperty("education.openapi.output");
        if (exportPath != null) {
            var mapper = new tools.jackson.databind.ObjectMapper();
            var document = (tools.jackson.databind.node.ObjectNode) mapper.readTree(contract);
            document.remove("servers");
            Files.writeString(Path.of(exportPath), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(document) + "\n");
        }
    }

    @Test
    void invalidBrapiTickersDoNotOpenTheSharedCircuit() {
        var registry = mock(HttpServiceProxyRegistry.class);
        when(registry.getClient("brapi", AssetQuoteClient.class)).thenReturn(ticker -> {
            if (!ticker.equals("PETR4")) {
                throw HttpClientErrorException.create(org.springframework.http.HttpStatus.NOT_FOUND,
                        "Not found", org.springframework.http.HttpHeaders.EMPTY, new byte[0], null);
            }
            return "ok";
        });
        var client = new BrapiClientConfiguration().resilientAssetQuoteClient(registry, circuitBreakerFactory);
        for (int i = 0; i < 6; i++) {
            String ticker = "INVALID" + i;
            assertEquals(404, assertThrows(ResponseStatusException.class, () -> client.getQuote(ticker)).getStatusCode().value());
        }
        assertEquals("ok", client.getQuote("PETR4"));
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
    void deletingCourseSoftDeletesItsLessonsAndSupportsLongDescriptionsAndUrls() {
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
        assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_course WHERE id = ?", Timestamp.class, course.getId()));
        assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_lesson WHERE id = ?", Timestamp.class, lesson.getId()));
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
    void startupSchemaWidensExistingEmailEventsAndLessonFieldsIdempotently() {
        jdbc.execute("ALTER TABLE event_publication ALTER COLUMN serialized_event TYPE varchar(255)");
        jdbc.execute("ALTER TABLE tb_lesson ALTER COLUMN description TYPE varchar(255)");
        jdbc.execute("ALTER TABLE tb_lesson ALTER COLUMN media_url TYPE varchar(255)");
        jdbc.execute("ALTER TABLE tb_lesson ALTER COLUMN thumbnail_url TYPE varchar(255)");
        var schema = new ResourceDatabasePopulator(new ClassPathResource("schema-postgresql.sql"));
        schema.execute(jdbc.getDataSource());
        schema.execute(jdbc.getDataSource());

        assertEquals("text", jdbc.queryForObject("""
                SELECT data_type FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'event_publication'
                  AND column_name = 'serialized_event'
                """, String.class));
        assertEquals(3, jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'tb_lesson' AND data_type = 'text'
                  AND column_name IN ('description', 'media_url', 'thumbnail_url')
                """, Integer.class));
    }

    @Test
    void registrationAndRecoverySupportMaximumLengthEmail() throws Exception {
        String email = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63)
                + "." + "d".repeat(56) + ".test";
        assertEquals(254, email.length());
        Browser browser = new Browser();

        assertEquals(202, browser.write(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Conta\",\"age\":22,\"email\":\"" + email
                        + "\",\"password\":\"" + PASSWORD + "\"}"))
                .getResponse().getStatus());
        assertTrue(users.findByEmail(email).isPresent());
        verify(mail, timeout(3000)).send(argThat((SimpleMailMessage message) ->
                email.equals(message.getTo()[0]) && message.getSubject().startsWith("Verifique")));

        assertEquals(200, browser.write(post("/api/users/forgot-password")
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\"}"))
                .getResponse().getStatus());
        verify(mail, timeout(3000)).send(argThat((SimpleMailMessage message) ->
                email.equals(message.getTo()[0]) && message.getSubject().startsWith("Redefinição")));
        await().atMost(Duration.ofSeconds(5)).until(() -> publications.findIncompletePublications().isEmpty());
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
        assertEquals(400, browser.write(post("/api/users/verify-email").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("token", first)).getResponse().getStatus());
        var form = browser.read(get("/api/users/verify-email").param("token", second)).getResponse();
        assertEquals(200, form.getStatus());
        assertFalse(users.findByEmail(EMAIL).orElseThrow().isEmailVerified());
        assertEquals("strict-origin", form.getHeader("Referrer-Policy"));
        var field = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(form.getContentAsString());
        assertTrue(field.find());
        var confirmed = mvc.perform(post("/api/users/verify-email").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.TEXT_HTML)
                .header("Origin", "http://localhost")
                .cookie(form.getCookie("XSRF-TOKEN")).param("_csrf", field.group(1)).param("token", second))
                .andReturn().getResponse();
        assertEquals(200, confirmed.getStatus());
        assertTrue(confirmed.getContentType().startsWith(MediaType.TEXT_HTML_VALUE));
        assertTrue(confirmed.getContentAsString().contains("E-mail confirmado"));
        assertTrue(confirmed.getContentAsString().contains("Ir para entrar"));
        assertTrue(users.findByEmail(EMAIL).orElseThrow().isEmailVerified());
    }

    @Test
    void deletingAccountRevokesEverySessionAndPreventsLoginAndTokenRecovery() throws Exception {
        User user = createVerifiedUser();
        Browser first = authenticatedBrowser();
        Browser second = authenticatedBrowser();
        service.forgotPassword(new AuthDTO.ForgotPasswordRequest(EMAIL));
        String resetToken = accountToken("PASSWORD_RESET");

        assertEquals(204, first.write(delete("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + PASSWORD + "\"}")).getResponse().getStatus());

        assertEquals(401, first.read(get("/auth/me")).getResponse().getStatus());
        assertEquals(401, second.read(get("/auth/me")).getResponse().getStatus());
        assertEquals(401, new Browser().write(post("/auth/login")
                .param("email", EMAIL).param("password", PASSWORD)).getResponse().getStatus());
        assertTrue(users.findByEmail(EMAIL).isEmpty());
        assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_user WHERE id = ?", Timestamp.class, user.getId()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM app_one_time_token WHERE username = ?", Integer.class, EMAIL));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM one_time_tokens WHERE username = ?", Integer.class, EMAIL));
        assertThrows(ResponseStatusException.class,
                () -> service.resetPassword(new AuthDTO.ResetPasswordRequest(resetToken, "NovaSenha123")));
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
    void failedSessionLookupPreservesPasswordAndAllowsSafeRecoveryRetry() throws Exception {
        assertRecoveryRollsBackWhenRevocationFails(false);
    }

    @Test
    void failedSessionDeletionPreservesPasswordAndAllowsSafeRecoveryRetry() throws Exception {
        assertRecoveryRollsBackWhenRevocationFails(true);
    }

    private void assertRecoveryRollsBackWhenRevocationFails(boolean failDeletion) throws Exception {
        createVerifiedUser();
        Browser first = authenticatedBrowser();
        Browser second = authenticatedBrowser();
        String token = accountSecurity.generatePasswordResetToken(EMAIL);
        if (failDeletion) {
            doThrow(new RedisConnectionFailureException("fixture revocation outage"))
                    .when(sessions).deleteById(anyString());
        } else {
            doThrow(new RedisConnectionFailureException("fixture revocation outage"))
                    .when(sessions).findByPrincipalName(EMAIL);
        }
        assertThrows(RedisConnectionFailureException.class,
                () -> service.resetPassword(new AuthDTO.ResetPasswordRequest(token, "NovaSenha123")));
        reset(sessions);
        assertEquals(200, first.read(get("/auth/me")).getResponse().getStatus());
        assertTrue(encoder.matches(PASSWORD, users.findByEmail(EMAIL).orElseThrow().getPassword()),
                "A failed revocation must not commit a new password while an old session still works");
        service.resetPassword(new AuthDTO.ResetPasswordRequest(token, "NovaSenha123"));
        assertTrue(encoder.matches("NovaSenha123", users.findByEmail(EMAIL).orElseThrow().getPassword()));
        assertEquals(401, first.read(get("/auth/me")).getResponse().getStatus());
        assertEquals(401, second.read(get("/auth/me")).getResponse().getStatus());
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
        String digest = jdbc.queryForObject("SELECT token_value FROM app_one_time_token WHERE username = ? AND purpose = ?",
                String.class, EMAIL, purpose);
        Pattern uuid = Pattern.compile("[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}");
        return await().atMost(Duration.ofSeconds(5)).until(() -> mockingDetails(mail).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("send"))
                .map(invocation -> invocation.getArgument(0))
                .filter(SimpleMailMessage.class::isInstance)
                .map(SimpleMailMessage.class::cast)
                .flatMap(message -> uuid.matcher(message.getText()).results())
                .map(java.util.regex.MatchResult::group)
                .filter(token -> TokenDigest.sha256(token).equals(digest))
                .findFirst(), java.util.Optional::isPresent).orElseThrow();
    }

    private Browser authenticatedBrowser() throws Exception {
        Browser browser = new Browser();
        assertEquals(204, browser.write(post("/auth/login").param("email", EMAIL).param("password", PASSWORD)).getResponse().getStatus());
        String partialSession = browser.session.getValue();
        assertEquals(403, browser.read(get("/auth/me")).getResponse().getStatus());
        String token = jdbc.queryForObject("SELECT token_value FROM one_time_tokens WHERE username = ?", String.class, EMAIL);
        assertEquals(204, browser.write(post("/auth/verify-2fa").param("token", token)).getResponse().getStatus());
        assertNotEquals(partialSession, browser.session.getValue());
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
