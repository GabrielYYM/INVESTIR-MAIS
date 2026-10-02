package com.repositorio.investir_mais.domain.asset.service.interfaces;

import java.util.List;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.DTO.CategoryResponseDTO;

public interface AssetCategoryQueryService {
    ServiceResult<List<CategoryResponseDTO>> listUserCategories();
}
