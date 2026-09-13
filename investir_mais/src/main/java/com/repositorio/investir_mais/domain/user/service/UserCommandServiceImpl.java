package com.repositorio.investir_mais.domain.user.service;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserUpdateRequestDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.interfaces.UserCommandService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


/**
 * Implementação do serviço de comando para gestão de usuários.
 * Centraliza as operações de escrita, aplicando regras de segurança,
 * orquestração com outros domínios (ex: inicialização de carteira).
 */
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Cria e registra um novo usuário no banco de dados.
     *
     * @param userRequestDTO DTO contendo os dados do prospecto (nome, e-mail, senha).
     * @return DTO com os dados do usuário persistido, ocultando informações de segurança.
     */
    @Override
    @Transactional
    public ServiceResult<UserResponseDTO> createUser(@NonNull UserRequestDTO userRequestDTO) {
        try {
            User user = userMapper.toUser(userRequestDTO);
            userRepository.save(user);

            return ServiceResult.success(userMapper.toUserResponseDTO(user));
        } catch (DataIntegrityViolationException e) {
            return ServiceResult.error(MessageConstants.User.EMAIL_ALREADY_IN_USE);
        } catch (IllegalArgumentException e) {
            return ServiceResult.error(e.getMessage());
        } catch (Exception e) {
            return ServiceResult.error("Ocorreu um erro ao processar seu cadastro. Tente novamente mais tarde.");
        }
    }

    /**
     * Remove permanentemente um usuário da base de dados.
     *
     * @param id UUID do usuário que deve ser excluído.
     */
    @Override
    @Transactional
    public ServiceResult<Void> deleteUserById(@NonNull UUID id) {
        if (!userRepository.existsById(id)) {
            return ServiceResult.notFound(MessageConstants.User.NOT_FOUND_WITH_ID + id);
        }
        userRepository.deleteById(id);
        return ServiceResult.success(null);
    }

    /**
     * Atualiza o perfil (nome e e-mail) de um usuário existente.
     * Medida de segurança: Exige a senha atual para autorizar alterações críticas no perfil.
     *
     * @param id UUID do usuário a ser atualizado.
     * @param userUpdateRequestDTO Novos dados do perfil e senha de confirmação.
     * @return DTO com o perfil atualizado do usuário.
     */
    @Override
    @Transactional
    public ServiceResult<UserResponseDTO> updateUserById(@NonNull UUID id, @NonNull UserUpdateRequestDTO userUpdateRequestDTO) {
        return userRepository.findById(id)
                .map(user -> {

                    try {
                        user.updateProfile(userUpdateRequestDTO.name(), userUpdateRequestDTO.email());
                        User updatedUser = userRepository.save(user);
                        return ServiceResult.success(userMapper.toUserResponseDTO(updatedUser));
                    } catch (Exception e) {
                        return ServiceResult.<UserResponseDTO>error(e.getMessage());
                    }
                })
                .orElseGet(() -> ServiceResult.notFound(MessageConstants.User.NOT_FOUND_FOR_UPDATE));
    }
}