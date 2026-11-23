import { screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { mockApi, fillFields } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";
import { adminUser, course, lesson, paged, student, teacher } from "../fixtures.js";

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

  it("não exibe botões de denúncia para o professor dono do curso", async () => {
    renderApp("/educacao/" + course.id, { session: teacher });
    expect(await screen.findByRole("heading", { name: course.name })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Denunciar professor" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Denunciar vídeo" })).not.toBeInTheDocument();
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

    // Clica no botão "Analisar" da tabela para abrir o modal de detalhes
    await user.click(await screen.findByRole("button", { name: "Analisar" }));
    const modal = within(await screen.findByRole("dialog"));

    expect(await modal.findByRole("button", { name: "Remover vídeo e resolver" })).toBeDisabled();
    await user.type(modal.getByLabelText("Parecer da denúncia"), "Vídeo conferido e removido");
    await user.click(modal.getByRole("button", { name: "Remover vídeo e resolver" }));
    expect(review).not.toHaveBeenCalled();
    await user.click(modal.getByRole("button", { name: "Confirmar remoção da aula" }));
    await waitFor(() => expect(review).toHaveBeenCalledOnce());
  });

  it("impede aluno de consultar administração de denuncias", async () => {
    const query = mockApi("get", "/api/reports", {});
    const { router } = renderApp("/admin/denuncias");
    await waitFor(() => expect(router.state.location.pathname).toBe("/")); expect(query).not.toHaveBeenCalled();
  });

  it("explica a exclusão imediata, a liberação do e-mail e a preservação do histórico", async () => {
    const remove = mockApi("delete", `/api/users/${student.id}`, { detail: "Senha incorreta." }, { status: 401 });
    const { user } = renderApp("/perfil");
    await user.click(await screen.findByRole("button", { name: "Exclusão de Conta" }));
    await user.click(await screen.findByRole("button", { name: "Excluir minha conta" }));
    await screen.findByRole("dialog");
    expect(screen.getByText(/seu e-mail ficará disponível/)).toBeInTheDocument();
    expect(screen.getByText(/histórico de ações, logs e auditoria será preservado/)).toBeInTheDocument();
    expect(screen.queryByRole("radio")).not.toBeInTheDocument();
    await user.click(screen.getByRole("checkbox"));
    await user.click(screen.getByRole("button", { name: /Prosseguir para confirmação/ }));
    await fillFields(user, { "Senha atual da conta": "incorrect" });
    await user.click(screen.getByRole("button", { name: "Confirmar exclusão da conta" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Senha incorreta");
    expect(remove).toHaveBeenCalledOnce();
  });
});
