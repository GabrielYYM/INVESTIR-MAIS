package com.repositorio.investir_mais.domain.user.service;

import com.repositorio.investir_mais.domain.user.DTO.AuthDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.AccountEmailRequested;
import com.repositorio.investir_mais.domain.AccountSecurityService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.modulith.NamedInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
    private final AccountSecurityService accountSecurity;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
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
        User user = findByEmailForUpdate(principalName);
        if (!user.getId().equals(requestedId)) {
            log.warn("Acesso negado: Usuário {} tentou alterar o perfil {}", user.getId(), requestedId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode alterar este perfil.");
        }

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual inválida.");
        }

        if (userRepository.existsByEmailAndIdNot(dto.email(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não foi possível atualizar o perfil. Verifique os dados informados.");
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
            accountSecurity.revokeAllSessions(previousEmail);
            accountSecurity.invalidateAll(previousEmail);
            sendVerificationLink(user.getEmail());
        }
        log.info("Perfil do usuário {} atualizado com sucesso", user.getId());
        return userMapper.toDTO(user);
    }

    public UserDTO.Response registerUser(UserDTO.Request dto) {
        if (dto.role() != null && dto.role() != UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O cadastro público permite apenas o perfil STUDENT.");
        }

        if (userRepository.existsByEmailIncludingDeleted(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não foi possível criar sua conta. Verifique os dados informados.");
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

    public void deleteAccount(String principalName, UUID requestedId, UserDTO.DeleteAccountRequest dto) {
        User user = findByEmailForUpdate(principalName);
        if (!user.getId().equals(requestedId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode excluir outra conta.");
        }
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual inválida.");
        }

        userRepository.delete(user);
        userRepository.flush();
        accountSecurity.invalidateAll(user.getEmail());
        accountSecurity.revokeAllSessions(user.getEmail());
        log.info("Conta excluída logicamente com sucesso. Usuário ID: {}", user.getId());
    }

    public void updatePassword(String email, AuthDTO.UpdatePasswordRequest dto) {
        User user = findByEmailForUpdate(email);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            log.warn("Falha ao atualizar senha para o usuário {}: Senha atual incorreta", user.getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual inválida.");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        accountSecurity.revokeAllSessions(user.getEmail());
        accountSecurity.invalidateAll(user.getEmail());
        log.info("Senha atualizada com sucesso para o usuário: {}", user.getId());
    }

    public void forgotPassword(AuthDTO.ForgotPasswordRequest dto) {
        if (userRepository.existsByEmail(dto.email())) {
            accountSecurity.requestPasswordResetToken(dto.email()).ifPresent(token ->
                    events.publishEvent(new AccountEmailRequested(dto.email(), token, AccountEmailRequested.Purpose.PASSWORD_RESET)));
        }
    }

    public void resetPassword(AuthDTO.ResetPasswordRequest dto) {
        String email = accountSecurity.consumePasswordResetToken(dto.token())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado."));

        User user = findByEmail(email);
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setEmailVerified(true);
        accountSecurity.revokeAllSessions(email);
        accountSecurity.invalidateAll(email);
        log.info("Senha redefinida e e-mail ativado com sucesso para o usuário: {}", user.getId());
    }

    public void verifyEmail(String tokenValue) {
        String email = accountSecurity.consumeEmailVerificationToken(tokenValue)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link inválido ou expirado."));
        User user = findByEmail(email);
        user.setEmailVerified(true);
    }

    private void sendVerificationLink(String email) {
        OneTimeToken token = accountSecurity.generateEmailVerificationToken(email);
        events.publishEvent(new AccountEmailRequested(email, token.getTokenValue(), AccountEmailRequested.Purpose.VERIFY_EMAIL));
    }

    public void resendVerification(String email) {
        userRepository.findByEmail(email).filter(user -> !user.isEmailVerified())
                .flatMap(user -> accountSecurity.requestEmailVerificationToken(user.getEmail()))
                .ifPresent(token -> events.publishEvent(new AccountEmailRequested(email, token.getTokenValue(),
                        AccountEmailRequested.Purpose.VERIFY_EMAIL)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserDTO.Response> listAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO.Response updateUserRole(String principalName, UUID userId, UserRole newRole) {
        if (newRole == UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é permitido conceder o perfil de Administrador pelo painel.");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (targetUser.getRole() == UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é permitido alterar o papel de um Administrador pelo painel.");
        }

        User admin = findByEmail(principalName);

        targetUser.setRole(newRole);
        User saved = userRepository.save(targetUser);

        accountSecurity.revokeAllSessions(targetUser.getEmail());
        log.info("Perfil do usuário {} alterado para {} pelo admin {}", targetUser.getId(), newRole, admin.getId());
        return userMapper.toDTO(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserDTO.AuditResponse> getUserAuditHistory(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
        return userRepository.findRevisions(userId).stream()
                .map(rev -> new UserDTO.AuditResponse(
                        rev.getRequiredRevisionNumber(),
                        rev.getRequiredRevisionInstant(),
                        rev.getMetadata().getRevisionType().name(),
                        userMapper.toDTO(rev.getEntity())
                ))
                .toList();
    }

    private User findByEmailForUpdate(String email) {
        return userRepository.findByEmailForUpdate(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }
}
