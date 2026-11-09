package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.education.DTO.TeacherDashboardDTO.CourseStats;
import com.repositorio.investir_mais.domain.education.DTO.TeacherDashboardDTO.Summary;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.CourseEnrollment;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.model.LessonProgress;
import com.repositorio.investir_mais.domain.education.repository.CourseEnrollmentRepository;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonProgressRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class TeacherDashboardIntegrationTest extends PostgreSqlIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @Autowired private UserRepository users;
    @Autowired private CourseRepository courses;
    @Autowired private LessonRepository lessons;
    @Autowired private CourseEnrollmentRepository enrollments;
    @Autowired private LessonProgressRepository progress;

    @Test
    void dashboardCountsByCourseAndPeriodWithoutDuplicatingRows() throws Exception {
        var professor = createUser("teacher", UserRole.TEACHER);
        var admin = createUser("admin", UserRole.ADMIN);
        var firstStudent = createUser("first", UserRole.STUDENT);
        var secondStudent = createUser("second", UserRole.STUDENT);
        var thirdStudent = createUser("third", UserRole.STUDENT);
        var main = createCourse(professor, "Curso principal");
        var second = createCourse(professor, "Segundo curso");
        var empty = createCourse(professor, "Curso vazio");
        var other = createCourse(createUser("other-teacher", UserRole.TEACHER), "Curso de outro professor");
        var firstLesson = createLesson(main);
        var secondLesson = createLesson(main);
        var removedLesson = createLesson(main);
        createLesson(second);
        var otherLesson = createLesson(other);

        var today = LocalDate.now();
        var old = today.minusDays(4).atStartOfDay();
        var withinPeriod = today.atStartOfDay();
        var beforeEnrollment = LocalDateTime.now().minusSeconds(1);
        mvc.perform(post("/api/courses/{courseId}/enroll", main.getId()).with(as(firstStudent)).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enrolled").value(true));
        entityManager.flush();
        var enrolledAt = jdbc.queryForObject("SELECT enrolled_at FROM tb_course_enrollment WHERE course_id = ? AND student_id = ?",
                LocalDateTime.class, main.getId(), firstStudent.getId());
        assertFalse(enrolledAt.isBefore(beforeEnrollment));
        assertFalse(enrolledAt.isAfter(LocalDateTime.now().plusSeconds(1)));
        enroll(secondStudent, main);
        var oldEnrollment = enroll(thirdStudent, main);
        assertEquals(1, jdbc.update("UPDATE tb_course_enrollment SET enrolled_at = ? WHERE id = ?", old, oldEnrollment.getId()));
        enroll(firstStudent, second);
        enroll(thirdStudent, other);

        watch(firstStudent, firstLesson, true, withinPeriod);
        watch(firstStudent, secondLesson, true, withinPeriod);
        watch(secondStudent, firstLesson, false, withinPeriod);
        watch(secondStudent, secondLesson, true, old);
        watch(firstStudent, removedLesson, true, withinPeriod);
        watch(thirdStudent, otherLesson, true, withinPeriod);
        entityManager.flush();
        entityManager.clear();
        lessons.deleteById(removedLesson.getId());
        lessons.flush();
        mvc.perform(get("/api/courses/{courseId}/progress", main.getId()).with(as(firstStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedLessonIds.length()").value(2));

        var response = dashboard(professor, today)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCourses").value(3))
                .andExpect(jsonPath("$.totalLessons").value(3))
                .andExpect(jsonPath("$.totalStudents").value(2))
                .andExpect(jsonPath("$.totalLessonsWatched").value(2))
                .andExpect(jsonPath("$.averageCompletionRate").value(40.0))
                .andReturn().getResponse();
        var stats = json.readValue(response.getContentAsByteArray(), Summary.class).courses();
        assertTrue(stats.contains(new CourseStats(main.getId(), main.getName(), 2, 2, 2, 50.0)), stats::toString);
        assertTrue(stats.contains(new CourseStats(empty.getId(), empty.getName(), 0, 0, 0, 0.0)), stats::toString);

        dashboard(admin, today)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCourses").value(4))
                .andExpect(jsonPath("$.totalLessons").value(4))
                .andExpect(jsonPath("$.totalStudents").value(3))
                .andExpect(jsonPath("$.totalLessonsWatched").value(3))
                .andExpect(jsonPath("$.averageCompletionRate").value(50.0));
        dashboard(firstStudent, today).andExpect(status().isForbidden());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void simultaneousProgressRequestsCreateOneEnrollmentAndOneCompletion() throws Exception {
        var professor = createUser("concurrent-teacher", UserRole.TEACHER);
        var student = createUser("concurrent-student", UserRole.STUDENT);
        var course = createCourse(professor, "Curso com progresso concorrente");
        var lesson = createLesson(course);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        Callable<Void> completeLesson = () -> {
            ready.countDown();
            assertTrue(start.await(5, TimeUnit.SECONDS));
            mvc.perform(post("/api/lessons/{courseId}/{lessonId}/progress", course.getId(), lesson.getId())
                            .with(as(student)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"completed\":true}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.enrolled").value(true))
                    .andExpect(jsonPath("$.completedLessonIds[0]").value(lesson.getId().toString()));
            return null;
        };

        try {
            try (var executor = Executors.newFixedThreadPool(2)) {
                var first = executor.submit(completeLesson);
                var second = executor.submit(completeLesson);
                assertTrue(ready.await(5, TimeUnit.SECONDS));
                start.countDown();
                first.get(10, TimeUnit.SECONDS);
                second.get(10, TimeUnit.SECONDS);
            }
            mvc.perform(post("/api/courses/{courseId}/enroll", course.getId()).with(as(student)).with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.completedLessons").value(1));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM tb_course_enrollment WHERE course_id = ?",
                    Integer.class, course.getId()));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM tb_lesson_progress WHERE lesson_id = ?",
                    Integer.class, lesson.getId()));
        } finally {
            jdbc.update("DELETE FROM tb_lesson_progress WHERE lesson_id = ?", lesson.getId());
            jdbc.update("DELETE FROM tb_course_enrollment WHERE course_id = ?", course.getId());
            jdbc.update("DELETE FROM tb_lesson WHERE id = ?", lesson.getId());
            jdbc.update("DELETE FROM tb_course WHERE id = ?", course.getId());
            jdbc.update("DELETE FROM tb_user WHERE id IN (?, ?)", student.getId(), professor.getId());
        }
    }

    private ResultActions dashboard(User viewer, LocalDate today) throws Exception {
        return mvc.perform(get("/api/teacher/dashboard").with(as(viewer))
                .param("startDate", today.minusDays(1).toString()).param("endDate", today.plusDays(1).toString()));
    }

    private RequestPostProcessor as(User account) {
        return user(account.getEmail()).authorities(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()),
                new SimpleGrantedAuthority("FACTOR_PASSWORD"), new SimpleGrantedAuthority("FACTOR_OTT"));
    }

    private User createUser(String name, UserRole role) {
        var account = new User();
        account.setName(name);
        account.setEmail(name + "@example.test");
        account.setAge(25);
        account.setPassword("unused-test-password");
        account.setRole(role);
        account.setEmailVerified(true);
        return users.saveAndFlush(account);
    }

    private Course createCourse(User professor, String name) {
        var course = new Course();
        course.setProfessor(professor);
        course.setName(name);
        course.setDescription("Descrição do curso");
        return courses.saveAndFlush(course);
    }

    private Lesson createLesson(Course course) {
        var lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle("Aula de teste");
        lesson.setDescription("Descrição da aula");
        lesson.setMediaUrl("https://example.test/video");
        lesson.setThumbnailUrl("https://example.test/capa");
        return lessons.saveAndFlush(lesson);
    }

    private CourseEnrollment enroll(User student, Course course) {
        return enrollments.saveAndFlush(CourseEnrollment.builder().student(student).course(course).build());
    }

    private void watch(User student, Lesson lesson, boolean completed, LocalDateTime date) {
        progress.save(LessonProgress.builder().student(student).lesson(lesson).completed(completed).completedAt(date).build());
    }
}
