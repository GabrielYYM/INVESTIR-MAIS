package com.repositorio.investirMais.common.constants;

/**
 * Centraliza todas as mensagens de resposta da API e detalhes de erro.
 * Facilita a manutenção, padronização e futura internacionalização (i18n).
 */
public final class MessageConstants {

    private MessageConstants() {
    }

    public static final class Auth {
        public static final String LOGIN_2FA_SENT = "Código de verificação enviado para o seu e-mail.";
        public static final String FORGOT_PASSWORD_SENT = "Se o e-mail existir, um link de recuperação foi enviado.";
        public static final String PASSWORD_RESET_SUCCESS = "Senha redefinida com sucesso.";
        public static final String EMAIL_RECOVERY_SUBJECT = "Recuperação de Senha - MVP";
        public static final String EMAIL_RECOVERY_BODY = "Olá %s,\n\nVocê solicitou a recuperação de senha.\nUtilize o token abaixo para redefinir sua senha:\n\n%s\n\nSe você não solicitou isso, ignore este e-mail.";
        public static final String EMAIL_2FA_SUBJECT = "Seu código de acesso (2FA) - MVP";
        public static final String EMAIL_2FA_BODY = "Olá %s,\n\nSeu código de acesso é: %s\nVálido por 5 minutos.";
        public static final String ERR_TOO_MANY_ATTEMPTS = "Muitas tentativas falhas.";
        public static final String ERR_TOO_MANY_ATTEMPTS_2FA = "Muitas tentativas falhas. Tente novamente mais tarde.";
        public static final String ERR_INVALID_CREDENTIALS = "Credenciais inválidas.";
        public static final String ERR_INVALID_2FA = "Código 2FA inválido.";
        public static final String ERR_EXPIRED_2FA = "Código 2FA expirado.";
        public static final String ERR_INVALID_TOKEN = "Token inválido ou não encontrado.";
        public static final String ERR_EXPIRED_TOKEN = "Token expirado.";
        public static final String ERR_HASH_EMAIL = "Erro ao gerar hash do e-mail para busca.";
        public static final String ERR_HASH_TOKEN = "Erro ao gerar hash do token.";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String TOKEN_ISSUER = "auth-api";
        public static final String PREFIX_2FA = "2FA:";
        public static final String ERR_INVALID_JWT = "Token inválido";
        public static final String ERR_RATELIMIT_EXCEEDED = "Muitas requisições. Por favor, aguarde alguns instantes.";
    }

    public static final class User {
        public static final String NOT_FOUND = "Usuário não encontrado.";
        public static final String NOT_FOUND_WITH_ID = "Usuário não encontrado com o ID: ";
        public static final String NOT_FOUND_FOR_TOKEN = "Usuário não encontrado para o token fornecido.";
        public static final String NOT_FOUND_FOR_UPDATE = "Usuário não encontrado.";
        public static final String INVALID_PASSWORD = "A senha atual informada está incorreta.";
        public static final String EMAIL_ALREADY_IN_USE = "Se os dados informados forem válidos, entraremos em contato.";
        public static final String EMAIL_ALREADY_IN_USE_BY_OTHER = "Email já está em uso por outro usuário.";
    }

    public static final class Admin {
        public static final String TOKEN_CLEANUP_SUCCESS = "Rotina de limpeza de tokens executada com sucesso.";
    }

    public static final class Asset {
        public static final String NOT_FOUND = "Ativo não encontrado.";
        public static final String CATEGORY_NOT_FOUND = "Categoria não encontrada.";
        public static final String CATEGORY_TARGET_EXCEEDED = "A soma das porcentagens alvo excede 100%.";
    }

    public static final class Question {
        public static final String NOT_FOUND = "Pergunta não encontrada.";
        public static final String EVALUATION_SAVED = "Avaliação do ativo salva com sucesso.";
    }

    public static final class Portfolio {
        public static final String NOT_FOUND = "Carteira não encontrada.";
        public static final String REBALANCE_CALC_SUCCESS = "Cálculo de rebalanceamento concluído.";
    }

    public static final class Exception {
        public static final String TITLE_NOT_FOUND = "Recurso não encontrado";
        public static final String TITLE_CONFLICT = "Conflito de dados";
        public static final String TITLE_INTERNAL_ERROR = "Erro interno do servidor";
        public static final String TITLE_BAD_REQUEST = "Requisição inválida";

        public static final String DETAIL_DATA_INTEGRITY = "Se os dados informados forem válidos, sua solicitação será processada.";
        public static final String DETAIL_INTERNAL_ERROR = "Ocorreu um erro inesperado. Nossa equipe já foi notificada.";
    }

    public static final class Infrastructure {
        public static final String ERR_ENCRYPTION_KEY_INVALID = "Chave de criptografia inválida ou ausente. O sistema não pode iniciar de forma segura.";
        public static final String ERR_KDF_SALT_INVALID = "Salt KDF inválido ou ausente.";
        public static final String ERR_KDF_DERIVATION_FAILED = "CRÍTICO: Falha ao derivar a chave de criptografia.";
        public static final String ERR_ENCRYPT_FAILED = "Erro ao criptografar o dado para o banco.";
        public static final String ERR_DECRYPT_FAILED = "Erro ao descriptografar o dado do banco.";
    }
}
