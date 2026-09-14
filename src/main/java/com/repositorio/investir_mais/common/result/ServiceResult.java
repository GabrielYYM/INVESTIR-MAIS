package com.repositorio.investir_mais.common.result;

public sealed interface ServiceResult<T> permits
        ServiceResult.Success,
        ServiceResult.NotFound,
        ServiceResult.Error {
    record Success<T>(T data) implements ServiceResult<T> {
    }

    record NotFound<T>(String message) implements ServiceResult<T> {
    }

    record Error<T>(String message, String detail) implements ServiceResult<T> {
    }

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