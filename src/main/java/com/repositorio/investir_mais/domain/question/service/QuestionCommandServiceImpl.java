package com.repositorio.investir_mais.domain.question.service;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.domain.question.DTO.EvaluationRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.question.mapper.QuestionMapper;
import com.repositorio.investir_mais.domain.question.model.Question;
import com.repositorio.investir_mais.domain.question.repository.QuestionRepository;
import com.repositorio.investir_mais.domain.question.service.interfaces.QuestionCommandService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação do serviço de comandos para Perguntas e Avaliações.
 * Gerenta o questionário qualitativo (Buy & Hold) associado a cada categoria de
 * ativo,
 * permitindo que o usuário personalize os critérios de avaliação de seus
 * ativos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionCommandServiceImpl implements QuestionCommandService {

    private final QuestionRepository questionRepository;
    private final AssetCategoryRepository categoryRepository;
    private final AssetRepository assetRepository;
    private final AssetEvaluationRepository evaluationRepository;
    private final UserContextService userContextService;
    private final QuestionMapper questionMapper;

    /**
     * Cria uma nova pergunta qualitativa para uma categoria de ativos.
     * 
     * @param categoryId UUID da categoria que receberá a pergunta.
     * @param request    DTO com o texto da pergunta.
     * @return ServiceResult com a pergunta criada ou erro se a categoria não
     *         pertencer ao usuário.
     */
    @Override
    @Transactional
    public ServiceResult<QuestionResponseDTO> createQuestion(@NonNull UUID categoryId,
            @NonNull QuestionRequestDTO request) {
        try {
            AssetCategory category = getCategoryForCurrentUser(categoryId);

            Question question = questionMapper.toEntity(request);
            question.setAssetCategory(category);
            Question savedQuestion = questionRepository.save(question);
            log.info("AUDITORIA: Pergunta qualitativa criada. ID: {} | Categoria: {}", savedQuestion.getId(),
                    category.getName());

            return ServiceResult.success(questionMapper.toResponse(savedQuestion));
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        }
    }

    /**
     * Atualiza o texto de uma pergunta existente.
     * 
     * @param id      UUID da pergunta a ser editada.
     * @param request Novo texto da pergunta.
     * @return ServiceResult com a pergunta atualizada.
     */
    @Override
    @Transactional
    public ServiceResult<QuestionResponseDTO> updateQuestion(@NonNull UUID id, @NonNull QuestionRequestDTO request) {
        try {
            Question question = questionRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Question.NOT_FOUND));

            getCategoryForCurrentUser(question.getAssetCategory().getId());

            question.setText(request.text());
            Question savedQuestion = questionRepository.save(question);
            log.info("AUDITORIA: Pergunta qualitativa atualizada. ID: {}", savedQuestion.getId());
            return ServiceResult.success(questionMapper.toResponse(savedQuestion));
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        }
    }

    /**
     * Remove uma pergunta qualitativa da categoria.
     * 
     * @param id UUID da pergunta a ser removida.
     * @return ServiceResult de sucesso ou erro se não encontrada.
     */
    @Override
    @Transactional
    public ServiceResult<Void> deleteQuestion(@NonNull UUID id) {
        try {
            Question question = questionRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Question.NOT_FOUND));

            getCategoryForCurrentUser(question.getAssetCategory().getId());
            questionRepository.delete(question);
            log.info("AUDITORIA: Pergunta qualitativa removida. ID: {}", id);
            return ServiceResult.success(null);
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        }
    }

    /**
     * Salva as respostas (avaliações) de um ativo para o questionário da sua
     * categoria.
     * Este método limpa as avaliações anteriores do ativo e registra as novas.
     * 
     * @param assetId     UUID do ativo que está sendo avaliado.
     * @param evaluations Lista de respostas (ID da pergunta e se é
     *                    positivo/negativo).
     * @return ServiceResult indicando sucesso.
     */
    @Override
    @Transactional
    public ServiceResult<Void> saveEvaluations(@NonNull UUID assetId, @NonNull List<EvaluationRequestDTO> evaluations) {
        try {
            Asset asset = assetRepository.findById(assetId)
                    .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Asset.NOT_FOUND));

            getCategoryForCurrentUser(asset.getCategory().getId());

            List<AssetEvaluation> currentEvaluations = evaluationRepository.findAllByAssetId(assetId);
            evaluationRepository.deleteAll(currentEvaluations);

            List<AssetEvaluation> newEvaluations = evaluations.stream()
                    .map(req -> {
                        Question q = questionRepository.findById(req.questionId())
                                .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Question.NOT_FOUND));

                        return AssetEvaluation.builder()
                                .asset(asset)
                                .question(q)
                                .isPositive(req.isPositive())
                                .build();
                    })
                    .toList();

            evaluationRepository.saveAll(newEvaluations);
            log.info("AUDITORIA: Avaliações salvas com sucesso para o ativo ID: {}", assetId);
            return ServiceResult.success(null);
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        }
    }

    private AssetCategory getCategoryForCurrentUser(UUID categoryId) {
        Portfolio portfolio = getCurrentUserPortfolio();
        return categoryRepository.findByIdAndPortfolioId(categoryId, portfolio.getId())
                .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Asset.CATEGORY_NOT_FOUND));
    }

    private Portfolio getCurrentUserPortfolio() {
        return userContextService.getCurrentUserPortfolio();
    }
}
