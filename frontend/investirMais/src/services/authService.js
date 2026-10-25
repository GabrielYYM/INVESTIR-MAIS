import { apiClient } from "./carteiraService";

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
