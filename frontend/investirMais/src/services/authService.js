import { post, request } from "./api";

export const login = (email, password) => post("/auth/login", new URLSearchParams({ email, password }));
export const verify2FA = async (token) => {
  await post("/auth/verify-2fa", new URLSearchParams({ token: token.trim() }));
  return request("/auth/me");
};
export const resendCode = () => post("/auth/code");
export const logout = () => post("/auth/logout");
