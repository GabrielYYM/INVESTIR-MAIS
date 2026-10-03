import apiClient from "./carteiraService";

/**
 * Busca as perguntas associadas a um tipo de ativo.
 * @param {string} role - Tipo de ativo
 */
export async function getQuestoesPorCategoria(role) {
  const { data } = await apiClient.get(`/api/questions/roles/${encodeURIComponent(role)}`);
  // Normaliza para o formato usado pelo frontend: { id, text }
  return (data.content ?? data).map((q) => ({ id: q.id, text: q.text, role: q.role }));
}

/**
 * Cria uma pergunta associada a um tipo de ativo.
 * @param {string} role - Tipo de ativo
 * @param {string} text - Texto da pergunta
 */
export async function criarQuestao(role, text) {
  const { data } = await apiClient.post("/api/questions", { text, role });
  return { id: data.id, text: data.text, role: data.role };
}

/**
 * Atualiza uma pergunta existente.
 * @param {string} questionId - UUID da questão
 * @param {string} text - Novo texto
 */
export async function atualizarQuestao(questionId, text) {
  const { data } = await apiClient.put(`/api/questions/${questionId}`, { text });
  return { id: data.id, text: data.text, role: data.role };
}

/**
 * Exclui uma pergunta pelo ID.
 * @param {string} questionId - UUID da questão
 */
export async function excluirQuestao(questionId) {
  await apiClient.delete(`/api/questions/${questionId}`);
}
