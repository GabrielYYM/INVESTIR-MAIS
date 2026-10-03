package com.repositorio.investir_mais.domain.tools.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final RestClient brapiClient;
    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    public List<AssetResponseDTO> listAllAssets() {
        return assetRepository.findAll()
            .stream()
            .map(assetMapper::toDTO)
            .toList();
    }

    public AssetResponseDTO findAssetById(UUID id) {
        return assetRepository.findById(id)
            .map(assetMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com o ID: " + id));
    }

    public AssetResponseDTO createAsset(AssetRequestDTO request) {
        if (request.role() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O tipo do ativo é obrigatório.");
        }
        Asset asset = assetMapper.toEntity(request);
        return assetMapper.toDTO(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponseDTO updateAsset(UUID id, AssetRequestDTO request) {
        Asset existingAsset = assetRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com o ID: " + id));

        assetMapper.updateEntityFromDto(request, existingAsset);

        return assetMapper.toDTO(assetRepository.save(existingAsset));
    }

    public void deleteAsset(UUID id) {
        assetRepository.deleteById(id);
    }

    public String getAssetQuote(String ticker) {
        return brapiClient.get()
            .uri("/quote/{ticker}", ticker)
            .retrieve()
            .body(String.class);
    }

    public String getQuotesForAllAssets() {
        List<Asset> assets = assetRepository.findAll();

        if (assets.isEmpty()) {
            return "[]";
        }

        String tickers = assets.stream()
            .map(Asset::getTicker)
            .collect(java.util.stream.Collectors.joining(","));

        return brapiClient.get()
            .uri("/quote/{tickers}", tickers)
            .retrieve()
            .body(String.class);
    }

    public List<AssetResponseDTO> listAssetsByRole(AssetRole role) {
        return assetRepository.findByRole(role)
            .stream()
            .map(assetMapper::toDTO)
            .toList();
    }

    public Asset findById(UUID id) {
        return assetRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Asset não encontrado com o ID: " + id));
    }
}
