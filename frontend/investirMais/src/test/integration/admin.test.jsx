import { screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { adminUser, course, student, teacherUser } from "../fixtures.js";
import { mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";

function setup(users = [adminUser, student, teacherUser]) {
  mockApi("get", "/api/users", () => users);
  return renderApp("/admin", { session: adminUser });
}
async function userRow(uid = student.id) {
  const section = within(await screen.findByRole("region", { name: "Usuários Cadastrados" }));
  return within((await section.findByText(uid)).closest("tr"));
}

describe("gestão de usuários", () => {
  it("explica que a promoção depende da aprovação dos documentos", async () => {
    const update = mockApi("patch", `/api/users/${student.id}/role`, {});
    const { user } = setup([student]);
    await user.click((await userRow()).getByRole("button", { name: "Alterar Papel" }));
    expect(screen.getByText("A promoção a professor ocorre pela aprovação dos documentos em Candidaturas de professores.")).toBeInTheDocument();
    await user.click(screen.getByRole("combobox", { name: "Novo Papel" }));
    expect(await screen.findByRole("option", { name: "Professor" })).toHaveAttribute("aria-disabled", "true");
    expect(screen.getByRole("option", { name: "Aluno" })).not.toHaveAttribute("aria-disabled", "true");
    expect(update).not.toHaveBeenCalled();
  });

  it("filtra usuários por apelido, e-mail e perfil, oculta e-mail na tabela e protege administradores", async () => {
    const { user } = setup();
    const row = await userRow(adminUser.id);
    expect(row.getByRole("button", { name: "Alterar Papel" })).toBeDisabled();
    expect(row.getByText("Você")).toBeInTheDocument();
    expect(screen.queryByText(adminUser.email)).not.toBeInTheDocument();
    expect(screen.queryByText(student.email)).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Professores", exact: true }));
    await waitFor(() => expect(screen.queryByText(student.id)).not.toBeInTheDocument());
    expect(await screen.findByText(teacherUser.id)).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Todos", exact: true }));
    const search = screen.getByPlaceholderText(/Buscar por apelido/);
    await user.type(search, "ANA@"); expect(await screen.findByText(student.id)).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByText(teacherUser.id)).not.toBeInTheDocument());
    await user.clear(search); await user.type(search, "inexistente");
    expect(await screen.findByText(/Nenhum usuário encontrado/)).toBeInTheDocument();
    await user.clear(search); await user.click(screen.getByRole("button", { name: "Alunos", exact: true }));
    expect(await screen.findByText(student.id)).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Admins", exact: true }));

    await waitFor(() => {
      expect(screen.getByRole("button", { name: "Admins", exact: true })).toHaveAttribute("aria-pressed", "true");
      expect(screen.getByText(adminUser.id)).toBeInTheDocument();
      expect(screen.queryByText(student.id)).not.toBeInTheDocument();
      expect(screen.queryByText(teacherUser.id)).not.toBeInTheDocument();
    });
  });

  it("mantém a busca ao trocar de perfil e limpa o texto sem perder o perfil selecionado", async () => {
    const { user } = setup();
    await userRow();
    const search = screen.getByPlaceholderText(/Buscar por apelido/);
    await user.type(search, "ANA@");
    await waitFor(() => expect(screen.queryByText(adminUser.id)).not.toBeInTheDocument());
    await user.click(screen.getByRole("button", { name: "Admins", exact: true }));
    expect(await screen.findByText(/Nenhum usuário encontrado/)).toBeInTheDocument();
    expect(search).toHaveValue("ANA@");

    await user.clear(search);
    expect(await screen.findByText(adminUser.id)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Admins", exact: true })).toHaveAttribute("aria-pressed", "true");
    expect(screen.queryByText(student.id)).not.toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Todos", exact: true }));
    expect(await screen.findByText(student.id)).toBeInTheDocument();
    expect(await screen.findByText(teacherUser.id)).toBeInTheDocument();
    expect(screen.getByText(adminUser.id)).toBeInTheDocument();
    expect(search).toHaveValue("");
  });

  it("altera o professor para aluno e revalida a listagem", async () => {
    let users = [adminUser, student, teacherUser];
    mockApi("get", "/api/users", () => users);
    const update = mockApi("patch", `/api/users/${teacherUser.id}/role`, async ({ request }) => {
      const body = await request.json(); expect(body).toEqual({ role: "STUDENT" });
      const updated = { ...teacherUser, role: body.role }; users = users.map(u => u.id === teacherUser.id ? updated : u); return updated;
    });
    const { user } = renderApp("/admin", { session: adminUser });
    await user.click((await userRow(teacherUser.id)).getByRole("button", { name: "Alterar Papel" }));
    await user.click(screen.getByRole("combobox", { name: "Novo Papel" }));
    await user.click(await screen.findByRole("option", { name: "Aluno" }));
    await user.click(screen.getByRole("button", { name: "Salvar Alteração" }));
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    expect((await userRow(teacherUser.id)).getByText("Aluno")).toBeInTheDocument(); expect(update).toHaveBeenCalledOnce();
  });

  it("preserva o diálogo e a escolha quando a atualização falha", async () => {
    mockApi("patch", `/api/users/${teacherUser.id}/role`, { detail: "Perfil indisponível." }, { status: 409 });
    const { user } = setup();
    await user.click((await userRow(teacherUser.id)).getByRole("button", { name: "Alterar Papel" }));
    await user.click(screen.getByRole("combobox", { name: "Novo Papel" }));
    await user.click(await screen.findByRole("option", { name: "Aluno" }));
    await user.click(screen.getByRole("button", { name: "Salvar Alteração" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Perfil indisponível");
    expect(screen.getByRole("combobox", { name: "Novo Papel" })).toHaveTextContent("Aluno");
    await user.click(screen.getByRole("button", { name: "Cancelar", exact: true }));
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    await user.click((await userRow()).getByRole("button", { name: "Alterar Papel" }));
    await user.keyboard("{Escape}");
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
  });

  it.each([{ users: [] }, { users: [student] }, { users: [{ ...student, role: "CUSTOM", createdAt: null }] }])("mostra bases vazias e dados incompletos %#", async ({ users }) => {
    setup(users); await screen.findByRole("heading", { name: "Usuários Cadastrados" });
    if (!users.length) expect(screen.getByText(/Nenhum usuário encontrado/)).toBeInTheDocument();
    else expect(screen.getByText(student.id)).toBeInTheDocument();
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
    const usersSection = within(await screen.findByRole("region", { name: "Usuários Cadastrados" }));
    await usersSection.findByText(students[0].id);
    expect(usersSection.queryByText(students[11].id)).not.toBeInTheDocument();
    await user.click(usersSection.getByRole("button", { name: /página 2/i }));
    expect(await usersSection.findByText(students[11].id)).toBeInTheDocument();
    await user.type(screen.getByPlaceholderText(/Buscar por apelido/), students[11].email);
    expect(usersSection.getByText(students[11].id)).toBeInTheDocument();
    await waitFor(() => expect(usersSection.queryByText(students[10].id)).not.toBeInTheDocument());
  });
});

describe("histórico de auditoria de ocorridos recentes", () => {
  it("mostra os logs de visualização juntos aos outros logs na tabela de auditoria", async () => {
    mockApi("get", "/api/audit/accesses", {
      items: [{
        id: "access-1",
        viewer_id: adminUser.id,
        viewer_name: adminUser.name,
        viewer_email: adminUser.email,
        viewer_role: "ADMIN",
        viewed_at: "2026-11-22T12:00:00Z",
        action: "DETAIL",
        record_references: "users:registro:7",
      }],
      totalItems: 1,
    });
    const { user } = setup();
    const auditSection = within(await screen.findByRole("region", { name: "Auditoria de Ocorridos Recentes" }));
    expect(screen.queryByRole("button", { name: "Ver logs de auditoria" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Ver log de visualizações" })).not.toBeInTheDocument();
    expect((await auditSection.findAllByText("Visualização")).length).toBeGreaterThan(0);
    expect(auditSection.getAllByText(adminUser.name).length).toBeGreaterThan(0);
    expect(auditSection.getAllByText(adminUser.id).length).toBeGreaterThan(0);

    const eyeButton = auditSection.getByRole("button", { name: "Ver detalhes da auditoria" });
    await user.click(eyeButton);

    const dialog = within(await screen.findByRole("dialog"));
    expect(dialog.getByText("users:registro:7")).toBeInTheDocument();
    expect(dialog.getByText(adminUser.email)).toBeInTheDocument();
  });

  it("pagina mais de cem ocorrências recentes na tabela principal sem misturar parâmetros da lista de usuários", async () => {
    mockApi("get", "/api/audit", {
      items: Array.from({ length: 103 }, (_, index) => ({
        entity: "users",
        id: student.id,
        revision: 103 - index,
        operation: 1,
        timestamp: 1763035200000,
        actor: adminUser.email,
        role: "ADMIN",
        data: JSON.stringify({ ...student, name: `Ocorrência ${103 - index}` }),
      })),
      totalItems: 103,
    });
    const { user, router } = setup();
    const auditSection = within(await screen.findByRole("region", { name: "Auditoria de Ocorridos Recentes" }));
    expect(await auditSection.findByText("Ocorrência 103")).toBeInTheDocument();
    expect(auditSection.getByText("Ocorrência 84")).toBeInTheDocument();
    await user.click(auditSection.getByRole("button", { name: /página 6/i }));
    expect(await auditSection.findByText("Ocorrência 1", { exact: true })).toBeInTheDocument();
    expect(router.state.location.search).toBe("");
  });

  it("exibe somente nome e UID na tabela de auditoria (sem e-mail) e abre todos os dados em modal pelo ícone de olho", async () => {
    mockApi("get", "/api/audit", {
      items: [
        {
          entity: "users",
          id: student.id,
          revision: 7,
          operation: 1,
          timestamp: "2026-11-13T12:00:00Z",
          actor: adminUser.email,
          role: "ADMIN",
          data: JSON.stringify({ ...student, name: "Ana Atualizada", role: "TEACHER" }),
        },
        {
          entity: "users",
          id: student.id,
          revision: 5,
          operation: 0,
          timestamp: "2026-11-12T12:00:00Z",
          actor: student.email,
          role: "STUDENT",
          data: JSON.stringify({ ...student, name: "Ana Antes", role: "STUDENT" }),
        },
        {
          entity: "courses",
          id: course.id,
          revision: 6,
          operation: 0,
          timestamp: 1763035200000,
          actor: teacherUser.email,
          role: "TEACHER",
          data: JSON.stringify(course),
        },
      ],
      totalItems: 3,
    });
    mockApi("get", `/api/audit/users/${student.id}/7`, { entity: "users", id: student.id, revision: 7, operation: 1, timestamp: "2026-11-13T12:00:00Z", actor: adminUser.email, role: "ADMIN", data: JSON.stringify({ ...student, name: "Ana Atualizada", role: "TEACHER" }) });
    mockApi("get", `/api/audit/users/${student.id}/5`, { entity: "users", id: student.id, revision: 5, operation: 0, data: JSON.stringify({ ...student, name: "Ana Antes", role: "STUDENT" }) });
    const { user } = setup();
    const auditSection = within(await screen.findByRole("region", { name: "Auditoria de Ocorridos Recentes" }));
    expect(await auditSection.findByText("Ana Atualizada")).toBeInTheDocument();
    expect(auditSection.getAllByText(student.id).length).toBeGreaterThan(0);
    expect(auditSection.getByText(course.name)).toBeInTheDocument();
    expect(auditSection.getByText(course.id)).toBeInTheDocument();
    expect(auditSection.queryByText(student.email)).not.toBeInTheDocument();
    expect(auditSection.queryByText(adminUser.email)).not.toBeInTheDocument();
    expect(auditSection.queryByText(teacherUser.email)).not.toBeInTheDocument();

    const eyeButtons = auditSection.getAllByRole("button", { name: "Ver detalhes da auditoria" });
    await user.click(eyeButtons[0]);

    const dialog = within(await screen.findByRole("dialog"));
    expect(dialog.getByText("Detalhes da Auditoria")).toBeInTheDocument();
    expect(dialog.queryByText(/Hibernate Envers/)).not.toBeInTheDocument();
    expect(dialog.getByRole("heading", { name: "Antes" })).toBeInTheDocument();
    expect(dialog.getByRole("heading", { name: "Depois" })).toBeInTheDocument();
    expect(dialog.getByText("7")).toBeInTheDocument();
    expect(dialog.getByText("Ana Atualizada")).toBeInTheDocument();
    expect(dialog.getByText(/Ana Antes/)).toBeInTheDocument();
    expect(dialog.getByText(student.id)).toBeInTheDocument();
    expect(dialog.getByText(student.email)).toBeInTheDocument();
    expect(dialog.getAllByText(adminUser.name).length).toBeGreaterThan(1);
    expect(dialog.getByText(adminUser.id)).toBeInTheDocument();
    expect(dialog.getByText(adminUser.email)).toBeInTheDocument();

    await user.click(dialog.getAllByRole("button", { name: "Fechar" })[0]);
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

    await user.click(eyeButtons[1]);
    const insertDialog = within(await screen.findByRole("dialog"));
    expect(insertDialog.queryByRole("heading", { name: "Antes" })).not.toBeInTheDocument();
    expect(insertDialog.queryByRole("heading", { name: "Depois" })).not.toBeInTheDocument();
    expect(insertDialog.getByRole("heading", { name: "Dados da Auditoria (Snapshot)" })).toBeInTheDocument();
    await user.click(insertDialog.getAllByRole("button", { name: "Fechar" })[0]);
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("filtra a tabela de auditoria usando os parâmetros do backend e permite limpar os filtros", async () => {
    const audit = mockApi("get", "/api/audit", ({ request }) => {
      const params = new URL(request.url).searchParams;
      if (params.get("entity") === "courses") {
        expect(params.get("entityId")).toBe(course.id);
        expect(params.get("actor")).toBe(adminUser.email);
        expect(params.get("role")).toBe("ADMIN");
        return {
          items: [{ entity: "courses", id: course.id, revision: 15, operation: 0, timestamp: 1763035200000, actor: adminUser.email, role: "ADMIN", data: JSON.stringify(course) }],
          totalItems: 1,
        };
      }
      return { items: [], totalItems: 0 };
    });
    const { user } = setup();
    const auditSection = within(await screen.findByRole("region", { name: "Auditoria de Ocorridos Recentes" }));
    await user.click(auditSection.getByRole("combobox", { name: "Entidade" }));
    await user.click(await screen.findByRole("option", { name: "Cursos" }));
    await user.type(auditSection.getByLabelText("ID do registro"), course.id);
    await user.type(auditSection.getByLabelText("Usuário autor (e-mail)"), adminUser.email);
    await user.click(auditSection.getByRole("combobox", { name: "Papel do autor" }));
    await user.click(await screen.findByRole("option", { name: "Administrador" }));
    await user.click(auditSection.getByRole("button", { name: "Filtrar" }));
    expect(await auditSection.findByText(course.name)).toBeInTheDocument();

    await user.click(auditSection.getByRole("button", { name: "Limpar" }));
    expect(await auditSection.findByText("Nenhum registro de auditoria encontrado")).toBeInTheDocument();
    expect(audit).toHaveBeenCalledTimes(3);
  });

  it.each([
    [{ items: [], totalItems: 0 }, 200, "Nenhum registro de auditoria encontrado"],
    [{ detail: "Auditoria indisponível." }, 503, "Auditoria indisponível."],
    [{
      items: [
        { entity: "users", id: student.id, revision: 1, timestamp: "2026-11-13T12:00:00Z", operation: "INSERT", data: JSON.stringify(student) },
        { entity: "users", id: teacherUser.id, revision: 2, timestamp: null, operation: "UPDATE", data: JSON.stringify(teacherUser) },
        { entity: "users", id: student.id, revision: 3, timestamp: null, operation: "DELETE", data: null },
        { entity: "users", id: "legado-id", revision: 4, timestamp: null, operation: "CUSTOM", data: JSON.stringify({ name: "Legado", role: "CUSTOM" }) },
      ],
      totalItems: 4,
    }, 200, "Criação"],
  ])("consulta os ocorridos recentes e trata a resposta %#", async (response, status, message) => {
    const audit = mockApi("get", "/api/audit", response, { status });
    setup();
    const auditSection = within(await screen.findByRole("region", { name: "Auditoria de Ocorridos Recentes" }));
    expect(await auditSection.findByText(message)).toBeInTheDocument();
    expect(audit).toHaveBeenCalledOnce();
  });
});
