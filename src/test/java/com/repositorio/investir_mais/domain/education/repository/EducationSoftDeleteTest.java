package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.sql.init.mode=never")
@ActiveProfiles("test")
@ContextConfiguration(classes = EducationSoftDeleteTest.JpaConfig.class)
class EducationSoftDeleteTest {
    private static final PageRequest FIRST_PAGE = PageRequest.of(0, 1);

    @Autowired private CourseRepository courses;
    @Autowired private LessonRepository lessons;
    @Autowired private UserRepository users;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbc;

    private User professor;

    @BeforeEach
    void createProfessor() {
        professor = new User();
        professor.setName("Professor");
        professor.setEmail("teacher@investir.com");
        professor.setRole(UserRole.TEACHER);
        professor = users.saveAndFlush(professor);
    }

    @Test
    void deletingLessonPreservesDataAndHidesItFromLookupsPagesAndAssociations() {
        Course course = createCourse("Curso");
        Lesson deleted = createLesson(course, "Aula excluída");
        Lesson active = createLesson(course, "Aula ativa");
        flushAndClear();

        lessons.deleteById(deleted.getId());
        flushAndClear();

        assertEquals("Aula excluída", jdbc.queryForObject(
                "SELECT title FROM tb_lesson WHERE id = ?", String.class, deleted.getId()));
        assertNotNull(deletedAt("tb_lesson", deleted.getId()));
        assertNull(deletedAt("tb_lesson", active.getId()));
        assertNull(deletedAt("tb_course", course.getId()));
        assertTrue(lessons.findById(deleted.getId()).isEmpty());
        assertFalse(lessons.existsById(deleted.getId()));
        assertTrue(lessons.findByIdAndCourse_Professor_Id(deleted.getId(), professor.getId()).isEmpty());
        assertEquals(1, lessons.count());
        assertEquals(1, lessons.findAll(FIRST_PAGE).getTotalElements());
        var page = lessons.findAllByCourseId(course.getId(), FIRST_PAGE);
        assertEquals(1, page.getTotalElements());
        assertEquals(active.getId(), page.getContent().getFirst().getId());
        var remainingLessons = courses.findById(course.getId()).orElseThrow().getLessons();
        assertEquals(1, remainingLessons.size());
        assertEquals(active.getId(), remainingLessons.getFirst().getId());
    }

    @Test
    void deletingCoursePreservesItsLessonsAndLeavesOtherCoursesVisible() {
        Course deleted = createCourse("Curso excluído");
        Lesson first = createLesson(deleted, "Primeira aula");
        Lesson second = createLesson(deleted, "Segunda aula");
        Course active = createCourse("Curso ativo");
        Lesson activeLesson = createLesson(active, "Aula ativa");
        flushAndClear();

        courses.deleteById(deleted.getId());
        flushAndClear();

        assertEquals("Curso excluído", jdbc.queryForObject(
                "SELECT name FROM tb_course WHERE id = ?", String.class, deleted.getId()));
        assertNotNull(deletedAt("tb_course", deleted.getId()));
        assertNotNull(deletedAt("tb_lesson", first.getId()));
        assertNotNull(deletedAt("tb_lesson", second.getId()));
        assertNull(deletedAt("tb_course", active.getId()));
        assertNull(deletedAt("tb_lesson", activeLesson.getId()));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM tb_course", Integer.class));
        assertEquals(3, jdbc.queryForObject("SELECT COUNT(*) FROM tb_lesson", Integer.class));
        assertTrue(courses.findById(deleted.getId()).isEmpty());
        assertFalse(courses.existsById(deleted.getId()));
        assertEquals(1, courses.count());
        assertEquals(1, courses.findAll(FIRST_PAGE).getTotalElements());
        var page = courses.findAllByProfessorId(professor.getId(), FIRST_PAGE);
        assertEquals(1, page.getTotalElements());
        assertEquals(active.getId(), page.getContent().getFirst().getId());
        assertTrue(lessons.findById(first.getId()).isEmpty());
        assertFalse(lessons.existsById(second.getId()));
        assertTrue(lessons.findByIdAndCourse_Professor_Id(first.getId(), professor.getId()).isEmpty());
        assertEquals(0, lessons.findAllByCourseId(deleted.getId(), FIRST_PAGE).getTotalElements());
        assertEquals(1, lessons.count());
        assertTrue(lessons.findById(activeLesson.getId()).isPresent());
    }

    @Test
    void deletingCourseDoesNotChangeTheTimestampOfAnAlreadyDeletedLesson() {
        Course course = createCourse("Curso");
        Lesson deleted = createLesson(course, "Aula já excluída");
        Lesson active = createLesson(course, "Aula ativa");
        flushAndClear();
        lessons.deleteById(deleted.getId());
        flushAndClear();
        Timestamp originalDeletion = Timestamp.valueOf("2020-01-01 12:00:00");
        jdbc.update("UPDATE tb_lesson SET deleted_at = ? WHERE id = ?", originalDeletion, deleted.getId());

        courses.deleteById(course.getId());
        flushAndClear();

        assertNotNull(originalDeletion);
        assertEquals(originalDeletion, deletedAt("tb_lesson", deleted.getId()));
        assertNotNull(deletedAt("tb_lesson", active.getId()));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM tb_lesson", Integer.class));
        assertEquals(0, courses.count());
        assertEquals(0, lessons.count());
    }

    @Test
    void rowsWithNoDeletionTimestampRemainVisible() {
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        jdbc.update("INSERT INTO tb_course (id, name, description, professor_id) VALUES (?, ?, ?, ?)",
                courseId, "Curso existente", "Descrição", professor.getId());
        jdbc.update("INSERT INTO tb_lesson (id, title, description, media_url, thumbnail_url, course_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                lessonId, "Aula existente", "Descrição", "https://example.com/video", "https://example.com/capa", courseId);

        assertTrue(courses.findById(courseId).isPresent());
        assertTrue(lessons.findById(lessonId).isPresent());
        assertEquals(1, courses.findAllByProfessorId(professor.getId(), FIRST_PAGE).getTotalElements());
        assertEquals(1, lessons.findAllByCourseId(courseId, FIRST_PAGE).getTotalElements());
        assertNull(deletedAt("tb_course", courseId));
        assertNull(deletedAt("tb_lesson", lessonId));
    }

    private Course createCourse(String name) {
        Course course = new Course();
        course.setName(name);
        course.setDescription("Descrição");
        course.setProfessor(professor);
        return courses.save(course);
    }

    private Lesson createLesson(Course course, String title) {
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
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

    private Timestamp deletedAt(String table, UUID id) {
        return jdbc.queryForObject("SELECT deleted_at FROM " + table + " WHERE id = ?", Timestamp.class, id);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableJpaAuditing
    @EntityScan(basePackageClasses = {Course.class, User.class})
    @EnableEnversRepositories(basePackageClasses = {CourseRepository.class, UserRepository.class})
    static class JpaConfig {
    }
}
