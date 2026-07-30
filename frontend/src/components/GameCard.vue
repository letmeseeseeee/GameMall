<script setup lang="ts">
import { ArrowUpRight, Plus, ShoppingBag } from "lucide-vue-next";
import type { GameSummary } from "../types";
import { gameCover, money } from "../lib";
import { useCartStore } from "../stores/cart";
import { useUiStore } from "../stores/ui";

defineProps<{ game: GameSummary }>();

const cart = useCartStore();
const ui = useUiStore();
</script>

<template>
  <article class="game-card">
    <button class="game-cover" type="button" @click="ui.detailGameId = game.id">
      <img :src="gameCover(game)" :alt="`${game.title} 游戏封面`" loading="lazy" />
      <span class="cover-category">{{ game.categoryName }}</span>
      <span class="cover-open"><ArrowUpRight :size="18" /></span>
    </button>
    <div class="game-card-body">
      <button class="game-title" type="button" @click="ui.detailGameId = game.id">
        {{ game.title }}
      </button>
      <p>{{ game.developer || "独立开发团队" }}</p>
      <div class="game-tags">
        <span v-for="tag in game.tags.slice(0, 3)" :key="tag.id">{{ tag.name }}</span>
      </div>
      <div class="game-card-foot">
        <div>
          <strong>{{ money(game.price) }}</strong>
          <small>已售 {{ game.soldCount }}</small>
        </div>
        <button
          class="add-cart-button"
          type="button"
          :disabled="game.stock <= 0"
          :title="game.stock > 0 ? '加入购物车' : '暂时缺货'"
          :aria-label="game.stock > 0 ? `将 ${game.title} 加入购物车` : `${game.title} 暂时缺货`"
          @click="cart.add(game.id)"
        >
          <ShoppingBag :size="18" />
          <Plus :size="12" />
        </button>
      </div>
    </div>
  </article>
</template>
