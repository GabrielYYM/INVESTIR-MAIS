import { beforeEach, describe, expect, it, vi } from "vitest";
import * as education from "../../generated/education.ts";
import * as account from "../../generated/account.ts";
import { dataProvider } from "../../services/dataProvider.js";
import { course, lesson, paged, student, teacherUser } from "../fixtures.js";

vi.mock("../../generated/education.ts");
vi.mock("../../generated/account.ts");
beforeEach(() => vi.resetAllMocks());

describe("contrato entre React-admin e Spring", () => {
  it("converte paginação, ordenação e sinal de cancelamento", async () => {
    education.getAllCourses.mockResolvedValue(paged([course], { totalElements: 21 }));
    const signal = new AbortController().signal;
    expect(await dataProvider.getList("courses", { pagination: { page: 3, perPage: 9 }, sort: { field: "name", order: "DESC" }, signal }))
      .toEqual({ data: [course], total: 21 });
    expect(education.getAllCourses).toHaveBeenCalledWith({ page: 2, size: 9, sort: ["name,desc"] }, { signal }, expect.anything());
  });
  it("consulta aulas somente no curso informado", async () => {
    education.getLessonsByCourse.mockResolvedValue(paged([lesson]));
    expect(await dataProvider.getList("lessons", { meta: { courseId: course.id } })).toMatchObject({ data: [lesson] });
    expect(education.getLessonsByCourse).toHaveBeenCalledWith(course.id, expect.objectContaining({ page: 0, size: 10 }), { signal: undefined });
    await expect(dataProvider.getList("lessons", {})).rejects.toMatchObject({ status: 400 });
  });
  it("filtra usuários sem distinguir maiúsculas e mantém totais gerais", async () => {
    account.listAllUsers.mockResolvedValue([student, teacherUser]);
    expect(await dataProvider.getList("users", { filter: { q: "ANA@", role: "STUDENT" } })).toMatchObject({
      data: [student], total: 1, meta: { stats: { total: 2, STUDENT: 1, TEACHER: 1, ADMIN: 0 } },
    });
  });
  it("ordena revisões numericamente e pagina o histórico sem alterar snapshots", async () => {
    const snapshot = { name: "Nome anterior" };
    account.getUserAuditHistory.mockResolvedValue([{ revisionNumber: 2, user: snapshot }, { revisionNumber: 10, user: snapshot }]);
    expect(await dataProvider.getList("audit", { meta: { userId: student.id }, pagination: { page: 2, perPage: 1 }, sort: { field: "revisionNumber", order: "DESC" } }))
      .toMatchObject({ total: 2, data: [{ id: 2, revisionNumber: 2, user: snapshot }] });
    expect(account.getUserAuditHistory).toHaveBeenCalledWith(student.id, { signal: undefined });
  });
  it("encontra uma pergunta no curso e devolve 404 se ela não existir", async () => {
    education.manage.mockResolvedValue([{ id: "pergunta", prompt: "Enunciado" }]);
    expect(await dataProvider.getOne("questions", { id: "pergunta", meta: { courseId: course.id } })).toMatchObject({ data: { prompt: "Enunciado" } });
    await expect(dataProvider.getOne("questions", { id: "outra", meta: { courseId: course.id } })).rejects.toMatchObject({ status: 404 });
  });
  it.each([
    ["courses", "createCourse", course], ["lessons", "createLesson", lesson], ["questions", "create", { id: "pergunta" }],
  ])("cria %s no endpoint correto", async (resource, method, result) => {
    education[method].mockResolvedValue(result);
    const data = { title: "Conteúdo" }, params = { data, meta: { courseId: course.id } };
    expect(await dataProvider.create(resource, params)).toEqual({ data: result });
    expect(education[method]).toHaveBeenCalledWith(...(resource === "courses" ? [data, params] : resource === "lessons" ? [course.id, data] : [course.id, data]));
  });
  it("envia a edição de pergunta com o curso e o identificador", async () => {
    education.update.mockResolvedValue({ id: "pergunta", prompt: "Atualizada" });
    const data = { prompt: "Atualizada" };
    expect(await dataProvider.update("questions", { id: "pergunta", data, meta: { courseId: course.id } })).toMatchObject({ data: { prompt: "Atualizada" } });
    expect(education.update).toHaveBeenCalledWith(course.id, "pergunta", data);
  });
  it("normaliza exclusão sem corpo e preserva o registro anterior", async () => {
    expect(await dataProvider.delete("questions", { id: "pergunta", previousData: { id: "pergunta", prompt: "Anterior" }, meta: { courseId: course.id } }))
      .toEqual({ data: { id: "pergunta", prompt: "Anterior" } });
    expect(education._delete).toHaveBeenCalledWith(course.id, "pergunta");
    expect(await dataProvider.delete("courses", { id: course.id })).toEqual({ data: { id: course.id } });
  });
  it("preserva HTTP e mensagem para o tratamento da sessão e dos formulários", async () => {
    account.updateUserRole.mockRejectedValue({ response: { status: 401, data: { detail: "Sessão encerrada." } } });
    await expect(dataProvider.update("users", { id: student.id, data: { role: "TEACHER" } }))
      .rejects.toMatchObject({ status: 401, message: "Sessão encerrada.", response: { status: 401 } });
  });
  it("trata rede indisponível e impede operações sem suporte", async () => {
    education.getCourseById.mockRejectedValue(new Error("offline"));
    await expect(dataProvider.getOne("courses", { id: course.id })).rejects.toMatchObject({ status: 500, message: expect.stringContaining("Não foi possível") });
    await expect(dataProvider.delete("users", { id: student.id })).rejects.toMatchObject({ status: 400 });
  });
  it("agrupa consultas e mutações mantendo os identificadores", async () => {
    education.getCourseById.mockImplementation(id => Promise.resolve({ id }));
    education.updateCourse.mockImplementation(id => Promise.resolve({ id }));
    const ids = [course.id, lesson.id];
    expect(await dataProvider.getMany("courses", { ids })).toEqual({ data: ids.map(id => ({ id })) });
    expect(await dataProvider.updateMany("courses", { ids, data: { name: "Atualizado" } })).toEqual({ data: ids });
    expect(await dataProvider.deleteMany("courses", { ids })).toEqual({ data: ids });
  });
});
