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

        if (request.birthDate() != null) {
            if (request.birthDate().isAfter(java.time.LocalDate.now())) {
                throw new IllegalArgumentException(MessageConstants.User.INVALID_BIRTH_DATE);
            }

            int age = java.time.Period.between(request.birthDate(), java.time.LocalDate.now()).getYears();
            if (age < 12) {
                if (request.guardianEmail() == null || request.guardianEmail().trim().isBlank()) {
                    throw new IllegalArgumentException(MessageConstants.Auth.ERR_GUARDIAN_EMAIL_REQUIRED);
                }

                String guardianEmail = request.guardianEmail().trim();
                if (guardianEmail.equalsIgnoreCase(request.email().trim())) {
                    throw new IllegalArgumentException(MessageConstants.Auth.ERR_GUARDIAN_EMAIL_SAME);
                }

                if (!guardianEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    throw new IllegalArgumentException("Formato do e-mail do responsável é inválido.");
                }
            }
        }
    }
}
