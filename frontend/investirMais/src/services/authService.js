import { apiClient } from "./api";
import * as account from "../generated/account.ts";

// These endpoints are Spring Security filters, outside the controller OpenAPI contract.
export async function login(email, password, retorno) {
  await apiClient.post("/auth/login", new URLSearchParams({ email, password, retorno }));
}
export async function verify2FA(code) {
  await apiClient.post("/auth/verify-2fa", new URLSearchParams({ token: code.trim() }));
}
export async function logout() { await apiClient.post("/auth/logout"); }

export const getCurrentUser = (signal) => account.currentUser({ signal });
export const deleteAccount = (id, currentPassword) => account.deleteAccount(id, { currentPassword });

export async function isSessionExpired(error) {
  if (error.response?.status !== 401) return false;
  try { await getCurrentUser(); return false; }
  catch (sessionError) { return [401, 403].includes(sessionError.response?.status); }
}
