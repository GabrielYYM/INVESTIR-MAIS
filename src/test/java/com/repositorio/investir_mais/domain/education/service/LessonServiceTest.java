package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.mapper.LessonMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
    @Mock
    private QuestionService questionService;
    @Mock private VideoPublishingPolicy videoPolicy;

    private CourseService courseService;

    private LessonService lessonService;

    private User professor;
    private User outroUser;
    private Course course;
    private Lesson lesson;
    private UUID courseId;
    private UUID lessonId;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository, lessonRepository, null, null, userService);
        lessonService = new LessonService(lessonRepository, courseService, lessonMapper, userService, questionService, videoPolicy);
        lenient().when(videoPolicy.validate(anyString(), any(UUID.class))).thenAnswer(call -> new VideoPublishingPolicy.VerifiedVideo(call.getArgument(0), "https://example.com/thumb"));
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
    void createLesson_WhenUserIsNotCourseOwner_ShouldThrowForbidden() {
        LessonDTO.Request request = new LessonDTO.Request("Aula 1", "Desc", "url_video", "url_thumb");
        when(userService.findByEmail("outro@investir.com")).thenReturn(outroUser);
        when(courseRepository.findForUpdateById(courseId)).thenReturn(Optional.of(course));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> lessonService.createLesson(courseId, request, "outro@investir.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void createLesson_OnSuccess_ShouldBindCourseAndSave() {
        LessonDTO.Request request = new LessonDTO.Request("Aula 1", "Desc", "url_video", "url_thumb");
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula 1", "Desc", "url_video", "url_thumb", courseId);

        when(userService.findByEmail("prof@investir.com")).thenReturn(professor);
        when(courseRepository.findForUpdateById(courseId)).thenReturn(Optional.of(course));
        when(lessonMapper.toEntity(request)).thenReturn(lesson);
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(lessonMapper.toDTO(lesson)).thenReturn(responseDTO);

        LessonDTO.Response result = lessonService.createLesson(courseId, request, "prof@investir.com");

        assertNotNull(result);
        assertEquals(lessonId, result.id());
        verify(lessonRepository).save(lesson);
    }

    @Test
    void createLesson_WhenUserIsAdmin_ShouldAllowAddingLessonToAnotherProfessorsCourse() {
        LessonDTO.Request request = new LessonDTO.Request("Aula 1", "Desc", "url_video", "url_thumb");
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);
        when(userService.findByEmail("admin@investir.com")).thenReturn(admin);
        when(courseRepository.findForUpdateById(courseId)).thenReturn(Optional.of(course));
        when(lessonMapper.toEntity(request)).thenReturn(lesson);
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        lessonService.createLesson(courseId, request, "admin@investir.com");

        verify(lessonRepository).save(lesson);
    }

    @Test
    void getLessonById_WhenLessonExists_ShouldReturnDTO() {
        when(userService.findByEmail("aluno@investir.com")).thenReturn(outroUser);
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula 1", "Desc", "url_video", "url_thumb", courseId);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toDTO(lesson)).thenReturn(responseDTO);
        when(questionService.canPreview(lesson, "aluno@investir.com")).thenReturn(true);

        LessonDTO.Response result = lessonService.getLessonForStudent(lessonId, "aluno@investir.com");

        assertNotNull(result);
        assertEquals("Aula 1", result.title());
    }

    @Test
    void getLessonsByCourseId_ShouldReturnPage() {
        when(userService.findByEmail("aluno@investir.com")).thenReturn(outroUser);
        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("position", "id"));
        Page<Lesson> page = new PageImpl<>(List.of(lesson));
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula 1", "Desc", "url_video", "url_thumb", courseId);

        when(lessonRepository.findAllByCourseId(courseId, pageable)).thenReturn(page);
        when(lessonMapper.toDTO(lesson)).thenReturn(responseDTO);
        when(questionService.canPreview(lesson, "aluno@investir.com")).thenReturn(true);

        Page<LessonDTO.Response> result = lessonService.getLessonsForStudent(courseId, pageable, "aluno@investir.com");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateLesson_OnSuccess_ShouldUpdateAndSave() {
        LessonDTO.Request request = new LessonDTO.Request("Aula 1 Atualizada", "Desc", "url_video", "url_thumb");
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula 1 Atualizada", "Desc", "url_video", "url_thumb", courseId);

        when(userService.findByEmail("prof@investir.com")).thenReturn(professor);
        when(lessonRepository.findByIdAndCourse_Professor_Id(lessonId, professor.getId())).thenReturn(Optional.of(lesson));
        when(lessonMapper.toDTO(lesson)).thenReturn(responseDTO);

        LessonDTO.Response result = lessonService.updateLesson(lessonId, request, "prof@investir.com");

        assertNotNull(result);
        verify(lessonMapper).updateEntity(request, lesson);
        verify(lessonRepository, never()).save(lesson);
    }

    @Test
    void updateLesson_WhenUserIsAdmin_ShouldAllowUpdatingAnotherProfessorsLesson() {
        LessonDTO.Request request = new LessonDTO.Request("Aula atualizada", "Descrição", "https://video", "https://thumb");
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);
        when(userService.findByEmail("admin@investir.com")).thenReturn(admin);
        when(lessonRepository.findForUpdateById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toDTO(lesson)).thenReturn(new LessonDTO.Response(
            lessonId, request.title(), request.description(), request.mediaUrl(), request.thumbnailUrl(), courseId));

        LessonDTO.Response result = lessonService.updateLesson(lessonId, request, "admin@investir.com");

        assertEquals("Aula atualizada", result.title());
        verify(lessonMapper).updateEntity(request, lesson);
    }

    @Test
    void deleteLesson_OnSuccess_ShouldDelete() {
        when(userService.findByEmail("prof@investir.com")).thenReturn(professor);
        when(lessonRepository.findByIdAndCourse_Professor_Id(lessonId, professor.getId())).thenReturn(Optional.of(lesson));

        lessonService.deleteLesson(lessonId, "prof@investir.com");

        verify(lessonRepository).delete(lesson);
    }

    @Test
    void reorderLessons_ShouldApplyCompleteOrderAndMoveQuestionAnchors() {
        professor.setRole(UserRole.TEACHER);
        lesson.setPosition(1);
        Lesson second = new Lesson(); second.setId(UUID.randomUUID()); second.setPosition(3);
        when(userService.findByEmail("teacher")).thenReturn(professor);
        when(courseRepository.findForUpdateById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findForUpdateByCourseIdOrderByPositionAscIdAsc(courseId)).thenReturn(List.of(lesson, second));

        lessonService.reorderLessons(courseId, new LessonDTO.OrderRequest(List.of(second.getId(), lessonId)), "teacher");

        assertEquals(1, second.getPosition());
        assertEquals(2, lesson.getPosition());
        verify(questionService).relocateLessonQuestions(courseId, Map.of(1, 2, 3, 1));
        verify(lessonRepository).flush();
    }

    @Test
    void reorderLessons_ShouldRejectPartialDuplicateAndForeignListsBeforeWriting() {
        professor.setRole(UserRole.TEACHER);
        lesson.setPosition(1);
        when(userService.findByEmail("teacher")).thenReturn(professor);
        when(courseRepository.findForUpdateById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findForUpdateByCourseIdOrderByPositionAscIdAsc(courseId)).thenReturn(List.of(lesson));
        for (var ids : List.of(List.<UUID>of(), List.of(UUID.randomUUID()), List.of(lessonId, lessonId))) {
            assertThrows(ResponseStatusException.class,
                    () -> lessonService.reorderLessons(courseId, new LessonDTO.OrderRequest(ids), "teacher"));
        }
        assertEquals(1, lesson.getPosition());
        verifyNoInteractions(questionService);
        verify(lessonRepository, never()).flush();
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void reorderLessons_OnlyResponsibleTeacherMayReorder(UserRole role) {
        outroUser.setRole(role);
        when(userService.findByEmail("other")).thenReturn(outroUser);
        when(courseRepository.findForUpdateById(courseId)).thenReturn(Optional.of(course));
        var exception = assertThrows(ResponseStatusException.class,
                () -> lessonService.reorderLessons(courseId, new LessonDTO.OrderRequest(List.of(lessonId)), "other"));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(lessonRepository, questionService);
    }

    @Test
    void getLessonOrder_ShouldReturnAllLessonsInStoredOrder() {
        professor.setRole(UserRole.TEACHER);
        lesson.setPosition(2);
        when(userService.findByEmail("teacher")).thenReturn(professor);
        when(courseRepository.findForUpdateById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findAllByCourseIdOrderByPositionAscIdAsc(courseId)).thenReturn(List.of(lesson));
        assertEquals(List.of(new LessonDTO.OrderItem(lessonId, lesson.getTitle(), 2)), lessonService.getLessonOrder(courseId, "teacher"));
    }

    @Test
    void createLesson_ShouldAppendAfterLastPosition() {
        var request = new LessonDTO.Request("Nova aula", "Descrição", "https://example.com/video", "https://example.com/image");
        when(userService.findByEmail("teacher")).thenReturn(professor);
        when(courseRepository.findForUpdateById(courseId)).thenReturn(Optional.of(course));
        when(lessonMapper.toEntity(request)).thenReturn(lesson);
        when(lessonRepository.nextPosition(courseId)).thenReturn(9);
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        lessonService.createLesson(courseId, request, "teacher");
        assertEquals(9, lesson.getPosition());
    }

    @Test
    void cannotCreateOrEditLessonWhenVideoValidationFails() {
        var request = new LessonDTO.Request("Finanças", "Descrição", "https://youtu.be/dQw4w9WgXcQ", "https://example.com/fake-thumbnail");
        when(userService.findByEmail("teacher")).thenReturn(professor);
        when(courseRepository.findForUpdateById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findByIdAndCourse_Professor_Id(lessonId, professor.getId())).thenReturn(Optional.of(lesson));
        when(videoPolicy.validate(request.mediaUrl(), professor.getId()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Canal não aprovado."));
        String originalTitle = lesson.getTitle();
        assertThrows(ResponseStatusException.class, () -> lessonService.createLesson(courseId, request, "teacher"));
        assertThrows(ResponseStatusException.class, () -> lessonService.updateLesson(lessonId, request, "teacher"));
        assertEquals(originalTitle, lesson.getTitle());
        verify(lessonRepository, never()).save(any());
        verifyNoInteractions(lessonMapper);
    }

    @Test
    void unavailableVideoDoesNotExposeItsMediaToTheStudent() {
        when(userService.findByEmail("student")).thenReturn(outroUser);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(questionService.canPreview(lesson, "student")).thenReturn(true);
        when(videoPolicy.deliveryIssues(List.of(lesson))).thenReturn(Map.of(lessonId, "Vídeo indisponível."));
        var result = lessonService.getLessonForStudent(lessonId, "student");
        assertNull(result.mediaUrl());
        assertNull(result.thumbnailUrl());
        assertEquals("Vídeo indisponível.", result.videoIssue());
        verifyNoInteractions(lessonMapper);
    }
}
