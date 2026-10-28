import { apiClient } from "./api";

export async function login(email, password) {
  await apiClient.post("/auth/login", new URLSearchParams({ email, password }));
}

export async function verify2FA(code) {
  await apiClient.post("/auth/verify-2fa", new URLSearchParams({ token: code.trim() }));
  return getCurrentUser();
}

export async function register(userData) {
  const { data } = await apiClient.post("/api/users/register", userData);
  return data;
}

export async function verifyEmail(token) {
  const { data } = await apiClient.get("/api/users/verify-email", {
    params: { token },
  });
  return data;
}

export async function resendVerification(email) {
  const { data } = await apiClient.post("/api/users/resend-verification", { email });
  return data;
}

export async function logout() {
  await apiClient.post("/auth/logout");
}

export async function getCurrentUser() {
  const { data } = await apiClient.get("/auth/me");
  return data;
}

export async function getUserProfile(id) {
  const { data } = await apiClient.get(`/api/users/${id}`);
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

export async function updatePassword(currentPassword, newPassword) {
  const { data } = await apiClient.put("/api/users/update-password", {
    currentPassword,
    newPassword,
  });
  return data;
}
