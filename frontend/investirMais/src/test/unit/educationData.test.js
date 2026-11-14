import { beforeEach, describe, expect, it, vi } from "vitest";
import * as client from "../../generated/education.ts";
import { canManageEducation, catalogLoader, courseAction, courseLoader, dashboardCourses } from "../../services/educationData.js";
import { pageHref } from "../../services/searchParams.js";
import { actionRequest, admin, course, lesson, loaderRequest, paged, student, teacher } from "../fixtures.js";

vi.mock("../../generated/education.ts");

beforeEach(() => {
  vi.resetAllMocks();
  client.getAllCourses.mockResolvedValue(paged([course]));
  client.getCourseById.mockResolvedValue(course);
  client.getLessonsByCourse.mockResolvedValue(paged([lesson], { size: 10 }));
  client.getLessonById.mockResolvedValue(lesson);
  client.getCourseProgress.mockResolvedValue({ enrolled: false, totalQuestions: 0 });
});

describe("permissões da interface de educação", () => {
  it.each([
    [student, undefined, false], [student, course, false], [teacher, undefined, true],
    [teacher, course, true], [teacher, { ...course, professorId: "outro" }, false],
    [admin, { ...course, professorId: "outro" }, true], [{ ...student, role: "UNKNOWN" }, course, false],
  ])("verifica perfil e propriedade %#", (user, selectedCourse, expected) => {
    expect(canManageEducation(user, selectedCourse)).toBe(expected);
  });
});

describe("loaders e paginação", () => {
  it("monta links da primeira página e páginas seguintes", () => {
    expect(pageHref("/educacao", 0)).toBe("/educacao");
    expect(pageHref("/educacao", 2)).toBe("/educacao?pagina=3");
  });

  it("limita os destaques e propaga cancelamento", async () => {
    const { signal } = loaderRequest();
    expect(await dashboardCourses(signal)).toMatchObject({ items: [course], totalItems: 1 });
    expect(client.getAllCourses).toHaveBeenCalledWith({ size: 3, sort: ["name", "id"] }, { signal });
  });

  it.each(["", "?pagina=0", "?pagina=-2", "?pagina=1.5", "?pagina=abc", "?pagina=2147483648", "?pagina=Infinity", "?pagina=NaN", "?pagina=2abc"])("usa a primeira página para %s", async (query) => {
    await catalogLoader({ request: loaderRequest("/educacao" + query) });
    expect(client.getAllCourses).toHaveBeenCalledWith(expect.objectContaining({ page: 0, size: 9 }), expect.anything());
  });

  it("converte a página exibida em índice da API", async () => {
    client.getAllCourses.mockResolvedValue(paged([course], { number: 2, totalPages: 3, totalElements: 20 }));
    const request = loaderRequest("/educacao?pagina=3");
    expect(await catalogLoader({ request })).toEqual({ items: [course], number: 2, totalPages: 3, totalItems: 20 });
    expect(client.getAllCourses).toHaveBeenCalledWith({ page: 2, size: 9, sort: ["name", "id"] }, { signal: request.signal });
  });

  it.each([[2, "/educacao?pagina=2"], [0, "/educacao"]])("corrige página fora do catálogo com %i páginas", async (totalPages, location) => {
    client.getAllCourses.mockResolvedValue(paged([], { totalPages }));
    const response = await catalogLoader({ request: loaderRequest("/educacao?pagina=99") }).catch((error) => error);
    expect(response).toBeInstanceOf(Response);
    expect(response.status).toBe(302);
    expect(response.headers.get("Location")).toBe(location);
    expect(response.headers.get("X-Remix-Replace")).toBe("true");
  });

  it("mantém outros filtros ao corrigir uma página que deixou de existir", async () => {
    client.getAllCourses.mockResolvedValue(paged([], { totalPages: 2 }));
    const response = await catalogLoader({ request: loaderRequest("/educacao?pagina=99&contexto=ensino") }).catch((error) => error);
    const query = new URL(response.headers.get("Location"), "http://localhost").searchParams;
    expect(query.get("pagina")).toBe("2");
    expect(query.get("contexto")).toBe("ensino");
  });

  it.each([{ courseId: "inválido" }, { courseId: course.id, lessonId: "inválido" }])("recusa identificadores inválidos antes de consultar a API %#", async (params) => {
    await expect(courseLoader({ request: loaderRequest(), params })).rejects.toMatchObject({ status: 404 });
    expect(client.getCourseById).not.toHaveBeenCalled();
  });

  it("seleciona a primeira aula do curso e compartilha o sinal das consultas", async () => {
    const request = loaderRequest("/educacao/" + course.id);
    expect(await courseLoader({ request, params: { courseId: course.id } })).toMatchObject({ course, lesson });
    expect(client.getCourseById).toHaveBeenCalledWith(course.id, { signal: request.signal });
    expect(client.getLessonsByCourse).toHaveBeenCalledWith(course.id, { page: 0, size: 10, sort: ["position", "id"] }, { signal: request.signal });
    expect(client.getLessonById).not.toHaveBeenCalled();
  });

  it("permite um curso sem aulas", async () => {
    client.getLessonsByCourse.mockResolvedValue(paged([]));
    expect(await courseLoader({ request: loaderRequest(), params: { courseId: course.id } })).toMatchObject({ lesson: undefined, page: { items: [] } });
  });

  it("recusa uma aula que pertence a outro curso", async () => {
    client.getLessonById.mockResolvedValue({ ...lesson, courseId: "outro" });
    await expect(courseLoader({ request: loaderRequest(), params: { courseId: course.id, lessonId: lesson.id } })).rejects.toMatchObject({ status: 404 });
  });
});

describe("ações de aprendizagem", () => {
  const payload = { optionIndex: 0 };
  const run = (intent) => courseAction({
    request: actionRequest({ intent, id: lesson.id, payload }, `/educacao/${course.id}/${lesson.id}?pagina=2`),
    params: { courseId: course.id },
  });
  it("envia a resposta e devolve somente o resultado do servidor", async () => {
    client.answer.mockResolvedValue({ passed: true });
    expect(await run("answer-question")).toEqual({ passed: true });
    expect(client.answer).toHaveBeenCalledWith(course.id, lesson.id, payload);
  });
  it("matricula o aluno", async () => {
    expect(await run("enroll-course")).toEqual({ ok: true });
    expect(client.enrollStudent).toHaveBeenCalledWith(course.id);
  });
  it("recusa uma operação desconhecida", async () => {
    expect(await run("unknown")).toMatchObject({ data: { error: "Operação inválida." }, init: { status: 400 } });
  });
  it.each([{ courseId: "inválido", id: lesson.id }, { courseId: course.id, id: "inválido" }])("recusa UUID inválido %#", async ({ courseId, id }) => {
    await expect(courseAction({ request: actionRequest({ intent: "answer-question", id }), params: { courseId } })).rejects.toMatchObject({ status: 404 });
    expect(client.answer).not.toHaveBeenCalled();
  });
  it.each([401, 403, 422, 500])("preserva o tratamento HTTP %i", async (status) => {
    client.answer.mockRejectedValue({ response: { status, data: { detail: "Falha da API" } } });
    const response = await run("answer-question");
    if (status === 401) {
      const destination = new URL(response.headers.get("Location"), "http://localhost");
      expect(destination.pathname).toBe("/entrar");
      expect(destination.searchParams.get("retorno")).toBe(`/educacao/${course.id}/${lesson.id}?pagina=2`);
    } else expect(response).toMatchObject({ data: { error: "Falha da API" }, init: { status } });
  });
  it("trata a indisponibilidade da rede", async () => {
    client.enrollStudent.mockRejectedValue(new Error("offline"));
    expect(await run("enroll-course")).toMatchObject({ init: { status: 500 }, data: { error: expect.stringContaining("Não foi possível") } });
  });
});
