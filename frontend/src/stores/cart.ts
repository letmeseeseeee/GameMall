import { defineStore } from "pinia";
import { cartApi, orderApi } from "../api";
import type { CartLine } from "../types";
import { useSessionStore } from "./session";
import { useUiStore } from "./ui";

export const useCartStore = defineStore("cart", {
  state: () => ({
    lines: [] as CartLine[],
    loading: false,
    checkingOut: false
  }),
  getters: {
    quantity: (state) => state.lines.reduce((sum, line) => sum + line.quantity, 0),
    total: (state) => state.lines.reduce((sum, line) => sum + Number(line.subtotal), 0)
  },
  actions: {
    async load() {
      if (!useSessionStore().loggedIn) {
        this.lines = [];
        return;
      }
      this.loading = true;
      try {
        this.lines = await cartApi.list();
      } catch (error) {
        useUiStore().toast((error as Error).message, "error");
      } finally {
        this.loading = false;
      }
    },
    async add(gameId: number) {
      if (!useSessionStore().loggedIn) {
        useUiStore().requestLogin();
        return;
      }
      try {
        await cartApi.add(gameId);
        await this.load();
        useUiStore().toast("已加入购物车");
      } catch (error) {
        useUiStore().toast((error as Error).message, "error");
      }
    },
    async setQuantity(line: CartLine, quantity: number) {
      if (quantity < 1) return this.remove(line.gameId);
      try {
        await cartApi.update(line.gameId, Math.min(quantity, line.stock));
        await this.load();
      } catch (error) {
        useUiStore().toast((error as Error).message, "error");
      }
    },
    async remove(gameId: number) {
      try {
        await cartApi.remove(gameId);
        await this.load();
      } catch (error) {
        useUiStore().toast((error as Error).message, "error");
      }
    },
    async checkout() {
      if (!this.lines.length || this.checkingOut) return;
      this.checkingOut = true;
      try {
        const key = crypto.randomUUID();
        const detail = await orderApi.create(key);
        this.lines = [];
        useUiStore().cartOpen = false;
        useUiStore().toast(`订单 ${detail.order.orderNo} 创建成功`);
        return detail;
      } catch (error) {
        useUiStore().toast((error as Error).message, "error");
      } finally {
        this.checkingOut = false;
      }
    }
  }
});
