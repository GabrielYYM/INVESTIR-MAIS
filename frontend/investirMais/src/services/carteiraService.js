import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
});

// ────────────────────────────────────────────────
// Mapeamento de tipo do frontend → nome da categoria no backend
// ────────────────────────────────────────────────
const TIPO_PARA_CATEGORIA = {
  "Ações nacionais": "AÇÃO NACIONAL",
  "Fundos Imobiliarios": "FUNDOS IMOBILIÁRIOS NACIONAL",
  "Ações internacionais": "AÇÃO INTERNACIONAL",
  "Renda Fixa Nacional": "RENDA FIXA NACIONAL",
  "Criptomoeda": "CRIPTOMOEDA",
  "Renda Fixa Internacional": "RENDA FIXA INTERNACIONAL",
};

const CATEGORIA_PARA_TIPO = Object.fromEntries(
  Object.entries(TIPO_PARA_CATEGORIA).map(([k, v]) => [v, k])
);

// ────────────────────────────────────────────────
// CATEGORIAS
// ────────────────────────────────────────────────

/** Retorna todas as categorias da carteira do usuário */
export async function getCategorias() {
  const { data } = await apiClient.get("/api/assets/categories");
  return data; // [{ id, name, targetPercentage, assets }]
}

/** Busca a categoria pelo nome. Retorna o objeto ou null. */
export async function getCategoriaByNome(nomeCat) {
  const categorias = await getCategorias();
  return categorias.find((c) => c.name === nomeCat) ?? null;
}

// ────────────────────────────────────────────────
// ATIVOS
// ────────────────────────────────────────────────

/**
 * Retorna todos os ativos da carteira, expandidos com valorAtual, percentual, tipo.
 * Pede cotações pela Brapi para calcular valorAtual.
 */
export async function getAtivosDoUsuario() {
  const categorias = await getCategorias();

  // Achata todos os ativos com o campo `tipo` injetado
  const ativos = categorias.flatMap((cat) =>
    (cat.assets ?? []).map((a) => ({
      id: a.id,
      ticker: a.ticker,
      quantidade: Number(a.quantity ?? 0),
      precoMedio: Number(a.averagePrice ?? 0),
      valorAtual: Number(a.currentPositionValue ?? 0),
      percentual: 0, // calculado abaixo
      tipo: CATEGORIA_PARA_TIPO[cat.name] ?? cat.name,
      categoryId: cat.id,
    }))
  );

  const total = ativos.reduce((s, a) => s + a.valorAtual, 0);
  return ativos.map((a) => ({
    ...a,
    percentual: total > 0 ? (a.valorAtual / total) * 100 : 0,
  }));
}

/**
 * Adiciona um ativo enviando para o backend real.
 * Descobre automaticamente o categoryId pelo tipo do ativo.
 */
export async function adicionarAtivo(novoAtivo) {
  // Encontra a categoria correspondente ao tipo
  const nomeCat = TIPO_PARA_CATEGORIA[novoAtivo.tipo] ?? novoAtivo.tipo;
  const categoria = await getCategoriaByNome(nomeCat);

  if (!categoria) {
    throw new Error(`Categoria "${nomeCat}" não encontrada no backend.`);
  }

  const payload = {
    ticker: novoAtivo.ticker,
    currentPositionValue: novoAtivo.currentPositionValue ?? novoAtivo.valorAtual ?? 0,
    quantity: novoAtivo.quantity ?? novoAtivo.quantidade ?? 0,
    averagePrice: novoAtivo.averagePrice ?? novoAtivo.precoMedio ?? 0,
    rawScore: novoAtivo.rawScore ?? null,
  };

  const { data } = await apiClient.post(
    `/api/assets/categories/${categoria.id}/assets`,
    payload
  );

  return {
    id: data.id,
    ticker: data.ticker,
    quantidade: Number(data.quantity ?? 0),
    precoMedio: Number(data.averagePrice ?? 0),
    valorAtual: Number(data.currentPositionValue ?? 0),
    tipo: novoAtivo.tipo,
    categoryId: categoria.id,
  };
}

export default apiClient;
