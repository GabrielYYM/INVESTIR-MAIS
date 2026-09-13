package com.repositorio.investir_mais.domain.asset.service;

import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.DTO.CategoryRequestDTO;
import com.repositorio.investir_mais.domain.asset.DTO.CategoryResponseDTO;
import com.repositorio.investir_mais.domain.asset.mapper.AssetMapper;
import com.repositorio.investir_mais.domain.asset.model.AssetCategory;
import com.repositorio.investir_mais.domain.asset.repository.AssetCategoryRepository;
import com.repositorio.investir_mais.domain.asset.service.interfaces.AssetCategoryCommandService;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.infrastructure.security.UserContextService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetCategoryCommandServiceImpl implements AssetCategoryCommandService {

    private final AssetCategoryRepository categoryRepository;
    private final UserContextService userContextService;
    private final AssetMapper assetMapper;

    @Override
    @Transactional
    public ServiceResult<CategoryResponseDTO> createCategory(@NonNull CategoryRequestDTO request) {
        try {
            Portfolio portfolio = userContextService.getCurrentUserPortfolio();
            portfolio.validateAndAddCategoryTarget(request.targetPercentage(), null);

            AssetCategory category = assetMapper.toEntity(request);
            category.setPortfolio(portfolio);

            AssetCategory savedCategory = categoryRepository.save(category);
            return ServiceResult.success(assetMapper.toResponse(savedCategory));
        } catch (IllegalArgumentException e) {
            return ServiceResult.error(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResult<CategoryResponseDTO> updateCategory(@NonNull UUID id, @NonNull CategoryRequestDTO request) {
        try {
            Portfolio portfolio = userContextService.getCurrentUserPortfolio();
            AssetCategory category = categoryRepository.findByIdAndPortfolioId(id, portfolio.getId())
                    .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Asset.CATEGORY_NOT_FOUND));

            portfolio.validateAndAddCategoryTarget(request.targetPercentage(), id);
            category.setName(request.name());
            category.setTargetPercentage(request.targetPercentage());

            AssetCategory updatedCategory = categoryRepository.save(category);
            return ServiceResult.success(assetMapper.toResponse(updatedCategory));
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ServiceResult.error(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteCategory(@NonNull UUID id) {
        Portfolio portfolio = userContextService.getCurrentUserPortfolio();
        return categoryRepository.findByIdAndPortfolioId(id, portfolio.getId())
                .map(category -> {
                    categoryRepository.delete(category);
                    return ServiceResult.<Void>success(null);
                })
                .orElseGet(() -> ServiceResult.notFound(MessageConstants.Asset.CATEGORY_NOT_FOUND));
    }
}
