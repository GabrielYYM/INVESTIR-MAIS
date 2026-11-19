import { screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { mockApi, fillFields } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";
import { adminUser, course, lesson, paged, student } from "../fixtures.js";

describe("denúncias e recuperação", () => {
  it.each([["vídeo", "LESSON", lesson.id], ["professor", "TEACHER", course.professorId]])("envia denúncia de %s com motivo e detalhes", async (label, targetType, targetId) => {
    const send = mockApi("post", "/api/reports", async ({ request }) => {
      expect(await request.json()).toEqual({ targetType, targetId, courseId: course.id, reason: "OFF_TOPIC", description: "O assunto não tem relação com investimentos." });
      return { id: "protocolo-teste", status: "OPEN" };
    }, { status: 201 });
    const { user } = renderApp("/educacao/" + course.id);
    await user.click(await screen.findByRole("button", { name: "Denunciar " + label }));
    const dialog = within(screen.getByRole("dialog"));
    expect(dialog.getByRole("button", { name: "Enviar denúncia" })).toBeDisabled();
    await user.selectOptions(dialog.getByLabelText("Motivo"), "OFF_TOPIC");
    await user.type(dialog.getByLabelText("Descreva o problema"), "O assunto não tem relação com investimentos.");
    await user.click(dialog.getByRole("button", { name: "Enviar denúncia" }));
    expect(await dialog.findByRole("status")).toHaveTextContent("Denúncia enviada");
    expect(send).toHaveBeenCalledOnce();
  });

  it("exibe erro de denúncia duplicada sem perder a descrição", async () => {
    mockApi("post", "/api/reports", { detail: "Você já tem uma denúncia em análise para este item." }, { status: 409 });
    const { user } = renderApp("/educacao/" + course.id);
    await user.click(await screen.findByRole("button", { name: "Denunciar vídeo" }));
    await user.type(screen.getByLabelText("Descreva o problema"), "Conteúdo impróprio");
    await user.click(screen.getByRole("button", { name: "Enviar denúncia" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("denúncia em análise");
    expect(screen.getByLabelText("Descreva o problema")).toHaveValue("Conteúdo impróprio");
  });

  it("exige parecer e confirmação para retirar uma aula denunciada", async () => {
    const report = { id: "report-1", targetType: "LESSON", targetId: lesson.id, courseId: course.id, targetLabel: lesson.title, reason: "INAPPROPRIATE", description: "Conteúdo impróprio", status: "OPEN", createdAt: "2026-11-17T12:00:00Z" };
    mockApi("get", "/api/reports", paged([report]));
    const review = mockApi("post", "/api/reports/report-1/review", async ({ request }) => {
      expect(await request.json()).toEqual({ status: "RESOLVED", note: "Vídeo conferido e removido", removeLesson: true }); return { ...report, status: "RESOLVED" };
    });
    const { user } = renderApp("/admin/denuncias", { session: adminUser });
    expect(await screen.findByRole("button", { name: "Remover vídeo e resolver" })).toBeDisabled();
    await user.type(screen.getByLabelText("Parecer da denúncia"), "Vídeo conferido e removido");
    await user.click(screen.getByRole("button", { name: "Remover vídeo e resolver" }));
    expect(review).not.toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "Confirmar remoção da aula" }));
    await waitFor(() => expect(review).toHaveBeenCalledOnce());
  });

  it("restaura conteúdo somente após confirmar", async () => {
    let items = [{ entity: "lessons", id: lesson.id, title: lesson.title, deleted_at: "2026-11-17T12:00:00Z" }];
    mockApi("get", "/api/trash", () => ({ items, totalItems: items.length }));
    const restore = mockApi("post", `/api/trash/lessons/${lesson.id}/restore`, () => { items = []; return null; }, { status: 204 });
    const { user } = renderApp("/admin/lixeira", { session: adminUser });
    await user.click(await screen.findByRole("button", { name: "Restaurar conteúdo" }));
    expect(restore).not.toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "Confirmar restauração" }));
    expect(await screen.findByText("Nenhum item na lixeira.")).toBeInTheDocument();
  });

  it("exige solicitação verificada antes de recuperar uma conta", async () => {
    mockApi("get", "/api/trash", { items: [], totalItems: 0 });
    mockApi("get", "/api/trash/accounts", { items: [{ ...student, deleted_at: "2026-11-17T12:00:00Z", purge_after: "2099-12-17T12:00:00Z" }], totalItems: 1 });
    const restore = mockApi("post", `/api/trash/accounts/${student.id}/restore`, async ({ request }) => {
      expect(await request.json()).toEqual({ reason: "Solicitação #123 pelo titular" }); return null;
    }, { status: 204 });
    const { user } = renderApp("/admin/lixeira", { session: adminUser });
    await user.selectOptions(await screen.findByLabelText("Itens excluídos"), "accounts");
    await user.click(await screen.findByRole("button", { name: "Restaurar conta" }));
    expect(screen.getByRole("button", { name: "Confirmar restauração" })).toBeDisabled();
    await user.type(screen.getByLabelText("Referência da solicitação do titular"), "Solicitação #123 pelo titular");
    await user.click(screen.getByRole("checkbox"));
    await user.click(screen.getByRole("button", { name: "Confirmar restauração" }));
    await waitFor(() => expect(restore).toHaveBeenCalledOnce());
  });

  it.each(["denuncias", "lixeira"])("impede aluno de consultar administração de %s", async path => {
    const query = mockApi("get", path === "denuncias" ? "/api/reports" : "/api/trash", {});
    const { router } = renderApp("/admin/" + path);
    await waitFor(() => expect(router.state.location.pathname).toBe("/")); expect(query).not.toHaveBeenCalled();
  });

  it("oferece eliminação imediata separada da desativação recuperável", async () => {
    const remove = mockApi("post", `/api/users/${student.id}/erasure`, { detail: "Senha incorreta." }, { status: 401 });
    const { user } = renderApp("/perfil");
    await user.click(await screen.findByRole("button", { name: "Excluir minha conta" }));
    await screen.findByRole("dialog");
    expect(screen.getByText(/política de recuperação, não um prazo obrigatório/)).toBeInTheDocument();
    await user.click(screen.getByRole("radio", { name: "Solicitar eliminação definitiva agora" }));
    await user.click(screen.getByRole("checkbox"));
    await user.click(screen.getByRole("button", { name: /Prosseguir para confirmação/ }));
    await fillFields(user, { "Senha atual da conta": "incorrect" });
    await user.click(screen.getByRole("button", { name: "Confirmar eliminação definitiva" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Senha incorreta");
    expect(remove).toHaveBeenCalledOnce();
  });
});
