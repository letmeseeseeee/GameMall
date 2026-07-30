<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { CalendarDays, Code2, LoaderCircle, PackageCheck, ShoppingBag, X } from "lucide-vue-next";
import { catalogApi } from "../api";
import { gameCover, money } from "../lib";
import { useCartStore } from "../stores/cart";
import { useUiStore } from "../stores/ui";
import type { Game } from "../types";

const ui = useUiStore();
const cart = useCartStore();
const game = ref<Game | null>(null);
const loading = ref(false);
const open = computed(() => ui.detailGameId !== null);

watch(
  () => ui.detailGameId,
  async (id) => {
    document.body.classList.toggle("locked", id !== null);
    game.value = null;
    if (id === null) return;
    loading.value = true;
    try {
      game.value = await catalogApi.detail(id);
    } catch (error) {
      ui.toast((error as Error).message, "error");
      ui.detailGameId = null;
    } finally {
      loading.value = false;
    }
  }
);

async function addToCart() {
  if (!game.value) return;
  await cart.add(game.value.id);
}
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="open" class="modal-backdrop" @mousedown.self="ui.detailGameId = null">
        <section class="modal detail-modal" role="dialog" aria-modal="true" aria-labelledby="detail-title">
          <button class="modal-close icon-button light-close" type="button" aria-label="关闭" @click="ui.detailGameId = null">
            <X :size="21" />
          </button>
          <div v-if="loading" class="modal-loading"><LoaderCircle class="spin" :size="30" />读取游戏详情...</div>
          <template v-else-if="game">
            <div class="detail-cover">
              <img :src="gameCover(game)" :alt="`${game.title} 游戏封面`" />
              <div class="detail-cover-shade" />
              <div class="detail-cover-copy">
                <span>{{ game.categoryName }}</span>
                <h2 id="detail-title">{{ game.title }}</h2>
                <p>{{ game.developer }}</p>
              </div>
            </div>
            <div class="detail-body">
              <div class="detail-tags">
                <span v-for="tag in game.tags" :key="tag.id">{{ tag.name }}</span>
              </div>
              <p class="detail-description">{{ game.description || "这款游戏正在等待更多介绍。" }}</p>
              <div class="detail-meta">
                <div><Code2 :size="18" /><span>发行商<strong>{{ game.publisher || game.developer || "-" }}</strong></span></div>
                <div><CalendarDays :size="18" /><span>发行时间<strong>{{ game.releaseDate?.slice(0, 10) || "待定" }}</strong></span></div>
                <div><PackageCheck :size="18" /><span>可售库存<strong>{{ game.stock }} 份</strong></span></div>
              </div>
              <div class="detail-purchase">
                <div><small>数字版售价</small><strong>{{ money(game.price) }}</strong></div>
                <button class="button button-primary" type="button" :disabled="game.stock <= 0" @click="addToCart">
                  <ShoppingBag :size="18" />{{ game.stock > 0 ? "加入购物车" : "暂时缺货" }}
                </button>
              </div>
            </div>
          </template>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>
