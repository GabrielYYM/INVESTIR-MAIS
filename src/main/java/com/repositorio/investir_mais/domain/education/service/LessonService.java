package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.mapper.LessonMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
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
    public LessonDTO.Response createLesson(UUID courseId, LessonDTO.Request request, String principalName) {
        Course course = courseService.findCourseOwnedBy(courseId, principalName);

        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setCourse(course);

        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Nova aula cadastrada com sucesso. Aula ID: {}, Curso ID: {}", savedLesson.getId(), courseId);
        return lessonMapper.toDTO(savedLesson);
    }

    @Transactional(readOnly = true)
    public LessonDTO.Response getLessonById(UUID lessonId) {
        return lessonRepository.findById(lessonId)
            .map(lessonMapper::toDTO)
            .orElseThrow(() -> {
                log.warn("Busca por aula inexistente. ID: {}", lessonId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada.");
            });
    }

    @Transactional(readOnly = true)
    public Page<LessonDTO.Response> getLessonsByCourseId(UUID courseId, Pageable pageable) {
        return lessonRepository.findAllByCourseId(courseId, pageable)
            .map(lessonMapper::toDTO);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonDTO.Response updateLesson(UUID lessonId, LessonDTO.Request request, String principalName) {
        Lesson lesson = findLessonOwnedBy(lessonId, principalName);
        lessonMapper.updateEntity(request, lesson);
        
        log.info("Aula atualizada com sucesso. ID: {}", lesson.getId());
        return lessonMapper.toDTO(lesson);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void deleteLesson(UUID lessonId, String principalName) {
        Lesson lesson = findLessonOwnedBy(lessonId, principalName);
        lessonRepository.delete(lesson);
        log.info("Aula removida com sucesso. ID: {}", lessonId);
    }

    public Lesson findLessonOwnedBy(UUID lessonId, String principalName) {
        User professor = userService.findByEmail(principalName);
        if (professor.getRole() == UserRole.ADMIN) {
            return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));
        }
        return lessonRepository.findByIdAndCourse_Professor_Id(lessonId, professor.getId())
            .orElseThrow(() -> {
                log.warn("Acesso negado ou aula não encontrada. Aula ID: {}, Usuário: {}", lessonId, professor.getId());
                return new ResponseStatusException(HttpStatus.FORBIDDEN, "Aula não encontrada ou sem permissão.");
            });
    }
}
