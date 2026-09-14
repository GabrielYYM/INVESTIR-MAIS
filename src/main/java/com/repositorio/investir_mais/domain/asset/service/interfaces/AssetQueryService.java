package com.repositorio.investir_mais.domain.asset.service.interfaces;

import java.util.List;
import java.util.UUID;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.DTO.AssetResponseDTO;

import lombok.NonNull;

public interface AssetQueryService {
    ServiceResult<List<AssetResponseDTO>> listAssetsByCategory(@NonNull UUID categoryId);
}
