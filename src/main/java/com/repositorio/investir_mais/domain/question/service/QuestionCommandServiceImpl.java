package com.repositorio.investir_mais.domain.question.service;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.domain.asset.model.Asset;
import com.repositorio.investir_mais.domain.asset.model.AssetCategory;
import com.repositorio.investir_mais.domain.asset.model.AssetEvaluation;
import com.repositorio.investir_mais.domain.asset.repository.AssetCategoryRepository;
import com.repositorio.investir_mais.domain.asset.repository.AssetEvaluationRepository;
import com.repositorio.investir_mais.domain.asset.repository.AssetRepository;
import com.repositorio.investir_mais.domain.question.DTO.EvaluationRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.question.mapper.QuestionMapper;
import com.repositorio.investir_mais.domain.question.model.Question;
import com.repositorio.investir_mais.domain.question.repository.QuestionRepository;
import com.repositorio.investir_mais.domain.question.service.interfaces.QuestionCommandService;
import com.repositorio.investir_mais.infrastructure.security.UserContextService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @Override
    @Transactional
    public ServiceResult<QuestionResponseDTO> createQuestion(
            @NonNull UUID categoryId,
            @NonNull QuestionRequestDTO request) {
        try {
            AssetCategory category = getCategoryForCurrentUser(categoryId);
            Question question = questionMapper.toEntity(request);
            question.setAssetCategory(category);
            Question savedQuestion = questionRepository.save(question);

            return ServiceResult.success(questionMapper.toResponse(savedQuestion));
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResult<QuestionResponseDTO> updateQuestion(
            @NonNull UUID id,
            @NonNull QuestionRequestDTO request) {
        try {
            Question question = questionRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Question.NOT_FOUND));
            getCategoryForCurrentUser(question.getAssetCategory().getId());
            question.setText(request.text());
            Question savedQuestion = questionRepository.save(question);

            return ServiceResult.success(questionMapper.toResponse(savedQuestion));
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteQuestion(
            @NonNull UUID id) {
        try {
            Question question = questionRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Question.NOT_FOUND));
            getCategoryForCurrentUser(question.getAssetCategory().getId());
            questionRepository.delete(question);
            return ServiceResult.success(null);
        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResult<Void> saveEvaluations(
            @NonNull UUID assetId,
            @NonNull List<EvaluationRequestDTO> evaluations) {
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