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

export const register = account.registerUser;
export const updateUserProfile = account.updateProfile;
export const getCurrentUser = (signal) => account.currentUser({ signal });
export const resendVerification = (email) => account.resendVerification({ email });
export const verifyEmail = (token) => account.verifyEmail({ token: token.trim() });
export const forgotPassword = (email) => account.forgotPassword({ email });
export const resetPassword = (token, newPassword) => account.resetPassword({ token, newPassword });
export const updatePassword = (currentPassword, newPassword) => account.updatePassword({ currentPassword, newPassword });
export const deleteAccount = (id, currentPassword) => account.deleteAccount(id, { currentPassword });
