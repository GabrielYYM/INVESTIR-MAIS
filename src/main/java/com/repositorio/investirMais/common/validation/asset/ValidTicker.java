package com.repositorio.investirMais.common.validation.asset;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "O ticker do ativo é obrigatório")
public @interface ValidTicker {
    String message() default "O ticker fornecido é inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
