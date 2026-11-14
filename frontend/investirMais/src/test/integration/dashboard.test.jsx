import { screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { adminUser, teacher, teacherStats } from "../fixtures.js";
import { mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";

function setup({ session = teacher, stats = teacherStats, path = "/", browser = false } = {}) {
  const request = mockApi("get", "/api/teacher/dashboard", ({ request }) => {
    const query = new URL(request.url).searchParams;
    return { ...stats, startDate: query.get("startDate") || stats.startDate, endDate: query.get("endDate") || stats.endDate };
  });
  return { ...renderApp(path, { session, browser }), request };
}

describe("métricas educacionais", () => {
  it.each([[teacher, "Painel do Professor"], [adminUser, "Painel de Ensino & Gestão (Admin)"]])("mostra métricas e cursos do perfil %#", async (session, label) => {
    const { user, router } = setup({ session });
    expect(await screen.findByText(label, {}, { timeout: 5000 })).toBeInTheDocument();
    expect(screen.getByRole("region", { name: "Estatísticas do Professor" })).toHaveTextContent("66.7%");
    const row = within(screen.getByRole("row", { name: /Renda fixa/ }));
    expect(row.getByRole("link", { name: "Ver Curso" })).toHaveAttribute("href", "/educacao/" + teacherStats.courses[0].courseId);
    await user.click(screen.getByRole("link", { name: "Gerenciar Cursos" }));
    await waitFor(() => expect(router.state.location.pathname).toBe("/educacao"));
  });

  it.each(["Últimos 30 dias", "Últimos 3 meses", "Últimos 6 meses"])("aplica o período predefinido %s", async (label) => {
    const { user, router } = setup();
    await user.click(await screen.findByRole("button", { name: label }));
    await waitFor(() => expect(router.state.location.search).toContain("startDate="));
    const query = new URLSearchParams(router.state.location.search);
    expect(screen.getByLabelText("De:")).toHaveValue(query.get("startDate"));
    expect(screen.getByLabelText("Até:")).toHaveValue(query.get("endDate"));
    await user.click(await screen.findByRole("button", { name: "Padrão (6 meses)" }));
    await waitFor(() => expect(router.state.location.search).toBe(""));
  });

  it("valida datas antes de consultar e mantém outros parâmetros ao filtrar", async () => {
    const { user, router, request } = setup({ path: "/?contexto=ensino" });
    await screen.findByText("Painel do Professor");
    const initialRequests = request.mock.calls.length;
    await user.clear(screen.getByLabelText("De:"));
    await user.type(screen.getByLabelText("De:"), "2026-12-01{Enter}");
    expect(screen.getByText(/data inicial não pode ser posterior/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Filtrar" })).toBeDisabled();
    await user.clear(screen.getByLabelText("De:"));
    await user.type(screen.getByLabelText("De:"), "2025-01-01{Enter}");
    expect(screen.getByText(/não pode exceder 6 meses/)).toBeInTheDocument();
    await user.click(within(screen.getByLabelText("De:").closest(".ant-picker")).getByRole("button", { name: "Limpar" }));
    expect(screen.getByText("Informe as datas de início e fim.")).toBeInTheDocument();
    expect(request).toHaveBeenCalledTimes(initialRequests);
    await user.clear(screen.getByLabelText("De:"));
    await user.type(screen.getByLabelText("De:"), "2026-10-01{Enter}");
    await user.click(screen.getByRole("button", { name: "Filtrar" }));
    await waitFor(() => expect(router.state.location.search).toContain("startDate=2026-10-01"));
    expect(new URLSearchParams(router.state.location.search).get("contexto")).toBe("ensino");
  });

  it("exibe o primeiro cadastro quando ainda não há cursos", async () => {
    setup({ stats: { ...teacherStats, totalCourses: 0, courses: [] } });
    expect(await screen.findByText("Nenhum curso cadastrado ainda")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Criar Primeiro Curso" })).toHaveAttribute("href", "/educacao");
  });

  it("reconsulta métricas e restaura os campos ao voltar e avançar no histórico do navegador", async () => {
    const { user, router, request } = setup({ path: "/?contexto=ensino", browser: true });
    await user.click(await screen.findByRole("button", { name: "Últimos 30 dias" }));
    await waitFor(() => expect(request).toHaveBeenCalledTimes(2));
    const firstPeriod = router.state.location.search;
    await user.click(screen.getByRole("button", { name: "Últimos 3 meses" }));
    await waitFor(() => expect(request).toHaveBeenCalledTimes(3));
    const secondPeriod = router.state.location.search;
    expect(secondPeriod).not.toBe(firstPeriod);
    await router.navigate(-1);
    await waitFor(() => expect(request).toHaveBeenCalledTimes(4));
    expect(router.state.location.search).toBe(firstPeriod);
    await waitFor(() => expect(screen.getByLabelText("De:")).toHaveValue(new URLSearchParams(firstPeriod).get("startDate")));
    await router.navigate(1);
    await waitFor(() => expect(request).toHaveBeenCalledTimes(5));
    expect(router.state.location.search).toBe(secondPeriod);
    await waitFor(() => expect(screen.getByLabelText("De:")).toHaveValue(new URLSearchParams(secondPeriod).get("startDate")));
    expect(new URLSearchParams(secondPeriod).get("contexto")).toBe("ensino");
  });

  it("recupera falha das métricas voltando ao período padrão", async () => {
    mockApi("get", "/api/teacher/dashboard", { detail: "Intervalo indisponível." }, { status: 503 });
    const { user, router } = renderApp("/?startDate=2020-01-01&contexto=ensino", { session: teacher });
    expect(await screen.findByText("Intervalo indisponível.")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Catálogo de Cursos" })).toBeInTheDocument();
    mockApi("get", "/api/teacher/dashboard", teacherStats);
    await user.click(screen.getByRole("button", { name: /Voltar para o período padrão/ }));
    expect(await screen.findByText("Painel do Professor")).toBeInTheDocument();
    expect(router.state.location.search).toBe("?contexto=ensino");
  });

  it("retorna ao login se a sessão das métricas expirar", async () => {
    mockApi("get", "/api/teacher/dashboard", {}, { status: 401 });
    const { router } = renderApp("/", { session: teacher });
    expect(await screen.findByRole("heading", { name: "Bem-vindo de volta" })).toBeInTheDocument();
    expect(router.state.location.pathname).toBe("/entrar");
  });
});
