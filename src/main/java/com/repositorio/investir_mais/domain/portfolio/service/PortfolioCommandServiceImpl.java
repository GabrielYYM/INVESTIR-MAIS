package com.repositorio.investir_mais.domain.portfolio.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.model.AssetCategory;
import com.repositorio.investir_mais.domain.asset.repository.AssetCategoryRepository;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.domain.portfolio.repository.PortfolioRepository;
import com.repositorio.investir_mais.domain.portfolio.service.interfaces.PortfolioCommandService;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioCommandServiceImpl implements PortfolioCommandService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final AssetCategoryRepository categoryRepository;

    @Override
    @Transactional
    public ServiceResult<Void> createPortfolioForUser(@NonNull UUID userId) {
        if (portfolioRepository.findByUserId(userId).isEmpty()) {
            return userRepository.findById(userId)
                    .map(user -> {
                        Portfolio portfolio = Portfolio.builder()
                                .user(user)
                                .build();
                        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
                        createDefaultCategories(savedPortfolio);
                        return ServiceResult.<Void>success(null);
                    })
                    .orElseGet(() -> ServiceResult.notFound(MessageConstants.User.NOT_FOUND_WITH_ID + userId));
        }
        return ServiceResult.success(null);
    }

    private void createDefaultCategories(Portfolio portfolio) {
        List<AssetCategory> defaults = List.of(
                AssetCategory.builder()
                        .name("AÇÃO NACIONAL")
                        .targetPercentage(new BigDecimal("25"))
                        .portfolio(portfolio)
                        .build(),
                AssetCategory.builder()
                        .name("FUNDOS IMOBILIÁRIOS NACIONAL")
                        .targetPercentage(new BigDecimal("15"))
                        .portfolio(portfolio)
                        .build(),
                AssetCategory.builder()
                        .name("AÇÃO INTERNACIONAL")
                        .targetPercentage(new BigDecimal("20"))
                        .portfolio(portfolio)
                        .build(),
                AssetCategory.builder()
                        .name("RENDA FIXA NACIONAL")
                        .targetPercentage(new BigDecimal("25"))
                        .portfolio(portfolio)
                        .build(),
                AssetCategory.builder()
                        .name("CRIPTOMOEDA")
                        .targetPercentage(new BigDecimal("10"))
                        .portfolio(portfolio)
                        .build(),
                AssetCategory.builder()
                        .name("RENDA FIXA INTERNACIONAL")
                        .targetPercentage(new BigDecimal("5"))
                        .portfolio(portfolio)
                        .build());
        categoryRepository.saveAll(defaults);
    }
}
