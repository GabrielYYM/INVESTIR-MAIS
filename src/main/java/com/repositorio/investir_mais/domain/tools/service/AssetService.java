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
import com.repositorio.investir_mais.domain.tools.repository.PortfolioRepository;
import com.repositorio.investir_mais.domain.tools.model.Portfolio;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final RestClient brapiClient;
    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public List<AssetResponseDTO> listAllAssets() {
        return assetRepository.findAll()
            .stream()
            .map(assetMapper::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AssetResponseDTO> listAllAssets(String principalName) {
        User user = resolveUser(principalName);
        return assetRepository.findByPortfolio_UserId_Id(user.getId()).stream().map(assetMapper::toDTO).toList();
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
    public AssetResponseDTO createAsset(String principalName, AssetRequestDTO request) {
        if (request.role() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O tipo do ativo é obrigatório.");
        }
        Asset asset = assetMapper.toEntity(request);
        asset.setPortfolio(getOrCreatePortfolio(resolveUser(principalName)));
        return assetMapper.toDTO(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponseDTO updateAsset(UUID id, AssetRequestDTO request) {
        Asset existingAsset = assetRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com o ID: " + id));

        assetMapper.updateEntityFromDto(request, existingAsset);

        return assetMapper.toDTO(assetRepository.save(existingAsset));
    }

    @Transactional
    public AssetResponseDTO updateAsset(String principalName, UUID id, AssetRequestDTO request) {
        Asset existingAsset = findOwnedAsset(principalName, id);
        assetMapper.updateEntityFromDto(request, existingAsset);
        return assetMapper.toDTO(assetRepository.save(existingAsset));
    }

    public void deleteAsset(UUID id) {
        assetRepository.deleteById(id);
    }

    @Transactional
    public void deleteAsset(String principalName, UUID id) {
        assetRepository.delete(findOwnedAsset(principalName, id));
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

    @Transactional(readOnly = true)
    public String getQuotesForAllAssets(String principalName) {
        User user = resolveUser(principalName);
        List<Asset> assets = assetRepository.findByPortfolio_UserId_Id(user.getId());
        if (assets.isEmpty()) return "[]";
        String tickers = assets.stream().map(Asset::getTicker)
            .collect(java.util.stream.Collectors.joining(","));
        return brapiClient.get().uri("/quote/{tickers}", tickers).retrieve().body(String.class);
    }

    public List<AssetResponseDTO> listAssetsByRole(AssetRole role) {
        return assetRepository.findByRole(role)
            .stream()
            .map(assetMapper::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AssetResponseDTO> listAssetsByRole(String principalName, AssetRole role) {
        User user = resolveUser(principalName);
        return assetRepository.findByPortfolio_UserId_IdAndRole(user.getId(), role)
            .stream().map(assetMapper::toDTO).toList();
    }

    private User resolveUser(String principalName) {
        try {
            return userRepository.findById(UUID.fromString(principalName))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        } catch (IllegalArgumentException ignored) {
            User user = userRepository.findByUserSecurityEmail(principalName);
            if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
            return user;
        }
    }

    private Portfolio getOrCreatePortfolio(User user) {
        return portfolioRepository.findByUserId_Id(user.getId()).orElseGet(() -> {
            Portfolio portfolio = new Portfolio();
            portfolio.setUserId(user);
            return portfolioRepository.save(portfolio);
        });
    }

    private Asset findOwnedAsset(String principalName, UUID id) {
        User user = resolveUser(principalName);
        return assetRepository.findById(id)
            .filter(asset -> asset.getPortfolio() != null
                && asset.getPortfolio().getUserId().getId().equals(user.getId()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ativo não encontrado."));
    }

    public Asset findById(UUID id) {
        return assetRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Asset não encontrado com o ID: " + id));
    }

}
