package com.repositorio.investir_mais.domain.asset.service.interfaces;

import java.util.UUID;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.DTO.AssetRequestDTO;
import com.repositorio.investir_mais.domain.asset.DTO.AssetResponseDTO;

import lombok.NonNull;

public interface AssetCommandService {
    ServiceResult<AssetResponseDTO> createAsset(@NonNull UUID categoryId, @NonNull AssetRequestDTO request);

    ServiceResult<AssetResponseDTO> updateAsset(@NonNull UUID id, @NonNull AssetRequestDTO request);

    ServiceResult<Void> deleteAsset(@NonNull UUID id);
}
