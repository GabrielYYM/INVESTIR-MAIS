import { useState, useCallback } from "react";

const STORAGE_KEY = "investir_mais_scores";

function loadScores() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || "{}");
  } catch {
    return {};
  }
}

/**
 * Hook para gerenciar o score de avaliação de cada ativo via localStorage.
 * score = (soma respostas / total perguntas) × 10  (0–10)
 *
 * Respostas: { [questionId]: 1 (Positivo) | 0 (Negativo) }
 * Por padrão, todo ativo inicia com 0.0 de score e aumenta conforme os votos nas questões.
 */
export function useScore() {
  const [scores, setScores] = useState(loadScores);

  /** Retorna o objeto { score, answers } de um ativo. Se ainda não avaliado, retorna score 0.0. */
  const getScore = useCallback(
    (ativoId) => {
      const entry = scores[String(ativoId)];
      if (entry) return entry;
      return { score: 0.0, answers: {} };
    },
    [scores]
  );

  /**
   * Salva as respostas e calcula o score de 0 a 10.
   * @param {string|number} ativoId
   * @param {Record<string|number, number>} answers  { questionId -> 0|1 }
   * @param {Array<{id}>} questions  lista de perguntas globais
   */
  const saveScore = useCallback((ativoId, answers, questions) => {
    if (!questions || !questions.length) return 0.0;
    const total = questions.reduce((sum, q) => sum + (answers[q.id] ?? 0), 0);
    const score = parseFloat(((total / questions.length) * 10).toFixed(1));
    const entry = { score, answers };

    setScores((prev) => {
      const next = { ...prev, [String(ativoId)]: entry };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });

    return score;
  }, []);

  return { scores, getScore, saveScore };
}

