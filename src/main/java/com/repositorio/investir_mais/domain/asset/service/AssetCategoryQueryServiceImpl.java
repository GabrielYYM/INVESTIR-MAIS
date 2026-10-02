package com.repositorio.investir_mais.domain.asset.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.DTO.CategoryResponseDTO;
import com.repositorio.investir_mais.domain.asset.mapper.AssetMapper;
import com.repositorio.investir_mais.domain.asset.repository.AssetCategoryRepository;
import com.repositorio.investir_mais.domain.asset.service.interfaces.AssetCategoryQueryService;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.infrastructure.security.UserContextService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetCategoryQueryServiceImpl implements AssetCategoryQueryService {

    private final AssetCategoryRepository categoryRepository;
    private final UserContextService userContextService;
    private final AssetMapper assetMapper;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<CategoryResponseDTO>> listUserCategories() {
        Portfolio portfolio = userContextService.getCurrentUserPortfolio();
        List<CategoryResponseDTO> categories = categoryRepository.findAllByPortfolioId(portfolio.getId()).stream()
                .map(assetMapper::toResponse)
                .toList();
        return ServiceResult.success(categories);
    }
}
