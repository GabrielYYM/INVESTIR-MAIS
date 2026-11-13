import { beforeEach, describe, expect, it, vi } from "vitest";
import * as client from "../../generated/education.ts";
import { canManageEducation, catalogAction, catalogLoader, courseAction, courseLoader, dashboardCourses, pageHref } from "../../services/educationData.js";
import { actionRequest, admin, course, lesson, loaderRequest, paged, student, teacher } from "../fixtures.js";

vi.mock("../../generated/education.ts");

beforeEach(() => {
  vi.resetAllMocks();
  client.getAllCourses.mockResolvedValue(paged([course]));
  client.getCourseById.mockResolvedValue(course);
  client.getLessonsByCourse.mockResolvedValue(paged([lesson], { size: 10 }));
  client.getLessonById.mockResolvedValue(lesson);
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

  it.each(["", "?pagina=0", "?pagina=-2", "?pagina=1.5", "?pagina=abc", "?pagina=2147483648", "?pagina=Infinity"])("usa a primeira página para %s", async (query) => {
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

describe("ações de cursos e aulas", () => {
  const payload = { name: "Novo curso", description: "Descrição" };
  const run = (intent, options = {}) => courseAction({
    request: actionRequest({ intent, id: lesson.id, payload, ...options }, "/educacao/" + course.id + "/" + lesson.id + "?pagina=2"),
    params: { courseId: course.id, lessonId: lesson.id },
  });

  it("cria um curso e navega para seu detalhe", async () => {
    client.createCourse.mockResolvedValue(course);
    const response = await catalogAction({ request: actionRequest({ intent: "create-curso", payload }) });
    expect(client.createCourse).toHaveBeenCalledWith(payload);
    expect(response.headers.get("Location")).toBe("/educacao/" + course.id);
  });

  it("recusa intenção não reconhecida no catálogo", async () => {
    await expect(catalogAction({ request: actionRequest({ intent: "delete-curso" }) })).rejects.toMatchObject({ status: 400 });
    expect(client.createCourse).not.toHaveBeenCalled();
  });

  it.each([["update-curso", "updateCourse", course.id], ["update-aula", "updateLesson", lesson.id]])("salva %s", async (intent, method, id) => {
    expect(await run(intent)).toEqual({ ok: true });
    expect(client[method]).toHaveBeenCalledWith(id, payload);
  });

  it("exclui curso e substitui a navegação pelo catálogo", async () => {
    const response = await run("delete-curso");
    expect(client.deleteCourse).toHaveBeenCalledWith(course.id);
    expect(response.headers.get("Location")).toBe("/educacao");
    expect(response.headers.get("X-Remix-Replace")).toBe("true");
  });

  it("cria aula e navega para o vídeo criado", async () => {
    client.createLesson.mockResolvedValue(lesson);
    const response = await run("create-aula");
    expect(client.createLesson).toHaveBeenCalledWith(course.id, payload);
    expect(response.headers.get("Location")).toBe(`/educacao/${course.id}/${lesson.id}`);
  });

  it("exclui a aula selecionada preservando a página da lista", async () => {
    const response = await run("delete-aula");
    expect(client.deleteLesson).toHaveBeenCalledWith(lesson.id);
    expect(response.headers.get("Location")).toBe(`/educacao/${course.id}?pagina=2`);
  });

  it("exclui outra aula sem trocar a seleção", async () => {
    expect(await run("delete-aula", { id: course.id })).toEqual({ ok: true });
    expect(client.deleteLesson).toHaveBeenCalledWith(course.id);
  });

  it("retorna 400 para operação desconhecida", async () => {
    expect(await run("unknown")).toMatchObject({ data: { error: "Operação inválida." }, init: { status: 400 } });
  });

  it.each([{ courseId: "inválido", id: lesson.id }, { courseId: course.id, id: "inválido" }])("recusa UUID inválido nas ações %#", async ({ courseId, id }) => {
    await expect(courseAction({ request: actionRequest({ intent: "delete-aula", id }), params: { courseId } })).rejects.toMatchObject({ status: 404 });
    expect(client.deleteLesson).not.toHaveBeenCalled();
  });

  it.each([401, 403, 422, 500])("preserva o tratamento HTTP %i nas mutações", async (status) => {
    client.updateCourse.mockRejectedValue({ response: { status, data: { detail: "Falha da API" } } });
    const response = await run("update-curso");
    if (status === 401) {
      const destination = new URL(response.headers.get("Location"), "http://localhost");
      expect(destination.pathname).toBe("/entrar");
      expect(destination.searchParams.get("retorno")).toBe(`/educacao/${course.id}/${lesson.id}?pagina=2`);
    }
    else expect(response).toMatchObject({ data: { error: "Falha da API" }, init: { status } });
  });

  it("trata erro de rede na criação de curso", async () => {
    client.createCourse.mockRejectedValue(new Error("offline"));
    const response = await catalogAction({ request: actionRequest({ intent: "create-curso", payload }) });
    expect(response).toMatchObject({ init: { status: 500 }, data: { error: expect.stringContaining("Não foi possível") } });
  });

  it("preserva a página do catálogo quando a sessão expira ao criar curso", async () => {
    client.createCourse.mockRejectedValue({ response: { status: 401 } });
    const response = await catalogAction({ request: actionRequest({ intent: "create-curso", payload }, "/educacao?pagina=2") });
    expect(new URL(response.headers.get("Location"), "http://localhost").searchParams.get("retorno"))
      .toBe("/educacao?pagina=2");
  });
});
