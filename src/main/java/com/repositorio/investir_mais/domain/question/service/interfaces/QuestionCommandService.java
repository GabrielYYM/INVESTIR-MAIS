package com.repositorio.investir_mais.domain.question.service.interfaces;

import java.util.List;
import java.util.UUID;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.question.DTO.EvaluationRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionResponseDTO;

import lombok.NonNull;

/**
 * Interface de comando para gestão de perguntas de avaliação e respostas de
 * ativos.
 * Define as operações para configurar critérios de pontuação e avaliar ativos
 * individuais.
 */
public interface QuestionCommandService {
    /**
     * Cria uma nova pergunta de avaliação para uma categoria específica.
     * 
     * @param categoryId UUID da categoria onde a pergunta será aplicada.
     * @param request    DTO com o texto da pergunta e peso.
     * @return Resultado contendo o DTO da pergunta criada.
     */
    ServiceResult<QuestionResponseDTO> createQuestion(@NonNull UUID categoryId, @NonNull QuestionRequestDTO request);

    /**
     * Atualiza os dados de uma pergunta de avaliação existente.
     * 
     * @param id      UUID da pergunta a ser atualizada.
     * @param request DTO com os novos dados.
     * @return Resultado contendo o DTO da pergunta atualizada.
     */
    ServiceResult<QuestionResponseDTO> updateQuestion(@NonNull UUID id, @NonNull QuestionRequestDTO request);

    /**
     * Remove uma pergunta de avaliação do sistema.
     * 
     * @param id UUID da pergunta a ser excluída.
     * @return Resultado indicando sucesso ou erro.
     */
    ServiceResult<Void> deleteQuestion(@NonNull UUID id);

    /**
     * Salva ou atualiza as avaliações (respostas) de um ativo para um conjunto de
     * perguntas.
     * Esta operação é fundamental para o cálculo do score final do ativo.
     * 
     * @param assetId     UUID do ativo sendo avaliado.
     * @param evaluations Lista de avaliações contendo o ID da pergunta e a resposta
     *                    (Sim/Não).
     * @return Resultado indicando sucesso do processamento.
     */
    ServiceResult<Void> saveEvaluations(@NonNull UUID assetId, @NonNull List<EvaluationRequestDTO> evaluations);
}
