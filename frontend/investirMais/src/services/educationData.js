import { data, redirect, replace } from "react-router";
import { apiError } from "./api.js";
import { getCourseProgress } from "./teacherService.js";
import {
  createCourse, createLesson, deleteCourse, deleteLesson, getAllCourses, getCourseById,
  getLessonById, getLessonsByCourse, updateCourse, updateLesson,
} from "../generated/education.ts";

const uuid = /^[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}$/i;

function pageIndex(url) {
  const page = Number(url.searchParams.get("pagina"));
  return Number.isSafeInteger(page) && page > 0 && page <= 2147483647 ? page - 1 : 0;
}

function readPage({ content: items, page: { number, totalPages, totalElements: totalItems } }) {
  return { items, number, totalPages, totalItems };
}

export function pageHref(pathname, number) {
  return number === 0 ? pathname : pathname + "?pagina=" + (number + 1);
}

function validatePage(url, requested, page) {
  if (requested > 0 && requested >= page.totalPages) {
    throw replace(pageHref(url.pathname, Math.max(0, page.totalPages - 1)));
  }
}

export async function dashboardCourses(signal) {
  return readPage(await getAllCourses({ size: 3, sort: ["name", "id"] }, { signal }));
}

export async function catalogLoader({ request }) {
  const url = new URL(request.url);
  const requested = pageIndex(url);
  const page = readPage(await getAllCourses({ page: requested, size: 9, sort: ["name", "id"] }, { signal: request.signal }));
  validatePage(url, requested, page);
  return page;
}

export async function courseLoader({ request, params: { courseId, lessonId } }) {
  if (!uuid.test(courseId) || (lessonId && !uuid.test(lessonId))) throw new Response(null, { status: 404 });

  const url = new URL(request.url);
  const requested = pageIndex(url);
  const { signal } = request;
  const [course, result, selected, progress] = await Promise.all([
    getCourseById(courseId, { signal }),
    getLessonsByCourse(courseId, { page: requested, size: 10, sort: ["title", "id"] }, { signal }),
    lessonId ? getLessonById(lessonId, { signal }) : null,
    getCourseProgress(courseId, signal).catch(() => null),
  ]);

  if (selected && selected.courseId !== course.id) throw new Response(null, { status: 404 });

  const page = readPage(result);
  validatePage(url, requested, page);
  return { course, page, lesson: selected ?? page.items[0], progress };
}

function mutationError(error) {
  const status = error.response?.status ?? 500;
  if (status === 401) return redirect("/entrar");
  return data({ error: apiError(error, "Não foi possível concluir a operação. Tente novamente.") }, { status });
}

export async function catalogAction({ request }) {
  const { intent, payload } = await request.json();
  if (intent !== "create-curso") throw new Response(null, { status: 400 });
  try {
    const course = await createCourse(payload);
    return redirect("/educacao/" + course.id);
  } catch (error) {
    return mutationError(error);
  }
}

export async function courseAction({ request, params: { courseId, lessonId } }) {
  const { intent, id, payload } = await request.json();
  if (!uuid.test(courseId) || (id != null && !uuid.test(id))) throw new Response(null, { status: 404 });
  const coursePath = "/educacao/" + courseId;

  try {
    switch (intent) {
      case "update-curso":
        await updateCourse(courseId, payload);
        break;
      case "delete-curso":
        await deleteCourse(courseId);
        return replace("/educacao");
      case "create-aula": {
        const created = await createLesson(courseId, payload);
        return redirect(coursePath + "/" + created.id);
      }
      case "update-aula":
        await updateLesson(id, payload);
        break;
      case "delete-aula":
        await deleteLesson(id);
        if (id === lessonId) return replace(pageHref(coursePath, pageIndex(new URL(request.url))));
        break;
      default:
        return data({ error: "Operação inválida." }, { status: 400 });
    }
    return { ok: true };
  } catch (error) {
    return mutationError(error);
  }
}

export function canManageEducation(user, course) {
  return user.role === "ADMIN" || (user.role === "TEACHER" && (!course || course.professorId === user.id));
}
