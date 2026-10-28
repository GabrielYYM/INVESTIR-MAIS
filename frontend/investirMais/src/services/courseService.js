import { apiClient } from "./api";

export async function criarCurso(courseData) {
  const { data } = await apiClient.post("/api/courses", courseData);
  return data;
}

export async function listarCursos({ page = 0, size = 10, sort = "name" } = {}) {
  const { data } = await apiClient.get("/api/courses", { params: { page, size, sort } });
  return data;
}

export async function buscarCurso(courseId) {
  const { data } = await apiClient.get(`/api/courses/${courseId}`);
  return data;
}

export async function listarCursosPorProfessor(professorId, { page = 0, size = 10 } = {}) {
  const { data } = await apiClient.get(`/api/courses/professor/${professorId}`, {
    params: { page, size, sort: "name" },
  });
  return data;
}

export async function atualizarCurso(courseId, courseData) {
  const { data } = await apiClient.put(`/api/courses/${courseId}`, courseData);
  return data;
}

export async function excluirCurso(courseId) {
  await apiClient.delete(`/api/courses/${courseId}`);
}
