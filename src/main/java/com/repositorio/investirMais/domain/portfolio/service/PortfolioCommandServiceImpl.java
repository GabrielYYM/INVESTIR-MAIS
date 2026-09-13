package com.repositorio.investirMais.domain.portfolio.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repositorio.investirMais.common.constants.MessageConstants;
import com.repositorio.investirMais.common.result.ServiceResult;
import com.repositorio.investirMais.domain.asset.model.AssetCategory;
import com.repositorio.investirMais.domain.asset.repository.AssetCategoryRepository;
import com.repositorio.investirMais.domain.portfolio.model.Portfolio;
import com.repositorio.investirMais.domain.portfolio.repository.PortfolioRepository;
import com.repositorio.investirMais.domain.portfolio.service.interfaces.PortfolioCommandService;
import com.repositorio.investirMais.domain.user.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Implementação do serviço de comandos para Carteiras (Portfolios).
 * Responsável pelo ciclo de vida da carteira, incluindo a criação automática
 * de categorias padrão durante o registro do usuário.
 */
@Service
@RequiredArgsConstructor
public class PortfolioCommandServiceImpl implements PortfolioCommandService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final AssetCategoryRepository categoryRepository;

    /**
     * Cria uma carteira de investimentos para um novo usuário.
     * Caso o usuário já possua uma carteira, a operação é ignorada para garantir a
     * idempotência.
     * Após a criação da carteira, as categorias de ativos padrão (Ações, FIIs,
     * etc.) são inicializadas.
     * 
     * @param userId UUID do usuário que deve receber a carteira.
     * @return ServiceResult indicando sucesso ou erro se o usuário não for
     *         encontrado.
     */
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

    /**
     * Inicializa a carteira com Categorias de Ativos pré-configuradas.
     * Define nomes e porcentagens alvo iniciais para orientar o usuário.
     * 
     * @param portfolio Entidade da carteira recém-criada.
     */
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
