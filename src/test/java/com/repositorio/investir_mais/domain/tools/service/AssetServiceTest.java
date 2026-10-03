package com.repositorio.investir_mais.domain.tools.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.tools.DTO.AssetRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.AssetResponseDTO;
import com.repositorio.investir_mais.domain.tools.mapper.AssetMapper;
import com.repositorio.investir_mais.domain.tools.model.Asset;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;
import com.repositorio.investir_mais.domain.tools.repository.AssetRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private RestClient brapiClient;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetMapper assetMapper;

    @InjectMocks
    private AssetService assetService;

    private Asset asset;
    private AssetRequestDTO assetRequestDTO;
    private AssetResponseDTO assetResponseDTO;
    private UUID assetId;

    @BeforeEach
    void setUp() {
        assetId = UUID.randomUUID();
        
        asset = new Asset();
        asset.setId(assetId);
        asset.setTicker("PETR4");
        asset.setRole(AssetRole.AÇÕES);
        asset.setQuantity(new BigDecimal("100"));
        asset.setAveragePrice(new BigDecimal("30.00"));

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

    @Nested
    @DisplayName("Testes de Consulta de Ativos")
    class FindAndListTests {

        @Test
        @DisplayName("Deve retornar lista de todos os ativos")
        void shouldListAllAssets() {
            when(assetRepository.findAll()).thenReturn(List.of(asset));
            when(assetMapper.toDTO(asset)).thenReturn(assetResponseDTO);

            List<AssetResponseDTO> result = assetService.listAllAssets();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("PETR4", result.get(0).ticker());
            verify(assetRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar ativo por ID com sucesso")
        void shouldFindAssetByIdSuccess() {
            when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
            when(assetMapper.toDTO(asset)).thenReturn(assetResponseDTO);

            AssetResponseDTO result = assetService.findAssetById(assetId);

            assertNotNull(result);
            assertEquals(assetId, result.id());
            verify(assetRepository, times(1)).findById(assetId);
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando ativo não for encontrado por ID")
        void shouldThrowExceptionWhenAssetNotFoundById() {
            when(assetRepository.findById(assetId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> assetService.findAssetById(assetId));
            verify(assetRepository, times(1)).findById(assetId);
        }

        @Test
        @DisplayName("Deve listar ativos por Role")
        void shouldListAssetsByRole() {
            when(assetRepository.findByRole(AssetRole.AÇÕES)).thenReturn(List.of(asset));
            when(assetMapper.toDTO(asset)).thenReturn(assetResponseDTO);

            List<AssetResponseDTO> result = assetService.listAssetsByRole(AssetRole.AÇÕES);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(AssetRole.AÇÕES, result.get(0).role());
            verify(assetRepository, times(1)).findByRole(AssetRole.AÇÕES);
        }
    }

    @Nested
    @DisplayName("Testes de Criação e Atualização")
    class SaveAndUpdateTests {

        @Test
        @DisplayName("Deve criar um ativo com sucesso")
        void shouldCreateAssetSuccess() {
            when(assetMapper.toEntity(assetRequestDTO)).thenReturn(asset);
            when(assetRepository.save(asset)).thenReturn(asset);
            when(assetMapper.toDTO(asset)).thenReturn(assetResponseDTO);

            AssetResponseDTO result = assetService.createAsset(assetRequestDTO);

            assertNotNull(result);
            assertEquals("PETR4", result.ticker());
            verify(assetRepository, times(1)).save(asset);
        }

        @Test
        @DisplayName("Deve lançar ResponseStatusException (BAD_REQUEST) se Role for nula na criação")
        void shouldThrowExceptionWhenRoleIsNullOnCreate() {
            AssetRequestDTO invalidDTO = new AssetRequestDTO(
                assetId,
                "PETR4", 
                new BigDecimal("3000.00"), 
                new BigDecimal("100"), 
                new BigDecimal("30.00"), 
                10, 
                null
            );

            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, 
                () -> assetService.createAsset(invalidDTO)
            );

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            verify(assetRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve atualizar um ativo com sucesso")
        void shouldUpdateAssetSuccess() {
            when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
            doNothing().when(assetMapper).updateEntityFromDto(assetRequestDTO, asset);
            when(assetRepository.save(asset)).thenReturn(asset);
            when(assetMapper.toDTO(asset)).thenReturn(assetResponseDTO);

            AssetResponseDTO result = assetService.updateAsset(assetId, assetRequestDTO);

            assertNotNull(result);
            verify(assetMapper, times(1)).updateEntityFromDto(assetRequestDTO, asset);
            verify(assetRepository, times(1)).save(asset);
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException ao tentar atualizar ativo inexistente")
        void shouldThrowExceptionWhenUpdatingNonExistingAsset() {
            when(assetRepository.findById(assetId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> assetService.updateAsset(assetId, assetRequestDTO));
            verify(assetRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção")
    class DeleteTests {

        @Test
        @DisplayName("Deve deletar ativo por ID")
        void shouldDeleteAssetById() {
            doNothing().when(assetRepository).deleteById(assetId);

            assetService.deleteAsset(assetId);

            verify(assetRepository, times(1)).deleteById(assetId);
        }
    }

    @Nested
    @DisplayName("Testes de Integração com RestClient (Cotações)")
    class QuoteTests {

        @Test
        @DisplayName("Deve retornar string vazia formato json quando não houver ativos para cotação")
        void shouldReturnEmptyJsonArrayWhenNoAssetsFoundForQuotes() {
            when(assetRepository.findAll()).thenReturn(List.of());

            String result = assetService.getQuotesForAllAssets();

            assertEquals("[]", result);
            verify(brapiClient, never()).get();
        }
    }
}