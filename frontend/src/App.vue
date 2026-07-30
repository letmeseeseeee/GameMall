<script setup lang="ts">
import { onBeforeUnmount, onMounted, watch } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import AppHeader from "./components/AppHeader.vue";
import MobileNav from "./components/MobileNav.vue";
import CartDrawer from "./components/CartDrawer.vue";
import AuthModal from "./components/AuthModal.vue";
import GameDetailModal from "./components/GameDetailModal.vue";
import GameEditorModal from "./components/GameEditorModal.vue";
import ToastHost from "./components/ToastHost.vue";
import { useCartStore } from "./stores/cart";
import { useCatalogStore } from "./stores/catalog";
import { useSessionStore } from "./stores/session";
import { useUiStore } from "./stores/ui";

const route = useRoute();
const router = useRouter();
const session = useSessionStore();
const cart = useCartStore();
const catalog = useCatalogStore();
const ui = useUiStore();

function handleUnauthorized() {
  session.logout(false);
  cart.lines = [];
  ui.authOpen = true;
  ui.toast("登录状态已过期，请重新登录", "error");
}

onMounted(async () => {
  window.addEventListener("gamemall:unauthorized", handleUnauthorized);
  await catalog.initialize();
  if (session.loggedIn) await cart.load();
});

onBeforeUnmount(() => {
  window.removeEventListener("gamemall:unauthorized", handleUnauthorized);
});

watch(
  () => session.loggedIn,
  async (loggedIn) => {
    if (loggedIn) await cart.load();
    else cart.lines = [];
  }
);

watch(
  () => [route.name, session.isAdmin],
  ([name, isAdmin]) => {
    if (name === "admin" && !isAdmin) {
      router.replace("/");
      ui.toast("运营后台仅对管理员开放", "error");
    }
  },
  { immediate: true }
);
</script>

<template>
  <div class="app-shell">
    <AppHeader />
    <main class="main-content">
      <RouterView />
    </main>
    <footer class="site-footer">
      <div class="page-container footer-inner">
        <div class="footer-brand">GameMall</div>
        <span>数字游戏发行与交易平台</span>
        <a href="/swagger-ui.html" target="_blank" rel="noreferrer">开发者 API</a>
      </div>
    </footer>
    <MobileNav />
    <CartDrawer />
    <AuthModal />
    <GameDetailModal />
    <GameEditorModal />
    <ToastHost />
  </div>
</template>
