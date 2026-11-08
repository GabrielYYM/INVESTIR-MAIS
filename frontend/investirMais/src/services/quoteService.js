import { apiClient } from "./api";

export async function buscarCotacao(ticker, signal) {
  const { data } = await apiClient.get(`/api/assets/${encodeURIComponent(ticker)}/quote`, { signal });
  return data;
}
