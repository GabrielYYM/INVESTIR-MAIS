import axios from "axios";

const baseURL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/+$/, "");
const crossOrigin = new URL(baseURL || "/", window.location.origin).origin !== window.location.origin;
const safeMethods = new Set(["get", "head", "options", "trace"]);

export const apiClient = axios.create({
  baseURL,
  withCredentials: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
  headers: { Accept: "application/json" },
});

let csrfRequest;

function prepareCsrf() {
  csrfRequest ??= apiClient.get("/csrf")
    .then(({ data }) => data.token)
    .finally(() => { csrfRequest = undefined; });
  return csrfRequest;
}

apiClient.interceptors.request.use(async (config) => {
  if (safeMethods.has(config.method)) return config;

  // Spring pode renovar o cookie entre as etapas da autenticação.
  const token = await prepareCsrf();
  if (crossOrigin) config.headers.set("X-XSRF-TOKEN", token);
  return config;
});

export function apiError(error, fallback) {
  const { status, data } = error.response ?? {};
  if (status === 429) return "Muitas tentativas. Aguarde alguns instantes e tente novamente.";
  return data?.detail || data?.message || fallback;
}
