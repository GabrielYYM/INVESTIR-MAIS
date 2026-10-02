import { apiClient } from "./authService";

/**
 * Cotações de ativos via backend do Investir+.
 *
 * O front não chama mais a Brapi diretamente: quem consulta a Brapi é o
 * backend (BrapiService), usando o token guardado no .env do servidor.
 * Assim o token nunca aparece no navegador.
 *
 * Endpoint usado: GET /api/assets/{ticker}
 * Resposta: { results: [{ symbol, shortName, regularMarketPrice,
 *             regularMarketChange, regularMarketChangePercent, currency }] }
 */

/**
 * Busca a cotação atual de um ou mais tickers.
 * Faz uma requisição por ticker, em sequência, para que a falha de um ativo
 * (ticker inválido ou fora do plano da Brapi) não derrube os demais.
 *
 * @param {string[]} tickers - ex: ["PETR4", "VALE3", "ITUB4"]
 * @returns {Promise<Map<string, object>>} mapa ticker -> dados da cotação
 */
export async function getQuotes(tickers) {
  if (!tickers || tickers.length === 0) return new Map();

  const uniqueTickers = [...new Set(tickers.map((t) => t.trim().toUpperCase()))];

  // A Brapi aceita só uma requisição simultânea no plano atual,
  // então os tickers são consultados um de cada vez (em sequência).
  const quoteMap = new Map();
  for (const ticker of uniqueTickers) {
    try {
      const { data } = await apiClient.get(`/api/assets/${encodeURIComponent(ticker)}`);
      (data?.results || []).forEach((result) => {
        quoteMap.set(result.symbol, result);
      });
    } catch (err) {
      const status = err?.response?.status;
      console.warn(
        `Sem cotação para ${ticker}${status ? ` (HTTP ${status})` : ""}, usando valor de fallback.`
      );
    }
  }

  return quoteMap;
}

/**
 * Busca a cotação de um único ticker. Retorna null se não encontrar.
 */
export async function getQuote(ticker) {
  const quotes = await getQuotes([ticker]);
  return quotes.get(ticker.trim().toUpperCase()) ?? null;
}