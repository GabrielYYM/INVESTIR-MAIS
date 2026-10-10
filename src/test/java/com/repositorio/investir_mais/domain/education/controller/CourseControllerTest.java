package com.repositorio.investir_mais.domain.education.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createCourse_WithValidData_ShouldReturnCreated() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        CourseDTO.Request requestDTO = new CourseDTO.Request("Curso de Investimentos", "Descrição exaustiva");
        CourseDTO.Response responseDTO = new CourseDTO.Response(courseId, "Curso de Investimentos", "Descrição exaustiva", professorId);

        when(courseService.createCourse(any(CourseDTO.Request.class), eq("prof@investir.com"))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/courses")
                .with(jwt().jwt(jwt -> jwt.subject("prof@investir.com")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.name").value("Curso de Investimentos"));
    }

    @Test
    void getAllCourses_ShouldReturnOkAndPage() throws Exception {
        when(courseService.getAllCourses(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/courses")
                .with(jwt()))
               .andExpect(status().isOk());
    }

    @Test
    void getCourseById_ShouldReturnOk() throws Exception {
        UUID courseId = UUID.randomUUID();
        CourseDTO.Response responseDTO = new CourseDTO.Response(courseId, "Curso A", "Descrição", UUID.randomUUID());

        when(courseService.getCourseById(courseId)).thenReturn(responseDTO);

        // Adicionado .with(jwt())
        mockMvc.perform(get("/api/courses/{courseId}", courseId)
                .with(jwt()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Curso A"));
    }

    @Test
    void updateCourse_ShouldReturnOk() throws Exception {
        UUID courseId = UUID.randomUUID();
        CourseDTO.Request requestDTO = new CourseDTO.Request("Curso Atualizado", "Nova Descrição");
        CourseDTO.Response responseDTO = new CourseDTO.Response(courseId, "Curso Atualizado", "Nova Descrição", UUID.randomUUID());

        when(courseService.updateCourse(eq(courseId), any(CourseDTO.Request.class), eq("prof@investir.com"))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/courses/{courseId}", courseId)
                .with(jwt().jwt(jwt -> jwt.subject("prof@investir.com")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Curso Atualizado"));
    }

    @Test
    void deleteCourse_ShouldReturnNoContent() throws Exception {
        UUID courseId = UUID.randomUUID();

        mockMvc.perform(delete("/api/courses/{courseId}", courseId)
                .with(jwt().jwt(jwt -> jwt.subject("prof@investir.com")))
                .with(csrf()))
               .andExpect(status().isNoContent());

        verify(courseService).deleteCourse(courseId, "prof@investir.com");
    }
}