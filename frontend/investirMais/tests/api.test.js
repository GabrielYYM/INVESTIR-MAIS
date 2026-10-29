import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError, post, request, SessionExpiredError } from "../src/services/api";
import { login, verify2FA } from "../src/services/authService";

const json = (body, status = 200, headers = {}) => new Response(JSON.stringify(body), { status, headers });
const empty = () => new Response(null, { status: 204 });
const put = (path, body) => request(path, { method: "PUT", body });
const remove = (path) => request(path, { method: "DELETE" });
beforeEach(() => { document.cookie = "XSRF-TOKEN=t; path=/"; });
afterEach(() => { vi.unstubAllGlobals(); document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/"; });

describe("Spring HTTP contract", () => {
  it("refreshes CSRF between authentication factors and uses form encoding", async () => {
    document.cookie = "XSRF-TOKEN=before-password; path=/";
    const fetcher = vi.fn().mockImplementation(async (path) => {
      if (path === "/auth/login") { document.cookie = "XSRF-TOKEN=after-password; path=/"; return empty(); }
      if (path === "/auth/me") return json({ id: "student", role: "STUDENT" });
      return empty();
    });
    vi.stubGlobal("fetch", fetcher);
    await login("aluno@example.com", "secret&value");
    const user = await verify2FA(" code ");
    expect(user.role).toBe("STUDENT");
    expect(fetcher.mock.calls.map(([path]) => path)).toEqual(["/auth/login", "/auth/verify-2fa", "/auth/me"]);
    expect(fetcher.mock.calls[0][1].body.get("password")).toBe("secret&value");
    expect(fetcher.mock.calls[0][1].headers.get("Content-Type")).toContain("application/x-www-form-urlencoded");
    expect(fetcher.mock.calls[0][1].headers.get("X-XSRF-TOKEN")).toBe("before-password");
    expect(fetcher.mock.calls[1][1].headers.get("X-XSRF-TOKEN")).toBe("after-password");
    expect(fetcher.mock.calls[1][1].body.get("token")).toBe("code");
    for (const [, options] of fetcher.mock.calls) expect(options.credentials).toBe("include");
  });
  it("serializes DTOs and handles empty responses", async () => {
    const fetcher = vi.fn().mockImplementation((path) => Promise.resolve(empty()));
    vi.stubGlobal("fetch", fetcher);
    await put("/api/users/u", { name: "Ana", age: 25 });
    await remove("/api/courses/c");
    expect(fetcher.mock.calls[0][1].body).toBe('{"name":"Ana","age":25}');
    expect(fetcher.mock.calls[1][1].method).toBe("DELETE");
  });
  it("does not write if initializing CSRF fails", async () => {
    document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/";
    const fetcher = vi.fn().mockResolvedValue(json({ detail: "Indisponível" }, 503));
    vi.stubGlobal("fetch", fetcher);
    await expect(post("/auth/login", {})).rejects.toThrow("Indisponível");
    expect(fetcher).toHaveBeenCalledTimes(1);
  });
  it("initializes an anonymous cookie and reinitializes after logout", async () => {
    document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/";
    const fetcher = vi.fn(async (path) => {
      if (path === "/auth/me") { document.cookie = "XSRF-TOKEN=new-token; path=/"; return new Response(null, { status: 401 }); }
      if (path === "/auth/logout") document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/";
      return empty();
    });
    vi.stubGlobal("fetch", fetcher);
    await login("a@example.com", "password");
    await post("/auth/logout");
    await login("a@example.com", "password");
    expect(fetcher.mock.calls.map(([path]) => path)).toEqual(["/auth/me", "/auth/login", "/auth/logout", "/auth/me", "/auth/login"]);
    expect(fetcher.mock.calls[1][1].headers.get("X-XSRF-TOKEN")).toBe("new-token");
  });
  it("does not send a write if the CSRF cookie is inaccessible", async () => {
    document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/";
    const fetcher = vi.fn().mockResolvedValue(new Response(null, { status: 401 }));
    vi.stubGlobal("fetch", fetcher);
    await expect(login("a@example.com", "password")).rejects.toThrow("cookie de segurança");
    expect(fetcher).toHaveBeenCalledTimes(1);
  });
  it("preserves ProblemDetail and Retry-After", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValueOnce(json({ detail: "E-mail já cadastrado." }, 409))
      .mockResolvedValueOnce(json({}, 429, { "Retry-After": "60" })));
    await expect(request("/test")).rejects.toMatchObject({ status: 409, message: "E-mail já cadastrado." });
    await expect(request("/test")).rejects.toThrow("60 segundos");
  });
  it("distinguishes an incorrect current password from an expired session on writes", async () => {
    const fetcher = vi.fn().mockImplementation((path) => Promise.resolve(path === "/auth/me" ? json({ id: "u" }) : new Response(null, { status: 401 })));
    vi.stubGlobal("fetch", fetcher);
    await expect(put("/api/users/u", {})).rejects.toMatchObject({ status: 401, name: "ApiError" });
    expect(fetcher.mock.calls.map(([path]) => path)).toEqual(["/api/users/u", "/auth/me"]);
    fetcher.mockImplementation((path) => Promise.resolve(new Response(null, { status: 401 })));
    await expect(put("/api/users/u", {})).rejects.toBeInstanceOf(SessionExpiredError);
  });
  it("does not mask a wrong password as expiration when the session check is unavailable", async () => {
    vi.stubGlobal("fetch", vi.fn().mockImplementation((path) => Promise.resolve(path === "/auth/me" ? json({}, 503) : json({ detail: "Senha atual inválida." }, 401))));
    await expect(put("/api/users/u", {})).rejects.toMatchObject({ message: "Senha atual inválida.", name: "ApiError" });
  });
  it("expires protected reads but does not revalidate a failed login", async () => {
    const fetcher = vi.fn().mockImplementation((path) => Promise.resolve(new Response(null, { status: 401 })));
    vi.stubGlobal("fetch", fetcher);
    await expect(request("/api/courses")).rejects.toBeInstanceOf(SessionExpiredError);
    await expect(login("a@example.com", "wrong")).rejects.toBeInstanceOf(ApiError);
    expect(fetcher.mock.calls.some(([path]) => path === "/auth/me")).toBe(false);
  });
  it("preserves cancellation and rejects successful HTML", async () => {
    const abort = new DOMException("aborted", "AbortError");
    vi.stubGlobal("fetch", vi.fn().mockRejectedValueOnce(abort).mockResolvedValueOnce(new Response("<html>login</html>")));
    await expect(request("/api/courses")).rejects.toBe(abort);
    await expect(request("/api/courses")).rejects.toThrow("resposta inesperada");
  });
});
