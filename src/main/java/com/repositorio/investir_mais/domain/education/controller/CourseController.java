package com.repositorio.investir_mais.domain.education.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.domain.education.DTO.CourseRequestDTO;
import com.repositorio.investir_mais.domain.education.DTO.CourseResponseDTO;
import com.repositorio.investir_mais.domain.education.service.CourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public CourseResponseDTO createCourse(
            Principal principal,
            @Valid @RequestBody CourseRequestDTO dto) {
        return courseService.createCourse(dto, principal.getName());
    }

    @GetMapping
    public Page<CourseResponseDTO> getAllCourses(@PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return courseService.getAllCourses(pageable);
    }

    @GetMapping("/{courseId}")
    public CourseResponseDTO getCourseById(@PathVariable UUID courseId) {
        return courseService.getCourseById(courseId);
    }

    @GetMapping("/professor/{professorId}")
    public Page<CourseResponseDTO> getCoursesByProfessor(
            @PathVariable UUID professorId,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return courseService.getCoursesByProfessor(professorId, pageable);
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public CourseResponseDTO updateCourse(
            @PathVariable UUID courseId,
            Principal principal,
            @Valid @RequestBody CourseRequestDTO dto) {
        return courseService.updateCourse(courseId, dto, principal.getName());
    }

    @DeleteMapping("/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void deleteCourse(
            @PathVariable UUID courseId,
            Principal principal) {
        courseService.deleteCourse(courseId, principal.getName());
    }
}
