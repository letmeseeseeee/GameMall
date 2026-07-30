<script setup lang="ts">
import { computed, watch } from "vue";
import { ArrowRight, Minus, Plus, ShoppingBag, Trash2, X } from "lucide-vue-next";
import { useRouter } from "vue-router";
import { gameCover, money } from "../lib";
import { useCartStore } from "../stores/cart";
import { useSessionStore } from "../stores/session";
import { useUiStore } from "../stores/ui";

const router = useRouter();
const cart = useCartStore();
const session = useSessionStore();
const ui = useUiStore();
const drawerLabel = computed(() => (session.loggedIn ? "购物车" : "尚未登录"));

watch(
  () => ui.cartOpen,
  async (open) => {
    document.body.classList.toggle("locked", open);
    if (open && session.loggedIn) await cart.load();
  }
);

async function checkout() {
  const order = await cart.checkout();
  if (order) router.push("/orders");
}
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <button
        v-if="ui.cartOpen"
        class="drawer-backdrop"
        type="button"
        aria-label="关闭购物车"
        @click="ui.cartOpen = false"
      />
    </Transition>
    <Transition name="drawer">
      <aside v-if="ui.cartOpen" class="cart-drawer" :aria-label="drawerLabel">
        <div class="drawer-head">
          <div><p>YOUR CART</p><h2>{{ drawerLabel }}</h2></div>
          <button class="icon-button" type="button" aria-label="关闭购物车" @click="ui.cartOpen = false">
            <X :size="21" />
          </button>
        </div>

        <div v-if="!session.loggedIn" class="drawer-empty">
          <ShoppingBag :size="38" />
          <h3>登录后管理购物车</h3>
          <p>你的商品和订单会与账户安全关联。</p>
          <button class="button button-primary" type="button" @click="ui.requestLogin">登录账户</button>
        </div>
        <div v-else-if="cart.loading" class="drawer-loading">正在读取购物车...</div>
        <div v-else-if="!cart.lines.length" class="drawer-empty">
          <ShoppingBag :size="38" />
          <h3>购物车还是空的</h3>
          <p>挑一款游戏，开始你的下一段旅程。</p>
          <button class="button button-secondary" type="button" @click="ui.cartOpen = false">继续浏览</button>
        </div>
        <div v-else class="cart-lines">
          <article v-for="line in cart.lines" :key="line.gameId" class="cart-line">
            <img :src="gameCover(line)" :alt="line.title" />
            <div class="cart-line-copy">
              <strong>{{ line.title }}</strong>
              <small>{{ money(line.price) }} / 件</small>
              <div class="quantity-stepper">
                <button type="button" aria-label="减少数量" @click="cart.setQuantity(line, line.quantity - 1)">
                  <Minus :size="14" />
                </button>
                <span>{{ line.quantity }}</span>
                <button
                  type="button"
                  aria-label="增加数量"
                  :disabled="line.quantity >= line.stock"
                  @click="cart.setQuantity(line, line.quantity + 1)"
                >
                  <Plus :size="14" />
                </button>
              </div>
            </div>
            <div class="cart-line-end">
              <strong>{{ money(line.subtotal) }}</strong>
              <button type="button" title="移除商品" aria-label="移除商品" @click="cart.remove(line.gameId)">
                <Trash2 :size="17" />
              </button>
            </div>
          </article>
        </div>

        <div v-if="session.loggedIn && cart.lines.length" class="cart-summary">
          <div><span>商品数量</span><strong>{{ cart.quantity }} 件</strong></div>
          <div class="cart-total"><span>合计</span><strong>{{ money(cart.total) }}</strong></div>
          <button class="button button-primary button-wide" type="button" :disabled="cart.checkingOut" @click="checkout">
            {{ cart.checkingOut ? "正在创建订单..." : "提交订单" }} <ArrowRight :size="18" />
          </button>
          <small>订单提交后将锁定库存 30 分钟</small>
        </div>
      </aside>
    </Transition>
  </Teleport>
</template>
