package com.repositorio.investir_mais.domain.education.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.service.LessonService;
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

@WebMvcTest(LessonController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LessonService lessonService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createLesson_ShouldReturnCreated() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        LessonDTO.Request requestDTO = new LessonDTO.Request("Aula 1", "Descrição", "url_video", "url_thumb");
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula 1", "Descrição", "url_video", "url_thumb", courseId);

        when(lessonService.createLesson(eq(courseId), any(LessonDTO.Request.class), eq("prof@investir.com"))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/lessons/course/{courseId}", courseId)
                .with(jwt().jwt(jwt -> jwt.subject("prof@investir.com")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.title").value("Aula 1"));
    }

    @Test
    void getLessonsByCourse_ShouldReturnOk() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(lessonService.getLessonsByCourseId(eq(courseId), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/lessons/course/{courseId}", courseId)
                .with(jwt()))
               .andExpect(status().isOk());
    }

    @Test
    void getLessonById_ShouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula 1", "Descrição", "url_video", "url_thumb", UUID.randomUUID());

        when(lessonService.getLessonById(lessonId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/lessons/{lessonId}", lessonId)
                .with(jwt()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.title").value("Aula 1"));
    }

    @Test
    void updateLesson_ShouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        LessonDTO.Request requestDTO = new LessonDTO.Request("Aula Alterada", "Descrição", "url_video", "url_thumb");
        LessonDTO.Response responseDTO = new LessonDTO.Response(lessonId, "Aula Alterada", "Descrição", "url_video", "url_thumb", UUID.randomUUID());

        when(lessonService.updateLesson(eq(lessonId), any(LessonDTO.Request.class), eq("prof@investir.com"))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/lessons/{lessonId}", lessonId)
                .with(jwt().jwt(jwt -> jwt.subject("prof@investir.com")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.title").value("Aula Alterada"));
    }

    @Test
    void deleteLesson_ShouldReturnNoContent() throws Exception {
        UUID lessonId = UUID.randomUUID();

        mockMvc.perform(delete("/api/lessons/{lessonId}", lessonId)
                .with(jwt().jwt(jwt -> jwt.subject("prof@investir.com")))
                .with(csrf()))
               .andExpect(status().isNoContent());

        verify(lessonService).deleteLesson(lessonId, "prof@investir.com");
    }
}