package com.repositorio.investir_mais.domain.education.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final LessonMapper lessonMapper;
    private final UserService userService;

    public LessonResponseDTO createLesson(UUID courseId, LessonRequestDTO request, String userEmail) {
        User user = userService.findByEmail(userEmail);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado."));

        if (!course.getProfessor().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para adicionar aulas neste curso.");
        }

        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setCourse(course);

        return lessonMapper.toDTO(lessonRepository.save(lesson));
    }

    @Transactional(readOnly = true)
    public LessonResponseDTO getLessonById(UUID lessonId) {
        return lessonRepository.findById(lessonId)
            .map(lessonMapper::toDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));
    }

    @Transactional(readOnly = true)
    public Page<LessonResponseDTO> getLessonsByCourseId(UUID courseId, Pageable pageable) {
        return lessonRepository.findAllByCourseId(courseId, pageable)
            .map(lessonMapper::toDTO);
    }

    public LessonResponseDTO updateLesson(UUID lessonId, LessonRequestDTO request, String userEmail) {
        Lesson lesson = findLessonOwnedBy(lessonId, userEmail);
        lessonMapper.updateEntity(request, lesson);
        return lessonMapper.toDTO(lessonRepository.save(lesson));
    }

    public void deleteLesson(UUID lessonId, String userEmail) {
        lessonRepository.delete(findLessonOwnedBy(lessonId, userEmail));
    }

    public Lesson findLessonOwnedBy(UUID lessonId, String userEmail) {
        User professor = userService.findByEmail(userEmail);
        return lessonRepository.findByIdAndCourse_Professor_Id(lessonId, professor.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Aula não encontrada ou sem permissão."));
    }
}