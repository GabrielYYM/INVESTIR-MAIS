package com.repositorio.investirMais.common.validation.question;

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
@NotNull(message = "O ID da pergunta é obrigatório")
public @interface ValidQuestionId {
    String message() default "O ID da pergunta não pode ser vazio";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
