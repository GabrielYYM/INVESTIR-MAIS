package com.repositorio.investir_mais.domain.asset.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.DTO.AssetResponseDTO;
import com.repositorio.investir_mais.domain.asset.mapper.AssetMapper;
import com.repositorio.investir_mais.domain.asset.repository.AssetCategoryRepository;
import com.repositorio.investir_mais.domain.asset.repository.AssetRepository;
import com.repositorio.investir_mais.domain.asset.service.interfaces.AssetQueryService;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.infrastructure.security.UserContextService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetQueryServiceImpl implements AssetQueryService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;
    private final UserContextService userContextService;
    private final AssetMapper assetMapper;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<AssetResponseDTO>> listAssetsByCategory(@NonNull UUID categoryId) {
        Portfolio portfolio = userContextService.getCurrentUserPortfolio();

        return categoryRepository.findByIdAndPortfolioId(categoryId, portfolio.getId())
                .map(category -> {
                    List<AssetResponseDTO> assets = assetRepository.findAllByCategoryId(categoryId).stream()
                            .map(assetMapper::toResponse)
                            .toList();
                    return ServiceResult.success(assets);
                })
                .orElse(ServiceResult.notFound(MessageConstants.Asset.CATEGORY_NOT_FOUND));
    }
}
