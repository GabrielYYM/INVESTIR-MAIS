package com.repositorio.investir_mais.domain.audit.model.enums;

public enum AuditAction {
    // Autenticação & Sessão
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    TWO_FACTOR_GENERATED,
    TWO_FACTOR_VERIFIED,
    TWO_FACTOR_FAILED,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_SUCCESS,

    // Usuários
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,

    // Ativos e Categorias
    ASSET_CREATED,
    ASSET_UPDATED,
    ASSET_DELETED,
    CATEGORY_CREATED,
    CATEGORY_UPDATED,
    CATEGORY_DELETED,

    // Perguntas
    QUESTION_CREATED,
    QUESTION_UPDATED,
    QUESTION_DELETED,

    // Administrativo e Infraestrutura
    ADMIN_TOKEN_CLEANUP,
    SECURITY_BLOCK
}
