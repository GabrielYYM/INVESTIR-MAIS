import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
});

// Injeta o token JWT em toda requisição, se já estiver logado
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("investirmais.token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ────────────────────────────────────────────────
// LOGIN (2 etapas: email/senha -> código 2FA por e-mail)
// ────────────────────────────────────────────────

/** Etapa 1: valida email/senha e dispara o código por e-mail. */
export async function iniciarLogin(email, password) {
  const { data } = await apiClient.post("/auth/login", { email, password });
  return data; // { message: "..." }
}

/** Etapa 2: confirma o código recebido por e-mail e retorna o JWT. */
export async function confirmarCodigo2FA(email, code) {
  const { data } = await apiClient.post("/auth/verify-2fa", { email, code });
  localStorage.setItem("investirmais.token", data.token);
  return data.token;
}

export async function logout() {
  const token = localStorage.getItem("investirmais.token");
  try {
    if (token) {
      await apiClient.post("/auth/logout");
    }
  } finally {
    localStorage.removeItem("investirmais.token");
  }
}

export function isAutenticado() {
  return Boolean(localStorage.getItem("investirmais.token"));
}

// ────────────────────────────────────────────────
// CADASTRO
// ────────────────────────────────────────────────

/** Cria um novo usuário (nasce com role ALUNO por padrão no back). */
export async function registrar(name, email, password) {
  const { data } = await apiClient.post("/api/users", { name, email, password });
  return data; // UserResponseDTO
}

/** Retorna os dados do usuário autenticado, incluindo a role. */
export async function getUsuarioAtual() {
  const { data } = await apiClient.get("/api/users/me");
  return data;
}
