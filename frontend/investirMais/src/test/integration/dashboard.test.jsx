import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { adminUser, teacher, teacherStats } from "../fixtures.js";
import { mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";

function setup({ session = teacher, stats = teacherStats, path = "/" } = {}) {
  const request = mockApi("get", "/api/teacher/dashboard", ({ request }) => {
    const query = new URL(request.url).searchParams;
    return { ...stats, startDate: query.get("startDate") || stats.startDate, endDate: query.get("endDate") || stats.endDate };
  });
  return { ...renderApp(path, { session }), request };
}

describe("métricas educacionais", () => {
  it.each([[teacher, "Painel do Professor"], [adminUser, "Painel de Ensino & Gestão (Admin)"]])("mostra métricas e cursos do perfil %#", async (session, label) => {
    const { user, router } = setup({ session });
    expect(await screen.findByText(label)).toBeInTheDocument();
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
    fireEvent.change(screen.getByLabelText("De:"), { target: { value: "2026-12-01" } });
    expect(screen.getByText(/data inicial não pode ser posterior/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Filtrar" })).toBeDisabled();
    fireEvent.change(screen.getByLabelText("De:"), { target: { value: "2025-01-01" } });
    expect(screen.getByText(/não pode exceder 6 meses/)).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText("De:"), { target: { value: "" } });
    expect(screen.getByText("Informe as datas de início e fim.")).toBeInTheDocument();
    expect(request).toHaveBeenCalledTimes(initialRequests);
    fireEvent.change(screen.getByLabelText("De:"), { target: { value: "2026-10-01" } });
    await user.click(screen.getByRole("button", { name: "Filtrar" }));
    await waitFor(() => expect(router.state.location.search).toContain("startDate=2026-10-01"));
    expect(new URLSearchParams(router.state.location.search).get("contexto")).toBe("ensino");
  });

  it("exibe o primeiro cadastro quando ainda não há cursos", async () => {
    setup({ stats: { ...teacherStats, totalCourses: 0, courses: [] } });
    expect(await screen.findByText("Nenhum curso cadastrado ainda")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Criar Primeiro Curso" })).toHaveAttribute("href", "/educacao");
  });

  it("recupera falha das métricas voltando ao período padrão", async () => {
    mockApi("get", "/api/teacher/dashboard", { detail: "Intervalo indisponível." }, { status: 503 });
    const { user, router } = renderApp("/?startDate=2020-01-01", { session: teacher });
    expect(await screen.findByText("Intervalo indisponível.")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Catálogo de Cursos" })).toBeInTheDocument();
    mockApi("get", "/api/teacher/dashboard", teacherStats);
    await user.click(screen.getByRole("button", { name: /Voltar para o período padrão/ }));
    expect(await screen.findByText("Painel do Professor")).toBeInTheDocument();
    expect(router.state.location.search).toBe("");
  });

  it("retorna ao login se a sessão das métricas expirar", async () => {
    mockApi("get", "/api/teacher/dashboard", {}, { status: 401 });
    const { router } = renderApp("/", { session: teacher });
    expect(await screen.findByRole("heading", { name: "Bem-vindo de volta" })).toBeInTheDocument();
    expect(router.state.location.pathname).toBe("/entrar");
  });
});
