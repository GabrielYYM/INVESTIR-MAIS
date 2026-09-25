package com.repositorio.investir_mais.infrastructure.exception.util;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;

import lombok.NonNull;

public class ProblemDetailBuilder {
    private static final String TITLE_VALIDATION_ERROR = "Erro de validação";
    private static final String DETAIL_VALIDATION_ERROR = "Um ou mais campos estão inválidos.";
    private static final String PROPERTY_ERRORS_KEY = "erros";
    private static final URI DEFAULT_INSTANCE = URI.create("about:blank");

    public static ProblemDetail build(@NonNull HttpStatus status, @NonNull String title, @NonNull String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setInstance(DEFAULT_INSTANCE);
        return problemDetail;
    }

    public static ProblemDetail buildValidation(@NonNull MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = build(HttpStatus.BAD_REQUEST, TITLE_VALIDATION_ERROR, DETAIL_VALIDATION_ERROR);

        List<String> errors = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        problemDetail.setProperty(PROPERTY_ERRORS_KEY, errors);
        return problemDetail;
    }
}
