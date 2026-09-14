import axios from "axios";

// Token gerado em https://brapi.dev/dashboard
// Nunca deixe o token hardcoded em produção — use variável de ambiente do Vite/CRA.
const BRAPI_BASE_URL = "https://brapi.dev/api";
const BRAPI_TOKEN = import.meta.env.VITE_BRAPI_TOKEN;

const brapiClient = axios.create({
  baseURL: BRAPI_BASE_URL,
  timeout: 10000,
});

/**
 * Busca a cotação atual de um ou mais tickers na Brapi.
 * A Brapi aceita múltiplos tickers separados por vírgula em uma única chamada,
 * então sempre agrupamos os pedidos em vez de fazer uma requisição por ativo.
 *
 * @param {string[]} tickers - ex: ["QQQ", "FLRY3", "WEGE3", "MCHI"]
 * @returns {Promise<Map<string, object>>} mapa ticker -> dados da cotação
 */
export async function getQuotes(tickers) {
  if (!tickers || tickers.length === 0) return new Map();

  const uniqueTickers = [...new Set(tickers)].join(",");

  const { data } = await brapiClient.get(`/quote/${uniqueTickers}`, {
    params: { token: BRAPI_TOKEN },
  });

  const quoteMap = new Map();
  (data.results || []).forEach((result) => {
    quoteMap.set(result.symbol, result);
  });

  return quoteMap;
}

export default brapiClient;
