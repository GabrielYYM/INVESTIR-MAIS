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
@PositiveOrZero(message = "O valor financeiro não pode ser negativo")
public @interface ValidFinanceAmount {
    String message() default "A quantia financeira deve ser zero ou maior";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
