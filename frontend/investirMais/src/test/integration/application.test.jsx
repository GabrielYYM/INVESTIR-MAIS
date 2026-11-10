import { render, screen, within } from "@testing-library/react";
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

  it.each(["/nao-existe", "/educacao/identificador-invalido", "/cotacao"])("mostra página não encontrada para %s", async (path) => {
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

describe("painel e catálogo", () => {
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
  ])("exibe o estado do catálogo com HTTP %i", async (status, message) => {
    server.use(http.get(origin + "/api/courses", () => HttpResponse.json(paged([]), { status })));
    renderApp();
    expect(await screen.findByText(message)).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Catálogo de Cursos" })).toBeInTheDocument();
  });

});
