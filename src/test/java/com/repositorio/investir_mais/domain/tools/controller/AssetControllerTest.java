package com.repositorio.investir_mais.domain.tools.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repositorio.investir_mais.domain.tools.DTO.AssetRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.AssetResponseDTO;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;
import com.repositorio.investir_mais.domain.tools.service.AssetService;

@WebMvcTest(controllers = AssetController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockBean(JpaMetamodelMappingContext.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetService assetService;

    private UUID assetId;
    private AssetResponseDTO assetResponseDTO;
    private AssetRequestDTO assetRequestDTO;

    @BeforeEach
    void setUp() {
        assetId = UUID.randomUUID();

        assetResponseDTO = new AssetResponseDTO(
            assetId, 
            "PETR4", 
            new BigDecimal("3000.00"), 
            new BigDecimal("100"), 
            new BigDecimal("30.00"), 
            10, 
            AssetRole.AÇÕES
        );

        assetRequestDTO = new AssetRequestDTO(
            assetId,
            "PETR4", 
            new BigDecimal("3000.00"), 
            new BigDecimal("100"), 
            new BigDecimal("30.00"), 
            10, 
            AssetRole.AÇÕES
        );
    }

    @Test
    @DisplayName("GET /api/assets - Deve listar todos os ativos com HTTP Status 200")
    void shouldListAllAssets() throws Exception {
        when(assetService.listAllAssets()).thenReturn(List.of(assetResponseDTO));

        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$[0].role").value("AÇÕES"));
    }

    @Test
    @DisplayName("POST /api/assets - Deve criar um ativo e retornar HTTP Status 200")
    void shouldCreateAsset() throws Exception {
        when(assetService.createAsset(any(AssetRequestDTO.class))).thenReturn(assetResponseDTO);

        mockMvc.perform(post("/api/assets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assetRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(assetId.toString()))
                .andExpect(jsonPath("$.ticker").value("PETR4"));
    }

    @Test
    @DisplayName("DELETE /api/assets/{id} - Deve deletar ativo com HTTP Status 200")
    void shouldDeleteAsset() throws Exception {
        mockMvc.perform(delete("/api/assets/{id}", assetId))
                .andExpect(status().isOk());
    }
}