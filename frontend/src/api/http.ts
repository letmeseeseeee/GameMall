import axios, { AxiosError } from "axios";
import type { ApiResponse } from "../types";

export const http = axios.create({
  baseURL: "/api",
  timeout: 10_000,
  headers: { "Content-Type": "application/json" }
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("gm_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResponse<unknown>;
    if (payload?.code !== 0) {
      return Promise.reject(new Error(payload?.message || "请求失败"));
    }
    return response;
  },
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response?.status === 401) {
      window.dispatchEvent(new CustomEvent("gamemall:unauthorized"));
    }
    const message =
      error.response?.data?.message ||
      (error.code === "ECONNABORTED" ? "请求超时，请稍后重试" : "服务暂时无法连接");
    return Promise.reject(new Error(message));
  }
);

export async function api<T>(url: string, config = {}): Promise<T> {
  const response = await http.request<ApiResponse<T>>({ url, ...config });
  return response.data.data;
}
