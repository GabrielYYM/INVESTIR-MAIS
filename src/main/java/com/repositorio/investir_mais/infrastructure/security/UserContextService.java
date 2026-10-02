package com.repositorio.investir_mais.infrastructure.security;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.domain.portfolio.repository.PortfolioRepository;
import com.repositorio.investir_mais.domain.portfolio.service.interfaces.PortfolioCommandService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserContextService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioCommandService portfolioCommandService;

    /**
     * Retorna o UserDetailsImpl do usuário autenticado via JWT.
     */
    public UserDetailsImpl getCurrentUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    /**
     * Retorna a carteira (Portfolio) do respectivo usuário logado.
     */
    public Portfolio getCurrentUserPortfolio() {
        var userId = getCurrentUserDetails().getUser().getId();
        ensurePortfolioExists(userId);
        return portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Portfolio.NOT_FOUND));
    }

    /**
     * Retorna a carteira carregada juntamente com as categorias e ativos.
     */
    public Portfolio getCurrentUserPortfolioWithCategoriesAndAssets() {
        var userId = getCurrentUserDetails().getUser().getId();
        ensurePortfolioExists(userId);
        return portfolioRepository.findWithCategoriesAndAssetsByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Portfolio.NOT_FOUND));
    }

    /**
     * Garante que o usuário tenha uma carteira. Se ainda não existir
     * (ex.: usuários cadastrados antes desta correção), cria a carteira
     * com as categorias padrão no primeiro acesso.
     */
    private void ensurePortfolioExists(java.util.UUID userId) {
        if (portfolioRepository.findByUserId(userId).isEmpty()) {
            portfolioCommandService.createPortfolioForUser(userId);
        }
    }
}