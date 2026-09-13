package com.repositorio.investirMais.common.validation.user;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.repositorio.investirMais.common.validation.constants.PasswordConstraintValidator;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "A senha é obrigatória")
@Size(min = 8, max = 50, message = "A senha deve ter no mínimo 8 e no máximo 50 caracteres")
public @interface ValidPassword {
    String message() default "A senha deve conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
