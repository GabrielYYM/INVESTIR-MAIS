package com.repositorio.investirMais.common.validation.user;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.repositorio.investirMais.common.validation.constants.EmailConstraintValidator;

@Documented
@Constraint(validatedBy = EmailConstraintValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "O email é obrigatório")
@Email(message = "O email não é válido")
@Size(min = 8, max = 50, message = "O email não pode ter mais de 50 caracteres")
public @interface ValidEmail {
    String message() default "O email deve seguir o formato valido, ex: example@gmail.com";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
