import { apiClient } from "./carteiraService";

/**
 * login.
 * Etapa 1: valida e-mail e senha → backend envia código 2FA por e-mail.
 * @param {string} email
 * @param {string} password
 * @returns {Promise<Object>} Mensagem de confirmação de envio do 2FA
 */
export async function login(email, password) {
  const { data } = await apiClient.post("/auth/login", { email, password });
  return data;
}

/**
 * Verificação do código 2FA.
 * Etapa 2: valida o código recebido por e-mail → retorna o token JWT.
 * @param {string} email
 * @param {string} code  Código 2FA de 6 dígitos
 * @returns {Promise<Object>} { token: "..." }
 */
export async function verify2FA(email, code) {
  const { data } = await apiClient.post("/auth/verify-2fa", { email, code });
  if (data.token) {
    localStorage.setItem("authToken", data.token);
  }
  return data;
}

/**
 * Criar conta.
 * @param {Object} userData
 * @param {string} userData.name
 * @param {string} userData.email
 * @param {string} userData.password
 * @returns {Promise<Object>}
 */
export async function register(userData) {
  const { data } = await apiClient.post("/api/users", userData);
  return data;
}

/**
 * Logout — invalida o token JWT no backend (blacklist) e limpa o localStorage.
 */
export async function logout() {
  const token = localStorage.getItem("authToken");

  try {
    if (token) {
      await apiClient.post("/auth/logout", {}, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
    }
  } catch (error) {
    console.warn("Erro ao notificar o backend sobre o logout:", error);
  } finally {
    localStorage.removeItem("authToken");
  }
}
