import { apiClient } from "./api";

export async function criarAula(courseId, lessonData) {
  const { data } = await apiClient.post(`/api/lessons/course/${courseId}`, lessonData);
  return data;
}

export async function listarAulasDoCurso(courseId, { page = 0, size = 10 } = {}) {
  const { data } = await apiClient.get(`/api/lessons/course/${courseId}`, {
    params: { page, size },
  });
  return data;
}

export async function buscarAula(lessonId) {
  const { data } = await apiClient.get(`/api/lessons/${lessonId}`);
  return data;
}

export async function atualizarAula(lessonId, lessonData) {
  const { data } = await apiClient.put(`/api/lessons/${lessonId}`, lessonData);
  return data;
}

export async function excluirAula(lessonId) {
  await apiClient.delete(`/api/lessons/${lessonId}`);
}
