package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.mapper.LessonMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class LessonConcurrentRequestsIntegrationTest extends PostgreSqlIntegrationTest {
    private static final String EMAIL = "concurrent-lesson@example.test";
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private CourseRepository courses;
    @MockitoSpyBean private LessonRepository lessons;
    @MockitoSpyBean private LessonMapper mapper;
    @PersistenceContext private EntityManager entityManager;

    @ParameterizedTest
    @ValueSource(strings = {"DELETE", "PUT"})
    @WithMockUser(username = EMAIL, authorities = {"ROLE_TEACHER", "FACTOR_PASSWORD", "FACTOR_OTT"})
    void concurrentWritesAreSerializedBeforeLessonDeletion(String method) throws Exception {
        User owner = users.findByEmail(EMAIL).orElseGet(() -> {
            var user = new User();
            user.setName("Professor de teste");
            user.setEmail(EMAIL);
            user.setAge(28);
            user.setPassword("unused-test-password");
            user.setRole(UserRole.TEACHER);
            user.setEmailVerified(true);
            return users.saveAndFlush(user);
        });
        var course = new Course();
        course.setName("Curso de teste");
        course.setDescription("Exclusões e edições concorrentes");
        course.setProfessor(owner);
        course = courses.saveAndFlush(course);
        var lesson = new Lesson();
        lesson.setTitle("Aula de teste");
        lesson.setDescription("Descrição da aula");
        lesson.setMediaUrl("https://example.test/video");
        lesson.setThumbnailUrl("https://example.test/thumbnail");
        lesson.setCourse(course);
        UUID lessonId = lessons.saveAndFlush(lesson).getId();

        var writeStarted = new CountDownLatch(1);
        var resumeWrite = new CountDownLatch(1);
        if (method.equals("DELETE")) {
            var calls = new AtomicInteger();
            doAnswer(invocation -> {
                if (calls.getAndIncrement() == 0) {
                    writeStarted.countDown();
                    assertTrue(resumeWrite.await(5, TimeUnit.SECONDS));
                }
                entityManager.remove(invocation.getArgument(0));
                return null;
            }).when(lessons).delete(any(Lesson.class));
        } else {
            doAnswer(invocation -> {
                writeStarted.countDown();
                assertTrue(resumeWrite.await(5, TimeUnit.SECONDS));
                return invocation.callRealMethod();
            }).when(mapper).updateEntity(any(LessonDTO.Request.class), any(Lesson.class));
        }

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(new DelegatingSecurityContextCallable<MvcResult>(() -> {
                var request = method.equals("DELETE") ? delete("/api/lessons/{id}", lessonId)
                        : put("/api/lessons/{id}", lessonId).contentType(MediaType.APPLICATION_JSON).content("""
                          {"title":"Aula editada","description":"Descrição atualizada",
                           "mediaUrl":"https://example.test/video","thumbnailUrl":"https://example.test/thumbnail"}
                          """);
                return mvc.perform(request.with(csrf())).andReturn();
            }));
            java.util.concurrent.Future<MvcResult> second;
            try {
                assertTrue(writeStarted.await(5, TimeUnit.SECONDS));
                var deleteStarted = new CountDownLatch(1);
                second = executor.submit(new DelegatingSecurityContextCallable<>(() -> {
                    deleteStarted.countDown();
                    return mvc.perform(delete("/api/lessons/{id}", lessonId).with(csrf())).andReturn();
                }));
                assertTrue(deleteStarted.await(5, TimeUnit.SECONDS));
                var pending = second;
                assertThrows(TimeoutException.class, () -> pending.get(300, TimeUnit.MILLISECONDS));
            } finally {
                resumeWrite.countDown();
            }
            var response = first.get(5, TimeUnit.SECONDS).getResponse();
            assertEquals(method.equals("DELETE") ? 204 : 200, response.getStatus());
            assertEquals(method.equals("DELETE") ? 403 : 204,
                    second.get(5, TimeUnit.SECONDS).getResponse().getStatus());
        }
        assertFalse(lessons.existsById(lessonId));
    }
}
