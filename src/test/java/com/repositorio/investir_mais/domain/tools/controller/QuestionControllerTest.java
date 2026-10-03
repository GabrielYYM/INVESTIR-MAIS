package com.repositorio.investir_mais.domain.tools.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repositorio.investir_mais.domain.tools.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;
import com.repositorio.investir_mais.domain.tools.service.QuestionService;

@WebMvcTest(controllers = QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockBean(JpaMetamodelMappingContext.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestionService questionService;

    private UUID questionId;
    private QuestionResponseDTO responseDTO;
    private QuestionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        questionId = UUID.randomUUID();
        responseDTO = new QuestionResponseDTO(questionId, "Qual o seu objetivo?", AssetRole.AÇÕES);
        requestDTO = new QuestionRequestDTO("Qual o seu objetivo?", AssetRole.AÇÕES);
    }

    @Test
    @DisplayName("GET /api/questions/roles/{role} - Deve retornar página de questões")
    void shouldGetQuestionsByRole() throws Exception {
        when(questionService.getQuestionsByRole(eq(AssetRole.AÇÕES), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseDTO)));

        mockMvc.perform(get("/api/questions/roles/AÇÕES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].text").value("Qual o seu objetivo?"));
    }

    @Test
    @DisplayName("POST /api/questions - Deve criar nova questão")
    void shouldCreateQuestion() throws Exception {
        when(questionService.createQuestion(any(QuestionRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questionId.toString()))
                .andExpect(jsonPath("$.text").value("Qual o seu objetivo?"));
    }
}