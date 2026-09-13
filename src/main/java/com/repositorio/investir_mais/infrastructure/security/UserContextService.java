package com.repositorio.investir_mais.infrastructure.security;

import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.domain.portfolio.repository.PortfolioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserContextService {

    private final PortfolioRepository portfolioRepository;

    public static final UUID MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    /**
     * Retorna o usuário logado com base no token JWT / Contexto do Spring Security.
     */
    /*
     * public UserDetailsImpl getCurrentUserDetails() {
     * return (UserDetailsImpl) SecurityContextHolder.getContext()
     * .getAuthentication()
     * .getPrincipal();
     * }
     */
    /**
     * Retorna a carteira (Portfolio) do respectivo usuário logado.
     */
    public Portfolio getCurrentUserPortfolio() {
        return portfolioRepository.findByUserId(MOCK_USER_ID)
                .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Portfolio.NOT_FOUND));
    }

    /**
     * Retorna a carteira carregada juntamente com as categorias e ativos.
     */
    public Portfolio getCurrentUserPortfolioWithCategoriesAndAssets() {
        return portfolioRepository.findWithCategoriesAndAssetsByUserId(MOCK_USER_ID)
                .orElseThrow(() -> new EntityNotFoundException(MessageConstants.Portfolio.NOT_FOUND));
    }
}
