package com.repositorio.investir_mais.common.constants;

/**
 * Centraliza todas as templates de mensagens de log do sistema.
 * Divide as mensagens por categorias para facilitar a manutenção e auditoria.
 */
public final class LogMessageConstants {

    private LogMessageConstants() {
    }

    public static final class AUTH {
        public static final String LOGIN_INITIATED = "LOGIN FASE 1: Credenciais válidas. Gerando 2FA para o usuário {}. IP: {}";
        public static final String LOGIN_SUCCESS = "LOGIN SUCESSO: 2FA validado. Token JWT emitido para o usuário {}. IP: {}";
        public static final String LOGIN_FAILED_USER_NOT_FOUND = "FALHA DE LOGIN: Usuário não existe. IP: {} | E-mail tentado: {}";
        public static final String LOGIN_FAILED_INVALID_PASSWORD = "FALHA DE LOGIN: Senha incorreta. IP: {} | E-mail: {}";

        public static final String LOGIN_2FA_FAILED_INVALID_CODE = "FALHA 2FA: Código inválido. IP: {} | E-mail: {}";
        public static final String LOGIN_2FA_FAILED_EXPIRED_CODE = "FALHA 2FA: Código expirado. IP: {} | E-mail: {}";

        public static final String PASSWORD_RECOVERY_INITIATED = "RECUPERAÇÃO DE SENHA: Solicitação iniciada para o e-mail: {} a partir do IP: {}";
        public static final String PASSWORD_RECOVERY_EMAIL_SENT = "E-mail de recuperação enviado para {}";
        public static final String PASSWORD_RECOVERY_EMAIL_ERROR = "Erro ao enviar e-mail de recuperação";
        public static final String PASSWORD_RESET_SUCCESS = "ALERTA DE SEGURANÇA: Senha redefinida com sucesso via token. IP de origem: {}";
    }

    public static final class SECURITY {
        public static final String BRUTE_FORCE_LOGIN_BLOCKED = "ALERTA: Tentativa de login bloqueada (Força Bruta). IP: {} | Conta alvo: {}";
        public static final String BRUTE_FORCE_2FA_BLOCKED = "ALERTA: Tentativa de 2FA bloqueada (Força Bruta). IP: {} | Conta alvo: {}";
        public static final String PASSWORD_RECOVERY_BLOCKED_RATE_LIMIT = "RECUPERAÇÃO DE SENHA BLOQUEADA (Rate Limit): IP {} está bloqueado.";
        public static final String RATE_LIMIT_EXCEEDED_DDOS = "BLOQUEIO DDoS (Rate Limit Excedido): IP {} disparou requisições demais.";
        public static final String JWT_VALIDATION_FAILED = "ACESSO NEGADO: Falha na validação do JWT. IP: {} | URI: {} | Motivo: {}";
    }

    public static final class ERROR {
        public static final String VALIDATION_FAILED = "Validação falhou (400): {} erros encontrados. Detalhe: {}";
        public static final String RESOURCE_NOT_FOUND = "Recurso não encontrado (404): {}";
        public static final String BUSINESS_RULE_VIOLATION = "Regra de negócio violada (400): {}";
        public static final String DATA_INTEGRITY_VIOLATION = "Violação de integridade no banco de dados (409): {}";
        public static final String INTERNAL_SERVER_ERROR = "ERRO INTERNO SEVERO (500): Ocorreu uma exceção não tratada.";
    }

    public static final class AUDIT {
        public static final String ASSET_CREATED = "AUDITORIA: Novo ativo criado. ID: {} | Ticker: {} | Categoria: {}";
        public static final String ASSET_UPDATED = "AUDITORIA: Ativo atualizado. ID: {} | Ticker: {}";
        public static final String ASSET_DELETED = "AUDITORIA: Ativo removido. ID: {}";

        public static final String CATEGORY_CREATED = "AUDITORIA: Nova categoria criada. ID: {} | Nome: {}";
        public static final String CATEGORY_UPDATED = "AUDITORIA: Categoria atualizada. ID: {} | Nome: {}";
        public static final String CATEGORY_DELETED = "AUDITORIA: Categoria removida. ID: {}";

        public static final String USER_CREATED = "AUDITORIA: Novo usuário registrado. ID: {} | E-mail: {}";
        public static final String USER_UPDATED = "AUDITORIA: Perfil de usuário atualizado. ID: {}";
        public static final String USER_DELETED = "AUDITORIA: Usuário removido do sistema. ID: {}";
    }
}
