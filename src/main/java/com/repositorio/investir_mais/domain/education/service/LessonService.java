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
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {

    private final LessonRepository lessionRepository;
    private final CourseService courseService;
    private final LessonMapper lessionMapper;
    private final UserService userService;

    public LessonResponseDTO createLession(UUID courseId, LessonRequestDTO request, String userEmail) {
        Course course = courseService.findCourseOwnedBy(courseId, userEmail);

        Lesson lession = lessionMapper.toEntity(request);
        lession.setCourse(course);

        return lessionMapper.toResponseDTO(lessionRepository.save(lession));
    }

    @Transactional(readOnly = true)
    public LessonResponseDTO getLessionById(UUID lessionId) {
        return lessionRepository.findById(lessionId)
            .map(lessionMapper::toResponseDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));
    }

    @Transactional(readOnly = true)
    public Page<LessonResponseDTO> getLessionsByCourseId(UUID courseId, Pageable pageable) {
        return lessionRepository.findAllByCourseId(courseId, pageable)
            .map(lessionMapper::toResponseDTO);
    }

    public LessonResponseDTO updateLession(UUID lessionId, LessonRequestDTO request, String userEmail) {
        Lesson lession = findLessionOwnedBy(lessionId, userEmail);
        lessionMapper.updateEntity(request, lession);
        return lessionMapper.toResponseDTO(lessionRepository.save(lession));
    }

    public void deleteLession(UUID lessionId, String userEmail) {
        lessionRepository.delete(findLessionOwnedBy(lessionId, userEmail));
    }

    private Lesson findLessionOwnedBy(UUID lessionId, String userEmail) {
        User professor = userService.findByEmail(userEmail);
        return lessionRepository.findByIdAndCourseProfessorId(lessionId, professor.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Aula não encontrada ou sem permissão."));
    }
}
