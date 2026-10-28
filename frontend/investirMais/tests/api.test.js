import { afterEach, describe, expect, it, vi } from "vitest";
import { ApiError, post, request, SessionExpiredError } from "../src/services/api";
import { login, verify2FA } from "../src/services/authService";

const json = (body, status = 200, headers = {}) => new Response(JSON.stringify(body), { status, headers });
const empty = () => new Response(null, { status: 204 });
const put = (path, body) => request(path, { method: "PUT", body });
const remove = (path) => request(path, { method: "DELETE" });
afterEach(() => vi.unstubAllGlobals());

describe("Spring HTTP contract", () => {
  it("refreshes CSRF between authentication factors and uses form encoding", async () => {
    const fetcher = vi.fn().mockResolvedValueOnce(json({ token: "before-password" })).mockResolvedValueOnce(empty())
      .mockResolvedValueOnce(json({ token: "after-password" })).mockResolvedValueOnce(empty())
      .mockResolvedValueOnce(json({ id: "student", role: "STUDENT" }));
    vi.stubGlobal("fetch", fetcher);
    await login("aluno@example.com", "secret&value");
    const user = await verify2FA(" code ");
    expect(user.role).toBe("STUDENT");
    expect(fetcher.mock.calls.map(([path]) => path)).toEqual(["/csrf", "/auth/login", "/csrf", "/auth/verify-2fa", "/auth/me"]);
    expect(fetcher.mock.calls[1][1].body.get("password")).toBe("secret&value");
    expect(fetcher.mock.calls[1][1].headers.get("Content-Type")).toContain("application/x-www-form-urlencoded");
    expect(fetcher.mock.calls[1][1].headers.get("X-XSRF-TOKEN")).toBe("before-password");
    expect(fetcher.mock.calls[3][1].headers.get("X-XSRF-TOKEN")).toBe("after-password");
    expect(fetcher.mock.calls[3][1].body.get("token")).toBe("code");
    for (const [, options] of fetcher.mock.calls) expect(options.credentials).toBe("include");
  });
  it("serializes DTOs and handles empty responses", async () => {
    const fetcher = vi.fn().mockImplementation((path) => Promise.resolve(path === "/csrf" ? json({ token: "t" }) : empty()));
    vi.stubGlobal("fetch", fetcher);
    await put("/api/users/u", { name: "Ana", age: 25 });
    await remove("/api/courses/c");
    expect(fetcher.mock.calls[1][1].body).toBe('{"name":"Ana","age":25}');
    expect(fetcher.mock.calls[3][1].method).toBe("DELETE");
  });
  it("does not write if fetching CSRF fails", async () => {
    const fetcher = vi.fn().mockResolvedValue(json({ detail: "Indisponível" }, 503));
    vi.stubGlobal("fetch", fetcher);
    await expect(post("/auth/login", {})).rejects.toThrow("Indisponível");
    expect(fetcher).toHaveBeenCalledTimes(1);
  });
  it("preserves ProblemDetail and Retry-After", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValueOnce(json({ detail: "E-mail já cadastrado." }, 409))
      .mockResolvedValueOnce(json({}, 429, { "Retry-After": "60" })));
    await expect(request("/test")).rejects.toMatchObject({ status: 409, message: "E-mail já cadastrado." });
    await expect(request("/test")).rejects.toThrow("60 segundos");
  });
  it("distinguishes an incorrect current password from an expired session on writes", async () => {
    const fetcher = vi.fn().mockImplementation((path) => Promise.resolve(path === "/csrf" ? json({ token: "t" }) : path === "/auth/me" ? json({ id: "u" }) : new Response(null, { status: 401 })));
    vi.stubGlobal("fetch", fetcher);
    await expect(put("/api/users/u", {})).rejects.toMatchObject({ status: 401, name: "ApiError" });
    expect(fetcher.mock.calls.map(([path]) => path)).toEqual(["/csrf", "/api/users/u", "/auth/me"]);
    fetcher.mockImplementation((path) => Promise.resolve(path === "/csrf" ? json({ token: "t" }) : new Response(null, { status: 401 })));
    await expect(put("/api/users/u", {})).rejects.toBeInstanceOf(SessionExpiredError);
  });
  it("does not mask a wrong password as expiration when the session check is unavailable", async () => {
    vi.stubGlobal("fetch", vi.fn().mockImplementation((path) => Promise.resolve(path === "/csrf" ? json({ token: "t" }) : path === "/auth/me" ? json({}, 503) : json({ detail: "Senha atual inválida." }, 401))));
    await expect(put("/api/users/u", {})).rejects.toMatchObject({ message: "Senha atual inválida.", name: "ApiError" });
  });
  it("expires protected reads but does not revalidate a failed login", async () => {
    const fetcher = vi.fn().mockImplementation((path) => Promise.resolve(path === "/csrf" ? json({ token: "t" }) : new Response(null, { status: 401 })));
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
