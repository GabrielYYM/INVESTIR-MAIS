package com.repositorio.investir_mais.common.validation.question;

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
@NotBlank(message = "O texto da pergunta é obrigatório")
public @interface ValidQuestionText {
    String message() default "O texto da pergunta não pode ser vazio";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
