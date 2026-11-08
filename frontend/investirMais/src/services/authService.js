import { apiClient } from "./api";

export async function login(email, password, retorno) {
  await apiClient.post("/auth/login", new URLSearchParams({ email, password, retorno }));
}

export async function verify2FA(code) {
  await apiClient.post("/auth/verify-2fa", new URLSearchParams({ token: code.trim() }));
}

export async function register(userData) {
  const { data } = await apiClient.post("/api/users/register", userData);
  return data;
}

export async function resendVerification(email) {
  const { data } = await apiClient.post("/api/users/resend-verification", { email });
  return data;
}

export async function verifyEmail(token) {
  const { data } = await apiClient.post("/api/users/verify-email", new URLSearchParams({ token: token.trim() }));
  return data;
}

export async function logout() {
  await apiClient.post("/auth/logout");
}

export async function getCurrentUser(signal) {
  const { data } = await apiClient.get("/auth/me", { signal });
  return data;
}

export async function updateUserProfile(id, updateData) {
  const { data } = await apiClient.put(`/api/users/${id}`, updateData);
  return data;
}

export async function forgotPassword(email) {
  const { data } = await apiClient.post("/api/users/forgot-password", { email });
  return data;
}

export async function resetPassword(token, newPassword) {
  const { data } = await apiClient.post("/api/users/reset-password", { token, newPassword });
  return data;
}
