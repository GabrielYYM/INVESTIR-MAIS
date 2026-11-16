import { data, redirect, replace } from "react-router";
import { apiError } from "./api.js";
import { getCurrentUser } from "./authService.js";
import { getCourseProgress } from "./teacherService.js";
import { loadPagination, pageHref } from "./searchParams.js";
import {
  getAllCourses, getCourseById, getLessonById, getLessonsByCourse,
  list as getQuestions, manage as getTeacherQuestions, answer as answerQuestion, enrollStudent,
} from "../generated/education.ts";

const uuid = /^[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}$/i;

function readPage({ content: items, page: { number, totalPages, totalElements: totalItems } }) {
  return { items, number, totalPages, totalItems };
}

function validatePage(url, requested, page) {
  if (requested > 0 && requested >= page.totalPages) {
    throw replace(pageHref(url.pathname + url.search, Math.max(0, page.totalPages - 1)));
  }
}

export async function dashboardCourses(signal) {
  return readPage(await getAllCourses({ size: 3, sort: ["name", "id"] }, { signal }));
}

export async function catalogLoader({ request }) {
  const url = new URL(request.url);
  const { pagina: requested } = loadPagination(url.searchParams);
  const page = readPage(await getAllCourses({ page: requested, size: 9, sort: ["name", "id"] }, { signal: request.signal }));
  validatePage(url, requested, page);
  return page;
}

export async function courseLoader({ request, params: { courseId, lessonId } }) {
  if (!uuid.test(courseId) || (lessonId && !uuid.test(lessonId))) throw new Response(null, { status: 404 });

  const url = new URL(request.url);
  const { pagina: requested } = loadPagination(url.searchParams);
  const { signal } = request;
  let progressError = null;
  const [course, result, selected, progress, user] = await Promise.all([
    getCourseById(courseId, { signal }),
    getLessonsByCourse(courseId, { page: requested, size: 10, sort: ["position", "id"] }, { signal }),
    lessonId ? getLessonById(lessonId, { signal }).catch((error) => {
      if (error.response?.status === 409) return { id: lessonId, courseId, title: "Aula bloqueada", locked: true };
      throw error;
    }) : null,
    getCourseProgress(courseId, signal).catch((error) => {
      if (signal.aborted || error.response?.status === 401) throw error;
      progressError = "Não foi possível carregar seu progresso. Tente novamente.";
      return null;
    }),
    getCurrentUser(signal),
  ]);

  if (selected && selected.courseId !== course.id) throw new Response(null, { status: 404 });

  const page = readPage(result);
  validatePage(url, requested, page);
  const canManage = canManageEducation(user, course);
  let questions = [];
  let questionsError = null;
  if (canManage || (progress?.enrolled && progress.totalQuestions > 0)) {
    try {
      questions = await (canManage ? getTeacherQuestions : getQuestions)(courseId, { signal });
    } catch (error) {
      if (signal.aborted || error.response?.status === 401) throw error;
      questionsError = apiError(error, "Não foi possível carregar as perguntas. Tente novamente.");
    }
  }
  const currentLesson = selected?.locked ? { ...(page.items.find((item) => item.id === selected.id) ?? selected), locked: true } : selected;
  return { course, page, lesson: currentLesson ?? page.items.find((item) => !item.locked) ?? page.items[0], progress,
    questions, questionsError, progressError, needsEnrollment: !canManage && progress?.totalQuestions > 0 && !progress.enrolled };
}

function mutationError(error, request) {
  const status = error.response?.status ?? 500;
  if (status === 401) {
    const url = new URL(request.url);
    return redirect("/entrar?" + new URLSearchParams({ retorno: url.pathname + url.search }));
  }
  return data({ error: apiError(error, "Não foi possível concluir a operação. Tente novamente.") }, { status });
}

export async function courseAction({ request, params: { courseId } }) {
  const { intent, id, payload } = await request.json();
  if (!uuid.test(courseId) || (id != null && !uuid.test(id))) throw new Response(null, { status: 404 });

  try {
    switch (intent) {
      case "answer-question":
        return await answerQuestion(courseId, id, payload);
      case "enroll-course":
        await enrollStudent(courseId);
        break;
      default:
        return data({ error: "Operação inválida." }, { status: 400 });
    }
    return { ok: true };
  } catch (error) {
    return mutationError(error, request);
  }
}

export function canManageEducation(user, course) {
  return user.role === "ADMIN" || (user.role === "TEACHER" && (!course || course.professorId === user.id));
}
