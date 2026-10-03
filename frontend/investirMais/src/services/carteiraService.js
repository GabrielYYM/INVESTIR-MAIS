import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

// Interceptor: adiciona o token JWT automaticamente em todas as requisições
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("authToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ────────────────────────────────────────────────
const ROLES = ["AÇÕES", "RENDA_FIXA", "CRIPTOMOEDAS", "FUNDOS_IMOBILIARIOS", "INTERNACIONAL"];
const TIPO_PARA_ROLE = {
  "Ações nacionais": "AÇÕES", "Ações internacionais": "INTERNACIONAL",
  "Fundos Imobiliarios": "FUNDOS_IMOBILIARIOS", "Renda Fixa Nacional": "RENDA_FIXA",
  "Renda Fixa Internacional": "RENDA_FIXA", "Criptomoeda": "CRIPTOMOEDAS",
};
const ROLE_PARA_TIPO = {
  "AÇÕES": "Ações nacionais", RENDA_FIXA: "Renda Fixa Nacional",
  CRIPTOMOEDAS: "Criptomoeda", FUNDOS_IMOBILIARIOS: "Fundos Imobiliarios",
  INTERNACIONAL: "Ações internacionais",
};

// ────────────────────────────────────────────────
// CATEGORIAS
// ────────────────────────────────────────────────

/** Retorna todas as categorias da carteira do usuário */
export async function getCategorias() {
  return Promise.all(ROLES.map(async (role) => {
    const { data } = await apiClient.get(`/api/assets/roles/${encodeURIComponent(role)}`);
    return { id: role, role, name: ROLE_PARA_TIPO[role], assets: data };
  }));
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
      tipo: ROLE_PARA_TIPO[cat.role],
      role: cat.role,
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
 * Usa o papel do ativo selecionado como classificação.
 */
export async function adicionarAtivo(novoAtivo) {
  const role = TIPO_PARA_ROLE[novoAtivo.tipo] ?? novoAtivo.role;
  if (!role) throw new Error("Tipo de ativo inválido.");

  const payload = {
    ticker: novoAtivo.ticker,
    currentPositionValue: novoAtivo.currentPositionValue ?? novoAtivo.valorAtual ?? 0,
    quantity: novoAtivo.quantity ?? novoAtivo.quantidade ?? 0,
    averagePrice: novoAtivo.averagePrice ?? novoAtivo.precoMedio ?? 0,
    rawScore: novoAtivo.rawScore ?? null,
    role,
  };

  const { data } = await apiClient.post("/api/assets", payload);

  return {
    id: data.id,
    ticker: data.ticker,
    quantidade: Number(data.quantity ?? 0),
    precoMedio: Number(data.averagePrice ?? 0),
    valorAtual: Number(data.currentPositionValue ?? 0),
    tipo: novoAtivo.tipo,
    role,
  };
}

/**
 * Atualiza um ativo existente no backend.
 */
export async function atualizarAtivo(id, ativoAtualizado) {
  if (!id) {
    throw new Error("ID do ativo é obrigatório para atualização.");
  }

  const payload = {
    id: id,
    ticker: ativoAtualizado.ticker,
    currentPositionValue: ativoAtualizado.currentPositionValue ?? ativoAtualizado.valorAtual ?? 0,
    quantity: ativoAtualizado.quantity ?? ativoAtualizado.quantidade ?? 0,
    averagePrice: ativoAtualizado.averagePrice ?? ativoAtualizado.precoMedio ?? 0,
  };

  const { data } = await apiClient.put(`/api/assets/${id}`, payload);
  return data;
}

/**
 * Exclui um ativo no backend pelo ID.
 */
export async function excluirAtivo(id) {
  if (!id) {
    throw new Error("ID do ativo é obrigatório para exclusão.");
  }
  await apiClient.delete(`/api/assets/${id}`);
}

export default apiClient;
