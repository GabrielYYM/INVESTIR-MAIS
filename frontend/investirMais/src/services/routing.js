import { data, redirect } from "react-router";
import { ApiError, request, SessionExpiredError } from "./api";

const expired = () => redirect("/login?notice=expired");
export async function requireUser({ request: navigation }) {
  try { return await request("/auth/me", { signal: navigation.signal }); }
  catch (error) {
    if (error.status === 401 || error.status === 403) throw redirect("/login");
    throw error;
  }
}

export const routeLoader = (handler) => async (args) => {
  try { return await handler(args); }
  catch (error) { if (error instanceof SessionExpiredError) throw expired(); throw error; }
};

export const routeAction = (handler, errorContext = () => ({})) => async (args) => {
  const form = await args.request.formData();
  try { return await handler(form, args); }
  catch (error) {
    if (error instanceof SessionExpiredError) return expired();
    if (!(error instanceof ApiError)) throw error;
    return data({ error: error.message, ...errorContext(form) }, { status: error.status || 503 });
  }
};

export async function loadPage(fetchPage, navigation, { size = 10, sort = "name" } = {}) {
  const url = new URL(navigation.url);
  const rawPage = Number(url.searchParams.get("page") || 0);
  const page = Number.isSafeInteger(rawPage) && rawPage >= 0 ? rawPage : 0;
  const result = await fetchPage({ page, size, sort: [sort] }, { signal: navigation.signal });
  if (page > 0 && !result.content.length) {
    url.searchParams.set("page", String(Math.max(0, result.page.totalPages - 1)));
    throw redirect(url.pathname + url.search);
  }
  return result;
}

export function confirmPassword(form) {
  if ((form.get("newPassword") || form.get("password")) !== form.get("confirmPassword")) {
    throw new ApiError(400, { detail: "As senhas não coincidem." });
  }
}
