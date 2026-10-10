package com.repositorio.investir_mais.domain.user.service;

import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.infrastructure.EmailOttHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final OneTimeTokenService oneTimeTokenService;
    private final EmailOttHandler emailOttHandler;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Tentativa de busca por e-mail não cadastrado: {}", email);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
            });
    }

    public UserDTO.Response getProfile(String principalName, UUID requestedId) {
        User user = findAuthenticatedUser(principalName);
        if (!user.getId().equals(requestedId)) {
            log.warn("Acesso negado: Usuário {} tentou acessar o perfil {}", user.getId(), requestedId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode acessar este perfil.");
        }
        return userMapper.toDTO(user);
    }

    public UserDTO.Response updateProfile(String principalName, UUID requestedId, UserDTO.UpdateProfileRequest dto) {
        User user = findAuthenticatedUser(principalName);
        if (!user.getId().equals(requestedId)) {
            log.warn("Acesso negado: Usuário {} tentou alterar o perfil {}", user.getId(), requestedId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode alterar este perfil.");
        }

        userRepository.findByEmail(dto.email())
            .filter(existing -> !existing.getId().equals(user.getId()))
            .ifPresent(existing -> {
                log.warn("Falha na atualização de perfil: E-mail {} já está em uso", dto.email());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
            });

        user.setName(dto.name());
        if (dto.age() != null) {
            user.setAge(dto.age());
        }
        user.setEmail(dto.email());
        
        User updated = userRepository.save(user);
        log.info("Perfil do usuário {} atualizado com sucesso", updated.getId());
        return userMapper.toDTO(updated);
    }

    public User findAuthenticatedUser(String principalName) {
        try {
            return userRepository.findById(UUID.fromString(principalName))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        } catch (IllegalArgumentException ignored) {
            return findByEmail(principalName);
        }
    }

    public UserDTO.Response registerUser(UserDTO.Request dto) {
        if (dto.role() == UserRole.ADMIN) {
            log.warn("Tentativa de cadastro direto com perfil ADMIN negada para o e-mail: {}", dto.email());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é permitido cadastrar perfil ADMIN diretamente.");
        }

        if (userRepository.findByEmail(dto.email()).isPresent()) {
            log.warn("Tentativa de cadastro com e-mail duplicado: {}", dto.email());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }

        User user = userMapper.toEntity(dto);
        if (user.getRole() == null) {
            user.setRole(UserRole.STUDENT);
        }
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        log.info("Novo usuário registrado com sucesso. ID: {}, Role: {}", savedUser.getId(), savedUser.getRole());

        OneTimeToken ott = oneTimeTokenService.generate(new GenerateOneTimeTokenRequest(savedUser.getEmail()));
        emailOttHandler.sendOttEmail(savedUser.getEmail(), ott.getTokenValue());

        return userMapper.toDTO(savedUser);
    }

    public void updatePassword(String email, AuthDTO.UpdatePasswordRequest dto) {
        User user = findAuthenticatedUser(email);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            log.warn("Falha ao atualizar senha para o usuário {}: Senha atual incorreta", user.getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual inválida.");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
        log.info("Senha atualizada com sucesso para o usuário: {}", user.getId());
    }

    public void forgotPassword(AuthDTO.ForgotPasswordRequest dto) {
        userRepository.findByEmail(dto.email()).ifPresentOrElse(
            user -> {
                OneTimeToken ott = oneTimeTokenService.generate(new GenerateOneTimeTokenRequest(dto.email()));
                emailOttHandler.sendOttEmail(dto.email(), ott.getTokenValue());
                log.info("Solicitação de recuperação de senha processada para o e-mail: {}", dto.email());
            },
            () -> log.info("Solicitação de recuperação de senha ignorada (e-mail não existe): {}", dto.email())
        );
    }

    public void resetPassword(AuthDTO.ResetPasswordRequest dto) {
        OneTimeToken consumedOtt = oneTimeTokenService.consume(new OneTimeTokenAuthenticationToken(dto.token()));

        if (consumedOtt == null) {
            log.warn("Tentativa de redefinição de senha com token inválido ou expirado");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado.");
        }

        User user = findByEmail(consumedOtt.getUsername());
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Senha redefinida e e-mail ativado com sucesso para o usuário: {}", user.getId());
    }
}