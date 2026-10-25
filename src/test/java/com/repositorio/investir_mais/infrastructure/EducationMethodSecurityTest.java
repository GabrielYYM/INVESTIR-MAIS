package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.mapper.CourseMapper;
import com.repositorio.investir_mais.domain.education.mapper.LessonMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.education.service.LessonService;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(EducationMethodSecurityTest.Config.class)
class EducationMethodSecurityTest {
    private static final String EMAIL = "teacher@investir.com";
    private static final UUID COURSE_ID = UUID.randomUUID();
    private static final UUID LESSON_ID = UUID.randomUUID();
    private static final CourseDTO.Request COURSE = new CourseDTO.Request("Curso", "Descrição");
    private static final LessonDTO.Request LESSON = new LessonDTO.Request("Aula", "Descrição", "video", "capa");

    @Autowired CourseService courses;
    @Autowired LessonService lessons;
    @Autowired CourseRepository courseRepository;
    @Autowired LessonRepository lessonRepository;
    @Autowired CourseMapper courseMapper;
    @Autowired LessonMapper lessonMapper;
    @Autowired UserService users;

    @BeforeEach
    void resetDependencies() {
        reset(courseRepository, lessonRepository, courseMapper, lessonMapper, users);
    }

    @ParameterizedTest
    @ValueSource(strings = {"createCourse", "updateCourse", "deleteCourse", "createLesson", "updateLesson", "deleteLesson"})
    @WithMockUser(username = EMAIL, roles = "STUDENT")
    void studentCannotWriteEvenWhenCallingServiceDirectly(String operation) {
        assertThrows(AccessDeniedException.class, write(operation));
        verifyNoInteractions(courseRepository, lessonRepository, courseMapper, lessonMapper, users);
    }

    @ParameterizedTest
    @ValueSource(strings = {"createCourse", "updateCourse", "deleteCourse", "createLesson", "updateLesson", "deleteLesson"})
    void anonymousCannotWriteEvenWhenCallingServiceDirectly(String operation) {
        assertThrows(AuthenticationCredentialsNotFoundException.class, write(operation));
        verifyNoInteractions(courseRepository, lessonRepository, courseMapper, lessonMapper, users);
    }

    @ParameterizedTest
    @ValueSource(strings = {"createCourse", "updateCourse", "deleteCourse", "createLesson", "updateLesson", "deleteLesson"})
    @WithMockUser(username = EMAIL, roles = "TEACHER")
    void teacherCanWriteOwnResourcesThroughSpringProxy(String operation) {
        prepareResources(UserRole.TEACHER, true);
        assertDoesNotThrow(write(operation));
        verify(users).findByEmail(EMAIL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"createCourse", "updateCourse", "deleteCourse", "createLesson", "updateLesson", "deleteLesson"})
    @WithMockUser(username = EMAIL, roles = "ADMIN")
    void administratorCanWriteOtherTeachersResources(String operation) {
        prepareResources(UserRole.ADMIN, false);
        assertDoesNotThrow(write(operation));
        verify(users).findByEmail(EMAIL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"updateCourse", "deleteCourse", "createLesson", "updateLesson", "deleteLesson"})
    @WithMockUser(username = EMAIL, roles = "TEACHER")
    void teacherStillCannotWriteOtherTeachersResources(String operation) {
        prepareResources(UserRole.TEACHER, false);
        var failure = assertThrows(ResponseStatusException.class, write(operation));
        assertEquals(403, failure.getStatusCode().value());
        verify(courseRepository, never()).save(any());
        verify(courseRepository, never()).delete(any());
        verify(lessonRepository, never()).save(any());
        verify(lessonRepository, never()).delete(any());
        verifyNoInteractions(courseMapper, lessonMapper);
    }

    private Executable write(String operation) {
        return switch (operation) {
            case "createCourse" -> () -> courses.createCourse(COURSE, EMAIL);
            case "updateCourse" -> () -> courses.updateCourse(COURSE_ID, COURSE, EMAIL);
            case "deleteCourse" -> () -> courses.deleteCourse(COURSE_ID, EMAIL);
            case "createLesson" -> () -> lessons.createLesson(COURSE_ID, LESSON, EMAIL);
            case "updateLesson" -> () -> lessons.updateLesson(LESSON_ID, LESSON, EMAIL);
            case "deleteLesson" -> () -> lessons.deleteLesson(LESSON_ID, EMAIL);
            default -> throw new IllegalArgumentException(operation);
        };
    }

    private void prepareResources(UserRole role, boolean owner) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(EMAIL);
        user.setRole(role);
        User professor = new User();
        professor.setId(UUID.randomUUID());
        Course course = new Course();
        course.setId(COURSE_ID);
        course.setProfessor(owner ? user : professor);
        Lesson lesson = new Lesson();
        lesson.setId(LESSON_ID);
        lesson.setCourse(course);
        when(users.findByEmail(EMAIL)).thenReturn(user);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(lessonRepository.findByIdAndCourse_Professor_Id(LESSON_ID, user.getId()))
                .thenReturn(owner ? Optional.of(lesson) : Optional.empty());
        when(courseMapper.toEntity(COURSE)).thenReturn(course);
        when(lessonMapper.toEntity(LESSON)).thenReturn(lesson);
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity
    @Import({CourseService.class, LessonService.class})
    static class Config {
        @Bean CourseRepository courseRepository() { return mock(CourseRepository.class); }
        @Bean LessonRepository lessonRepository() { return mock(LessonRepository.class); }
        @Bean CourseMapper courseMapper() { return mock(CourseMapper.class); }
        @Bean LessonMapper lessonMapper() { return mock(LessonMapper.class); }
        @Bean UserService users() { return mock(UserService.class); }
    }
}
