import { data, redirect, replace } from "react-router";
import { apiError } from "./api.js";
import { getCurrentUser } from "./authService.js";
import { getCourseProgress } from "./teacherService.js";
import {
  createCourse, createLesson, deleteCourse, deleteLesson, getAllCourses, getCourseById,
  getLessonById, getLessonsByCourse, updateCourse, updateLesson,
  create as createQuestion, update as updateQuestion, _delete as deleteQuestion,
  list as getQuestions, manage as getTeacherQuestions, answer as answerQuestion, enrollStudent,
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

export async function catalogAction({ request }) {
  const { intent, payload } = await request.json();
  if (intent !== "create-curso") throw new Response(null, { status: 400 });
  try {
    const course = await createCourse(payload);
    return redirect("/educacao/" + course.id);
  } catch (error) {
    return mutationError(error, request);
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
      case "create-question":
        await createQuestion(courseId, payload);
        break;
      case "update-question":
        await updateQuestion(courseId, id, payload);
        break;
      case "delete-question":
        await deleteQuestion(courseId, id);
        break;
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
