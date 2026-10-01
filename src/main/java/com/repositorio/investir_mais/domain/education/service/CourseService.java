package com.repositorio.investir_mais.domain.education.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.education.DTO.CourseRequestDTO;
import com.repositorio.investir_mais.domain.education.DTO.CourseResponseDTO;
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

    public CourseResponseDTO createCourse(CourseRequestDTO request, String userEmail) {
        User professor = userService.findByEmail(userEmail);
        Course course = courseMapper.toEntity(request);
        course.setProfessorId(professor);
        return courseMapper.toResponseDTO(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public CourseResponseDTO getCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
            .map(courseMapper::toResponseDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado."));
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(courseMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> getCoursesByProfessor(UUID professorId, Pageable pageable) {
        return courseRepository.findAllByProfessorId(professorId, pageable).map(courseMapper::toResponseDTO);
    }

    public CourseResponseDTO updateCourse(UUID courseId, CourseRequestDTO request, String userEmail) {
        Course course = findCourseOwnedBy(courseId, userEmail);
        courseMapper.updateEntity(request, course);
        return courseMapper.toResponseDTO(courseRepository.save(course));
    }

    public void deleteCourse(UUID courseId, String userEmail) {
        courseRepository.delete(findCourseOwnedBy(courseId, userEmail));
    }

    public Course findCourseOwnedBy(UUID courseId, String userEmail) {
        User professor = userService.findByEmail(userEmail);
        return courseRepository.findByIdAndProfessorId(courseId, professor.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Curso não encontrado ou sem permissão."));
    }
}
