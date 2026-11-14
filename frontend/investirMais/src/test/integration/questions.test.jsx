import { screen, waitFor, within } from "@testing-library/react";
import { http, HttpResponse } from "msw/http";
import { describe, expect, it, vi } from "vitest";
import { course, lesson, otherLesson, paged, student, teacher } from "../fixtures.js";
import { fillFields } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";
import { origin, server } from "../server.js";

const coursePath = "/educacao/" + course.id;
const base = origin + "/api/courses/" + course.id + "/questions";
const question = { id: "55555555-5555-4555-8555-555555555555", prompt: "Qual é um título público?",
  afterLesson: 1, position: 0, options: ["Tesouro Selic", "CDB"], correctOptionIndex: 0 };

async function fillQuestion(user, dialog) {
  await fillFields(user, {
    Enunciado: question.prompt,
    "Após a aula de número": 1,
    "Alternativa 1": question.options[0],
    "Alternativa 2": question.options[1],
  }, dialog);
}

describe("perguntas para professores", () => {
  it("valida, cria uma pergunta e atualiza o conteúdo sem recarregar a página", async () => {
    const payloads = []; let items = [];
    server.use(
      http.get(base + "/manage", () => HttpResponse.json(items)),
      http.post(base, async ({ request }) => { const body = await request.json(); payloads.push(body); items = [{ ...question, ...body }]; return HttpResponse.json(items[0], { status: 201 }); }),
    );
    const { user } = renderApp(coursePath, { session: teacher });
    await user.click(await screen.findByRole("button", { name: "Nova pergunta" }));
    const dialog = within(screen.getByRole("dialog", { name: "Nova pergunta" }));
    await user.click(dialog.getByRole("button", { name: "Salvar pergunta" }));
    expect(dialog.getByRole("alert")).toHaveTextContent("Informe o enunciado");
    expect(payloads).toHaveLength(0);
    await fillQuestion(user, dialog);
    await user.click(dialog.getByRole("button", { name: "Salvar pergunta" }));
    expect(await screen.findByRole("heading", { name: question.prompt })).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    expect(payloads).toEqual([{ prompt: question.prompt, afterLesson: 1, position: 0, options: question.options, correctOptionIndex: 0 }]);
    expect(screen.getByText("Tesouro Selic").closest("li")).toHaveTextContent("Correta");
  });

  it("ajusta a alternativa correta ao remover uma opção e salva a edição", async () => {
    let item = { ...question, options: ["A", "B", "C"], correctOptionIndex: 2 };
    const payloads = [];
    server.use(
      http.get(base + "/manage", () => HttpResponse.json([item])),
      http.put(base + "/:id", async ({ params, request }) => { expect(params.id).toBe(question.id); const body = await request.json(); payloads.push(body); item = { ...item, ...body }; return HttpResponse.json(item); }),
    );
    const { user } = renderApp(coursePath, { session: teacher });
    await user.click(await screen.findByRole("button", { name: "Editar pergunta " + question.prompt }));
    const dialog = within(screen.getByRole("dialog", { name: "Editar pergunta" }));
    expect(dialog.getByText(/alunos precisarão acertar/)).toBeInTheDocument();
    await user.click(dialog.getByRole("button", { name: "Remover alternativa 2" }));
    expect(dialog.getByRole("radio", { name: "Alternativa correta 2" })).toBeChecked();
    await user.click(dialog.getByRole("button", { name: "Salvar pergunta" }));
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    expect(payloads[0]).toEqual({ prompt: question.prompt, afterLesson: 1, position: 0, options: ["A", "C"], correctOptionIndex: 1 });
  });

  it("confirma a exclusão e preserva a pergunta ao cancelar", async () => {
    let items = [question];
    const remove = vi.fn(() => { items = []; return new HttpResponse(null, { status: 204 }); });
    server.use(http.get(base + "/manage", () => HttpResponse.json(items)), http.delete(base + "/:id", remove));
    const { user } = renderApp(coursePath, { session: teacher });
    await user.click(await screen.findByRole("button", { name: "Excluir pergunta " + question.prompt }));
    await user.click(screen.getByRole("button", { name: "Cancelar" }));
    expect(remove).not.toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "Excluir pergunta " + question.prompt }));
    await user.click(screen.getByRole("button", { name: "Confirmar exclusão" }));
    await waitFor(() => expect(screen.queryByRole("heading", { name: question.prompt })).not.toBeInTheDocument());
    expect(remove).toHaveBeenCalledOnce(); expect(screen.getByRole("heading", { name: course.name })).toBeInTheDocument();
  });

  it("cadastra perguntas no mesmo pedido de criação do curso", async () => {
    const payloads = [];
    server.use(http.post(origin + "/api/courses", async ({ request }) => { payloads.push(await request.json()); return HttpResponse.json(course, { status: 201 }); }));
    const { user } = renderApp("/educacao", { session: teacher });
    await user.click(await screen.findByRole("button", { name: "Novo curso" }));
    const dialog = within(screen.getByRole("dialog", { name: "Novo curso" }));
    await user.type(dialog.getByLabelText("Nome do curso"), course.name);
    await user.type(dialog.getByLabelText("Descrição"), course.description);
    await user.click(dialog.getByRole("button", { name: "Adicionar pergunta" }));
    await fillQuestion(user, dialog);
    await user.click(dialog.getByRole("button", { name: "Salvar curso" }));
    await screen.findByRole("heading", { name: course.name, level: 1 });
    expect(payloads[0].questions).toEqual([{ prompt: question.prompt, afterLesson: 1, position: 0, options: question.options, correctOptionIndex: 0 }]);
  });

  it("não envia alternativas repetidas e preserva os dados se a API falhar", async () => {
    const create = vi.fn(() => HttpResponse.json({ detail: "Não foi possível salvar a pergunta." }, { status: 500 }));
    server.use(http.post(base, create));
    const { user } = renderApp(coursePath, { session: teacher });
    await user.click(await screen.findByRole("button", { name: "Nova pergunta" }));
    const dialog = within(screen.getByRole("dialog"));
    await fillQuestion(user, dialog);
    await user.clear(dialog.getByLabelText("Alternativa 2", { exact: true }));
    await user.type(dialog.getByLabelText("Alternativa 2", { exact: true }), " tesouro selic ");
    await user.click(dialog.getByRole("button", { name: "Salvar pergunta" }));
    expect(dialog.getByRole("alert")).toHaveTextContent("precisam ser diferentes"); expect(create).not.toHaveBeenCalled();
    await user.clear(dialog.getByLabelText("Alternativa 2", { exact: true }));
    await user.type(dialog.getByLabelText("Alternativa 2", { exact: true }), "CDB");
    await user.click(dialog.getByRole("button", { name: "Salvar pergunta" }));
    expect(await dialog.findByRole("alert")).toHaveTextContent("Não foi possível salvar");
    expect(dialog.getByLabelText("Enunciado")).toHaveValue(question.prompt);
  });
});

function studentFlow({ enrolled = true, lockedUrl = false } = {}) {
  let passed = false; let joined = enrolled;
  const submissions = [];
  server.use(
    http.get(origin + "/auth/me", () => HttpResponse.json(student)),
    http.get(origin + "/api/courses/:id/progress", () => HttpResponse.json({ enrolled: joined, totalLessons: 2, completedLessons: 1, completedLessonIds: [lesson.id], totalQuestions: 1, passedQuestions: Number(passed), progressPercentage: passed ? 66.7 : 33.3 })),
    http.post(origin + "/api/courses/:id/enroll", () => { joined = true; return HttpResponse.json({ enrolled: true }, { status: 201 }); }),
    http.get(base, () => HttpResponse.json([{ ...question, correctOptionIndex: undefined, locked: false, passed }])),
    http.get(origin + "/api/lessons/course/:id", () => HttpResponse.json(paged([
      { ...lesson, position: 1 }, { ...otherLesson, position: 2, locked: !passed, mediaUrl: passed ? otherLesson.mediaUrl : null },
    ], { size: 10 }))),
    http.get(origin + "/api/lessons/:id", ({ params }) => params.id === otherLesson.id && !passed && lockedUrl ? HttpResponse.json({ detail: "Aula bloqueada" }, { status: 409 }) : HttpResponse.json({ ...(params.id === otherLesson.id ? otherLesson : lesson), position: params.id === otherLesson.id ? 2 : 1 })),
    http.post(base + "/:id/answers", async ({ request }) => {
      const body = await request.json(); submissions.push(body); passed = body.optionIndex === 0; return HttpResponse.json({ questionId: question.id, passed });
    }),
  );
  return submissions;
}

describe("perguntas para alunos", () => {
  it("permite tentar após erro e libera a aula e o progresso depois do acerto", async () => {
    const submissions = studentFlow();
    const { user } = renderApp(coursePath);
    await screen.findByRole("heading", { name: question.prompt });
    expect(screen.queryByRole("button", { name: "Nova pergunta" })).not.toBeInTheDocument();
    expect(screen.queryByRole("link", { name: otherLesson.title })).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Responder pergunta" })).toBeDisabled();
    await user.click(screen.getByRole("radio", { name: "CDB" }));
    await user.click(screen.getByRole("button", { name: "Responder pergunta" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Ainda não é a alternativa correta");
    expect(screen.queryByRole("link", { name: otherLesson.title })).not.toBeInTheDocument();
    await waitFor(() => expect(screen.getByRole("button", { name: "Responder pergunta" })).toBeEnabled());
    await user.click(screen.getByRole("radio", { name: "Tesouro Selic" }));
    await user.click(screen.getByRole("button", { name: "Responder pergunta" }));
    expect(await screen.findByText("Pergunta concluída")).toBeInTheDocument();
    expect(await screen.findByRole("link", { name: otherLesson.title })).toBeInTheDocument();
    expect(screen.getByText("1 de 1 perguntas concluídas")).toBeInTheDocument();
    expect(submissions).toEqual([{ optionIndex: 1 }, { optionIndex: 0 }]);
  });

  it("mostra a matrícula antes de consultar perguntas do curso", async () => {
    studentFlow({ enrolled: false }); const getQuestions = vi.fn(() => HttpResponse.json([{ ...question, passed: false, locked: false }]));
    server.use(http.get(base, getQuestions));
    const { user } = renderApp(coursePath);
    await screen.findByRole("button", { name: "Começar curso" }); expect(getQuestions).not.toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "Começar curso" }));
    expect(await screen.findByRole("heading", { name: question.prompt })).toBeInTheDocument(); expect(getQuestions).toHaveBeenCalledOnce();
  });

  it("trata endereço direto de aula bloqueada sem derrubar a página do curso", async () => {
    studentFlow({ lockedUrl: true }); renderApp(coursePath + "/" + otherLesson.id);
    expect(await screen.findByRole("heading", { name: "Aula bloqueada" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: question.prompt })).toBeInTheDocument();
    expect(screen.queryByRole("link", { name: "Abrir no YouTube" })).not.toBeInTheDocument();
  });

  it("recupera falha de carregamento das perguntas preservando a aula", async () => {
    studentFlow(); let failed = true;
    server.use(http.get(base, () => failed ? HttpResponse.json({ detail: "Perguntas indisponíveis." }, { status: 503 }) : HttpResponse.json([{ ...question, passed: false, locked: false }])));
    const { user } = renderApp(coursePath);
    expect(await screen.findByRole("alert")).toHaveTextContent("Perguntas indisponíveis");
    expect(screen.getByRole("heading", { name: lesson.title })).toBeInTheDocument();
    failed = false; await user.click(screen.getByRole("button", { name: "Tentar novamente" }));
    expect(await screen.findByRole("heading", { name: question.prompt })).toBeInTheDocument();
  });

  it("impede envios duplicados enquanto a resposta está sendo verificada", async () => {
    studentFlow(); const send = vi.fn(async () => { await new Promise((resolve) => setTimeout(resolve, 100)); return HttpResponse.json({ passed: false, questionId: question.id }); });
    server.use(http.post(base + "/:id/answers", send));
    const { user } = renderApp(coursePath);
    await user.click(await screen.findByRole("radio", { name: "CDB" }));
    await user.dblClick(screen.getByRole("button", { name: "Responder pergunta" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Ainda não é"); expect(send).toHaveBeenCalledOnce();
  });

  it("retorna ao login preservando a aula se a sessão expirar ao responder", async () => {
    studentFlow(); server.use(http.post(base + "/:id/answers", () => new HttpResponse(null, { status: 401 })));
    const destination = coursePath + "/" + lesson.id;
    const { user, router } = renderApp(destination);
    await user.click(await screen.findByRole("radio", { name: "CDB" }));
    await user.click(screen.getByRole("button", { name: "Responder pergunta" }));
    await screen.findByRole("heading", { name: "Bem-vindo de volta" });
    expect(new URLSearchParams(router.state.location.search).get("retorno")).toBe(destination);
  });
});
