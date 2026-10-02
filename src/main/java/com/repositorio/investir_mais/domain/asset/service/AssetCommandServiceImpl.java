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
public class AssetCommandServiceImpl implements AssetCommandService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;
    private final UserContextService userContextService;
    private final AssetMapper assetMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ServiceResult<AssetResponseDTO> createAsset(@NonNull UUID categoryId, @NonNull AssetRequestDTO request) {
        return getCategoryForCurrentUser(categoryId)
                .map(category -> {
                    Asset asset = assetMapper.toEntity(request);
                    asset.setCategory(category);

                    Asset savedAsset = assetRepository.save(asset);

                    log.info(LogMessageConstants.AUDIT.ASSET_CREATED, savedAsset.getId(), savedAsset.getTicker(), category.getName());
                    auditLogService.log(AuditAction.ASSET_CREATED, "ASSET", savedAsset.getId().toString(),
                            AuditStatus.SUCCESS, "Ativo criado: " + savedAsset.getTicker() + " na categoria " + category.getName());

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

                    log.info(LogMessageConstants.AUDIT.ASSET_UPDATED, updatedAsset.getId(), updatedAsset.getTicker());
                    auditLogService.log(AuditAction.ASSET_UPDATED, "ASSET", updatedAsset.getId().toString(),
                            AuditStatus.SUCCESS, "Ativo atualizado: " + updatedAsset.getTicker());

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

                    log.info(LogMessageConstants.AUDIT.ASSET_DELETED, id);
                    auditLogService.log(AuditAction.ASSET_DELETED, "ASSET", id.toString(),
                            AuditStatus.SUCCESS, "Ativo removido: " + asset.getTicker());

                    return ServiceResult.<Void>success(null);
                })
                .orElseGet(() -> ServiceResult.notFound(MessageConstants.Asset.NOT_FOUND));
    }

    private Optional<AssetCategory> getCategoryForCurrentUser(UUID categoryId) {
        Portfolio portfolio = userContextService.getCurrentUserPortfolio();
        return categoryRepository.findByIdAndPortfolioId(categoryId, portfolio.getId());
    }
}
