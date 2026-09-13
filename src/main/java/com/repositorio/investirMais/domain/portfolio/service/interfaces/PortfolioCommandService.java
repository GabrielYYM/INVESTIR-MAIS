package com.repositorio.investirMais.domain.portfolio.service.interfaces;

import java.util.UUID;

import com.repositorio.investirMais.common.result.ServiceResult;

import lombok.NonNull;

/**
 * Interface de comando para operações na carteira de investimentos.
 * Gerencia a criação e manutenção da estrutura da carteira do usuário.
 */
public interface PortfolioCommandService {
    /**
     * Inicializa uma nova carteira de investimentos para um usuário
     * recém-cadastrado.
     * 
     * @param userId UUID do usuário proprietário da nova carteira.
     * @return Resultado indicando o sucesso da criação.
     */
    ServiceResult<Void> createPortfolioForUser(@NonNull UUID userId);
}
