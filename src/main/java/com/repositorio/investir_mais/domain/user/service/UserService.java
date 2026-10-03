package com.repositorio.investir_mais.domain.user.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.user.DTO.ForgotPasswordRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.ResetPasswordRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UpdatePasswordDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.infrastructure.EmailOttHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final OneTimeTokenService oneTimeTokenService;
    private final EmailOttHandler emailOttHandler;

    public User findByEmail(String email) {
        User user = userRepository.findByUserSecurityEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
        return user;
    }

    public UserResponseDTO registerUser(UserRequestDTO dto) {
        if (dto.role() == UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é permitido cadastrar perfil ADMIN diretamente.");
        }

        User user = userMapper.toEntity(dto);
        user.getUserSecurity().setPassword(passwordEncoder.encode(dto.password()));
        user.getUserSecurity().setEmailVerified(false);
        User savedUser = userRepository.save(user);

        String email = savedUser.getUserSecurity().getEmail();
        OneTimeToken ott = oneTimeTokenService.generate(new GenerateOneTimeTokenRequest(email));
        emailOttHandler.sendOttEmail(email, ott.getTokenValue());

        return userMapper.toDTO(savedUser);
    }

    public void updatePassword(String email, UpdatePasswordDTO dto) {
        User user = findByEmail(email);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getUserSecurity().getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual inválida.");
        }

        user.getUserSecurity().setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    public void forgotPassword(ForgotPasswordRequestDTO dto) {
        User user = userRepository.findByUserSecurityEmail(dto.email());
        if (user == null) {
            return;
        }

        OneTimeToken ott = oneTimeTokenService.generate(new GenerateOneTimeTokenRequest(dto.email()));
        emailOttHandler.sendOttEmail(dto.email(), ott.getTokenValue());
    }

    public void resetPassword(ResetPasswordRequestDTO dto) {
        OneTimeToken consumedOtt = oneTimeTokenService.consume(new OneTimeTokenAuthenticationToken(dto.token()));

        if (consumedOtt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado.");
        }

        User user = findByEmail(consumedOtt.getUsername());

        user.getUserSecurity().setPassword(passwordEncoder.encode(dto.newPassword()));
        user.getUserSecurity().setEmailVerified(true);
        userRepository.save(user);
    }
}