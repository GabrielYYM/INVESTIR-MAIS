import { screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { adminPending, adminUser, student, teacherUser } from "../fixtures.js";
import { mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";

function setup() {
  mockApi("get", "/api/users", [adminUser, student]);
  return renderApp("/admin", { session: adminUser });
}
const summary = { ...adminPending, openReports: 3, pendingTeacherApplications: 2, recoverableAccounts: 5, expiringAccounts: 1, expiredAccounts: 4, nextRecoveryDeadline: "2026-11-20T12:00:00Z" };

describe("painel de pendências do admin", () => {
  it("centraliza denúncias e candidaturas sem recuperação de contas", async () => {
    mockApi("get", "/api/admin/pending", summary);
    setup();
    const panel = within(await screen.findByRole("region", { name: "Pendências da administração" }));
    await panel.findByText("Denúncias em análise");
    for (const [label, count] of [["Denúncias em análise", "3"], ["Candidaturas pendentes", "2"]]) {
      expect(within(panel.getByText(label).closest(".panel")).getByText(count, { exact: true })).toBeInTheDocument();
    }
    expect(panel.queryByText(/Próximo fim de prazo/)).not.toBeInTheDocument();
    expect(panel.getByRole("link", { name: "Analisar denúncias" })).toHaveAttribute("href", "/admin/denuncias");
    expect(panel.getByRole("link", { name: "Analisar candidaturas" })).toHaveAttribute("href", "/admin/professores");
    expect(panel.queryByRole("link", { name: "Ver contas em recuperação" })).not.toBeInTheDocument();
  });

  it("mostra ausência de pendências sem alerta nem prazo fictício", async () => {
    setup();
    expect(await screen.findByText("Nenhuma pendência no momento.")).toBeInTheDocument();
    expect(screen.queryByText(/Próximo fim de prazo/)).not.toBeInTheDocument();
    expect(screen.queryByText(/perdem a possibilidade/)).not.toBeInTheDocument();
  });

  it("exibe erro das pendências sem impedir a gestão dos usuários", async () => {
    const query = mockApi("get", "/api/admin/pending", { detail: "Resumo temporariamente indisponível." }, { status: 503 });
    setup();
    expect(await screen.findByRole("alert")).toHaveTextContent("Resumo temporariamente indisponível.");
    expect(await screen.findByText(student.id)).toBeInTheDocument();
    expect(screen.queryByText(student.email)).not.toBeInTheDocument();
    expect(screen.queryByText("Denúncias em análise")).not.toBeInTheDocument();
    expect(query).toHaveBeenCalledOnce();
  });

  it.each([student, teacherUser])("impede consulta por perfil não administrativo: $role", async session => {
    const query = mockApi("get", "/api/admin/pending", summary);
    const { router } = renderApp("/admin", { session });
    await waitFor(() => expect(router.state.location.pathname).toBe("/"));
    expect(query).not.toHaveBeenCalled();
  });
});
