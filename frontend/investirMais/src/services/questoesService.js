import apiClient from "./carteiraService";

/**
 * Busca as perguntas de uma categoria específica do backend.
 * @param {string} categoryId - UUID da categoria
 */
export async function getQuestoesPorCategoria(categoryId) {
  const { data } = await apiClient.get(`/api/questions/categories/${categoryId}`);
  // Normaliza para o formato usado pelo frontend: { id, text }
  return data.map((q) => ({ id: q.id, text: q.text }));
}

/**
 * Cria uma nova pergunta vinculada a uma categoria.
 * @param {string} categoryId - UUID da categoria
 * @param {string} text - Texto da pergunta
 */
export async function criarQuestao(categoryId, text) {
  const { data } = await apiClient.post(`/api/questions/categories/${categoryId}`, { text });
  return { id: data.id, text: data.text };
}

/**
 * Atualiza uma pergunta existente.
 * @param {string} questionId - UUID da questão
 * @param {string} text - Novo texto
 */
export async function atualizarQuestao(questionId, text) {
  const { data } = await apiClient.put(`/api/questions/${questionId}`, { text });
  return { id: data.id, text: data.text };
}

/**
 * Exclui uma pergunta pelo ID.
 * @param {string} questionId - UUID da questão
 */
export async function excluirQuestao(questionId) {
  await apiClient.delete(`/api/questions/${questionId}`);
}
