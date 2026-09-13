package com.repositorio.investirMais.common.validation.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "O código é obrigatório")
@Size(min = 6, max = 6, message = "O código deve ter exatamente 6 caracteres")
public @interface Valid2FACode {
    String message() default "Código 2FA no formato inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
