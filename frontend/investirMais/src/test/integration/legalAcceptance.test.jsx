import { screen, waitFor } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { HttpResponse } from "msw/http";
import { renderApp } from "../renderApp.jsx";
import { mockApi } from "../helpers.js";
import { legalRelease } from "../fixtures.js";
import { apiClient } from "../../services/api.js";

describe("versões dos documentos e confirmação", () => {
  it("encaminha uma conta pendente, registra a versão explícita e retorna ao destino", async () => {
    let required = true;
    mockApi("get", "/api/legal/status", () => ({ currentVersion: 1, acceptanceRequired: required }));
    const bodies = [];
    mockApi("post", "/api/legal/acceptances", async ({ request }) => {
      bodies.push(await request.json()); required = false;
      return { currentVersion: 1, acceptanceRequired: false };
    });
    const { user, router } = renderApp("/educacao?busca=renda");
    await screen.findByRole("heading", { name: "Confirme os documentos da plataforma" });
    expect(router.state.location.pathname).toBe("/aceitar-documentos");
    expect(screen.getByRole("button", { name: "Confirmar e continuar" })).toBeDisabled();
    await user.click(screen.getByRole("checkbox"));
    await user.click(screen.getByRole("button", { name: "Confirmar e continuar" }));
    await waitFor(() => expect(router.state.location.pathname).toBe("/educacao"));
    expect(router.state.location.search).toBe("?busca=renda");
    expect(bodies).toEqual([{ version: 1, accepted: true }]);
  });

  it("permite gerenciar e excluir a conta mesmo com documentos pendentes", async () => {
    mockApi("get", "/api/legal/status", { currentVersion: 1, acceptanceRequired: true });
    mockApi("get", "/api/legal/acceptances", []);
    const { user } = renderApp("/aceitar-documentos");
    await user.click(await screen.findByRole("link", { name: "Gerenciar ou excluir minha conta" }));
    await screen.findByRole("heading", { name: "Meu perfil" });
    expect(await screen.findByText("Nenhuma confirmação registrada.")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Excluir minha conta" })).toBeInTheDocument();
  });

  it("atualiza o texto e desmarca o aceite quando a versão muda durante a leitura", async () => {
    let release = legalRelease;
    mockApi("get", "/api/legal/current", () => release);
    const accept = mockApi("post", "/api/legal/acceptances", () => {
      release = { ...legalRelease, version: 2 };
      return HttpResponse.json({ detail: "Os documentos foram atualizados. Leia a nova versão antes de confirmar." }, { status: 409 });
    });
    const { user } = renderApp("/aceitar-documentos");
    await user.click(await screen.findByRole("checkbox"));
    await user.click(screen.getByRole("button", { name: "Confirmar e continuar" }));
    await screen.findByText(/Aceito os Termos de Uso.*versão 2/);
    expect(screen.getByRole("checkbox")).not.toBeChecked();
    expect(screen.getByRole("alert")).toHaveTextContent("Os documentos foram atualizados");
    expect(accept).toHaveBeenCalledOnce();
  });

  it("cobra nova confirmação quando uma sessão aberta recebe o bloqueio da API", async () => {
    const { router } = renderApp("/");
    await screen.findByRole("heading", { name: "Olá, Ana." });
    mockApi("get", "/api/courses", HttpResponse.json({ code: "LEGAL_ACCEPTANCE_REQUIRED" }, { status: 428 }));
    await expect(apiClient.get("/api/courses")).rejects.toMatchObject({ response: { status: 428 } });
    await screen.findByRole("heading", { name: "Confirme os documentos da plataforma" });
    expect(router.state.location.pathname).toBe("/aceitar-documentos");
  });

  it("preserva o erro ao falhar e permite sair sem aceitar", async () => {
    mockApi("post", "/api/legal/acceptances", HttpResponse.json({}, { status: 500 }));
    mockApi("post", "/auth/logout", null, { status: 204 });
    const { user, router } = renderApp("/aceitar-documentos");
    await user.click(await screen.findByRole("checkbox"));
    await user.click(screen.getByRole("button", { name: "Confirmar e continuar" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Não foi possível registrar");
    await user.click(screen.getByRole("button", { name: "Sair", exact: true }));
    await waitFor(() => expect(router.state.location.pathname).toBe("/entrar"));
  });

  it("exibe a versão aceita no perfil e abre o conteúdo histórico", async () => {
    const historical = mockApi("get", "/api/legal/versions/1", legalRelease);
    const { user } = renderApp("/perfil");
    await user.click(await screen.findByRole("link", { name: "Versão 1", exact: true }));
    await screen.findByText("Versão 1 · Publicada em 14/11/2026");
    expect(historical).toHaveBeenCalledOnce();
  });
});
