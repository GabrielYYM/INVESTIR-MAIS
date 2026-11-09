import { apiClient } from "./api.js";

export async function getAllUsers(signal) {
  const { data } = await apiClient.get("/api/users", { signal });
  return data;
}

export async function updateUserRole(userId, role) {
  const { data } = await apiClient.patch(`/api/users/${userId}/role`, { role });
  return data;
}

export async function getUserAudit(userId) {
  const { data } = await apiClient.get(`/api/users/${userId}/audit`);
  return data;
}

