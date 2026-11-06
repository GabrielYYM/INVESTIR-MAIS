package com.repositorio.investir_mais.domain.user.repository;

import com.repositorio.investir_mais.domain.AccountSecurityService;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.service.UserService;
import com.repositorio.investir_mais.infrastructure.SecurityConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = UserSoftDeleteTest.JpaConfig.class)
class UserSoftDeleteTest {
    private static final String PASSWORD = "SenhaSegura123";

    @Autowired private UserRepository users;
    @Autowired private CourseRepository courses;
    @Autowired private LessonRepository lessons;
    @Autowired private UserService service;
    @Autowired private AccountSecurityService security;
    @Autowired private PasswordEncoder encoder;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private FindByIndexNameSessionRepository<MapSession> sessions;

    @Test
    void deletingAccountPreservesUserButBlocksLookupsLoginAndAccountTokens() {
        User deleted = createUser("deleted@investir.com", UserRole.STUDENT);
        User active = createUser("active@investir.com", UserRole.STUDENT);
        String resetToken = security.generatePasswordResetToken(deleted.getEmail());
        String verificationToken = security.generateEmailVerificationToken(deleted.getEmail()).getTokenValue();
        jdbc.update("INSERT INTO one_time_tokens (token_value, username, expires_at) VALUES (?, ?, ?)",
                "login-token", deleted.getEmail(), Timestamp.from(Instant.now().plusSeconds(300)));
        var session = new MapSession();
        when(sessions.findByPrincipalName(deleted.getEmail())).thenReturn(Map.of(session.getId(), session));
        entityManager.clear();

        service.deleteAccount(deleted.getEmail(), deleted.getId(), new UserDTO.DeleteAccountRequest(PASSWORD));
        flushAndClear();

        assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_user WHERE id = ?", Timestamp.class, deleted.getId()));
        assertEquals(deleted.getEmail(), jdbc.queryForObject("SELECT email FROM tb_user WHERE id = ?", String.class, deleted.getId()));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM tb_user", Integer.class));
        assertTrue(users.findById(deleted.getId()).isEmpty());
        assertTrue(users.findByEmail(deleted.getEmail()).isEmpty());
        assertFalse(users.existsById(deleted.getId()));
        assertFalse(users.existsByEmail(deleted.getEmail()));
        assertEquals(1, users.count());
        assertEquals(1, users.findAll(PageRequest.of(0, 1)).getTotalElements());
        assertTrue(users.findById(active.getId()).isPresent());
        assertThrows(UsernameNotFoundException.class, () -> new SecurityConfig()
                .userDetailsService(users).loadUserByUsername(deleted.getEmail()));
        assertTrue(users.existsByEmailIncludingDeleted(deleted.getEmail()));
        assertTrue(users.existsByEmailAndIdNot(deleted.getEmail(), active.getId()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM app_one_time_token", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM one_time_tokens", Integer.class));
        assertTrue(security.consumePasswordResetToken(resetToken).isEmpty());
        assertTrue(security.consumeEmailVerificationToken(verificationToken).isEmpty());
        verifyNoInteractions(sessions);

        service.forgotPassword(new AuthDTO.ForgotPasswordRequest(deleted.getEmail()));
        service.resendVerification(deleted.getEmail());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM app_one_time_token", Integer.class));
        var conflict = assertThrows(ResponseStatusException.class, () -> service.registerUser(
                new UserDTO.Request("Novo usuário", 25, deleted.getEmail(), PASSWORD, UserRole.STUDENT)));
        assertEquals(409, conflict.getStatusCode().value());
    }

    @Test
    void deletingTeacherSoftDeletesTheirCoursesAndLessonsAndPreservesOtherTeachers() {
        User teacher = createUser("deleted-teacher@investir.com", UserRole.TEACHER);
        Course deletedCourse = createCourse(teacher);
        Lesson deletedLesson = createLesson(deletedCourse);
        User other = createUser("other-teacher@investir.com", UserRole.TEACHER);
        Course activeCourse = createCourse(other);
        Lesson activeLesson = createLesson(activeCourse);
        flushAndClear();

        service.deleteAccount(teacher.getEmail(), teacher.getId(), new UserDTO.DeleteAccountRequest(PASSWORD));
        flushAndClear();

        assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_course WHERE id = ?", Timestamp.class, deletedCourse.getId()));
        assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_lesson WHERE id = ?", Timestamp.class, deletedLesson.getId()));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM tb_course", Integer.class));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM tb_lesson", Integer.class));
        assertFalse(courses.existsById(deletedCourse.getId()));
        assertFalse(lessons.existsById(deletedLesson.getId()));
        assertEquals(0, courses.findAllByProfessorId(teacher.getId(), PageRequest.of(0, 1)).getTotalElements());
        assertEquals(0, lessons.findAllByCourseId(deletedCourse.getId(), PageRequest.of(0, 1)).getTotalElements());
        assertTrue(users.findById(other.getId()).isPresent());
        assertTrue(courses.findById(activeCourse.getId()).isPresent());
        assertTrue(lessons.findById(activeLesson.getId()).isPresent());
        assertNull(jdbc.queryForObject("SELECT deleted_at FROM tb_user WHERE id = ?", Timestamp.class, other.getId()));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void committedDeletionPreservesTheUserAndRecordsTheAuditEntry() {
        User user = createUser("audit@investir.com", UserRole.STUDENT);
        try {
            service.deleteAccount(user.getEmail(), user.getId(), new UserDTO.DeleteAccountRequest(PASSWORD));

            assertNotNull(jdbc.queryForObject("SELECT deleted_at FROM tb_user WHERE id = ?", Timestamp.class, user.getId()));
            assertFalse(users.existsById(user.getId()));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM tb_user_audit WHERE id = ? AND revtype = 2",
                    Integer.class, user.getId()));
        } finally {
            jdbc.update("DELETE FROM tb_user_audit WHERE id = ?", user.getId());
            jdbc.update("DELETE FROM tb_user WHERE id = ?", user.getId());
        }
    }

    @Test
    void deletingTeacherPreservesPreviouslyDeletedCourseAndLessonTimestamps() {
        User teacher = createUser("teacher@investir.com", UserRole.TEACHER);
        Course deletedCourse = createCourse(teacher);
        Lesson deletedLesson = createLesson(deletedCourse);
        flushAndClear();
        courses.deleteById(deletedCourse.getId());
        flushAndClear();
        Timestamp previousDeletion = Timestamp.valueOf("2020-01-01 12:00:00");
        jdbc.update("UPDATE tb_course SET deleted_at = ? WHERE id = ?", previousDeletion, deletedCourse.getId());
        jdbc.update("UPDATE tb_lesson SET deleted_at = ? WHERE id = ?", previousDeletion, deletedLesson.getId());

        service.deleteAccount(teacher.getEmail(), teacher.getId(), new UserDTO.DeleteAccountRequest(PASSWORD));
        flushAndClear();

        assertEquals(previousDeletion, jdbc.queryForObject("SELECT deleted_at FROM tb_course WHERE id = ?", Timestamp.class, deletedCourse.getId()));
        assertEquals(previousDeletion, jdbc.queryForObject("SELECT deleted_at FROM tb_lesson WHERE id = ?", Timestamp.class, deletedLesson.getId()));
    }

    private User createUser(String email, UserRole role) {
        User user = new User();
        user.setName("Usuário");
        user.setEmail(email);
        user.setPassword(encoder.encode(PASSWORD));
        user.setRole(role);
        user.setEmailVerified(true);
        return users.saveAndFlush(user);
    }

    private Course createCourse(User teacher) {
        Course course = new Course();
        course.setName("Curso");
        course.setDescription("Descrição");
        course.setProfessor(teacher);
        return courses.save(course);
    }

    private Lesson createLesson(Course course) {
        Lesson lesson = new Lesson();
        lesson.setTitle("Aula");
        lesson.setDescription("Descrição");
        lesson.setMediaUrl("https://example.com/video");
        lesson.setThumbnailUrl("https://example.com/capa");
        lesson.setCourse(course);
        return lessons.save(lesson);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableJpaAuditing
    @EntityScan(basePackageClasses = {Course.class, User.class})
    @EnableEnversRepositories(basePackageClasses = {CourseRepository.class, UserRepository.class})
    @Import({UserService.class, AccountSecurityService.class})
    static class JpaConfig {
        @Bean UserMapper userMapper() { return Mappers.getMapper(UserMapper.class); }
        @Bean PasswordEncoder passwordEncoder() { return new SecurityConfig().passwordEncoder(); }
    }
}
