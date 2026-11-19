package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.education.DTO.*;
import com.repositorio.investir_mais.domain.education.model.ContentReport.*;
import com.repositorio.investir_mais.domain.education.repository.*;
import com.repositorio.investir_mais.domain.education.service.*;
import com.repositorio.investir_mais.domain.legal.service.LegalDocumentsService;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.privacy.account-erasure-cron=-", "app.security.token-cleanup-cron=-", "spring.autoconfigure.exclude=org.springframework.boot.session.autoconfigure.SessionAutoConfiguration,org.springframework.boot.session.data.redis.autoconfigure.SessionDataRedisAutoConfiguration"})
@AutoConfigureMockMvc @ActiveProfiles("test")
@WithMockUser(username = "admin@recovery.test", authorities = {"ROLE_ADMIN", "FACTOR_PASSWORD", "FACTOR_OTT"})
class ModerationAndRecoveryIntegrationTest extends PostgreSqlIntegrationTest {
    @MockitoBean FindByIndexNameSessionRepository<?> sessions;
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired UserService accounts;
    @Autowired AccountDeletionService deletions;
    @Autowired CourseService courses;
    @Autowired LessonService lessons;
    @Autowired QuestionService questions;
    @Autowired ContentTrashService trash;
    @Autowired ContentReportRepository reports;
    @Autowired LegalDocumentsService legal;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired AdminPendingRepository pending;
    @Autowired ContentReportService reportService;
    @Autowired com.repositorio.investir_mais.domain.user.repository.TeacherApplicationRepository applications;
    User admin, teacher, student;

    // The optional native PostgreSQL server is used only for automated tests; the official database is never a fallback.
    @BeforeAll static void startContainersWhenNeeded() {
        if (System.getenv("INVESTIR_TEST_DATABASE_URL") == null) {
            Assumptions.assumeTrue(org.testcontainers.DockerClientFactory.instance().isDockerAvailable(), "Docker ou PostgreSQL isolado é necessário.");
            POSTGRES.start(); REDIS.start();
        }
    }
    @AfterAll static void stopContainersWhenNeeded() {
        if (System.getenv("INVESTIR_TEST_DATABASE_URL") == null) { REDIS.stop(); POSTGRES.stop(); }
    }
    @BeforeEach void fixtures() {
        admin = users.findByEmail("admin@recovery.test").orElseGet(() -> account(UserRole.ADMIN, "admin@recovery.test"));
        teacher = account(UserRole.TEACHER, UUID.randomUUID() + "@recovery.test");
        student = account(UserRole.STUDENT, UUID.randomUUID() + "@recovery.test");
        for (var user : List.of(admin, teacher, student)) legal.accept(user.getEmail(), legal.current().version());
    }
    private User account(UserRole role, String email) {
        var user = new User(); user.setName("Pessoa teste"); user.setEmail(email); user.setRole(role); user.setEmailVerified(true);
        user.setPassword(encoder.encode("Temporary123!")); return users.saveAndFlush(user);
    }
    private RequestPostProcessor as(User user) {
        return user(user.getEmail()).authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole()),
            FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY), FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.OTT_AUTHORITY));
    }
    private CourseDTO.Response course() { return courses.createCourse(new CourseDTO.Request("Investimentos iniciais", "Descrição", null, "Renda fixa", null), teacher.getEmail()); }
    private LessonDTO.Response lesson(UUID courseId) { return lessons.createLesson(courseId, new LessonDTO.Request("Aula teste", "Descrição", "https://youtu.be/dQw4w9WgXcQ", null), teacher.getEmail()); }
    private QuestionDTO.TeacherQuestion question(UUID courseId, int afterLesson) {
        return questions.create(courseId, new QuestionDTO.Request("Questão teste", afterLesson, 0, List.of("Sim", "Não"), 0), teacher.getEmail());
    }

    @Test void reportsArePrivateValidatedAndRemovedContentRemainsInTrash() throws Exception {
        var course = course(); var lesson = lesson(course.id()); var question = question(course.id(), lesson.position());
        String body = """
            {"targetType":"LESSON","targetId":"%s","courseId":"%s","reason":"OFF_TOPIC","description":"Conteúdo sem relação com investimentos"}
            """.formatted(lesson.id(), course.id());
        mvc.perform(post("/api/reports").with(as(student)).with(csrf()).contentType("application/json").content(body))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("OPEN")).andExpect(jsonPath("$.reporterId").doesNotExist());
        mvc.perform(post("/api/reports").with(as(student)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isConflict());
        mvc.perform(get("/api/reports").with(as(student))).andExpect(status().isForbidden());
        mvc.perform(get("/api/reports").with(as(admin))).andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray()).andExpect(jsonPath("$.page.totalElements").isNumber());
        mvc.perform(get("/api/trash").with(as(teacher))).andExpect(status().isForbidden());
        var report = reports.findAll().stream().filter(value -> value.getTargetId().equals(lesson.id())).findFirst().orElseThrow();
        mvc.perform(post("/api/reports/{id}/review", report.getId()).with(as(admin)).with(csrf()).contentType("application/json")
            .content("{\"status\":\"RESOLVED\",\"note\":\"Conteúdo conferido\",\"removeLesson\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVED"));
        assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_lesson WHERE id=?", Object.class, lesson.id()));
        assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_question WHERE id=?", Object.class, question.id()));
        assertTrue(jdbc.queryForObject("SELECT count(*) FROM tb_content_report_aud WHERE id=?", Integer.class, report.getId()) >= 2);
        mvc.perform(post("/api/reports/{id}/review", report.getId()).with(as(admin)).with(csrf()).contentType("application/json")
            .content("{\"status\":\"DISMISSED\",\"note\":\"Reanálise\"}")).andExpect(status().isConflict());
    }

    @Test void reportTargetsAndReasonsCannotBeForged() throws Exception {
        var course = course(); var other = course(); var lesson = lesson(other.id());
        mvc.perform(post("/api/reports").with(as(student)).with(csrf()).contentType("application/json").content("""
            {"targetType":"LESSON","targetId":"%s","courseId":"%s","reason":"OTHER","description":"Outro motivo"}
            """.formatted(lesson.id(), course.id()))).andExpect(status().isNotFound());
        mvc.perform(post("/api/reports").with(as(student)).with(csrf()).contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test void contentRestorationRespectsParentsAndDoesNotRestorePreviouslyDeletedChildren() {
        var course = course(); var lesson = lesson(course.id()); var question = question(course.id(), lesson.position());
        courses.deleteCourse(course.id(), teacher.getEmail());
        assertThrows(ResponseStatusException.class, () -> trash.restore("lessons", lesson.id(), admin.getEmail()));
        trash.restore("courses", course.id(), admin.getEmail());
        assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_lesson WHERE id=?", Object.class, lesson.id()));
        assertThrows(ResponseStatusException.class, () -> trash.restore("questions", question.id(), admin.getEmail()));
        trash.restore("lessons", lesson.id(), admin.getEmail()); trash.restore("questions", question.id(), admin.getEmail());
        assertNull(jdbc.queryForObject("SELECT deleted_at FROM tb_question WHERE id=?", Object.class, question.id()));
        assertEquals(3, jdbc.queryForObject("SELECT count(*) FROM tb_content_restoration WHERE entity_id IN (?,?,?)", Integer.class, course.id(), lesson.id(), question.id()));
    }

    @Test void accountRecoveryPreservesDataProgressAndPreviouslyDeletedContent() {
        var removed = course(); courses.deleteCourse(removed.id(), teacher.getEmail());
        var active = course(); var lesson = lesson(active.id());
        lessons.updateLessonProgress(active.id(), lesson.id(), true, student.getEmail());
        accounts.deleteAccount(teacher.getEmail(), teacher.getId(), new UserDTO.DeleteAccountRequest("Temporary123!"));
        assertEquals(teacher.getEmail(), jdbc.queryForObject("SELECT email FROM tb_user WHERE id=?", String.class, teacher.getId()));
        assertFalse(users.existsById(teacher.getId()));
        assertThrows(ResponseStatusException.class, () -> trash.restore("courses", active.id(), admin.getEmail()));
        deletions.restore(teacher.getId(), admin.getId(), "Pedido do titular verificado #123");
        assertTrue(users.existsById(teacher.getId()));
        assertNull(jdbc.queryForObject("SELECT deleted_at FROM tb_course WHERE id=?", Object.class, active.id()));
        assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_course WHERE id=?", Object.class, removed.id()));
        assertEquals(100.0, courses.getCourseProgress(active.id(), student.getEmail()).progressPercentage());
        accounts.deleteAccount(student.getEmail(), student.getId(), new UserDTO.DeleteAccountRequest("Temporary123!"));
        deletions.restore(student.getId(), admin.getId(), "Pedido confirmado #456");
        assertEquals(100.0, courses.getCourseProgress(active.id(), student.getEmail()).progressPercentage());
    }

    @Test void expiredAccountCannotBeRestoredAndIsPhysicallyErasedWithItsSnapshots() {
        var course = course(); lesson(course.id());
        accounts.deleteAccount(teacher.getEmail(), teacher.getId(), new UserDTO.DeleteAccountRequest("Temporary123!"));
        assertFalse(deletions.erase(teacher.getId(), false));
        jdbc.update("UPDATE account_deletion SET purge_after=? WHERE user_id=? AND status='PENDING'", java.sql.Timestamp.from(Instant.now().minusSeconds(1)), teacher.getId());
        assertThrows(ResponseStatusException.class, () -> deletions.restore(teacher.getId(), admin.getId(), "Pedido atrasado"));
        assertTrue(deletions.erase(teacher.getId(), false));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM tb_user WHERE id=?", Integer.class, teacher.getId()));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM tb_user_audit WHERE id=?", Integer.class, teacher.getId()));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM tb_course WHERE id=?", Integer.class, course.id()));
        assertTrue(users.existsById(student.getId()));
    }

    @Test void immediateErasureNeedsTheOwnersPasswordAndDoesNotCreateARecoveryWindow() throws Exception {
        var publicationId = UUID.randomUUID();
        jdbc.update("INSERT INTO event_publication(id,publication_date,listener_id,serialized_event,event_type,completion_attempts,status) VALUES (?,CURRENT_TIMESTAMP,'test',?,? ,0,'FAILED')",
            publicationId, "{\"email\":\"" + student.getEmail() + "\",\"token\":\"expired\",\"purpose\":\"VERIFY_EMAIL\"}", "com.repositorio.investir_mais.domain.AccountEmailRequested");
        mvc.perform(post("/api/users/{id}/erasure", student.getId()).with(as(student)).with(csrf()).contentType("application/json")
            .content("{\"currentPassword\":\"incorrect\"}")).andExpect(status().isUnauthorized());
        assertTrue(users.existsById(student.getId()));
        var currentSession = new org.springframework.mock.web.MockHttpSession();
        mvc.perform(post("/api/users/{id}/erasure", student.getId()).session(currentSession).with(as(student)).with(csrf()).contentType("application/json")
            .content("{\"currentPassword\":\"Temporary123!\"}")).andExpect(status().isNoContent());
        assertTrue(currentSession.isInvalid());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM tb_user WHERE id=?", Integer.class, student.getId()));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM account_deletion WHERE user_id=?", Integer.class, student.getId()));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM tb_user_audit WHERE id=?", Integer.class, student.getId()));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM event_publication WHERE id=?", Integer.class, publicationId));
        assertEquals(1, legal.history(admin.getEmail()).size());
    }

    @Test void exposesTheNewEndpointsInTheApiContract() throws Exception {
        String contract = mvc.perform(get("/v3/api-docs").with(as(admin))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(contract.contains("/api/reports")); assertTrue(contract.contains("/api/trash/accounts/{id}/restore"));
        assertTrue(contract.contains("/api/admin/pending"));
        Files.createDirectories(Path.of("target")); Files.writeString(Path.of("target/moderation-openapi.json"), contract);
    }

    @Test @org.springframework.transaction.annotation.Transactional
    void adminPendingSummaryCountsOnlyActionableItemsAndEnforcesAccess() throws Exception {
        Instant now = Instant.now();
        var before = pending.summarize(now);
        var course = course(); var lesson = lesson(course.id());
        var report = reportService.submit(student.getEmail(), new ContentReportDTO.Submit(Target.LESSON, lesson.id(), course.id(), Reason.OTHER, "Revisar conteúdo"));
        var candidate = account(UserRole.STUDENT, UUID.randomUUID() + "@recovery.test");
        legal.accept(candidate.getEmail(), legal.current().version());
        for (var user : List.of(student, candidate)) {
            var application = new com.repositorio.investir_mais.domain.user.model.TeacherApplication();
            application.setUserId(user.getId()); application.setStatus("PENDING"); application.setSubmittedAt(now);
            applications.saveAndFlush(application);
        }
        var distant = account(UserRole.STUDENT, UUID.randomUUID() + "@recovery.test");
        for (var user : List.of(student, teacher, distant)) {
            accounts.deleteAccount(user.getEmail(), user.getId(), new UserDTO.DeleteAccountRequest("Temporary123!"));
        }
        jdbc.update("UPDATE account_deletion SET purge_after=? WHERE user_id=? AND status='PENDING'", java.sql.Timestamp.from(now.plus(7, java.time.temporal.ChronoUnit.DAYS)), student.getId());
        jdbc.update("UPDATE account_deletion SET purge_after=? WHERE user_id=? AND status='PENDING'", java.sql.Timestamp.from(now), teacher.getId());

        var summary = pending.summarize(now);
        assertEquals(before.openReports() + 1, summary.openReports());
        assertEquals(before.pendingTeacherApplications() + 1, summary.pendingTeacherApplications());
        assertEquals(before.recoverableAccounts() + 2, summary.recoverableAccounts());
        assertEquals(before.expiringAccounts() + 1, summary.expiringAccounts());
        assertEquals(before.expiredAccounts() + 1, summary.expiredAccounts());
        mvc.perform(get("/api/admin/pending").with(as(admin))).andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
            .andExpect(jsonPath("$.openReports").value((int) summary.openReports()))
            .andExpect(jsonPath("$.pendingTeacherApplications").value((int) summary.pendingTeacherApplications()))
            .andExpect(jsonPath("$.expiringAccounts").value((int) summary.expiringAccounts()))
            .andExpect(jsonPath("$.expiredAccounts").value((int) summary.expiredAccounts()))
            .andExpect(jsonPath("$.nextRecoveryDeadline").isString()).andExpect(jsonPath("$.items").doesNotExist());
        mvc.perform(get("/api/admin/pending").with(as(candidate))).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/pending").with(anonymous())).andExpect(status().isUnauthorized());

        var reviewed = reports.findById(report.id()).orElseThrow(); reviewed.setStatus(Status.RESOLVED); reports.saveAndFlush(reviewed);
        var approved = applications.findByUserId(candidate.getId()).orElseThrow(); approved.setStatus("APPROVED"); applications.saveAndFlush(approved);
        assertEquals(before.openReports(), pending.summarize(now).openReports());
        assertEquals(before.pendingTeacherApplications(), pending.summarize(now).pendingTeacherApplications());
    }

    @Test void legalEvidenceCannotBeChangedOrErasedForAnotherSubject() {
        var transaction = new org.springframework.transaction.support.TransactionTemplate(
            new org.springframework.jdbc.datasource.DataSourceTransactionManager(Objects.requireNonNull(jdbc.getDataSource())));
        assertThrows(org.springframework.dao.DataAccessException.class, () -> transaction.executeWithoutResult(status ->
            jdbc.update("DELETE FROM legal_document_acceptance WHERE user_id=?", student.getId())));
        assertThrows(org.springframework.dao.DataAccessException.class, () -> transaction.executeWithoutResult(status -> {
            jdbc.queryForObject("SELECT set_config('app.erasure_user_id', ?, true)", String.class, UUID.randomUUID().toString());
            jdbc.update("DELETE FROM legal_document_acceptance WHERE user_id=?", student.getId());
        }));
        assertEquals(1, legal.history(student.getEmail()).size());
        assertEquals(1, legal.history(admin.getEmail()).size());
    }
}
