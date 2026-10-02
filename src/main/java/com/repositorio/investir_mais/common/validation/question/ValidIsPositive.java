package com.repositorio.investir_mais.common.validation.question;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@NotNull(message = "A avaliação da pergunta é obrigatória")
public @interface ValidIsPositive {
    String message() default "A avaliação da pergunta deve ser verdadeira (true) ou falsa (false)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
