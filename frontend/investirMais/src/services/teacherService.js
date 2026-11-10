import { apiClient } from "./api.js";

export async function getTeacherDashboard(params, signal) {
  const { data } = await apiClient.get("/api/teacher/dashboard", { params, signal });
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

