package com.repositorio.investir_mais.domain.user.service.interfaces;

import java.util.UUID;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserUpdateRequestDTO;

/**
 * Interface de comando para gestão de usuários.
 * Define as operações de ciclo de vida do usuário, incluindo criação, atualização e exclusão.
 */
public interface UserCommandService {
    /**
     * Registra um novo usuário no sistema.
     * Realiza a criptografia da senha e inicializa as estruturas de segurança.
     *
     * @param userRequestDTO DTO com os dados do novo usuário.
     * @return Resultado contendo o DTO do usuário criado.
     */
    ServiceResult<UserResponseDTO> createUser(UserRequestDTO userRequestDTO);

    /**
     * Remove um usuário do sistema permanentemente.
     *
     * @param id UUID do usuário a ser excluído.
     * @return Resultado indicando sucesso ou erro.
     */
    ServiceResult<Void> deleteUserById(UUID id);

    /**
     * Atualiza as informações de perfil de um usuário existente.
     *
     * @param id UUID do usuário para atualização.
     * @param userUpdateRequestDTO DTO com os campos a serem alterados.
     * @return Resultado contendo o DTO do usuário atualizado.
     */
    ServiceResult<UserResponseDTO> updateUserById(UUID id, UserUpdateRequestDTO userUpdateRequestDTO);
}
