package com.repositorio.investir_mais.domain.tools.controller;

import java.util.UUID;
import java.util.List;
import java.security.Principal;
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
    public List<AssetResponseDTO> listAllAssets(Principal principal) {
        return principal == null ? assetService.listAllAssets() : assetService.listAllAssets(principal.getName());
    }

    @GetMapping("/roles/{role}")
    public List<AssetResponseDTO> listByRole(@PathVariable AssetRole role, Principal principal) {
        return principal == null ? assetService.listAssetsByRole(role)
            : assetService.listAssetsByRole(principal.getName(), role);
    }

    @PostMapping
    public AssetResponseDTO createAsset(@Valid @RequestBody AssetRequestDTO request, Principal principal) {
        return principal == null ? assetService.createAsset(request)
            : assetService.createAsset(principal.getName(), request);
    }

    @PutMapping("/{id}")
    public AssetResponseDTO updateAsset(@PathVariable UUID id, @Valid @RequestBody AssetRequestDTO request,
            Principal principal) {
        return principal == null ? assetService.updateAsset(id, request)
            : assetService.updateAsset(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteAsset(@PathVariable UUID id, Principal principal) {
        if (principal == null) assetService.deleteAsset(id);
        else assetService.deleteAsset(principal.getName(), id);
    }

    @GetMapping("/quotes")
    public String getQuotesForAllAssets(Principal principal) {
        return principal == null ? assetService.getQuotesForAllAssets()
            : assetService.getQuotesForAllAssets(principal.getName());
    }

    @GetMapping("/quote/{ticker}")
    public String getAssetQuote(@PathVariable String ticker) {
        return assetService.getAssetQuote(ticker);
    }
}

