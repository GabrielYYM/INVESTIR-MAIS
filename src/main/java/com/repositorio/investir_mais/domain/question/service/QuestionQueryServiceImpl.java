package com.repositorio.investir_mais.domain.question.service;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.repository.AssetCategoryRepository;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.domain.question.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.question.mapper.QuestionMapper;
import com.repositorio.investir_mais.domain.question.repository.QuestionRepository;
import com.repositorio.investir_mais.domain.question.service.interfaces.QuestionQueryService;
import com.repositorio.investir_mais.infrastructure.security.UserContextService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionQueryServiceImpl implements QuestionQueryService {

    private final UserContextService userContextService;
    private final QuestionRepository questionRepository;
    private final AssetCategoryRepository categoryRepository;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<QuestionResponseDTO>> listByCategoryId(
            @NonNull UUID categoryId) {
        try {
            Portfolio portfolio = userContextService.getCurrentUserPortfolio();

            return categoryRepository.findByIdAndPortfolioId(categoryId, portfolio.getId())
                    .map(category -> {
                        List<QuestionResponseDTO> questions = questionRepository.findAllByAssetCategoryId(categoryId)
                                .stream()
                                .map(questionMapper::toResponse)
                                .toList();
                        return ServiceResult.success(questions);
                    })
                    .orElse(ServiceResult.notFound(MessageConstants.Asset.CATEGORY_NOT_FOUND));

        } catch (EntityNotFoundException e) {
            return ServiceResult.notFound(e.getMessage());
        } catch (Exception e) {
            return ServiceResult.error(e.getMessage());
        }
    }
}
