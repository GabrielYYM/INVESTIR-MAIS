package com.repositorio.investir_mais.domain;

public record AccountEmailRequested(String email, String token, Purpose purpose) {
    public enum Purpose { VERIFY_EMAIL, PASSWORD_RESET }
}
