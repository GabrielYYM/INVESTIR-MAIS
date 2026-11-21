package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.mapper.LessonMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.service.UserService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseService courseService;
    private final LessonMapper lessonMapper;
    private final UserService userService;
    private final QuestionService questionService;

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonDTO.Response createLesson(UUID courseId, LessonDTO.Request request, String principalName) {
        Course course = courseService.findCourseOwnedBy(courseId, principalName);
        var video = LessonVideo.parse(request.mediaUrl());

        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setCourse(course);
        lesson.setMediaUrl(video.watchUrl());
        lesson.setThumbnailUrl(video.thumbnailUrl());
        lesson.setPosition(Math.max(1, lessonRepository.nextPosition(courseId)));

        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Nova aula cadastrada com sucesso. Aula ID: {}, Curso ID: {}", savedLesson.getId(), courseId);
        return lessonMapper.toDTO(savedLesson);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public LessonDTO.Response getLessonForStudent(UUID lessonId, String email) {
        return lessonMapper.toDTO(findLessonOrThrow(lessonId));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Page<LessonDTO.Response> getLessonsForStudent(UUID courseId, Pageable pageable, String email) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("position", "id"));
        return lessonRepository.findAllByCourseId(courseId, pageable).map(lessonMapper::toDTO);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public List<LessonDTO.OrderItem> getLessonOrder(UUID courseId, String email) {
        findCourseForOrdering(courseId, email);
        return lessonRepository.findAllByCourseIdOrderByPositionAscIdAsc(courseId).stream()
                .map(l -> new LessonDTO.OrderItem(l.getId(), l.getTitle(), l.getPosition())).toList();
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void reorderLessons(UUID courseId, LessonDTO.OrderRequest request, String email) {

        findCourseForOrdering(courseId, email);
        List<UUID> ids = request.lessonIds();
        if (ids == null || ids.stream().anyMatch(Objects::isNull) || new HashSet<>(ids).size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe cada aula uma única vez.");
        }
        var lessons = lessonRepository.findForUpdateByCourseIdOrderByPositionAscIdAsc(courseId);
        var byId = lessons.stream().collect(Collectors.toMap(Lesson::getId, Function.identity()));
        if (!byId.keySet().equals(new HashSet<>(ids))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A lista de aulas foi alterada. Atualize a página e tente novamente com todas as aulas do curso.");
        }
        var positions = new HashMap<Integer, Integer>();
        for (int i = 0; i < ids.size(); i++) {
            Lesson lesson = byId.get(ids.get(i));
            positions.put(lesson.getPosition(), i + 1);
        }
        questionService.relocateLessonQuestions(courseId, positions);
        for (int i = 0; i < ids.size(); i++) byId.get(ids.get(i)).setPosition(i + 1);
        lessonRepository.flush();
        log.info("Ordem das aulas do curso {} alterada pelo professor {}", courseId, email);
    }

    private Course findCourseForOrdering(UUID courseId, String email) {
        User user = userService.findByEmail(email);
        Course course = courseService.findCourseForProgressUpdate(courseId);
        if (user.getRole() != UserRole.ADMIN && (user.getRole() != UserRole.TEACHER || !course.getProfessor().getId().equals(user.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente administradores e o professor responsável podem alterar a ordem das aulas.");
        }
        return course;
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonDTO.Response updateLesson(UUID lessonId, LessonDTO.Request request, String principalName) {
        Lesson lesson = findLessonOwnedBy(lessonId, principalName);
        var video = LessonVideo.parse(request.mediaUrl());
        lessonMapper.updateEntity(request, lesson);
        lesson.setMediaUrl(video.watchUrl());
        lesson.setThumbnailUrl(video.thumbnailUrl());

        log.info("Aula atualizada com sucesso. ID: {}", lesson.getId());
        return lessonMapper.toDTO(lesson);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void deleteLesson(UUID lessonId, String principalName) {
        Lesson lesson = findLessonOwnedBy(lessonId, principalName);
        questionService.assertCanDeleteLesson(lesson);
        lessonRepository.delete(lesson);
        log.info("Aula removida com sucesso. ID: {}", lessonId);
    }

    @PreAuthorize("isAuthenticated()")
    public CourseDTO.CourseProgressResponse updateLessonProgress(UUID courseId, UUID lessonId, boolean completed, String studentEmail) {
        User student = userService.findByEmailForUpdate(studentEmail);
        Course course = courseService.findCourseOrThrow(courseId);
        Lesson lesson = findLessonOrThrow(lessonId);

        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A aula informada não pertence ao curso.");
        }

        courseService.findCourseForProgressUpdate(courseId);
        lessonRepository.findForUpdateById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));
        student.enrollIn(course);
        student.setLessonCompleted(lesson, completed);

        log.info("Progresso da aula {} atualizado para {} pelo aluno {}", lessonId, completed, student.getEmail());
        return courseService.buildCourseProgress(courseId, student.getId(), true);
    }

    private Lesson findLessonOrThrow(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));
    }

    public Lesson findLessonOwnedBy(UUID lessonId, String principalName) {
        User professor = userService.findByEmail(principalName);
        if (professor.getRole() == UserRole.ADMIN) {
            return lessonRepository.findForUpdateById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));
        }
        return lessonRepository.findByIdAndCourse_Professor_Id(lessonId, professor.getId())
            .orElseThrow(() -> {
                log.warn("Acesso negado ou aula não encontrada. Aula ID: {}, Usuário: {}", lessonId, professor.getId());
                return new ResponseStatusException(HttpStatus.FORBIDDEN, "Aula não encontrada ou sem permissão.");
            });
    }
}
