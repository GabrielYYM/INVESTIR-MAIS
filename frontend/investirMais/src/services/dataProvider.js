import { HttpError } from "react-admin";
import { isCancel } from "axios";
import * as education from "../generated/education.ts";
import * as account from "../generated/account.ts";
import { apiError } from "./api.js";

export const roleNames = { STUDENT: "Aluno", TEACHER: "Professor", ADMIN: "Administrador" };
const courseId = ({ meta }) => {
  if (!meta?.courseId) throw new HttpError("Informe o curso desta operação.", 400);
  return meta.courseId;
};
const resources = {
  courses: { list: education.getAllCourses, one: education.getCourseById, create: education.createCourse, update: education.updateCourse, delete: education.deleteCourse },
  lessons: { list: (query, options, params) => education.getLessonsByCourse(courseId(params), query, options), one: education.getLessonById,
    create: (data, params) => education.createLesson(courseId(params), data), update: education.updateLesson, delete: education.deleteLesson },
  questions: { list: (_, options, params) => education.manage(courseId(params), options),
    create: (data, params) => education.create(courseId(params), data),
    update: (id, data, params) => education.update(courseId(params), id, data),
    delete: (id, params) => education._delete(courseId(params), id) },
  users: { list: (_, options) => account.listAllUsers(options), one: account.getProfile, update: account.updateUserRole },
  audit: { list: (_, options, params) => account.getUserAuditHistory(params.meta.userId, options) },
};

async function call(resource, operation, ...args) {
  try {
    const fn = resources[resource]?.[operation];
    if (!fn) throw new HttpError("Operação indisponível para este recurso.", 400);
    return await fn(...args);
  } catch (error) {
    if (isCancel(error)) throw new DOMException("Consulta cancelada.", "AbortError");
    if (error instanceof HttpError) throw error;
    const failure = new HttpError(apiError(error, "Não foi possível concluir a operação. Tente novamente."), error.response?.status ?? 500);
    failure.response = error.response;
    throw failure;
  }
}

export const dataProvider = {
  supportAbortSignal: true,
  async getList(resource, params) {
    const { page = 1, perPage = 10 } = params.pagination ?? {};
    const { field = "id", order = "ASC" } = params.sort ?? {};
    const result = await call(resource, "list", { page: page - 1, size: perPage, sort: [`${field},${order.toLowerCase()}`] }, { signal: params.signal }, params);
    if (!Array.isArray(result)) return { data: result.content, total: result.page.totalElements };
    const { q = "", role } = params.filter ?? {};
    const rows = result.map(row => resource === "audit" ? { ...row, id: row.revisionNumber } : row)
      .filter(row => (!role || row.role === role) && (!q || `${row.name} ${row.email}`.toLocaleLowerCase("pt-BR").includes(q.toLocaleLowerCase("pt-BR"))))
      .sort((a, b) => String(a[field] ?? "").localeCompare(String(b[field] ?? ""), "pt-BR", { numeric: true }) * (order === "DESC" ? -1 : 1));
    return { data: rows.slice((page - 1) * perPage, page * perPage), total: rows.length, meta: { stats: { total: result.length, ...Object.fromEntries(["ADMIN", "TEACHER", "STUDENT"].map(role => [role, result.filter(row => row.role === role).length])) } } };
  },
  async getOne(resource, params) {
    if (resource === "questions") {
      const rows = await call(resource, "list", {}, { signal: params.signal }, params);
      const data = rows.find(row => row.id === params.id);
      if (!data) throw new HttpError("Pergunta não encontrada.", 404);
      return { data };
    }
    return { data: await call(resource, "one", params.id, { signal: params.signal }) };
  },
  async create(resource, params) { return { data: await call(resource, "create", params.data, params) }; },
  async update(resource, params) { return { data: await call(resource, "update", params.id, params.data, params) }; },
  async delete(resource, params) { await call(resource, "delete", params.id, params); return { data: params.previousData ?? { id: params.id } }; },
  async getMany(resource, params) { return { data: await Promise.all(params.ids.map(id => dataProvider.getOne(resource, { ...params, id }).then(result => result.data))) }; },
  getManyReference: (resource, params) => dataProvider.getList(resource, { ...params, filter: { ...params.filter, [params.target]: params.id } }),
  async updateMany(resource, params) { return { data: await Promise.all(params.ids.map(id => dataProvider.update(resource, { ...params, id }).then(result => result.data.id))) }; },
  async deleteMany(resource, params) { return { data: await Promise.all(params.ids.map(id => dataProvider.delete(resource, { ...params, id }).then(result => result.data.id))) }; },
};
