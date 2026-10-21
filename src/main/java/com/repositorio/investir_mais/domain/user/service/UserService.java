package com.repositorio.investir_mais.domain.user.service;

import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.AccountEmailRequested;
import com.repositorio.investir_mais.domain.AccountSessions;
import com.repositorio.investir_mais.domain.AccountTokens;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.modulith.NamedInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@NamedInterface("user-service")
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AccountTokens oneTimeTokenService;
    private final AccountSessions accountSessions;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Tentativa de busca por e-mail não cadastrado: {}", email);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
            });
    }

    @Transactional(readOnly = true)
    public UserDTO.Response getProfile(String principalName, UUID requestedId) {
        User user = findByEmail(principalName);
        if (!user.getId().equals(requestedId)) {
            log.warn("Acesso negado: Usuário {} tentou acessar o perfil {}", user.getId(), requestedId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode acessar este perfil.");
        }
        return userMapper.toDTO(user);
    }

    public UserDTO.Response updateProfile(String principalName, UUID requestedId, UserDTO.UpdateProfileRequest dto) {
        User user = findByEmail(principalName);
        if (!user.getId().equals(requestedId)) {
            log.warn("Acesso negado: Usuário {} tentou alterar o perfil {}", user.getId(), requestedId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode alterar este perfil.");
        }

        if (userRepository.existsByEmailAndIdNot(dto.email(), user.getId())) {
            log.warn("Falha na atualização de perfil: E-mail {} já está em uso", dto.email());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }

        user.setName(dto.name());
        if (dto.age() != null) {
            user.setAge(dto.age());
        }
        String previousEmail = user.getEmail();
        boolean emailChanged = !previousEmail.equals(dto.email());
        user.setEmail(dto.email());
        if (emailChanged) {
            user.setEmailVerified(false);
            accountSessions.revokeAll(previousEmail);
            oneTimeTokenService.invalidateAll(previousEmail);
            sendVerificationLink(user.getEmail());
        }
        log.info("Perfil do usuário {} atualizado com sucesso", user.getId());
        return userMapper.toDTO(user);
    }

    public UserDTO.Response registerUser(UserDTO.Request dto) {
        if (dto.role() != null && dto.role() != UserRole.STUDENT) {
            log.warn("Tentativa de cadastro público com perfil não permitido para o e-mail: {}", dto.email());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O cadastro público permite apenas o perfil STUDENT.");
        }

        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Tentativa de cadastro com e-mail duplicado: {}", dto.email());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }

        User user = userMapper.toEntity(dto);
        user.setRole(UserRole.STUDENT);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        log.info("Novo usuário registrado com sucesso. ID: {}, Role: {}", savedUser.getId(), savedUser.getRole());

        sendVerificationLink(savedUser.getEmail());

        return userMapper.toDTO(savedUser);
    }

    public void updatePassword(String email, AuthDTO.UpdatePasswordRequest dto) {
        User user = findByEmail(email);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            log.warn("Falha ao atualizar senha para o usuário {}: Senha atual incorreta", user.getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual inválida.");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        accountSessions.revokeAll(user.getEmail());
        oneTimeTokenService.invalidateAll(user.getEmail());
        log.info("Senha atualizada com sucesso para o usuário: {}", user.getId());
    }

    public void forgotPassword(AuthDTO.ForgotPasswordRequest dto) {
        if (userRepository.existsByEmail(dto.email())) {
            String token = oneTimeTokenService.generatePasswordResetToken(dto.email());
            events.publishEvent(new AccountEmailRequested(dto.email(), token, AccountEmailRequested.Purpose.PASSWORD_RESET));
            log.info("Solicitação de recuperação de senha processada para o e-mail: {}", dto.email());
        }
    }

    public void resetPassword(AuthDTO.ResetPasswordRequest dto) {
        String email = oneTimeTokenService.consumePasswordResetToken(dto.token())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado."));

        User user = findByEmail(email);
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setEmailVerified(true);
        accountSessions.revokeAll(email);
        oneTimeTokenService.invalidateAll(email);
        log.info("Senha redefinida e e-mail ativado com sucesso para o usuário: {}", user.getId());
    }

    public void verifyEmail(String tokenValue) {
        String email = oneTimeTokenService.consumeEmailVerificationToken(tokenValue)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link inválido ou expirado."));
        User user = findByEmail(email);
        user.setEmailVerified(true);
    }

    private void sendVerificationLink(String email) {
        OneTimeToken token = oneTimeTokenService.generateEmailVerificationToken(email);
        events.publishEvent(new AccountEmailRequested(email, token.getTokenValue(), AccountEmailRequested.Purpose.VERIFY_EMAIL));
    }

    public void resendVerification(String email) {
        userRepository.findByEmail(email).filter(user -> !user.isEmailVerified())
                .ifPresent(user -> sendVerificationLink(user.getEmail()));
    }
}
