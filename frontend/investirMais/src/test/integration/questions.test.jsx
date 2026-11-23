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
    const dialog = within(await screen.findByRole("dialog", { name: "Nova pergunta" }));
    await user.click(dialog.getByRole("button", { name: "Salvar pergunta" }));
    expect(await dialog.findByText("Informe o enunciado da pergunta.")).toBeInTheDocument();
    expect(dialog.getByLabelText("Enunciado")).toHaveAttribute("aria-invalid", "true");
    expect(payloads).toHaveLength(0);
    await fillQuestion(user, dialog);
    await user.click(dialog.getByRole("button", { name: "Salvar pergunta" }));
    await waitFor(() => expect(payloads).toEqual([{ prompt: question.prompt, afterLesson: 1, position: 0, options: question.options, correctOptionIndex: 0 }]));
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    expect(await screen.findByRole("heading", { name: question.prompt })).toBeInTheDocument();
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
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    expect(await screen.findByRole("heading", { name: question.prompt })).toBeInTheDocument();
    expect(remove).not.toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "Excluir pergunta " + question.prompt }));
    const confirmation = within(await screen.findByRole("dialog", { name: "Excluir pergunta" }));
    await user.click(confirmation.getByRole("button", { name: "Confirmar exclusão" }));
    await waitFor(() => expect(remove).toHaveBeenCalledOnce());
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    expect(await screen.findByRole("heading", { name: course.name })).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByRole("heading", { name: question.prompt })).not.toBeInTheDocument());
  });

  it("orienta criar questões dentro das aulas após criar o curso", async () => {
    const { user } = renderApp("/educacao", { session: teacher });
    await user.click(await screen.findByRole("button", { name: "Novo curso" }));
    const dialog = within(await screen.findByRole("dialog", { name: "Novo curso" }));
    expect(dialog.getByText(/cadastre as questões dentro de cada aula/)).toBeInTheDocument();
    expect(dialog.queryByRole("button", { name: "Adicionar pergunta" })).not.toBeInTheDocument();
  });

  it.each([401, 409])("trata recusa da exclusão de pergunta (%i)", async status => {
    server.use(
      http.get(base + "/manage", () => HttpResponse.json([question])),
      http.delete(base + "/:id", () => HttpResponse.json({ detail: "Exclusão indisponível." }, { status })),
    );
    const { user, router } = renderApp(coursePath + "?pagina=1", { session: teacher });
    await user.click(await screen.findByRole("button", { name: "Excluir pergunta " + question.prompt }));
    await user.click(screen.getByRole("button", { name: "Confirmar exclusão" }));
    if (status === 401) {
      expect(await screen.findByRole("heading", { name: "Bem-vindo de volta" })).toBeInTheDocument();
      expect(new URLSearchParams(router.state.location.search).get("retorno")).toBe(coursePath + "?pagina=1");
    } else {
      expect(await within(screen.getByRole("dialog")).findByRole("alert")).toHaveTextContent("Exclusão indisponível.");
      await user.click(screen.getByRole("button", { name: "Cancelar" }));
      expect(await screen.findByRole("heading", { name: question.prompt })).toBeInTheDocument();
    }
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
    expect(await dialog.findByText("As alternativas precisam ser diferentes.")).toBeInTheDocument(); expect(create).not.toHaveBeenCalled();
    await user.clear(dialog.getByLabelText("Alternativa 2", { exact: true }));
    await user.type(dialog.getByLabelText("Alternativa 2", { exact: true }), "CDB");
    await user.click(dialog.getByRole("button", { name: "Salvar pergunta" }));
    expect(await dialog.findByRole("alert")).toHaveTextContent("Não foi possível salvar");
    expect(dialog.getByLabelText("Enunciado")).toHaveValue(question.prompt);
  });

  it("mostra somente questões da aula selecionada", async () => {
    server.use(http.get(base + "/manage", () => HttpResponse.json([question, { ...question, id: "outra", prompt: "Questão da segunda aula", afterLesson: 2 }])));
    renderApp(coursePath, { session: teacher });
    expect(await screen.findByRole("heading", { name: question.prompt })).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: "Questão da segunda aula" })).not.toBeInTheDocument();
  });

});

function studentFlow({ enrolled = true } = {}) {
  let passed = false; let joined = enrolled;
  const submissions = [];
  server.use(
    http.get(origin + "/auth/me", () => HttpResponse.json(student)),
    http.get(origin + "/api/courses/:id/progress", () => HttpResponse.json({ enrolled: joined, totalLessons: 2, completedLessons: 1, completedLessonIds: [lesson.id], totalQuestions: 1, passedQuestions: Number(passed), progressPercentage: passed ? 66.7 : 33.3 })),
    http.post(origin + "/api/courses/:id/enroll", () => { joined = true; return HttpResponse.json({ enrolled: true }, { status: 201 }); }),
    http.get(base, () => HttpResponse.json([{ ...question, correctOptionIndex: undefined, locked: false, passed }])),
    http.get(origin + "/api/lessons/course/:id", () => HttpResponse.json(paged([
      { ...lesson, position: 1 }, { ...otherLesson, position: 2, locked: false },
    ], { size: 10 }))),
    http.get(origin + "/api/lessons/:id", ({ params }) => HttpResponse.json({ ...(params.id === otherLesson.id ? otherLesson : lesson), position: params.id === otherLesson.id ? 2 : 1 })),
    http.post(base + "/:id/answers", async ({ request }) => {
      const body = await request.json(); submissions.push(body); passed = body.optionIndex === 0; return HttpResponse.json({ questionId: question.id, passed });
    }),
  );
  return submissions;
}

describe("perguntas para alunos", () => {
  it("mantém os vídeos livres e atualiza a questão e o progresso depois do acerto", async () => {
    const submissions = studentFlow();
    const { user } = renderApp(coursePath);
    await screen.findByRole("heading", { name: question.prompt });
    expect(screen.queryByRole("button", { name: "Nova pergunta" })).not.toBeInTheDocument();
    expect(screen.getByRole("link", { name: otherLesson.title })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Responder pergunta" })).toBeDisabled();
    await user.click(screen.getByRole("radio", { name: "CDB" }));
    await user.click(screen.getByRole("button", { name: "Responder pergunta" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Ainda não é a alternativa correta");
    expect(screen.getByRole("link", { name: otherLesson.title })).toBeInTheDocument();
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

  it("permite abrir diretamente outra videoaula sem acertar as questões anteriores", async () => {
    const submissions = studentFlow();
    const { user } = renderApp(coursePath + "/" + otherLesson.id);
    expect(await screen.findByRole("heading", { name: otherLesson.title })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Abrir no YouTube" })).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: question.prompt })).not.toBeInTheDocument();
    await user.click(screen.getByRole("link", { name: lesson.title }));
    expect(await screen.findByRole("heading", { name: question.prompt })).toBeInTheDocument();
    expect(submissions).toHaveLength(0);
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

  it("mostra pergunta concluída sem botão de corrigir para acertos", async () => {
    studentFlow({ enrolled: true });
    server.use(http.get(base, () => HttpResponse.json([{ ...question, correctOptionIndex: undefined, locked: false, passed: true, incorrect: false }])));
    renderApp(coursePath);
    expect(await screen.findByText("Pergunta concluída")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Corrigir resposta" })).not.toBeInTheDocument();
  });

  it("permite ao aluno corrigir uma pergunta respondida incorretamente", async () => {
    let passed = false;
    server.use(
      http.get(origin + "/auth/me", () => HttpResponse.json(student)),
      http.get(origin + "/api/courses/:id/progress", () => HttpResponse.json({ enrolled: true, totalLessons: 2, completedLessons: 1, completedLessonIds: [lesson.id], totalQuestions: 1, passedQuestions: Number(passed), progressPercentage: passed ? 66.7 : 33.3 })),
      http.post(origin + "/api/courses/:id/enroll", () => HttpResponse.json({ enrolled: true }, { status: 201 })),
      http.get(base, () => HttpResponse.json([{ ...question, correctOptionIndex: undefined, locked: false, passed, incorrect: !passed }])),
      http.get(origin + "/api/lessons/course/:id", () => HttpResponse.json(paged([
        { ...lesson, position: 1 }, { ...otherLesson, position: 2, locked: false },
      ], { size: 10 }))),
      http.get(origin + "/api/lessons/:id", ({ params }) => HttpResponse.json({ ...(params.id === otherLesson.id ? otherLesson : lesson), position: params.id === otherLesson.id ? 2 : 1 })),
      http.post(base + "/:id/answers", async ({ request }) => {
        const body = await request.json(); passed = body.optionIndex === 0; return HttpResponse.json({ questionId: question.id, passed });
      }),
    );
    const { user } = renderApp(coursePath);
    expect(await screen.findByText("Resposta incorreta")).toBeInTheDocument();
    const correctBtn = screen.getByRole("button", { name: "Corrigir resposta" });
    expect(correctBtn).toBeInTheDocument();
    await user.click(correctBtn);
    expect(screen.getByRole("button", { name: "Responder pergunta" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Cancelar" })).toBeInTheDocument();
    await user.click(screen.getByRole("radio", { name: "Tesouro Selic" }));
    await user.click(screen.getByRole("button", { name: "Responder pergunta" }));
    expect(await screen.findByText("Pergunta concluída")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Corrigir resposta" })).not.toBeInTheDocument();
  });
});
