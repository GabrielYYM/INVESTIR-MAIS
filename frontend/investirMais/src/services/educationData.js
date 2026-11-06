import { replace } from "react-router";
import { buscarCurso, listarCursos } from "./courseService.js";
import { buscarAula, listarAulasDoCurso } from "./lessonService.js";

function pageIndex(url) {
  const page = Number(url.searchParams.get("pagina"));
  return Number.isSafeInteger(page) && page > 0 && page <= 2147483647 ? page - 1 : 0;
}

function readPage(response) {
  return {
    items: response.content,
    number: response.page?.number ?? response.number,
    totalPages: response.page?.totalPages ?? response.totalPages,
    totalItems: response.page?.totalElements ?? response.totalElements,
  };
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
  return readPage(await listarCursos({ size: 3, signal }));
}

export async function catalogLoader({ request }) {
  const url = new URL(request.url);
  const requested = pageIndex(url);
  const page = readPage(await listarCursos({ page: requested, size: 9, signal: request.signal }));
  validatePage(url, requested, page);
  return page;
}

export async function courseLoader({ request, params: { courseId, lessonId } }) {
  const uuid = /^[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}$/i;
  if (!uuid.test(courseId) || (lessonId && !uuid.test(lessonId))) throw new Response(null, { status: 404 });

  const url = new URL(request.url);
  const requested = pageIndex(url);
  const { signal } = request;
  const [course, result, selected] = await Promise.all([
    buscarCurso(courseId, signal),
    listarAulasDoCurso(courseId, { page: requested, signal }),
    lessonId ? buscarAula(lessonId, signal) : null,
  ]);

  if (selected && selected.courseId !== course.id) throw new Response(null, { status: 404 });

  const page = readPage(result);
  validatePage(url, requested, page);
  return { course, page, lesson: selected ?? page.items[0] };
}

export function canManageEducation(user, course) {
  return user.role === "ADMIN" || (user.role === "TEACHER" && (!course || course.professorId === user.id));
}
