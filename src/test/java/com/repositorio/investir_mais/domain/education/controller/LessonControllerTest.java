package com.repositorio.investir_mais.domain.education.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repositorio.investir_mais.domain.education.DTO.LessonRequestDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessonResponseDTO;
import com.repositorio.investir_mais.domain.education.service.LessonService;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;

@WebMvcTest(LessonController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createLesson_ShouldReturnCreated() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        LessonRequestDTO requestDTO = new LessonRequestDTO("Aula 1", "Descrição", "url_video", "url_thumb");
        LessonResponseDTO responseDTO = new LessonResponseDTO(lessonId, "Aula 1", "Descrição", "url_video", "url_thumb", courseId);

        Principal principal = () -> "prof@investir.com";

        when(lessonService.createLesson(eq(courseId), any(LessonRequestDTO.class), eq("prof@investir.com"))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/lessons/course/{courseId}", courseId)
                .principal(principal)
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

        mockMvc.perform(get("/api/lessons/course/{courseId}", courseId))
               .andExpect(status().isOk());
    }

    @Test
    void updateLesson_ShouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        LessonRequestDTO requestDTO = new LessonRequestDTO("Aula Alterada", "Descrição", "url_video", "url_thumb");
        LessonResponseDTO responseDTO = new LessonResponseDTO(lessonId, "Aula Alterada", "Descrição", "url_video", "url_thumb", UUID.randomUUID());

        Principal principal = () -> "prof@investir.com";

        when(lessonService.updateLesson(eq(lessonId), any(LessonRequestDTO.class), eq("prof@investir.com"))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/lessons/{lessonId}", lessonId)
                .principal(principal)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.title").value("Aula Alterada"));
    }

    @Test
    void deleteLesson_ShouldReturnNoContent() throws Exception {
        UUID lessonId = UUID.randomUUID();
        Principal principal = () -> "prof@investir.com";

        mockMvc.perform(delete("/api/lessons/{lessonId}", lessonId)
                .principal(principal)
                .with(csrf()))
               .andExpect(status().isNoContent());

        verify(lessonService).deleteLesson(lessonId, "prof@investir.com");
    }
}