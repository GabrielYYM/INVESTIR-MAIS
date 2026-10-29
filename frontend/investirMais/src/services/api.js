const BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");
const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS"]);

export class ApiError extends Error {
  constructor(status, problem, retryAfter) {
    super(problem?.detail || problem?.message || ({
      401: "Confira suas credenciais ou entre novamente.",
      403: "Você não tem permissão para esta operação. Atualize a página e tente novamente.",
      404: "Registro não encontrado.",
      429: `Muitas tentativas. Tente novamente${retryAfter ? ` em ${retryAfter} segundos` : " mais tarde"}.`,
      500: "O servidor está indisponível ou encontrou um erro. Tente novamente.",
      503: "Serviço temporariamente indisponível. Tente novamente em instantes.",
    })[status] || "Não foi possível concluir a operação.");
    this.name = "ApiError";
    this.status = status;
  }
}

export class SessionExpiredError extends ApiError {
  constructor() { super(401, { detail: "Sua sessão expirou. Entre novamente." }); }
}

const csrfCookie = () => document.cookie.split(";").map((cookie) => cookie.trim())
  .find((cookie) => cookie.startsWith("XSRF-TOKEN="))?.slice("XSRF-TOKEN=".length);
let csrfInitialization;

async function getCsrfToken(signal) {
  signal?.throwIfAborted();
  if (!csrfCookie()) {
    // Even an anonymous 401/403 passes through CsrfFilter and initializes the cookie.
    csrfInitialization ??= request("/auth/me").catch((error) => {
      if (error.status !== 401 && error.status !== 403) throw error;
    }).finally(() => { csrfInitialization = undefined; });
    await csrfInitialization;
  }
  signal?.throwIfAborted();
  const token = csrfCookie();
  if (!token) throw new ApiError(0, { detail: "Não foi possível obter o cookie de segurança. Verifique se o frontend e a API usam o mesmo domínio." });
  return decodeURIComponent(token);
}

export async function request(path, { method = "GET", body, signal } = {}) {
  method = method.toUpperCase();
  const headers = new Headers({ Accept: "application/json, application/problem+json" });
  if (!SAFE_METHODS.has(method)) {
    headers.set("X-XSRF-TOKEN", await getCsrfToken(signal));
  }
  if (body instanceof URLSearchParams) headers.set("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
  else if (body !== undefined) { headers.set("Content-Type", "application/json"); if (typeof body !== "string") body = JSON.stringify(body); }
  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, { method, body, signal, headers, credentials: "include", cache: "no-store", redirect: "error" });
  } catch (error) {
    if (error.name === "AbortError") throw error;
    throw new ApiError(0, { detail: "Não foi possível conectar ao servidor. Tente novamente." });
  }
  const text = await response.text();
  let result;
  try { result = text ? JSON.parse(text) : undefined; }
  catch { if (response.ok) throw new ApiError(502, { detail: "O servidor retornou uma resposta inesperada." }); }
  if (!response.ok) {
    if (response.status === 401 && path.startsWith("/api/")) {
      if (SAFE_METHODS.has(method)) throw new SessionExpiredError();
      // A wrong current password is also a 401. Check the session before redirecting.
      try { await request("/auth/me", { signal }); }
      catch (error) {
        if (error.status === 401 || error.status === 403) throw new SessionExpiredError();
        if (error.name === "AbortError") throw error;
      }
    }
    throw new ApiError(response.status, result, response.headers.get("Retry-After"));
  }
  return result;
}

export const post = (path, body, signal) => request(path, { method: "POST", body, signal });
export const apiRequest = request;
