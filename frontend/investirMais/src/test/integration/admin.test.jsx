import { screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { adminUser, student, teacherUser } from "../fixtures.js";
import { mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";

function setup(users = [adminUser, student, teacherUser]) {
  mockApi("get", "/api/users", () => users);
  return renderApp("/admin", { session: adminUser });
}
async function userRow(email = student.email) {
  return within((await screen.findByText(email)).closest("tr"));
}

describe("gestão de usuários", () => {
  it("filtra usuários por nome, e-mail e perfil e protege administradores", async () => {
    const { user } = setup();
    const row = await userRow(adminUser.email);
    expect(row.getByRole("button", { name: "Alterar Papel" })).toBeDisabled();
    expect(row.getByText("Você")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Professores", exact: true }));
    expect(screen.queryByText(student.email)).not.toBeInTheDocument();
    expect(screen.getByText(teacherUser.email)).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Todos", exact: true }));
    const search = screen.getByPlaceholderText("Buscar por nome ou e-mail...");
    await user.type(search, "ANA@"); expect(screen.getByText(student.email)).toBeInTheDocument();
    expect(screen.queryByText(teacherUser.email)).not.toBeInTheDocument();
    await user.clear(search); await user.type(search, "inexistente");
    expect(screen.getByText(/Nenhum usuário encontrado/)).toBeInTheDocument();
    await user.clear(search); await user.click(screen.getByRole("button", { name: "Alunos", exact: true }));
    expect(screen.getByText(student.email)).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Admins", exact: true }));
    expect(screen.getByText(adminUser.email)).toBeInTheDocument();
  });

  it("altera o papel, envia apenas o novo perfil e revalida a listagem", async () => {
    let users = [adminUser, student, teacherUser];
    mockApi("get", "/api/users", () => users);
    const update = mockApi("patch", `/api/users/${student.id}/role`, async ({ request }) => {
      const body = await request.json(); expect(body).toEqual({ role: "TEACHER" });
      const updated = { ...student, role: body.role }; users = users.map((u) => u.id === student.id ? updated : u); return updated;
    });
    const { user } = renderApp("/admin", { session: adminUser });
    await user.click((await userRow()).getByRole("button", { name: "Alterar Papel" }));
    const dialog = within(screen.getByRole("dialog"));
    expect(dialog.getByRole("button", { name: "Salvar Alteração" })).toBeDisabled();
    await user.selectOptions(dialog.getByLabelText("Novo Papel"), "TEACHER");
    await user.click(dialog.getByRole("button", { name: "Salvar Alteração" }));
    expect(await screen.findByRole("status")).toHaveTextContent(/alterado com sucesso para Professor/);
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    expect((await userRow()).getByText("Professor")).toBeInTheDocument(); expect(update).toHaveBeenCalledOnce();
  });

  it("preserva o diálogo e a escolha quando a atualização falha", async () => {
    mockApi("patch", `/api/users/${teacherUser.id}/role`, { detail: "Perfil indisponível." }, { status: 409 });
    const { user } = setup();
    await user.click((await userRow(teacherUser.email)).getByRole("button", { name: "Alterar Papel" }));
    await user.selectOptions(screen.getByLabelText("Novo Papel"), "STUDENT");
    await user.click(screen.getByRole("button", { name: "Salvar Alteração" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Perfil indisponível");
    expect(screen.getByLabelText("Novo Papel")).toHaveValue("STUDENT");
    await user.click(screen.getByRole("button", { name: "Cancelar", exact: true }));
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    await user.click((await userRow()).getByRole("button", { name: "Alterar Papel" }));
    await user.keyboard("{Escape}");
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it.each([{ users: [] }, { users: [student] }, { users: [{ ...student, role: "CUSTOM", age: null, createdAt: null }] }])("mostra bases vazias e dados incompletos %#", async ({ users }) => {
    setup(users); await screen.findByRole("heading", { name: "Usuários Cadastrados" });
    if (!users.length) expect(screen.getByText(/Nenhum usuário encontrado/)).toBeInTheDocument();
    else expect(screen.getByText(student.email)).toBeInTheDocument();
  });

  it.each([401, 403])("redireciona ao perder autorização (%i)", async (status) => {
    mockApi("get", "/api/users", {}, { status });
    const { router } = renderApp("/admin", { session: adminUser });
    await waitFor(() => expect(router.state.location.pathname).toBe("/"));
  });

  it("impede aluno de consultar a administração", async () => {
    const request = mockApi("get", "/api/users", []);
    const { router } = renderApp("/admin", { session: student });
    await waitFor(() => expect(router.state.location.pathname).toBe("/")); expect(request).not.toHaveBeenCalled();
  });

  it("pagina a base de usuários e encontra um aluno fora da primeira página", async () => {
    const students = Array.from({ length: 12 }, (_, index) => ({
      ...student, id: `aluno-${index}`, name: `Aluno ${index}`, email: `aluno${index}@example.test`,
    }));
    const { user } = setup([adminUser, ...students]);
    await screen.findByText(students[0].email);
    expect(screen.queryByText(students[11].email)).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Página 2", exact: true }));
    expect(await screen.findByText(students[11].email)).toBeInTheDocument();
    await user.type(screen.getByPlaceholderText("Buscar por nome ou e-mail..."), students[11].email);
    expect(screen.getByText(students[11].email)).toBeInTheDocument();
    expect(screen.queryByText(students[10].email)).not.toBeInTheDocument();
  });
});

describe("histórico de auditoria", () => {
  it("consulta novamente ao reabrir e separa o histórico de cada usuário", async () => {
    let name = "Snapshot anterior";
    const audit = mockApi("get", `/api/users/${student.id}/audit`, () => [
      { revisionNumber: 1, operation: "UPDATE", user: { ...student, name } },
    ]);
    mockApi("get", `/api/users/${teacherUser.id}/audit`, [
      { revisionNumber: 2, operation: "INSERT", user: { ...teacherUser, name: "Snapshot do professor" } },
    ]);
    const { user } = setup();
    await user.click((await userRow()).getByRole("button", { name: "Auditoria" }));
    expect(await screen.findByText("Snapshot anterior")).toBeInTheDocument();
    await user.click(within(screen.getByRole("dialog")).getAllByRole("button", { name: "Fechar" })[0]);
    name = "Snapshot atualizado";
    await user.click((await userRow()).getByRole("button", { name: "Auditoria" }));
    expect(await screen.findByText("Snapshot atualizado")).toBeInTheDocument();
    expect(audit).toHaveBeenCalledTimes(2);
    await user.click(within(screen.getByRole("dialog")).getAllByRole("button", { name: "Fechar" })[0]);
    await user.click((await userRow(teacherUser.email)).getByRole("button", { name: "Auditoria" }));
    expect(await screen.findByText("Snapshot do professor")).toBeInTheDocument();
    expect(screen.queryByText("Snapshot atualizado")).not.toBeInTheDocument();
  });

  it("cancela a consulta pendente ao fechar o histórico", async () => {
    let signal;
    let release;
    const response = new Promise((resolve) => { release = resolve; });
    const audit = mockApi("get", `/api/users/${student.id}/audit`, ({ request }) => {
      signal = request.signal;
      return response;
    });
    try {
      const { user } = setup();
      await user.click((await userRow()).getByRole("button", { name: "Auditoria" }));
      await waitFor(() => expect(audit).toHaveBeenCalledOnce());
      await user.click(within(screen.getByRole("dialog")).getAllByRole("button", { name: "Fechar" })[0]);
      await waitFor(() => expect(signal.aborted).toBe(true));
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    } finally {
      release([]);
    }
  });

  it.each([
    [[], 200, "Nenhum registro de auditoria encontrado"],
    [{ detail: "Auditoria indisponível." }, 503, "Auditoria indisponível."],
    [[{ revisionNumber: 1, timestamp: "2026-11-13T12:00:00Z", operation: "INSERT", user: student },
      { revisionNumber: 2, timestamp: null, operation: "UPDATE", user: teacherUser },
      { revisionNumber: 3, timestamp: null, operation: "DELETE", user: null },
      { revisionNumber: 4, timestamp: null, operation: "CUSTOM", user: { name: "Legado", role: "CUSTOM" } }], 200, "Criação (INSERT)"],
  ])("consulta o histórico e trata a resposta %#", async (response, status, message) => {
    const audit = mockApi("get", `/api/users/${student.id}/audit`, response, { status });
    const { user } = setup(); await user.click((await userRow()).getByRole("button", { name: "Auditoria" }));
    const dialog = within(screen.getByRole("dialog"));
    expect(await dialog.findByText(message)).toBeInTheDocument(); expect(audit).toHaveBeenCalledOnce();
    await user.click(dialog.getAllByRole("button", { name: "Fechar" })[0]);
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });
});
