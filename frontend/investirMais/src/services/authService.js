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
// CADASTRO E VERIFICAÇÃO DE EMAIL
// ────────────────────────────────────────────────

/** Cria um novo usuário com suporte a data de nascimento e responsável para menores de 12 anos. */
export async function registrar(name, email, password, birthDate, guardianEmail) {
  const payload = {
    name,
    email,
    password,
    birthDate: birthDate || null,
    guardianEmail: guardianEmail || null,
  };
  const { data } = await apiClient.post("/api/users", payload);
  return data; // UserResponseDTO
}

/** Confirma o código do usuário e, se menor de 12 anos, o código do responsável. */
export async function verificarCadastro(email, code, guardianCode) {
  const payload = {
    email,
    code,
    guardianCode: guardianCode || null,
  };
  const { data } = await apiClient.post("/api/users/verify-registration", payload);
  return data; // MessageResponseDTO
}

/** Reenvia os códigos de verificação de cadastro para o usuário e o responsável. */
export async function reenviarCodigoCadastro(email) {
  const { data } = await apiClient.post("/api/users/resend-verification", { email });
  return data; // MessageResponseDTO
}

/** Retorna os dados do usuário autenticado, incluindo a role. */
export async function getUsuarioAtual() {
  const { data } = await apiClient.get("/api/users/me");
  return data;
}
