import { defineStore } from "pinia";
import { authApi } from "../api";
import type { UserSession } from "../types";
import { useUiStore } from "./ui";

function readUser(): UserSession | null {
  try {
    const user = JSON.parse(localStorage.getItem("gm_user") || "null");
    const token = localStorage.getItem("gm_token");
    return user && token ? { ...user, token } : null;
  } catch {
    return null;
  }
}

export const useSessionStore = defineStore("session", {
  state: () => ({
    user: readUser() as UserSession | null,
    submitting: false
  }),
  getters: {
    loggedIn: (state) => Boolean(state.user?.token),
    isAdmin: (state) => state.user?.role === "ADMIN"
  },
  actions: {
    async authenticate(mode: "login" | "register", username: string, password: string) {
      this.submitting = true;
      try {
        const user =
          mode === "login"
            ? await authApi.login(username, password)
            : await authApi.register(username, password);
        this.user = user;
        localStorage.setItem("gm_token", user.token);
        localStorage.setItem("gm_user", JSON.stringify(user));
        useUiStore().authOpen = false;
        useUiStore().toast(mode === "login" ? "登录成功" : "注册成功");
      } finally {
        this.submitting = false;
      }
    },
    logout(showToast = true) {
      this.user = null;
      localStorage.removeItem("gm_token");
      localStorage.removeItem("gm_user");
      if (showToast) useUiStore().toast("已退出登录");
    }
  }
});
