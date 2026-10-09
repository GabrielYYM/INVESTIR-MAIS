package com.repositorio.investir_mais.domain.education.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.mapper.CourseMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private CourseService courseService;

    private User professor;
    private User outroProfessor;
    private Course course;
    private UUID courseId;
    private UUID professorId;

    @BeforeEach
    void setUp() {
        professorId = UUID.randomUUID();
        professor = new User();
        professor.setId(professorId);

        outroProfessor = new User();
        outroProfessor.setId(UUID.randomUUID());

        courseId = UUID.randomUUID();
        course = new Course();
        course.setId(courseId);
        course.setName("Finanças Pessoais");
        course.setProfessor(professor);
    }

    @Test
    void createCourse_OnSuccess_ShouldBindProfessorAndSave() {
        CourseDTO.Request request = new CourseDTO.Request("Finanças Pessoais", "Descrição do Curso");
        CourseDTO.Response responseDTO = new CourseDTO.Response(courseId, "Finanças Pessoais", "Descrição do Curso", professorId);

        when(userService.findAuthenticatedUser("prof@investir.com")).thenReturn(professor);
        when(courseMapper.toEntity(request)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toDTO(course)).thenReturn(responseDTO);

        CourseDTO.Response result = courseService.createCourse(request, "prof@investir.com");

        assertNotNull(result);
        assertEquals(courseId, result.id());
        verify(courseRepository).save(course);
    }

    @Test
    void getCourseById_WhenCourseExists_ShouldReturnDTO() {
        CourseDTO.Response responseDTO = new CourseDTO.Response(courseId, "Finanças Pessoais", "Descrição do Curso", professorId);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseMapper.toDTO(course)).thenReturn(responseDTO);

        CourseDTO.Response result = courseService.getCourseById(courseId);

        assertNotNull(result);
        assertEquals("Finanças Pessoais", result.name());
    }

    @Test
    void getCourseById_WhenCourseDoesNotExist_ShouldThrowNotFound() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> courseService.getCourseById(courseId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getAllCourses_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(List.of(course));
        CourseDTO.Response responseDTO = new CourseDTO.Response(courseId, "Finanças Pessoais", "Descrição do Curso", professorId);

        when(courseRepository.findAll(pageable)).thenReturn(page);
        when(courseMapper.toDTO(course)).thenReturn(responseDTO);

        Page<CourseDTO.Response> result = courseService.getAllCourses(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getCoursesByProfessor_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(List.of(course));
        CourseDTO.Response responseDTO = new CourseDTO.Response(courseId, "Finanças Pessoais", "Descrição do Curso", professorId);

        when(courseRepository.findAllByProfessorId(professorId, pageable)).thenReturn(page);
        when(courseMapper.toDTO(course)).thenReturn(responseDTO);

        Page<CourseDTO.Response> result = courseService.getCoursesByProfessor(professorId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateCourse_WhenUserIsNotOwner_ShouldThrowForbidden() {
        CourseDTO.Request request = new CourseDTO.Request("Finanças Avançadas", "Nova Descrição");
        when(userService.findAuthenticatedUser("outro@investir.com")).thenReturn(outroProfessor);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> courseService.updateCourse(courseId, request, "outro@investir.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void updateCourse_OnSuccess_ShouldUpdateAndSave() {
        CourseDTO.Request request = new CourseDTO.Request("Finanças Avançadas", "Nova Descrição");
        CourseDTO.Response responseDTO = new CourseDTO.Response(courseId, "Finanças Avançadas", "Nova Descrição", professorId);

        when(userService.findAuthenticatedUser("prof@investir.com")).thenReturn(professor);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toDTO(course)).thenReturn(responseDTO);

        CourseDTO.Response result = courseService.updateCourse(courseId, request, "prof@investir.com");

        assertNotNull(result);
        verify(courseMapper).updateEntity(request, course);
        verify(courseRepository).save(course);
    }

    @Test
    void deleteCourse_OnSuccess_ShouldDelete() {
        when(userService.findAuthenticatedUser("prof@investir.com")).thenReturn(professor);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseService.deleteCourse(courseId, "prof@investir.com");

        verify(courseRepository).delete(course);
    }

    @Test
    void deleteCourse_WhenUserIsNotOwner_ShouldThrowForbidden() {
        when(userService.findAuthenticatedUser("outro@investir.com")).thenReturn(outroProfessor);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> courseService.deleteCourse(courseId, "outro@investir.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(courseRepository, never()).delete(any());
    }
}