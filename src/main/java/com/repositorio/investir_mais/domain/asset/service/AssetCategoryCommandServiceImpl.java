package com.repositorio.investir_mais.domain.asset.service;

import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.LogMessageConstants;
import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.DTO.CategoryRequestDTO;
import com.repositorio.investir_mais.domain.asset.DTO.CategoryResponseDTO;
import com.repositorio.investir_mais.domain.asset.mapper.AssetMapper;
import com.repositorio.investir_mais.domain.asset.model.AssetCategory;
import com.repositorio.investir_mais.domain.asset.repository.AssetCategoryRepository;
import com.repositorio.investir_mais.domain.asset.service.interfaces.AssetCategoryCommandService;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditAction;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditStatus;
import com.repositorio.investir_mais.domain.audit.service.interfaces.AuditLogService;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.infrastructure.security.UserContextService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetCategoryCommandServiceImpl implements AssetCategoryCommandService {

    private final AssetCategoryRepository categoryRepository;
    private final UserContextService userContextService;
    private final AssetMapper assetMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ServiceResult<CategoryResponseDTO> createCategory(@NonNull CategoryRequestDTO request) {
        try {
            Portfolio portfolio = userContextService.getCurrentUserPortfolio();
            portfolio.validateAndAddCategoryTarget(request.targetPercentage(), null);

            AssetCategory category = assetMapper.toEntity(request);
            category.setPortfolio(portfolio);

            AssetCategory savedCategory = categoryRepository.save(category);

            log.info(LogMessageConstants.AUDIT.CATEGORY_CREATED, savedCategory.getId(), savedCategory.getName());
            auditLogService.log(AuditAction.CATEGORY_CREATED, "CATEGORY", savedCategory.getId().toString(),
                    AuditStatus.SUCCESS, "Categoria criada: " + savedCategory.getName());

            return ServiceResult.success(assetMapper.toResponse(savedCategory));
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao criar categoria: {}", e.getMessage());
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

            log.info(LogMessageConstants.AUDIT.CATEGORY_UPDATED, updatedCategory.getId(), updatedCategory.getName());
            auditLogService.log(AuditAction.CATEGORY_UPDATED, "CATEGORY", updatedCategory.getId().toString(),
                    AuditStatus.SUCCESS, "Categoria atualizada: " + updatedCategory.getName());

            return ServiceResult.success(assetMapper.toResponse(updatedCategory));
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao atualizar categoria {}: {}", id, e.getMessage());
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

                    log.info(LogMessageConstants.AUDIT.CATEGORY_DELETED, id);
                    auditLogService.log(AuditAction.CATEGORY_DELETED, "CATEGORY", id.toString(),
                            AuditStatus.SUCCESS, "Categoria removida: " + category.getName());

                    return ServiceResult.<Void>success(null);
                })
                .orElseGet(() -> ServiceResult.notFound(MessageConstants.Asset.CATEGORY_NOT_FOUND));
    }
}
