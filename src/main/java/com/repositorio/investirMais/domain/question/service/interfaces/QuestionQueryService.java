package com.repositorio.investirMais.domain.question.service.interfaces;

import java.util.List;
import java.util.UUID;

import com.repositorio.investirMais.common.result.ServiceResult;
import com.repositorio.investirMais.domain.question.DTO.QuestionResponseDTO;

import lombok.NonNull;

/**
 * Interface de consulta para perguntas de avaliação qualitativa.
 */
public interface QuestionQueryService {
    /**
     * Lista todas as perguntas de avaliação configuradas para uma categoria.
     * 
     * @param categoryId UUID da categoria para consulta.
     * @return Resultado contendo a lista de perguntas (DTOs).
     */
    ServiceResult<List<QuestionResponseDTO>> listByCategoryId(@NonNull UUID categoryId);
}
