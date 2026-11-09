package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.mapper.LessonMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.education.service.LessonService;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class EducationConcurrencyIntegrationTest extends PostgreSqlIntegrationTest {
    private static final String EMAIL = "concurrent-teacher@example.test";
    @Autowired private UserRepository users;
    @Autowired private CourseRepository courses;
    @MockitoSpyBean private LessonMapper lessonMapper;
    @Autowired private CourseService courseService;
    @Autowired private LessonService lessonService;
    @Autowired private JdbcTemplate jdbc;

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ROLE_TEACHER", "FACTOR_PASSWORD", "FACTOR_OTT"})
    void courseDeletionWaitsForLessonCreationAndDeletesTheNewLesson() throws Exception {
        var owner = new User();
        owner.setName("Professor de teste");
        owner.setEmail(EMAIL);
        owner.setAge(28);
        owner.setPassword("unused-test-password");
        owner.setRole(UserRole.TEACHER);
        owner.setEmailVerified(true);
        owner = users.saveAndFlush(owner);
        var course = new Course();
        course.setName("Curso concorrente");
        course.setDescription("Teste de criação e exclusão simultâneas");
        course.setProfessor(owner);
        var courseId = courses.saveAndFlush(course).getId();

        var beforeInsert = new CountDownLatch(1);
        var resumeInsert = new CountDownLatch(1);
        doAnswer(invocation -> {
            beforeInsert.countDown();
            assertTrue(resumeInsert.await(5, TimeUnit.SECONDS));
            return invocation.callRealMethod();
        }).when(lessonMapper).toEntity(any(LessonDTO.Request.class));

        try (var executor = Executors.newFixedThreadPool(2)) {
            var creation = executor.submit(new DelegatingSecurityContextCallable<>(() -> lessonService.createLesson(
                    courseId, new LessonDTO.Request("Aula concorrente", "Descrição", "https://example.test/video",
                            "https://example.test/thumbnail"), EMAIL)));
            assertTrue(beforeInsert.await(5, TimeUnit.SECONDS));
            var deletionStarted = new CountDownLatch(1);
            var deletion = executor.submit(new DelegatingSecurityContextCallable<>(() -> {
                deletionStarted.countDown();
                courseService.deleteCourse(courseId, EMAIL);
                return null;
            }));
            try {
                assertTrue(deletionStarted.await(5, TimeUnit.SECONDS));
                assertThrows(TimeoutException.class, () -> deletion.get(300, TimeUnit.MILLISECONDS));
            } finally {
                resumeInsert.countDown();
            }
            creation.get(5, TimeUnit.SECONDS);
            deletion.get(5, TimeUnit.SECONDS);
        }

        assertFalse(courses.existsById(courseId));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM tb_lesson WHERE course_id = ? AND deleted_at IS NULL",
                Integer.class, courseId));
    }
}
