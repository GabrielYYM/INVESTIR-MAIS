import { render, screen, waitFor, within } from "@testing-library/react";
import { http, HttpResponse } from "msw/http";
import { describe, expect, it, vi } from "vitest";
import { course, paged } from "../fixtures.js";
import { renderApp } from "../renderApp.jsx";
import { origin, server } from "../server.js";

describe("rotas e sessão", () => {
  it("monta a aplicação com o roteador do navegador", async () => {
    window.history.replaceState(null, "", "/entrar");
    const { default: App } = await import("../../App.jsx");
    render(<App />);
    expect(screen.getByRole("heading", { name: "Bem-vindo de volta" })).toBeInTheDocument();
  });

  it.each([401, 403])("preserva o destino solicitado quando a sessão responde %i", async (status) => {
    server.use(
      http.get(origin + "/auth/me", () => HttpResponse.json({}, { status })),
      http.get(origin + "/api/courses", () => HttpResponse.json({}, { status })),
    );
    const { router } = renderApp("/educacao?pagina=2");
    expect(await screen.findByRole("heading", { name: "Bem-vindo de volta" })).toBeInTheDocument();
    expect(router.state.location.pathname).toBe("/entrar");
    expect(new URLSearchParams(router.state.location.search).get("retorno")).toBe("/educacao?pagina=2");
  });

  it("permite tentar novamente após falha no carregamento", async () => {
    server.use(http.get(origin + "/auth/me", () => HttpResponse.json({}, { status: 503 }), { once: true }));
    const { user } = renderApp("/perfil");
    expect(await screen.findByRole("heading", { name: "Não foi possível carregar esta página" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Tentar novamente" }));
    expect(await screen.findByRole("heading", { name: "Meu perfil" })).toBeInTheDocument();
  });

  it.each(["/nao-existe", "/educacao/identificador-invalido"])("mostra página não encontrada para %s", async (path) => {
    renderApp(path);
    expect(await screen.findByRole("heading", { name: "Página não encontrada" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Tentar novamente" })).not.toBeInTheDocument();
  });

  it("navega pelo menu e encerra a sessão no backend", async () => {
    const logout = vi.fn(() => new HttpResponse(null, { status: 204 }));
    server.use(http.post(origin + "/auth/logout", logout));
    const { user, router } = renderApp();
    expect(await screen.findByRole("heading", { name: "Olá, Ana." })).toBeInTheDocument();
    const menu = within(screen.getByRole("navigation", { name: "Menu principal" }));
    expect(menu.getByRole("link", { name: "Dashboard" })).toHaveAttribute("aria-current", "page");
    await user.click(menu.getByRole("link", { name: "Educação" }));
    expect(await screen.findByRole("heading", { name: "Educação financeira" })).toBeInTheDocument();
    await user.click(menu.getByRole("link", { name: "Meu perfil" }));
    expect(await screen.findByRole("heading", { name: "Meu perfil" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Sair da conta" }));
    expect(await screen.findByRole("heading", { name: "Bem-vindo de volta" })).toBeInTheDocument();
    expect(logout).toHaveBeenCalledOnce();
    expect(router.state.location.pathname).toBe("/entrar");
  });

  it("exibe falha ao sair e mantém a área autenticada", async () => {
    server.use(http.post(origin + "/auth/logout", () => HttpResponse.json({}, { status: 503 })));
    const { user } = renderApp("/perfil");
    await user.click(await screen.findByRole("button", { name: "Sair da conta" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Não foi possível encerrar a sessão.");
    expect(screen.getByRole("heading", { name: "Meu perfil" })).toBeInTheDocument();
  });
});

describe("painel e consulta de cotação", () => {
  it("exibe os cursos em destaque com limite e ordenação da API", async () => {
    let query;
    server.use(http.get(origin + "/api/courses", ({ request }) => {
      query = new URL(request.url).searchParams;
      return HttpResponse.json(paged([course]));
    }));
    renderApp();
    expect(await screen.findByRole("heading", { name: "Olá, Ana." })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Renda fixa" })).toBeInTheDocument();
    expect(query.get("size")).toBe("3");
    expect(query.getAll("sort")).toEqual(["name", "id"]);
  });

  it.each([
    [200, "Ainda não há cursos no catálogo."], [503, "O catálogo está indisponível no momento."],
  ])("mantém a consulta de cotação com catálogo HTTP %i", async (status, message) => {
    server.use(http.get(origin + "/api/courses", () => HttpResponse.json(paged([]), { status })));
    renderApp();
    expect(await screen.findByText(message)).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Consultar cotação" })).toBeInTheDocument();
  });

  it.each([
    [{ currency: "BRL", regularMarketChangePercent: 1.25, shortName: "Petrobras" }, /R\$\s*35,25/, "+1,25%"],
    [{ currency: "BRL", regularMarketChangePercent: -2.5, longName: "Petrobras PN" }, /R\$\s*35,25/, "-2,5%"],
    [{ currency: "inválida", regularMarketChangePercent: null, shortName: "Petrobras" }, /35,25/, null],
  ])("consulta e formata o preço e a variação %#", async (fields, price, change) => {
    const quote = vi.fn(({ params }) => {
      expect(params.ticker).toBe("PETR4");
      return HttpResponse.json({ results: [{ symbol: "PETR4", regularMarketPrice: 35.25, ...fields }] });
    });
    server.use(http.get(origin + "/api/assets/:ticker/quote", quote));
    const { user } = renderApp();
    await user.type(await screen.findByRole("textbox", { name: "Ticker" }), "petr4");
    await user.click(screen.getByRole("button", { name: "Buscar" }));
    expect(await screen.findByText(price)).toBeInTheDocument();
    expect(screen.getByText(fields.shortName || fields.longName)).toBeInTheDocument();
    if (change) expect(screen.getByText(change)).toBeInTheDocument();
    else expect(screen.queryByText(/%$/)).not.toBeInTheDocument();
    expect(quote).toHaveBeenCalledOnce();
  });

  it.each([
    [404, {}, "Ativo não encontrado."], [503, {}, "A cotação está indisponível"],
    [200, { results: [] }, "Nenhuma cotação disponível para PETR4."],
  ])("mostra a falha da consulta HTTP %i sem perder o painel", async (status, body, message) => {
    server.use(http.get(origin + "/api/assets/:ticker/quote", () => HttpResponse.json(body, { status })));
    const { user } = renderApp();
    await user.type(await screen.findByRole("textbox", { name: "Ticker" }), "PETR4");
    await user.click(screen.getByRole("button", { name: "Buscar" }));
    expect(await screen.findByRole("alert")).toHaveTextContent(message);
    expect(screen.getByRole("heading", { name: "Olá, Ana." })).toBeInTheDocument();
  });

  it("rejeita ticker inválido antes da chamada à API", async () => {
    const quote = vi.fn();
    server.use(http.get(origin + "/api/assets/:ticker/quote", quote));
    const { user } = renderApp();
    await user.type(await screen.findByRole("textbox", { name: "Ticker" }), "<PETR4>");
    await user.click(screen.getByRole("button", { name: "Buscar" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Informe um ticker válido.");
    expect(quote).not.toHaveBeenCalled();
  });

  it("redireciona ao login se a sessão expirar durante a consulta", async () => {
    server.use(http.get(origin + "/api/assets/:ticker/quote", () => HttpResponse.json({}, { status: 401 })));
    const { user, router } = renderApp();
    await user.type(await screen.findByRole("textbox", { name: "Ticker" }), "PETR4");
    await user.click(screen.getByRole("button", { name: "Buscar" }));
    await waitFor(() => expect(router.state.location.pathname).toBe("/entrar"));
    expect(await screen.findByRole("heading", { name: "Bem-vindo de volta" })).toBeInTheDocument();
  });
});
