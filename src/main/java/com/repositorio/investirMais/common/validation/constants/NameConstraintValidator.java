package com.repositorio.investirMais.common.validation.constants;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.repositorio.investirMais.common.validation.user.ValidName;

public class NameConstraintValidator implements ConstraintValidator<ValidName, String> {
    private static final String NAME_PATTERN = "^[a-zA-ZÀ-ÿ\\s]+$";

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.trim().isEmpty()) {
            return true;
        }
        return name.matches(NAME_PATTERN);
    }
}
