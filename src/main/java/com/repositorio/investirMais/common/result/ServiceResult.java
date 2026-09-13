package com.repositorio.investirMais.common.result;

/**
 * Interface selada genérica para representar o resultado de operações de
 * serviço.
 * Utiliza o conceito de Algebraic Data Types (ADTs) do Java 17+.
 * 
 * @param <T> Tipo do objeto de sucesso (ex: DTO)
 */
public sealed interface ServiceResult<T> permits
        ServiceResult.Success,
        ServiceResult.NotFound,
        ServiceResult.Error {

    /**
     * Representa uma operação concluída com sucesso.
     */
    record Success<T>(T data) implements ServiceResult<T> {
    }

    /**
     * Representa a ausência de um recurso solicitado (404).
     */
    record NotFound<T>(String message) implements ServiceResult<T> {
    }

    /**
     * Representa um erro de negócio ou falha na operação (400/409/500).
     */
    record Error<T>(String message, String detail) implements ServiceResult<T> {
    }

    // Factory methods para facilitar o uso
    static <T> ServiceResult<T> success(T data) {
        return new Success<>(data);
    }

    static <T> ServiceResult<T> notFound(String message) {
        return new NotFound<>(message);
    }

    static <T> ServiceResult<T> error(String message) {
        return new Error<>(message, null);
    }

    static <T> ServiceResult<T> error(String message, String detail) {
        return new Error<>(message, detail);
    }
}
