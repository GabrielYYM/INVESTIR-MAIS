package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.mapper.LessonMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private LessonMapper lessonMapper;
    @Mock
    private UserService userService;

    private CourseService courseService;

    private LessonService lessonService;

    private User professor;
    private Course course;
    private Lesson lesson;
    private UUID courseId;
    private UUID lessonId;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository, lessonRepository, null, userService);
        lessonService = new LessonService(lessonRepository, courseService, lessonMapper, userService);
        UUID professorId = UUID.randomUUID();
        professor = new User();
        professor.setId(professorId);

        courseId = UUID.randomUUID();
        course = new Course();
        course.setId(courseId);
        course.setProfessor(professor);

        lessonId = UUID.randomUUID();
        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setTitle("Aula 1 - Introdução");
        lesson.setCourse(course);
    }

    @Test
    void createLesson_OnSuccess_ShouldBindCourseAndSave() {
        LessonDTO.Request request = new LessonDTO.Request("Aula 1", "Desc", "url_video", "url_thumb");
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula 1", "Desc", "url_video", "url_thumb", courseId);

        when(courseRepository.findForUpdateById(courseId)).thenReturn(Optional.of(course));
        when(lessonMapper.toEntity(request)).thenReturn(lesson);
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(lessonMapper.toDTO(lesson)).thenReturn(responseDTO);

        LessonDTO.Response result = lessonService.createLesson(courseId, request);

        assertNotNull(result);
        assertEquals(lessonId, result.id());
        verify(lessonRepository).save(lesson);
    }

    @Test
    void getLessonById_WhenLessonExists_ShouldReturnDTO() {
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula 1", "Desc", "url_video", "url_thumb", courseId);
        when(lessonRepository.findResponseById(lessonId)).thenReturn(Optional.of(responseDTO));

        LessonDTO.Response result = lessonService.getLessonById(lessonId);

        assertNotNull(result);
        assertEquals("Aula 1", result.title());
    }

    @Test
    void getLessonsByCourseId_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula 1", "Desc", "url_video", "url_thumb", courseId);

        Page<LessonDTO.Response> page = new PageImpl<>(List.of(responseDTO));

        when(lessonRepository.findResponsesByCourseId(courseId, pageable)).thenReturn(page);

        Page<LessonDTO.Response> result = lessonService.getLessonsByCourseId(courseId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateLesson_OnSuccess_ShouldUpdateAndSave() {
        LessonDTO.Request request = new LessonDTO.Request("Aula 1 Atualizada", "Desc", "url_video", "url_thumb");
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula 1 Atualizada", "Desc", "url_video", "url_thumb", courseId);

        when(lessonRepository.findForUpdateById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toDTO(lesson)).thenReturn(responseDTO);

        LessonDTO.Response result = lessonService.updateLesson(lessonId, request);

        assertNotNull(result);
        verify(lessonMapper).updateEntity(request, lesson);
        verify(lessonRepository, never()).save(lesson);
    }

    @Test
    void deleteLesson_OnSuccess_ShouldDelete() {
        when(lessonRepository.findForUpdateById(lessonId)).thenReturn(Optional.of(lesson));

        lessonService.deleteLesson(lessonId);

        verify(lessonRepository).delete(lesson);
    }
}
