package com.repositorio.investir_mais.domain.user.service;

import com.repositorio.investir_mais.infrastructure.security.EmailOttHandler;

import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.user.DTO.LoginRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.LoginResponseDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final OneTimeTokenService oneTimeTokenService;
    private final EmailOttHandler emailOttHandler;
    private final JwtEncoder jwtEncoder;

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

    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByUserSecurityEmail(dto.email());
        if (user == null || !passwordEncoder.matches(dto.password(), user.getUserSecurity().getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }

        if (!user.getUserSecurity().isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "E-mail ainda não foi verificado. Acesse o link enviado no seu e-mail.");
        }

        Instant now = Instant.now();
        long expiresIn = 7200L;
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("investir-mais-api")
            .issuedAt(now)
            .expiresAt(now.plus(expiresIn, ChronoUnit.SECONDS))
            .subject(user.getUserSecurity().getEmail())
            .claim("role", user.getUserSecurity().getRole().name())
            .build();
        String jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new LoginResponseDTO(jwtValue, "Bearer", expiresIn);
    }

    public void updatePassword(String email, String newPassword) {
    User user = userRepository.findByUserSecurityEmail(email);
    if (user == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
    }
    user.getUserSecurity().setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    }

    public User findByEmail(String email) {
    User user = userRepository.findByUserSecurityEmail(email);
    if (user == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado.");
    }
    return user;
    }
}