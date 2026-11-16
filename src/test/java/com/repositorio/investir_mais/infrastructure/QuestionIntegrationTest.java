package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.LegalDocumentsService;
import com.repositorio.investir_mais.domain.education.DTO.*;
import com.repositorio.investir_mais.domain.education.model.*;
import com.repositorio.investir_mais.domain.education.repository.*;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class QuestionIntegrationTest extends PostgreSqlIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired CourseRepository courses;
    @Autowired LessonRepository lessons;
    @Autowired QuestionRepository questions;
    @Autowired DataSource dataSource;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired LegalDocumentsService legal;
    User teacher, student, other, admin;
    Course course;
    Lesson first, second;

    @BeforeEach
    void setup() {
        teacher = user(UserRole.TEACHER); student = user(UserRole.STUDENT);
        other = user(UserRole.STUDENT); admin = user(UserRole.ADMIN);
        course = new Course(); course.setName("Curso de teste"); course.setDescription("Descrição"); course.setProfessor(teacher);
        courses.saveAndFlush(course);
        first = lesson(1); second = lesson(2);
        student.enrollIn(course); student = users.saveAndFlush(student);
    }

    @AfterEach
    void cleanupCommittedFixtures() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) return;
        jdbc.update("delete from tb_question_pass where question_id in (select id from tb_question where course_id = ?)", course.getId());
        jdbc.update("delete from tb_question where course_id = ?", course.getId());
        jdbc.update("delete from tb_lesson_progress where lesson_id in (select id from tb_lesson where course_id = ?)", course.getId());
        jdbc.update("delete from tb_course_enrollment where course_id = ?", course.getId());
        jdbc.update("delete from tb_lesson where course_id = ?", course.getId());
        jdbc.update("delete from tb_course where id = ?", course.getId());
        for (var u : List.of(teacher, student, other, admin)) {
            jdbc.update("delete from tb_user_audit where id = ?", u.getId());
            jdbc.update("delete from tb_user where id = ?", u.getId());
        }
    }

    @Test
    void courseCreationStoresOptionsInQuestionWithoutExtraOptionOrAnswerTables() throws Exception {
        var request = new CourseDTO.Request("Curso com perguntas", "Descrição", List.of(request(0, 0)));
        var response = mvc.perform(post("/api/courses").with(as(teacher)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request))).andExpect(status().isCreated()).andReturn().getResponse();
        UUID id = json.readValue(response.getContentAsString(), CourseDTO.Response.class).id();
        em.flush(); em.clear();
        var q = questions.findAllByCourseIdOrderByAfterLessonAscPositionAscIdAsc(id).getFirst();
        assertEquals(List.of("Certa", "Errada"), q.getOptions()); assertEquals(0, q.getCorrectOptionIndex());
        assertEquals(0, jdbc.queryForObject("select count(*) from information_schema.tables where table_schema = 'public' "
                + "and table_name in ('tb_question_answer', 'tb_question_option')", Integer.class));
    }

    @Test
    void wrongAnswerStoresNothingAndCorrectAnswerStoresOnlyStudentAndQuestion() throws Exception {
        var q = create(1, 0);
        answer(q.id(), 0, 409);
        progress(first, 200);
        assertFalse(answer(q.id(), 1, 200).passed());
        assertFalse(answer(q.id(), 1, 200).passed());
        assertEquals(0, passCount());
        progress(second, 409);
        mvc.perform(get("/api/lessons/" + second.getId()).with(as(student))).andExpect(status().isConflict());
        mvc.perform(get(base()).with(as(student))).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correctOptionIndex").doesNotExist())
                .andExpect(jsonPath("$[0].options[0]").value("Certa"));
        assertTrue(answer(q.id(), 0, 200).passed());
        assertTrue(answer(q.id(), 0, 200).passed()); assertEquals(1, passCount());
        assertEquals(2, jdbc.queryForObject("select count(*) from information_schema.columns "
                + "where table_schema = 'public' and table_name = 'tb_question_pass'", Integer.class));
        progress(second, 200);
        mvc.perform(get("/api/courses/" + course.getId() + "/progress").with(as(student)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.passedQuestions").value(1))
                .andExpect(jsonPath("$.progressPercentage").value(100.0));
    }

    @Test
    void crudRemainsAvailableAfterPassAndEditingInvalidatesOnlyThatQuestion() throws Exception {
        var q = create(0, 0); var next = create(0, 1);
        answer(q.id(), 0, 200); answer(next.id(), 0, 200); assertEquals(2, passCount());
        mvc.perform(put(base() + "/" + q.id()).with(as(teacher)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new QuestionDTO.Request("Pergunta alterada", 0, 0, List.of("A", "B"), 1))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correctOptionIndex").value(1));
        assertEquals(1, passCount()); progress(first, 409);
        assertFalse(answer(q.id(), 0, 200).passed()); assertTrue(answer(q.id(), 1, 200).passed());
        progress(first, 200);
        mvc.perform(delete(base() + "/" + q.id()).with(as(teacher)).with(csrf())).andExpect(status().isNoContent());
        em.flush(); em.clear(); assertTrue(questions.findById(q.id()).isEmpty());
        create(2, 0);
    }

    @Test
    void pendingFinalQuestionPreventsOneHundredPercentAndQuestionsAreOrdered() throws Exception {
        var one = create(0, 0); var two = create(0, 1); var last = create(2, 0);
        answer(two.id(), 0, 409); answer(last.id(), 0, 409);
        answer(one.id(), 0, 200); answer(two.id(), 0, 200);
        progress(first, 200); progress(second, 200);
        mvc.perform(get("/api/courses/" + course.getId() + "/progress").with(as(student)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.progressPercentage").value(80.0));
        answer(last.id(), 0, 200);
        mvc.perform(get("/api/courses/" + course.getId() + "/progress").with(as(student)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.progressPercentage").value(100.0));
    }

    @Test
    void validationRejectsInvalidIndexAndDuplicateOptionsWithoutPersistingAnything() throws Exception {
        var invalid = new QuestionDTO.Request("Pergunta", 0, 0, List.of("A", "B"), 2);
        mvc.perform(post(base()).with(as(teacher)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(invalid))).andExpect(status().isBadRequest());
        var duplicate = new QuestionDTO.Request("Pergunta", 0, 0, List.of("A", " a "), 0);
        mvc.perform(post(base()).with(as(teacher)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(duplicate))).andExpect(status().isBadRequest());
        assertEquals(0, questions.count());
        var q = create(0, 0); answer(q.id(), 2, 400); answer(q.id(), -1, 400);
        assertEquals(0, passCount());
    }

    @Test
    void permissionsEnrollmentAndCsrfAreEnforced() throws Exception {
        var q = create(0, 0); User anotherTeacher = user(UserRole.TEACHER);
        mvc.perform(get(base() + "/manage").with(as(student))).andExpect(status().isForbidden());
        mvc.perform(get(base() + "/manage").with(as(anotherTeacher))).andExpect(status().isForbidden());
        mvc.perform(get(base() + "/manage").with(as(admin))).andExpect(status().isOk());
        mvc.perform(get(base()).with(as(other))).andExpect(status().isForbidden());
        mvc.perform(get(base())).andExpect(status().isUnauthorized());
        mvc.perform(post(base() + "/" + q.id() + "/answers").with(as(student)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIndex\":0}")).andExpect(status().isForbidden());
        mvc.perform(post(base() + "/" + q.id() + "/answers").with(as(other)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIndex\":0}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/lessons/" + second.getId()).with(as(teacher))).andExpect(status().isOk());
        mvc.perform(get("/api/lessons/" + second.getId()).with(as(admin))).andExpect(status().isOk());
        assertEquals(0, passCount());
    }

    @Test
    void softDeletePreservesPassButExcludesDeletedQuestionsFromProgress() throws Exception {
        var q = create(0, 0); answer(q.id(), 0, 200);
        em.flush(); em.clear(); courses.deleteById(course.getId()); courses.flush(); em.clear();
        assertTrue(questions.findById(q.id()).isEmpty()); assertEquals(1, passCount());
        assertEquals(0, lessons.countPassedQuestions(course.getId(), student.getId()));
        assertNotNull(jdbc.queryForObject("select deleted_at from tb_question where id = ?", Timestamp.class, q.id()));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentCorrectAnswersCreateOnePass() throws Exception {
        var q = create(0, 0);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Callable<Boolean> send = () -> {
                assertTrue(start.await(10, TimeUnit.SECONDS)); return answer(q.id(), 0, 200).passed();
            };
            var one = executor.submit(send); var two = executor.submit(send); start.countDown();
            assertTrue(one.get(15, TimeUnit.SECONDS)); assertTrue(two.get(15, TimeUnit.SECONDS));
        }
        assertEquals(1, passCount());
    }

    private QuestionDTO.Request request(int afterLesson, int position) {
        return new QuestionDTO.Request("Qual alternativa?", afterLesson, position, List.of("Certa", "Errada"), 0);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"TEACHER", "ADMIN"})
    void lessonOrderPersistsForStudentsAndQuestionsFollowTheirLesson(UserRole role) throws Exception {
        User actor = role == UserRole.ADMIN ? admin : teacher;
        var question = create(1, 1);
        String orderPath = "/api/lessons/course/" + course.getId() + "/order";
        mvc.perform(put(orderPath).with(as(actor)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LessonDTO.OrderRequest(List.of(second.getId(), first.getId())))))
                .andExpect(status().isNoContent());
        em.flush(); em.clear();
        assertEquals(2, questions.findById(question.id()).orElseThrow().getAfterLesson());
        mvc.perform(get("/api/lessons/course/" + course.getId()).with(as(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(second.getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(first.getId().toString()));
    }

    @Test
    void orderEndpointRejectsOtherTeachersStudentsAndInvalidLists() throws Exception {
        String orderPath = "/api/lessons/course/" + course.getId() + "/order";
        User unrelatedTeacher = user(UserRole.TEACHER);
        for (User user : List.of(other, student, unrelatedTeacher)) {
            mvc.perform(put(orderPath).with(as(user)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(new LessonDTO.OrderRequest(List.of(second.getId(), first.getId())))))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(put(orderPath).with(as(teacher)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LessonDTO.OrderRequest(List.of(first.getId(), first.getId())))))
                .andExpect(status().isBadRequest());
        for (var ids : List.of(List.of(first.getId()), List.of(first.getId(), UUID.randomUUID()))) {
            mvc.perform(put(orderPath).with(as(teacher)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(new LessonDTO.OrderRequest(ids))))
                    .andExpect(status().isConflict());
        }
        em.flush(); em.clear();
        assertEquals(1, lessons.findById(first.getId()).orElseThrow().getPosition());
        assertEquals(2, lessons.findById(second.getId()).orElseThrow().getPosition());
    }

    @Test
    void legacyMigrationBackfillsAlphabeticallyAndDoesNotResetSavedOrder() throws Exception {
        first.setTitle("Zulu"); first.setPosition(0);
        second.setTitle("Alpha"); second.setPosition(0);
        var third = lesson(1);
        var question = create(1, 1);
        em.flush();
        var migration = new ResourceDatabasePopulator(
                new ClassPathResource("lesson-order-postgresql.sql"));
        migration.execute(dataSource); em.clear();
        assertEquals(List.of(second.getId(), first.getId(), third.getId()),
                lessons.findAllByCourseIdOrderByPositionAscIdAsc(course.getId()).stream().map(Lesson::getId).toList());
        assertEquals(3, questions.findById(question.id()).orElseThrow().getAfterLesson());

        String orderPath = "/api/lessons/course/" + course.getId() + "/order";
        mvc.perform(put(orderPath).with(as(teacher)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LessonDTO.OrderRequest(List.of(third.getId(), second.getId(), first.getId())))))
                .andExpect(status().isNoContent());
        em.flush(); em.clear(); migration.execute(dataSource); em.clear();
        assertEquals(List.of(third.getId(), second.getId(), first.getId()),
                lessons.findAllByCourseIdOrderByPositionAscIdAsc(course.getId()).stream().map(Lesson::getId).toList());
        assertEquals(1, questions.findById(question.id()).orElseThrow().getAfterLesson());
    }
    private QuestionDTO.TeacherQuestion create(int afterLesson, int position) throws Exception {
        var response = mvc.perform(post(base()).with(as(teacher)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request(afterLesson, position)))).andExpect(status().isCreated()).andReturn().getResponse();
        return json.readValue(response.getContentAsString(), QuestionDTO.TeacherQuestion.class);
    }
    private QuestionDTO.AnswerResponse answer(UUID questionId, int optionIndex, int expected) throws Exception {
        var response = mvc.perform(post(base() + "/" + questionId + "/answers").with(as(student)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new QuestionDTO.AnswerRequest(optionIndex)))).andExpect(status().is(expected)).andReturn().getResponse();
        return expected == 200 ? json.readValue(response.getContentAsString(), QuestionDTO.AnswerResponse.class) : null;
    }
    private void progress(Lesson lesson, int expected) throws Exception {
        mvc.perform(post("/api/lessons/" + course.getId() + "/" + lesson.getId() + "/progress").with(as(student)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"completed\":true}")).andExpect(status().is(expected));
    }
    private int passCount() {
        return jdbc.queryForObject("select count(*) from tb_question_pass where student_id = ?", Integer.class, student.getId());
    }
    private String base() { return "/api/courses/" + course.getId() + "/questions"; }
    private User user(UserRole role) {
        User u = new User(); u.setName("Usuário teste"); u.setEmail(UUID.randomUUID() + "@example.test");
        u.setRole(role); u.setEmailVerified(true); u.setPassword("unused-test-password");
        u = users.saveAndFlush(u);
        legal.accept(u.getEmail(), legal.current().version());
        return u;
    }
    private Lesson lesson(int position) {
        Lesson l = new Lesson(); l.setCourse(course); l.setPosition(position); l.setTitle("Aula " + position);
        l.setDescription("Conteúdo"); l.setMediaUrl("https://example.com/video"); l.setThumbnailUrl("https://example.com/image");
        return lessons.saveAndFlush(l);
    }
    private RequestPostProcessor as(User u) {
        return SecurityMockMvcRequestPostProcessors.user(u.getEmail())
                .authorities(new SimpleGrantedAuthority("ROLE_" + u.getRole()), new SimpleGrantedAuthority("FACTOR_PASSWORD"), new SimpleGrantedAuthority("FACTOR_OTT"));
    }
}
