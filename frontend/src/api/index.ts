import { api } from "./http";
import type {
  CartLine,
  Category,
  Game,
  GamePayload,
  GameSummary,
  GameTag,
  Order,
  OrderDetail,
  PageResult,
  UserSession
} from "../types";

export const authApi = {
  login: (username: string, password: string) =>
    api<UserSession>("/auth/login", { method: "POST", data: { username, password } }),
  register: (username: string, password: string) =>
    api<UserSession>("/auth/register", { method: "POST", data: { username, password } })
};

export const catalogApi = {
  categories: () => api<Category[]>("/categories"),
  tags: () => api<GameTag[]>("/tags"),
  games: (params: Record<string, string | number>) =>
    api<PageResult<GameSummary>>("/games", { params }),
  detail: (id: number) => api<Game>(`/games/${id}`)
};

export const cartApi = {
  list: () => api<CartLine[]>("/cart"),
  add: (gameId: number, quantity = 1) =>
    api<void>("/cart", { method: "POST", data: { gameId, quantity } }),
  update: (gameId: number, quantity: number) =>
    api<void>(`/cart/${gameId}`, { method: "PUT", params: { quantity } }),
  remove: (gameId: number) => api<void>(`/cart/${gameId}`, { method: "DELETE" })
};

export const orderApi = {
  list: () => api<Order[]>("/orders", { params: { page: 1, size: 50 } }),
  detail: (id: number) => api<OrderDetail>(`/orders/${id}`),
  create: (idempotencyKey: string) =>
    api<OrderDetail>("/orders", {
      method: "POST",
      data: { fromCart: true, idempotencyKey }
    }),
  pay: (id: number) => api<OrderDetail>(`/orders/${id}/pay`, { method: "POST" }),
  cancel: (id: number) => api<void>(`/orders/${id}/cancel`, { method: "POST" })
};

export const adminApi = {
  create: (payload: GamePayload) =>
    api<Game>("/admin/games", { method: "POST", data: payload }),
  update: (id: number, payload: GamePayload) =>
    api<void>(`/admin/games/${id}`, { method: "PUT", data: payload }),
  status: (id: number, status: number) =>
    api<void>(`/admin/games/${id}/status`, { method: "PATCH", params: { status } })
};
