import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("authToken") || localStorage.getItem("investirmais.token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Login - Etapa 1: valida e-mail e senha. Backend dispara código 2FA por e-mail.
 */
export async function login(email, password) {
  const { data } = await apiClient.post("/auth/login", { email, password });
  return data;
}
export const iniciarLogin = login;

/**
 * Login - Etapa 2: valida código 2FA e salva token JWT.
 */
export async function verify2FA(email, code) {
  const { data } = await apiClient.post("/auth/verify-2fa", { email, code });
  if (data.token) {
    localStorage.setItem("authToken", data.token);
    localStorage.setItem("investirmais.token", data.token);
  }
  return data;
}

export async function confirmarCodigo2FA(email, code) {
  const data = await verify2FA(email, code);
  return data.token;
}

/**
 * Logout - invalida token no backend e limpa localStorage.
 */
export async function logout() {
  const token = localStorage.getItem("authToken") || localStorage.getItem("investirmais.token");
  try {
    if (token) {
      await apiClient.post("/auth/logout");
    }
  } catch (error) {
    console.warn("Erro ao notificar backend sobre logout:", error);
  } finally {
    localStorage.removeItem("authToken");
    localStorage.removeItem("investirmais.token");
  }
}

export function isAutenticado() {
  return Boolean(localStorage.getItem("authToken") || localStorage.getItem("investirmais.token"));
}

/**
 * Cadastro de novo usuário.
 */
export async function registrar(name, email, password, birthDate, guardianEmail, termsAccepted) {
  const payload = typeof name === "object" ? name : {
    name,
    email,
    password,
    birthDate: birthDate || null,
    guardianEmail: guardianEmail || null,
    termsAccepted: termsAccepted,
  };
  const { data } = await apiClient.post("/api/users", payload);
  return data;
}
export const register = registrar;

/**
 * Confirmação de código de cadastro (aluno e responsável se menor).
 */
export async function verificarCadastro(email, code, guardianCode) {
  const payload = {
    email,
    code,
    guardianCode: guardianCode || null,
  };
  const { data } = await apiClient.post("/api/users/verify-registration", payload);
  return data;
}

/**
 * Reenvia códigos de verificação de cadastro.
 */
export async function reenviarCodigoCadastro(email) {
  const { data } = await apiClient.post("/api/users/resend-verification", { email });
  return data;
}

/**
 * Retorna os dados do usuário autenticado atual (incluindo role).
 */
export async function getUsuarioAtual() {
  const { data } = await apiClient.get("/api/users/me");
  return data;
}

/**
 * Obtém o ID do usuário a partir do token JWT no localStorage.
 */
export function getUserIdFromToken() {
  const token = localStorage.getItem("authToken") || localStorage.getItem("investirmais.token");
  if (!token) return null;
  try {
    const payloadBase64 = token.split(".")[1];
    const payloadJson = atob(payloadBase64.replace(/-/g, "+").replace(/_/g, "/"));
    const payload = JSON.parse(payloadJson);
    return payload.sub || null;
  } catch (e) {
    return null;
  }
}

/**
 * Busca perfil do usuário pelo ID.
 */
export async function getUserProfile(id) {
  const { data } = await apiClient.get(`/api/users/${id}`);
  return data;
}

/**
 * Atualiza o perfil do usuário pelo ID.
 */
export async function updateUserProfile(id, updateData) {
  const { data } = await apiClient.put(`/api/users/${id}`, updateData);
  return data;
}

export async function deleteAccount(id) {
  await apiClient.delete(`/api/users/${id}`);
  localStorage.removeItem("authToken");
  localStorage.removeItem("investirmais.token");
}
