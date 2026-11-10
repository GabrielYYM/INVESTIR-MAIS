import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiError } from "../../services/api.js";

describe("mensagens de erro HTTP", () => {
  it.each([
    [{ response: { data: { detail: "Detalhe", message: "Mensagem" } } }, "Detalhe"],
    [{ response: { data: { message: "Mensagem" } } }, "Mensagem"],
    [new Error("Network Error"), "Falha ao carregar."],
    [{ response: { data: { detail: { secret: "não mostrar" } } } }, "Falha ao carregar."],
    [{ response: { data: "Erro interno" } }, "Falha ao carregar."],
    [{ response: { status: 429, headers: { "retry-after": "30" } } }, "Muitas tentativas. Aguarde 30 s e tente novamente."],
    ...[undefined, "0", "-1", "1.5", "Wed, 21 Oct 2026 07:28:00 GMT", "9007199254740992"].map((retry) => [
      { response: { status: 429, headers: { "retry-after": retry } } },
      "Muitas tentativas. Aguarde alguns instantes e tente novamente.",
    ]),
  ])("traduz a resposta sem exibir conteúdo não textual %#", (error, expected) => {
    expect(apiError(error, "Falha ao carregar.")).toBe(expected);
  });
});

describe("cliente HTTP e preparação de CSRF", () => {
  let client;
  let adapter;
  beforeEach(async () => {
    vi.resetModules();
    client = (await import("../../services/api.js")).apiClient;
    adapter = vi.fn(async (config) => ({ data: { token: "fresh-token" }, status: 200, statusText: "OK", headers: {}, config }));
    client.defaults.adapter = adapter;
  });

  it("configura cookies de sessão, timeout e nomes do token do Spring", () => {
    expect(client.defaults).toMatchObject({ withCredentials: true, timeout: 30000, xsrfCookieName: "XSRF-TOKEN", xsrfHeaderName: "X-XSRF-TOKEN" });
    expect(client.defaults.headers.Accept).toBe("application/json");
  });

  it("envia o período informado ao painel e propaga o sinal de cancelamento", async () => {
    const { getTeacherDashboard } = await import("../../services/teacherService.js");
    const { signal } = new AbortController();
    await getTeacherDashboard({ startDate: "2026-10-01", endDate: undefined }, signal);

    const [config] = adapter.mock.calls[0];
    expect(config.signal).toBe(signal);
    expect(client.getUri(config)).toMatch(/\/api\/teacher\/dashboard\?startDate=2026-10-01$/);
  });

  it.each(["get", "head", "options", "trace"])("não solicita CSRF em %s", async (method) => {
    await client.request({ method, url: "/resource" });
    expect(adapter).toHaveBeenCalledTimes(1);
    expect(adapter.mock.calls[0][0].url).toBe("/resource");
  });

  it.each(["post", "put", "patch", "delete"])("prepara CSRF antes de %s", async (method) => {
    await client.request({ method, url: "/resource" });
    expect(adapter.mock.calls.map(([config]) => config.url)).toEqual(["/csrf", "/resource"]);
  });

  it("compartilha a preparação em escritas simultâneas e renova entre operações", async () => {
    await Promise.all([client.post("/first"), client.put("/second")]);
    expect(adapter.mock.calls.filter(([config]) => config.url === "/csrf")).toHaveLength(1);
    await client.post("/third");
    expect(adapter.mock.calls.filter(([config]) => config.url === "/csrf")).toHaveLength(2);
  });

  it("bloqueia a escrita se a preparação falhar e permite uma nova tentativa", async () => {
    adapter.mockRejectedValueOnce(new Error("CSRF indisponível"));
    await expect(client.post("/resource")).rejects.toThrow("CSRF indisponível");
    expect(adapter).toHaveBeenCalledTimes(1);
    await client.post("/resource");
    expect(adapter.mock.calls.map(([config]) => config.url)).toEqual(["/csrf", "/csrf", "/resource"]);
  });

  it("envia o token da resposta em API de outra origem e remove barras finais da base", async () => {
    vi.stubEnv("VITE_API_BASE_URL", "https://api.example.test///");
    vi.resetModules();
    const { apiClient } = await import("../../services/api.js");
    apiClient.defaults.adapter = adapter;
    await apiClient.post("/resource");
    expect(apiClient.defaults.baseURL).toBe("https://api.example.test");
    expect(adapter.mock.calls.at(-1)[0].headers.get("X-XSRF-TOKEN")).toBe("fresh-token");
  });
});
