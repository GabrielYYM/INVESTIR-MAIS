package com.repositorio.investir_mais.domain.user.service.interfaces;

import java.util.UUID;

import com.repositorio.investir_mais.common.result.ServiceResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;

/**
 * Interface de consulta para gestão de usuários.
 * Define as operações de leitura e busca de informações de usuários e detalhes de segurança.
 */
public interface UserQueryService {
    /**
     * Busca um usuário pelo seu identificador único.
     *
     * @param id UUID do usuário.
     * @return Resultado contendo o DTO de resposta do usuário.
     */
    ServiceResult<UserResponseDTO> findUserById(UUID id);

    /**
     * Lista todos os usuários cadastrados de forma paginada.
     *
     * @param pageable Configuração de paginação.
     * @return Página contendo os DTOs dos usuários.
     */
    ServiceResult<Page<UserResponseDTO>> listAllUsers(Pageable pageable);

}
