package com.repositorio.investir_mais.infrastructure.exception;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.repositorio.investir_mais.common.constants.LogMessageConstants;
import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.infrastructure.exception.util.ProblemDetailBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //429
    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimitExceededException(RateLimitExceededException ex) {
        log.warn(LogMessageConstants.ERROR.BUSINESS_RULE_VIOLATION, ex.getMessage());
        return ProblemDetailBuilder.build(
                HttpStatus.TOO_MANY_REQUESTS,
                "Limite de Requisições Excedido",
                ex.getMessage()
        );
    }

    //400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn(LogMessageConstants.ERROR.VALIDATION_FAILED, "Malformed JSON", ex.getMessage());
        return ProblemDetailBuilder.build(
                HttpStatus.BAD_REQUEST,
                "Requisição Malformada",
                "O corpo da requisição contém dados inválidos ou em formato incorreto."
        );
    }

    //400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn(
                LogMessageConstants.ERROR.VALIDATION_FAILED,
                ex.getErrorCount(),
                ex.getMessage()
        );
        return ProblemDetailBuilder.buildValidation(ex);
    }

    //404
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn(LogMessageConstants.ERROR.RESOURCE_NOT_FOUND,
                ex.getMessage()
        );
        return ProblemDetailBuilder.build(
                HttpStatus.NOT_FOUND,
                MessageConstants.Exception.TITLE_NOT_FOUND,
                ex.getMessage()
        );
    }

    //400
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn(
                LogMessageConstants.ERROR.BUSINESS_RULE_VIOLATION,
                ex.getMessage()
        );
        return ProblemDetailBuilder.build(
                HttpStatus.BAD_REQUEST,
                MessageConstants.Exception.TITLE_BAD_REQUEST,
                ex.getMessage()
        );
    }

    //409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn(
                LogMessageConstants.ERROR.DATA_INTEGRITY_VIOLATION,
                ex.getMostSpecificCause().getMessage());
        return ProblemDetailBuilder.build(
                HttpStatus.CONFLICT,
                MessageConstants.Exception.TITLE_CONFLICT,
                MessageConstants.Exception.DETAIL_DATA_INTEGRITY
        );
    }

    // Trata exceções lançadas como ErrorResponseException
    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleErrorResponseException(ErrorResponseException ex) {
        log.warn("Erro de resposta HTTP: {}", ex.getMessage());
        return (ProblemDetail) ex.getBody();
    }

    //500
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error(
                LogMessageConstants.ERROR.INTERNAL_SERVER_ERROR,
                ex
        );

        return ProblemDetailBuilder.build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                MessageConstants.Exception.TITLE_INTERNAL_ERROR,
                MessageConstants.Exception.DETAIL_INTERNAL_ERROR
        );
    }
}
