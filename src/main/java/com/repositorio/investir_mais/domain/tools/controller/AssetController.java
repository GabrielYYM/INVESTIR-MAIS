package com.repositorio.investir_mais.domain.tools.controller;

import java.util.List;
import java.util.UUID;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.domain.tools.DTO.AssetRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.AssetResponseDTO;
import com.repositorio.investir_mais.domain.tools.service.AssetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    @GetMapping
    public List<AssetResponseDTO> listAllAssets() {
        return assetService.listAllAssets();
    }

    @GetMapping("/roles/{role}")
    public List<AssetResponseDTO> listByRole(@PathVariable AssetRole role) {
        return assetService.listAssetsByRole(role);
    }

    @PostMapping
    public AssetResponseDTO createAsset(@Valid @RequestBody AssetRequestDTO request) {
        return assetService.createAsset(request);
    }

    @PutMapping("/{id}")
    public AssetResponseDTO updateAsset(@PathVariable UUID id, @Valid @RequestBody AssetRequestDTO request) {
        return assetService.updateAsset(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteAsset(@PathVariable UUID id) {
    assetService.deleteAsset(id);
    }

    @GetMapping("/quotes")
    public String getQuotesForAllAssets() {
        return assetService.getQuotesForAllAssets();
    }

    @GetMapping("/quote/{ticker}")
    public String getAssetQuote(@PathVariable String ticker) {
        return assetService.getAssetQuote(ticker);
    }
}

