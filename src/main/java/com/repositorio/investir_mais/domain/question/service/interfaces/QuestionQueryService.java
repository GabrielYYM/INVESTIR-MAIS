package com.repositorio.investir_mais.domain.question.service.interfaces;

import java.util.List;
import java.util.UUID;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.question.DTO.QuestionResponseDTO;

import lombok.NonNull;

public interface QuestionQueryService {
    ServiceResult<List<QuestionResponseDTO>> listByCategoryId(@NonNull UUID categoryId);
}
