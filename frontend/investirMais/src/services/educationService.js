import { apiClient } from "./authService";

// ────────────────────────────────────────────────
// FEED / HOME (aluno)
// ────────────────────────────────────────────────

/** Todo conteúdo publicado, mais recente primeiro — usado na Home. */
export async function getConteudoPublicado() {
  const { data } = await apiClient.get("/api/education/contents/published");
  return data; // ContentResponse[]
}

// ────────────────────────────────────────────────
// MÓDULOS
// ────────────────────────────────────────────────

/** Módulos visíveis ao aluno (com ao menos 1 conteúdo publicado). */
export async function getModulosVisiveis() {
  const { data } = await apiClient.get("/api/education/modules");
  return data;
}

/** Conteúdos publicados de um módulo específico (playlist do player). */
export async function getConteudoDoModulo(moduleId) {
  const { data } = await apiClient.get(`/api/education/contents/by-module/${moduleId}`);
  return data;
}

// ────────────────────────────────────────────────
// PROFESSOR — gestão de conteúdo
// ────────────────────────────────────────────────

export async function getMeusConteudos() {
  const { data } = await apiClient.get("/api/education/contents/mine");
  return data;
}

export async function getUploadSignature() {
  const { data } = await apiClient.post("/api/education/contents/upload-signature");
  return data; // { signature, timestamp, apiKey, cloudName, uploadPreset }
}

export async function criarConteudo({ title, type, mediaUrl }) {
  const { data } = await apiClient.post("/api/education/contents", { title, type, mediaUrl });
  return data;
}

export async function atualizarConteudo(id, { title, description, thumbnailUrl, moduleId, orderInModule }) {
  const { data } = await apiClient.put(`/api/education/contents/${id}`, {
    title,
    description,
    thumbnailUrl,
    moduleId,
    orderInModule,
  });
  return data;
}

export async function publicarConteudo(id) {
  const { data } = await apiClient.post(`/api/education/contents/${id}/publish`);
  return data;
}

export async function excluirConteudo(id) {
  await apiClient.delete(`/api/education/contents/${id}`);
}

export async function getMeusModulos() {
  const { data } = await apiClient.get("/api/education/modules/mine");
  return data;
}

export async function criarModulo({ name, description, orderIndex }) {
  const { data } = await apiClient.post("/api/education/modules", { name, description, orderIndex });
  return data;
}
