package com.repositorio.investir_mais.domain.education.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.education.DTO.LessonRequestDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessonResponseDTO;
import com.repositorio.investir_mais.domain.education.mapper.LessonMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.service.UserService;

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

    @InjectMocks
    private LessonService lessonService;

    private User professor;
    private User outroUser;
    private Course course;
    private Lesson lesson;
    private UUID courseId;
    private UUID lessonId;

    @BeforeEach
    void setUp() {
        UUID professorId = UUID.randomUUID();
        professor = new User();
        professor.setId(professorId);

        outroUser = new User();
        outroUser.setId(UUID.randomUUID());

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
    void createLesson_QuandoNaoForDonoDoCurso_DeveLancarForbidden() {
        LessonRequestDTO request = new LessonRequestDTO("Aula 1", "Desc", "url_video", "url_thumb");
        when(userService.findByEmail("outro@investir.com")).thenReturn(outroUser);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> lessonService.createLesson(courseId, request, "outro@investir.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void createLesson_ComSucesso_DeveVincularCursoESalvar() {
        LessonRequestDTO request = new LessonRequestDTO("Aula 1", "Desc", "url_video", "url_thumb");
        LessonResponseDTO responseDTO = new LessonResponseDTO(lessonId, "Aula 1", "Desc", "url_video", "url_thumb", courseId);

        when(userService.findByEmail("prof@investir.com")).thenReturn(professor);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonMapper.toEntity(request)).thenReturn(lesson);
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(lessonMapper.toDTO(lesson)).thenReturn(responseDTO);

        LessonResponseDTO result = lessonService.createLesson(courseId, request, "prof@investir.com");

        assertNotNull(result);
        assertEquals(lessonId, result.id());
        verify(lessonRepository).save(lesson);
    }

    @Test
    void getLessonById_QuandoExiste_DeveRetornarDTO() {
        LessonResponseDTO responseDTO = new LessonResponseDTO(lessonId, "Aula 1", "Desc", "url_video", "url_thumb", courseId);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toDTO(lesson)).thenReturn(responseDTO);

        LessonResponseDTO result = lessonService.getLessonById(lessonId);

        assertNotNull(result);
        assertEquals("Aula 1", result.title());
    }

    @Test
    void getLessonsByCourseId_DeveRetornarPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Lesson> page = new PageImpl<>(List.of(lesson));
        LessonResponseDTO responseDTO = new LessonResponseDTO(lessonId, "Aula 1", "Desc", "url_video", "url_thumb", courseId);

        when(lessonRepository.findAllByCourseId(courseId, pageable)).thenReturn(page);
        when(lessonMapper.toDTO(lesson)).thenReturn(responseDTO);

        Page<LessonResponseDTO> result = lessonService.getLessonsByCourseId(courseId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateLesson_ComSucesso_DeveAtualizarESalvar() {
        LessonRequestDTO request = new LessonRequestDTO("Aula 1 Atualizada", "Desc", "url_video", "url_thumb");
        LessonResponseDTO responseDTO = new LessonResponseDTO(lessonId, "Aula 1 Atualizada", "Desc", "url_video", "url_thumb", courseId);

        when(userService.findByEmail("prof@investir.com")).thenReturn(professor);
        when(lessonRepository.findByIdAndCourse_Professor_Id(lessonId, professor.getId())).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(lessonMapper.toDTO(lesson)).thenReturn(responseDTO);

        LessonResponseDTO result = lessonService.updateLesson(lessonId, request, "prof@investir.com");

        assertNotNull(result);
        verify(lessonMapper).updateEntity(request, lesson);
        verify(lessonRepository).save(lesson);
    }

    @Test
    void deleteLesson_ComSucesso_DeveDeletar() {
        when(userService.findByEmail("prof@investir.com")).thenReturn(professor);
        when(lessonRepository.findByIdAndCourse_Professor_Id(lessonId, professor.getId())).thenReturn(Optional.of(lesson));

        lessonService.deleteLesson(lessonId, "prof@investir.com");

        verify(lessonRepository).delete(lesson);
    }
}