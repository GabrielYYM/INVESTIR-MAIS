package com.repositorio.investir_mais.domain.education.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.mapper.CourseMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final UserService userService;

    public CourseDTO.Response createCourse(CourseDTO.Request request, String userEmail) {
        User professor = userService.findByEmail(userEmail);
        Course course = courseMapper.toEntity(request);
        course.setProfessor(professor);
        return courseMapper.toDTO(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public CourseDTO.Response getCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
            .map(courseMapper::toDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado."));
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO.Response> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(courseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO.Response> getCoursesByProfessor(UUID professorId, Pageable pageable) {
        return courseRepository.findAllByProfessorId(professorId, pageable).map(courseMapper::toDTO);
    }

    public CourseDTO.Response updateCourse(UUID courseId, CourseDTO.Request request, String userEmail) {
        Course course = findCourseOwnedBy(courseId, userEmail);
        courseMapper.updateEntity(request, course);
        return courseMapper.toDTO(courseRepository.save(course));
    }

    public void deleteCourse(UUID courseId, String userEmail) {
        Course course = findCourseOwnedBy(courseId, userEmail);
        courseRepository.delete(course);
    }

    private Course findCourseOwnedBy(UUID courseId, String userEmail) {
        User professor = userService.findByEmail(userEmail);
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado."));

        if (!course.getProfessor().getId().equals(professor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para alterar este curso.");
        }
        return course;
    }
}