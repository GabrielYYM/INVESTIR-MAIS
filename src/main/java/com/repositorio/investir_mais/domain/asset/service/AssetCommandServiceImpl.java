package com.repositorio.investir_mais.domain.asset.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.LogMessageConstants;
import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.DTO.AssetRequestDTO;
import com.repositorio.investir_mais.domain.asset.DTO.AssetResponseDTO;
import com.repositorio.investir_mais.domain.asset.mapper.AssetMapper;
import com.repositorio.investir_mais.domain.asset.model.Asset;
import com.repositorio.investir_mais.domain.asset.model.AssetCategory;
import com.repositorio.investir_mais.domain.asset.repository.AssetCategoryRepository;
import com.repositorio.investir_mais.domain.asset.repository.AssetRepository;
import com.repositorio.investir_mais.domain.asset.service.interfaces.AssetCommandService;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.infrastructure.security.UserContextService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetCommandServiceImpl implements AssetCommandService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;
    private final UserContextService userContextService;
    private final AssetMapper assetMapper;

    @Override
    @Transactional
    public ServiceResult<AssetResponseDTO> createAsset(@NonNull UUID categoryId, @NonNull AssetRequestDTO request) {
        return getCategoryForCurrentUser(categoryId)
                .map(category -> {
                    Asset asset = assetMapper.toEntity(request);
                    asset.setCategory(category);

                    Asset savedAsset = assetRepository.save(asset);
                    return ServiceResult.success(assetMapper.toResponse(savedAsset));
                })
                .orElseGet(() -> ServiceResult.notFound(MessageConstants.Asset.CATEGORY_NOT_FOUND));
    }

    @Override
    @Transactional
    public ServiceResult<AssetResponseDTO> updateAsset(@NonNull UUID id, @NonNull AssetRequestDTO request) {
        return assetRepository.findById(id)
                .map(asset -> {
                    if (getCategoryForCurrentUser(asset.getCategory().getId()).isEmpty()) {
                        return ServiceResult.<AssetResponseDTO>notFound(MessageConstants.Asset.CATEGORY_NOT_FOUND);
                    }

                    assetMapper.updateEntity(request, asset);

                    Asset updatedAsset = assetRepository.save(asset);
                    return ServiceResult.success(assetMapper.toResponse(updatedAsset));
                })
                .orElseGet(() -> ServiceResult.notFound(MessageConstants.Asset.NOT_FOUND));
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteAsset(@NonNull UUID id) {
        return assetRepository.findById(id)
                .map(asset -> {
                    if (getCategoryForCurrentUser(asset.getCategory().getId()).isEmpty()) {
                        return ServiceResult.<Void>notFound(MessageConstants.Asset.CATEGORY_NOT_FOUND);
                    }

                    assetRepository.delete(asset);
                    return ServiceResult.<Void>success(null);
                })
                .orElseGet(() -> ServiceResult.notFound(MessageConstants.Asset.NOT_FOUND));
    }

    private Optional<AssetCategory> getCategoryForCurrentUser(UUID categoryId) {
        Portfolio portfolio = userContextService.getCurrentUserPortfolio();
        return categoryRepository.findByIdAndPortfolioId(categoryId, portfolio.getId());
    }
}
