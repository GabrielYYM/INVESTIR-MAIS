package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.mapper.LessonMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseService courseService;
    private final LessonMapper lessonMapper;
    private final UserService userService;

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonDTO.Response createLesson(UUID courseId, LessonDTO.Request request) {
        Course course = courseService.findCourseForUpdateOrThrow(courseId);

        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setCourse(course);

        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Nova aula cadastrada com sucesso. Aula ID: {}, Curso ID: {}", savedLesson.getId(), courseId);
        return lessonMapper.toDTO(savedLesson);
    }

    @Transactional(readOnly = true)
    public LessonDTO.Response getLessonById(UUID lessonId) {
        return lessonRepository.findResponseById(lessonId)
            .orElseThrow(() -> {
                log.warn("Busca por aula inexistente. ID: {}", lessonId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada.");
            });
    }

    @Transactional(readOnly = true)
    public Page<LessonDTO.Response> getLessonsByCourseId(UUID courseId, Pageable pageable) {
        return lessonRepository.findResponsesByCourseId(courseId, pageable);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonDTO.Response updateLesson(UUID lessonId, LessonDTO.Request request) {
        Lesson lesson = findLessonForUpdateOrThrow(lessonId);
        lessonMapper.updateEntity(request, lesson);
        
        log.info("Aula atualizada com sucesso. ID: {}", lesson.getId());
        return lessonMapper.toDTO(lesson);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void deleteLesson(UUID lessonId) {
        Lesson lesson = findLessonForUpdateOrThrow(lessonId);
        lessonRepository.delete(lesson);
        log.info("Aula removida com sucesso. ID: {}", lessonId);
    }

    @PreAuthorize("isAuthenticated()")
    public CourseDTO.CourseProgressResponse updateLessonProgress(UUID courseId, UUID lessonId, boolean completed, String studentEmail) {
        User student = userService.findByEmailForUpdate(studentEmail);
        Course course = courseService.findCourseOrThrow(courseId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));

        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A aula informada não pertence ao curso.");
        }

        student.enrollIn(course);
        student.setLessonCompleted(lesson, completed);

        log.info("Progresso da aula {} atualizado para {} pelo aluno {}", lessonId, completed, student.getEmail());
        return courseService.buildCourseProgress(courseId, student.getId(), true);
    }

    private Lesson findLessonForUpdateOrThrow(UUID lessonId) {
        return lessonRepository.findForUpdateById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));
    }
}
