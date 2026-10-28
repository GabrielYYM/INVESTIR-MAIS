import { apiClient } from "./api";

// Brapi
export async function buscarCotacao(ticker) {
  const { data } = await apiClient.get(`/api/assets/${encodeURIComponent(ticker)}/quote`);
  return data;
}
