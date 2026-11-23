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

  it("permite enviar até 3 documentos na candidatura com documento de identificação e comprovante adicional", async () => {
    let application = null;
    mockApi("get", "/api/teacher-applications/requirements", ["qualificacao"]);
    mockApi("get", "/api/teacher-applications/mine", () => application);
    const send = mockApi("post", "/api/teacher-applications", async ({ request }) => {
      const body = await request.formData();
      const allTypes = body.getAll("types");
      const allFiles = body.getAll("files");
      expect(allTypes).toEqual(["qualificacao", "documento-de-identificacao", "comprovante-adicional"]);
      expect(allFiles).toHaveLength(3);
      application = {
        id: "candidate-3",
        status: "PENDING",
        documents: allTypes.map((type, i) => ({ id: `doc-${i}`, type })),
      };
      return application;
    });

    const { user } = renderApp("/cadastro-professor");
    await user.upload(await screen.findByLabelText("qualificacao"), new File(["%PDF-1.7"], "qualificacao.pdf", { type: "application/pdf" }));
    await user.upload(screen.getByLabelText("Documento oficial de identificação"), new File(["%PDF-1.7"], "identidade.pdf", { type: "application/pdf" }));
    await user.upload(screen.getByLabelText("Comprovante adicional"), new File(["%PDF-1.7"], "diploma.pdf", { type: "application/pdf" }));

    vi.stubGlobal("File", NodeFile);
    try {
      await user.click(screen.getByRole("button", { name: "Enviar candidatura" }));
      expect(await screen.findByRole("heading", { name: "Situação: Em análise" })).toBeInTheDocument();
      expect(send).toHaveBeenCalledOnce();
    } finally {
      vi.unstubAllGlobals();
    }
  });

  it("exige parecer para aprovar professor", async () => {
    const application = { id: "candidate", userId: student.id, userName: student.name, status: "PENDING", submittedAt: "2026-11-16T10:00:00Z", documents: [] };
    mockApi("get", "/api/teacher-applications", paged([application]));
    const review = mockApi("post", "/api/teacher-applications/candidate/review", async ({ request }) => {
      expect(await request.json()).toEqual({ approve: true, reason: "Qualificação conferida" }); return { ...application, status: "APPROVED" };
    });
    const { user } = renderApp("/admin/professores", { session: adminUser });
    expect(await screen.findByText(student.name)).toBeInTheDocument();
    expect(screen.queryByText(student.id, { exact: false })).not.toBeInTheDocument();

    // Clica no botão "Avaliar" da tabela para abrir o modal de detalhes
    await user.click(screen.getByRole("button", { name: "Avaliar" }));
    const modal = within(await screen.findByRole("dialog"));
    expect(await modal.findByRole("heading", { name: student.name, exact: true })).toBeInTheDocument();
    expect(modal.getByRole("button", { name: "Aprovar professor" })).toBeDisabled();

    await user.type(modal.getByLabelText("Parecer da análise"), "Qualificação conferida");
    await user.click(modal.getByRole("button", { name: "Aprovar professor" }));
    await waitFor(() => expect(review).toHaveBeenCalledOnce());
  });

  it("filtra auditoria por autor", async () => {
    mockApi("get", "/api/users", [adminUser]);
    const audit = mockApi("get", "/api/audit", ({ request }) => {
      const params = new URL(request.url).searchParams;
      if (params.get("actor")) { expect(params.get("actor")).toBe("admin@example.test"); }
      return { items: [], totalItems: 0 };
    });
    const { user } = renderApp("/admin", { session: adminUser });
    await user.type(await screen.findByLabelText("Usuário autor (e-mail)"), "admin@example.test");
    await user.click(screen.getByRole("button", { name: "Filtrar" }));
    await waitFor(() => expect(audit).toHaveBeenCalledTimes(2));
  });

  it("revisa e publica a próxima versão dos documentos legais", async () => {
    const publish = mockApi("post", "/api/legal/versions", async ({ request }) => {
      const body = await request.json(); expect(body.version).toBe(legalRelease.version + 1);
      expect(body.documents).toHaveLength(2);
      expect(body.documents.map(d => d.id)).toEqual(["termos", "privacidade"]);
      expect(body.documents[0].title).toBe("Termos e Condições Atualizados");
      return body;
    });
    const { user } = renderApp("/admin/documentos", { session: adminUser });
    const document1 = within(await screen.findByRole("group", { name: "Documento 1" }));
    const titleInput = document1.getByRole("textbox", { name: /^Título do documento/ });
    await user.clear(titleInput);
    await user.type(titleInput, "Termos e Condições Atualizados");
    await user.click(screen.getByRole("button", { name: "Revisar publicação" }));
    expect(publish).not.toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "Publicar versão e solicitar nova confirmação" }));
    await waitFor(() => expect(publish).toHaveBeenCalledOnce());
  });

  it("separa matrículas pessoais do catálogo", async () => {
    mockApi("get", "/api/learning/courses", [{ id: course.id, name: "Minha matrícula", completed_lessons: 1, total_lessons: 3 }]);
    renderApp("/");
    expect(await screen.findByRole("heading", { name: "Minha matrícula" })).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: "Minhas respostas" })).not.toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Catálogo de Cursos" })).toBeInTheDocument();
  });

  it("não consulta auditoria quando o usuário é aluno", async () => {
    const audit = mockApi("get", "/api/audit", {});
    const { router } = renderApp("/admin");
    await waitFor(() => expect(router.state.location.pathname).toBe("/")); expect(audit).not.toHaveBeenCalled();
  });
});
