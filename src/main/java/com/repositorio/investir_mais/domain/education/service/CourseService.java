package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.mapper.CourseMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final UserService userService;

    public CourseDTO.Response createCourse(CourseDTO.Request request, String principalName) {
        User professor = userService.findAuthenticatedUser(principalName);
        Course course = courseMapper.toEntity(request);
        course.setProfessor(professor);
        
        Course savedCourse = courseRepository.save(course);
        log.info("Novo curso criado com sucesso. ID: {}, Professor ID: {}", savedCourse.getId(), professor.getId());
        return courseMapper.toDTO(savedCourse);
    }

    @Transactional(readOnly = true)
    public CourseDTO.Response getCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
            .map(courseMapper::toDTO)
            .orElseThrow(() -> {
                log.warn("Busca por curso inexistente. ID: {}", courseId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado.");
            });
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO.Response> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(courseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO.Response> getCoursesByProfessor(UUID professorId, Pageable pageable) {
        return courseRepository.findAllByProfessorId(professorId, pageable).map(courseMapper::toDTO);
    }

    public CourseDTO.Response updateCourse(UUID courseId, CourseDTO.Request request, String principalName) {
        Course course = findCourseOwnedBy(courseId, principalName);
        courseMapper.updateEntity(request, course);
        
        Course updatedCourse = courseRepository.save(course);
        log.info("Curso atualizado com sucesso. ID: {}", updatedCourse.getId());
        return courseMapper.toDTO(updatedCourse);
    }

    public void deleteCourse(UUID courseId, String principalName) {
        Course course = findCourseOwnedBy(courseId, principalName);
        courseRepository.delete(course);
        log.info("Curso removido com sucesso. ID: {}", courseId);
    }

    private Course findCourseOwnedBy(UUID courseId, String principalName) {
        User professor = userService.findAuthenticatedUser(principalName);
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> {
                log.warn("Tentativa de alterar/excluir curso inexistente. ID: {}", courseId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado.");
            });

        if (!course.getProfessor().getId().equals(professor.getId())) {
            log.warn("Acesso negado: Usuário {} tentou modificar o curso {} pertencente ao professor {}", 
                professor.getId(), courseId, course.getProfessor().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para alterar este curso.");
        }
        return course;
    }
}