import { apiClient } from "./api.js";

export async function getTeacherDashboard(paramsOrSignal, maybeSignal) {
  let params = {};
  let signal;

  if (paramsOrSignal && (paramsOrSignal instanceof AbortSignal || paramsOrSignal.signal)) {
    signal = paramsOrSignal instanceof AbortSignal ? paramsOrSignal : paramsOrSignal.signal;
  } else if (paramsOrSignal && typeof paramsOrSignal === "object") {
    params = paramsOrSignal;
    signal = maybeSignal;
  }

  const query = new URLSearchParams();
  if (params.startDate) query.set("startDate", params.startDate);
  if (params.endDate) query.set("endDate", params.endDate);
  const qs = query.toString();
  const url = qs ? `/api/teacher/dashboard?${qs}` : "/api/teacher/dashboard";

  const { data } = await apiClient.get(url, { signal });
  return data;
}

export async function enrollCourse(courseId) {
  const { data } = await apiClient.post(`/api/courses/${courseId}/enroll`);
  return data;
}

export async function updateLessonProgress(courseId, lessonId, completed) {
  const { data } = await apiClient.post(`/api/lessons/${courseId}/${lessonId}/progress`, { completed });
  return data;
}

export async function getCourseProgress(courseId, signal) {
  const { data } = await apiClient.get(`/api/courses/${courseId}/progress`, { signal });
  return data;
}

