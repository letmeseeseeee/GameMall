<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ChevronDown, LogIn, LogOut, Search, ShoppingBag, UserRound } from "lucide-vue-next";
import { useCartStore } from "../stores/cart";
import { useCatalogStore } from "../stores/catalog";
import { useSessionStore } from "../stores/session";
import { useUiStore } from "../stores/ui";

const route = useRoute();
const router = useRouter();
const session = useSessionStore();
const catalog = useCatalogStore();
const cart = useCartStore();
const ui = useUiStore();
const searchText = ref(catalog.keyword);
const accountOpen = ref(false);

const initial = computed(() => session.user?.username.slice(0, 1).toUpperCase() || "游");

async function submitSearch() {
  await router.push("/");
  await catalog.search(searchText.value);
  document.querySelector("#catalog")?.scrollIntoView({ behavior: "smooth" });
}

function navigate(path: string) {
  accountOpen.value = false;
  router.push(path);
}

function openAccount() {
  if (!session.loggedIn) {
    ui.authOpen = true;
    return;
  }
  accountOpen.value = !accountOpen.value;
}

function logout() {
  accountOpen.value = false;
  session.logout();
  if (route.name !== "store") router.push("/");
}
</script>

<template>
  <header class="site-header">
    <div class="header-inner">
      <button class="brand-button" type="button" aria-label="返回商城首页" @click="navigate('/')">
        <span class="brand-mark" aria-hidden="true"><span /><span /></span>
        <span>GameMall</span>
      </button>

      <nav class="primary-nav" aria-label="主导航">
        <RouterLink to="/" :class="{ active: route.name === 'store' }">商城</RouterLink>
        <RouterLink to="/orders" :class="{ active: route.name === 'orders' }">订单</RouterLink>
        <RouterLink
          v-if="session.isAdmin"
          to="/admin"
          :class="{ active: route.name === 'admin' }"
        >
          运营后台
        </RouterLink>
      </nav>

      <form class="header-search" role="search" @submit.prevent="submitSearch">
        <Search :size="18" aria-hidden="true" />
        <input
          v-model="searchText"
          type="search"
          placeholder="搜索游戏、开发商"
          autocomplete="off"
          aria-label="搜索游戏"
        />
        <kbd>Enter</kbd>
      </form>

      <div class="header-actions">
        <button
          class="icon-action"
          type="button"
          title="购物车"
          aria-label="打开购物车"
          @click="ui.cartOpen = true"
        >
          <ShoppingBag :size="20" />
          <span v-if="cart.quantity" class="count-badge">{{ cart.quantity }}</span>
        </button>

        <div class="account-wrap">
          <button class="account-action" type="button" @click="openAccount">
            <span class="avatar">{{ initial }}</span>
            <span class="account-copy">
              <strong>{{ session.user?.username || "登录" }}</strong>
              <small>{{ session.isAdmin ? "管理员" : session.loggedIn ? "玩家" : "访客" }}</small>
            </span>
            <ChevronDown v-if="session.loggedIn" :size="16" />
            <LogIn v-else :size="17" />
          </button>
          <div v-if="accountOpen" class="account-menu">
            <div class="account-identity">
              <UserRound :size="20" />
              <span><strong>{{ session.user?.username }}</strong><small>{{ session.isAdmin ? "运营管理员" : "GameMall 玩家" }}</small></span>
            </div>
            <button type="button" @click="logout"><LogOut :size="17" />退出登录</button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
