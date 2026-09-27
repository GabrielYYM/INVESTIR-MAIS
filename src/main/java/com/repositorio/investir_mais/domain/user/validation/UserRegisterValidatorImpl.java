package com.repositorio.investir_mais.domain.user.validation;

import org.springframework.stereotype.Component;

import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.common.security.CryptoService;
import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.validation.interfaces.UserRegisterValidator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRegisterValidatorImpl implements UserRegisterValidator {
    private final UserRepository userRepository;
    private final CryptoService cryptoService;

    @Override
    public void validate(@NonNull UserRequestDTO request) {
        String hash = cryptoService.generateSha256Hash(request.email());

        if (userRepository.existsBySecurityEmailHash(hash)) {
            throw new IllegalArgumentException(MessageConstants.User.EMAIL_ALREADY_IN_USE);
        }
    }
}
