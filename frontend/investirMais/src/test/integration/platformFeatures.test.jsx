import { screen, waitFor, within } from "@testing-library/react";
import { File as NodeFile } from "node:buffer";
import { describe, expect, it, vi } from "vitest";
import { mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";
import { adminUser, student, course, paged, legalRelease } from "../fixtures.js";

describe("novos fluxos da plataforma", () => {
  it("envia candidatura com documento e mostra análise pendente", async () => {
    let application = null;
    mockApi("get", "/api/teacher-applications/requirements", ["qualificacao"]);
    mockApi("get", "/api/teacher-applications/mine", () => application);
    const send = mockApi("post", "/api/teacher-applications", async ({ request }) => {
      const body = await request.formData();
      expect(body.get("types")).toBe("qualificacao");
      expect(body.get("files").name).toBe("qualificacao.pdf");
      application = { id: "candidate", status: "PENDING", documents: [{ id: "doc", type: "qualificacao" }] };
      return application;
    });
    const { user } = renderApp("/cadastro-professor");
    await user.upload(await screen.findByLabelText("qualificacao"), new File(["%PDF-1.7"], "qualificacao.pdf", { type: "application/pdf" }));
    vi.stubGlobal("File", NodeFile);
    try {
      await user.click(screen.getByRole("button", { name: "Enviar candidatura" }));
      expect(await screen.findByRole("heading", { name: "Situação: Em análise" })).toBeInTheDocument();
      expect(send).toHaveBeenCalledOnce();
    } finally { vi.unstubAllGlobals(); }
  });

  it("exige parecer para aprovar professor", async () => {
    const application = { id: "candidate", userId: student.id, status: "PENDING", submittedAt: "2026-11-16T10:00:00Z", documents: [] };
    mockApi("get", "/api/teacher-applications", paged([application]));
    const review = mockApi("post", "/api/teacher-applications/candidate/review", async ({ request }) => {
      expect(await request.json()).toEqual({ approve: true, reason: "Qualificação conferida" }); return { ...application, status: "APPROVED" };
    });
    const { user } = renderApp("/admin/professores", { session: adminUser });
    expect(await screen.findByRole("button", { name: "Aprovar professor" })).toBeDisabled();
    await user.type(screen.getByLabelText("Parecer da análise"), "Qualificação conferida");
    await user.click(screen.getByRole("button", { name: "Aprovar professor" }));
    await waitFor(() => expect(review).toHaveBeenCalledOnce());
  });

  it("filtra auditoria por revisão e autor", async () => {
    const audit = mockApi("get", "/api/audit", ({ request }) => {
      const params = new URL(request.url).searchParams;
      if (params.get("revision")) { expect(params.get("revision")).toBe("42"); expect(params.get("actor")).toBe("admin@example.test"); }
      return { items: [], totalItems: 0 };
    });
    const { user } = renderApp("/admin/auditoria", { session: adminUser });
    await user.type(await screen.findByLabelText("Revisão"), "42");
    await user.type(screen.getByLabelText("Usuário autor (e-mail)"), "admin@example.test");
    await user.click(screen.getByRole("button", { name: "Filtrar" }));
    await waitFor(() => expect(audit).toHaveBeenCalledTimes(2));
  });

  it("revisa um novo documento antes de publicar a próxima versão", async () => {
    const publish = mockApi("post", "/api/legal/versions", async ({ request }) => {
      const body = await request.json(); expect(body.version).toBe(legalRelease.version + 1);
      expect(body.documents.at(-1)).toMatchObject({ id: "regulamento", title: "Regulamento" }); return body;
    });
    const { user } = renderApp("/admin/documentos", { session: adminUser });
    await user.click(await screen.findByRole("button", { name: "Adicionar documento" }));
    const document = within(screen.getByRole("group", { name: "Documento 3" }));
    for (const [name, text] of [[/^Identificador/, "regulamento"], [/^Nome da aba/, "Regras"], [/^Título do documento/, "Regulamento"], [/^Título da seção/, "Acesso"], [/^Texto da seção/, "Regras de acesso ao curso."]]) { await user.click(document.getByRole("textbox", { name })); await user.paste(text); }
    await user.click(screen.getByRole("button", { name: "Revisar publicação" }));
    expect(publish).not.toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "Publicar versão e solicitar nova confirmação" }));
    await waitFor(() => expect(publish).toHaveBeenCalledOnce());
  });

  it("separa matrículas e respostas pessoais do catálogo", async () => {
    mockApi("get", "/api/learning/courses", [{ id: course.id, name: "Minha matrícula", completed_lessons: 1, total_lessons: 3 }]);
    mockApi("get", "/api/learning/answers", paged([{ id: "answer", prompt: "Minha questão", selectedAnswer: "Minha alternativa", correct: false, answeredAt: "2026-11-16T10:00:00Z", courseId: course.id }]));
    renderApp("/");
    expect(await screen.findByRole("heading", { name: "Minha matrícula" })).toBeInTheDocument();
    expect(await screen.findByText("Sua resposta: Minha alternativa")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Catálogo de Cursos" })).toBeInTheDocument();
  });

  it("não consulta auditoria quando o usuário é aluno", async () => {
    const audit = mockApi("get", "/api/audit", {});
    const { router } = renderApp("/admin/auditoria");
    await waitFor(() => expect(router.state.location.pathname).toBe("/")); expect(audit).not.toHaveBeenCalled();
  });
});
