import { useEffect, useState, useCallback } from "react";
import { getAtivosDoUsuario } from "../services/carteiraService";
import { getQuotes } from "../services/brapiService";

const FALLBACK_PRICES = {
  PETR4: 38.5,
  VALE3: 62.0,
  HGLG11: 161.5,
  AAPL: 185.0,
  QQQ: 480.0,
  BTC: 350000.0,
};

/**
 * Combina os ativos cadastrados pelo usuário com a cotação
 * em tempo real de cada ticker (Brapi), calculando valor atual e
 * percentual de participação na carteira.
 */
export function useCarteira() {
  const [ativos, setAtivos] = useState([]);
  const [valorTotal, setValorTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const carregar = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const ativosDoUsuario = await getAtivosDoUsuario();
      const tickers = ativosDoUsuario.map((a) => a.ticker);

      let cotacoes = new Map();
      try {
        cotacoes = await getQuotes(tickers);
      } catch (e) {
        console.warn("Não foi possível buscar cotações na Brapi, usando fallback:", e);
      }

      const ativosComValor = ativosDoUsuario.map((ativo) => {
        const cotacao = cotacoes.get(ativo.ticker);
        const precoAtual =
          cotacao?.regularMarketPrice ?? FALLBACK_PRICES[ativo.ticker] ?? 100.0;
        const valorAtual = precoAtual * ativo.quantidade;

        return {
          ...ativo,
          precoAtual,
          valorAtual,
          variacao: cotacao?.regularMarketChangePercent ?? null,
        };
      });

      const total = ativosComValor.reduce((soma, a) => soma + a.valorAtual, 0);

      const ativosComPercentual = ativosComValor.map((a) => ({
        ...a,
        percentual: total > 0 ? (a.valorAtual / total) * 100 : 0,
      }));

      setAtivos(ativosComPercentual);
      setValorTotal(total);
    } catch (err) {
      console.error("Erro ao carregar carteira:", err);
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  return { ativos, valorTotal, loading, error, recarregar: carregar };
}

