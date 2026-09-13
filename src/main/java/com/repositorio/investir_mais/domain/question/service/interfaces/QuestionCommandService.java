package com.repositorio.investir_mais.domain.question.service.interfaces;

import java.util.List;
import java.util.UUID;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.question.DTO.EvaluationRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionResponseDTO;

import lombok.NonNull;

public interface QuestionCommandService {
    ServiceResult<QuestionResponseDTO> createQuestion(@NonNull UUID categoryId, @NonNull QuestionRequestDTO request);

    ServiceResult<QuestionResponseDTO> updateQuestion(@NonNull UUID id, @NonNull QuestionRequestDTO request);

    ServiceResult<Void> deleteQuestion(@NonNull UUID id);

    ServiceResult<Void> saveEvaluations(@NonNull UUID assetId, @NonNull List<EvaluationRequestDTO> evaluations);
}
