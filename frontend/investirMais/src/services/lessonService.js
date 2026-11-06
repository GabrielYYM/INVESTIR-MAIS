import { apiClient } from "./api";

export async function criarAula(courseId, lessonData) {
  const { data } = await apiClient.post(`/api/lessons/course/${courseId}`, lessonData);
  return data;
}

export async function listarAulasDoCurso(courseId, { page = 0, size = 10, signal } = {}) {
  const { data } = await apiClient.get(`/api/lessons/course/${courseId}`, {
    params: { page, size, sort: "title" },
    signal,
  });
  return data;
}

export async function buscarAula(lessonId, signal) {
  const { data } = await apiClient.get(`/api/lessons/${lessonId}`, { signal });
  return data;
}

export async function atualizarAula(lessonId, lessonData) {
  const { data } = await apiClient.put(`/api/lessons/${lessonId}`, lessonData);
  return data;
}

export async function excluirAula(lessonId) {
  await apiClient.delete(`/api/lessons/${lessonId}`);
}
