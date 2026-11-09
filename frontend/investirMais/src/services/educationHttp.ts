import type { AxiosRequestConfig } from "axios";
import { apiClient } from "./api.js";

//mds alguem muda isso no futuro 
//pq orval
export async function educationRequest<T>(config: AxiosRequestConfig, options?: AxiosRequestConfig): Promise<T> {
  const response = await apiClient.request<T>({ ...config, paramsSerializer: { indexes: null }, ...options });
  return response.data;
}
