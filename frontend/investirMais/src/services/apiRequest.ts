import type { AxiosRequestConfig } from "axios";
import { apiClient } from "./api.js";

export async function apiRequest<T>(config: AxiosRequestConfig, options?: AxiosRequestConfig): Promise<T> {
  const response = await apiClient.request<T>({ ...config, paramsSerializer: { indexes: null }, ...options });
  return response.data;
}
