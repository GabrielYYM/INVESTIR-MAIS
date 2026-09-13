package com.repositorio.investirMais.common.validation.asset;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.PositiveOrZero;

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@PositiveOrZero(message = "O percentual alvo deve ser zero ou positivo")
public @interface ValidTargetPercentage {
    String message() default "O percentual alvo não pode ser negativo";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
