<script setup lang="ts">
import { CircleUserRound, LayoutDashboard, ReceiptText, ShoppingBag, Store } from "lucide-vue-next";
import { useRoute, useRouter } from "vue-router";
import { useCartStore } from "../stores/cart";
import { useSessionStore } from "../stores/session";
import { useUiStore } from "../stores/ui";

const route = useRoute();
const router = useRouter();
const session = useSessionStore();
const cart = useCartStore();
const ui = useUiStore();

function account() {
  if (session.loggedIn) router.push("/orders");
  else ui.authOpen = true;
}
</script>

<template>
  <nav class="mobile-nav" aria-label="移动端导航">
    <button :class="{ active: route.name === 'store' }" type="button" @click="router.push('/')">
      <Store :size="20" /><span>商城</span>
    </button>
    <button :class="{ active: route.name === 'orders' }" type="button" @click="router.push('/orders')">
      <ReceiptText :size="20" /><span>订单</span>
    </button>
    <button type="button" @click="ui.cartOpen = true">
      <ShoppingBag :size="20" /><span>购物车</span><b v-if="cart.quantity">{{ cart.quantity }}</b>
    </button>
    <button
      v-if="session.isAdmin"
      :class="{ active: route.name === 'admin' }"
      type="button"
      @click="router.push('/admin')"
    >
      <LayoutDashboard :size="20" /><span>运营</span>
    </button>
    <button v-else type="button" @click="account">
      <CircleUserRound :size="20" /><span>账户</span>
    </button>
  </nav>
</template>
