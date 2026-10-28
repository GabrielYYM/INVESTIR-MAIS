import axios from "axios";

//generic api.js 
//update in future
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/+$/, "");

export function getCookie(name) {
  if (typeof document === "undefined") return null;
  const match = document.cookie.match(new RegExp("(?:^|;\\s*)" + name + "=([^;]*)"));
  return match ? decodeURIComponent(match[1]) : null;
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
  headers: {
    Accept: "application/json",
  },
});

let csrfPromise = null;

export async function getCsrfToken(force = false) {
  const existingToken = getCookie("XSRF-TOKEN");
  if (existingToken && !force) {
    return existingToken;
  }

  if (!csrfPromise) {
    csrfPromise = apiClient
      .get("/csrf")
      .then((response) => response.data?.token || getCookie("XSRF-TOKEN"))
      .finally(() => {
        csrfPromise = null;
      });
  }

  return csrfPromise;
}

apiClient.interceptors.request.use(
  async (config) => {
    const method = (config.method || "get").toLowerCase();
    const isMutating = !["get", "head", "options"].includes(method);
    const isCsrfUrl = typeof config.url === "string" && config.url.endsWith("/csrf");

    if (isMutating && !isCsrfUrl) {
      try {
        const token = await getCsrfToken();
        if (token) {
          if (config.headers?.set) {
            config.headers.set("X-XSRF-TOKEN", token);
          } else {
            config.headers = config.headers || {};
            config.headers["X-XSRF-TOKEN"] = token;
          }
        }
      } catch (error) {
        console.error("Falha ao obter token CSRF para a requisição:", error);
      }
    }

    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    if (
      status === 403 &&
      originalRequest &&
      !originalRequest._retryCsrf &&
      !originalRequest.url?.endsWith("/csrf")
    ) {
      originalRequest._retryCsrf = true;
      try {
        const newToken = await getCsrfToken(true);
        if (newToken) {
          if (originalRequest.headers?.set) {
            originalRequest.headers.set("X-XSRF-TOKEN", newToken);
          } else {
            originalRequest.headers = originalRequest.headers || {};
            originalRequest.headers["X-XSRF-TOKEN"] = newToken;
          }
        }
        return apiClient(originalRequest);
      } catch (csrfError) {
        return Promise.reject(csrfError);
      }
    }

    if (status === 401 && typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("auth:unauthorized"));
    }

    return Promise.reject(error);
  }
);

export default apiClient;
