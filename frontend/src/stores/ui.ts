import { defineStore } from "pinia";

export interface ToastMessage {
  id: number;
  text: string;
  type: "success" | "error";
}

export const useUiStore = defineStore("ui", {
  state: () => ({
    cartOpen: false,
    authOpen: false,
    detailGameId: null as number | null,
    editingGameId: null as number | null,
    gameEditorOpen: false,
    toasts: [] as ToastMessage[],
    nextToastId: 1
  }),
  actions: {
    toast(text: string, type: ToastMessage["type"] = "success") {
      const id = this.nextToastId++;
      this.toasts.push({ id, text, type });
      window.setTimeout(() => this.removeToast(id), 3000);
    },
    removeToast(id: number) {
      this.toasts = this.toasts.filter((item) => item.id !== id);
    },
    requestLogin() {
      this.cartOpen = false;
      this.authOpen = true;
      this.toast("请先登录后继续", "error");
    }
  }
});
