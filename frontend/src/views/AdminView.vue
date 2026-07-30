<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { Braces, Edit3, PackagePlus, Search, ToggleLeft, ToggleRight } from "lucide-vue-next";
import { adminApi } from "../api";
import { gameCover, money } from "../lib";
import { useCatalogStore } from "../stores/catalog";
import { useSessionStore } from "../stores/session";
import { useUiStore } from "../stores/ui";

const session = useSessionStore();
const catalog = useCatalogStore();
const ui = useUiStore();
const keyword = ref("");
const statusBusy = ref<number | null>(null);

const rows = computed(() => {
  const term = keyword.value.trim().toLowerCase();
  if (!term) return catalog.games;
  return catalog.games.filter((game) =>
    [game.title, game.developer, game.categoryName].some((value) =>
      value?.toLowerCase().includes(term)
    )
  );
});

const metrics = computed(() => ({
  games: catalog.games.length,
  stock: catalog.games.reduce((sum, game) => sum + game.stock, 0),
  sold: catalog.games.reduce((sum, game) => sum + game.soldCount, 0),
  lowStock: catalog.games.filter((game) => game.stock < 100).length
}));

function editGame(id: number) {
  ui.editingGameId = id;
  ui.gameEditorOpen = true;
}

function createGame() {
  ui.editingGameId = null;
  ui.gameEditorOpen = true;
}

async function disableGame(id: number) {
  statusBusy.value = id;
  try {
    await adminApi.status(id, 0);
    ui.toast("商品已下架");
    await catalog.loadGames();
  } catch (error) {
    ui.toast((error as Error).message, "error");
  } finally {
    statusBusy.value = null;
  }
}

onMounted(() => {
  if (session.isAdmin) catalog.loadGames();
});
</script>

<template>
  <section v-if="session.isAdmin" class="admin-page">
    <div class="page-container admin-container">
      <div class="page-title-row admin-title-row">
        <div>
          <p class="section-kicker">OPERATIONS CONSOLE</p>
          <h1>商品运营</h1>
          <p>维护在售游戏、价格、标签与可售库存。</p>
        </div>
        <div class="title-actions">
          <a class="button button-secondary" href="/swagger-ui.html" target="_blank" rel="noreferrer">
            <Braces :size="17" />API 文档
          </a>
          <button class="button button-primary" type="button" @click="createGame">
            <PackagePlus :size="17" />新增商品
          </button>
        </div>
      </div>

      <div class="admin-metrics">
        <div><span>在售商品</span><strong>{{ metrics.games }}</strong><small>当前可见 SKU</small></div>
        <div><span>可售库存</span><strong>{{ metrics.stock }}</strong><small>实时库存合计</small></div>
        <div><span>累计销量</span><strong>{{ metrics.sold }}</strong><small>在售商品销量</small></div>
        <div><span>库存预警</span><strong>{{ metrics.lowStock }}</strong><small>低于 100 份</small></div>
      </div>

      <div class="admin-toolbar">
        <label class="table-search">
          <Search :size="17" />
          <input v-model="keyword" type="search" placeholder="筛选当前商品" aria-label="筛选商品" />
        </label>
        <span>{{ rows.length }} 条商品记录</span>
      </div>

      <div class="table-wrap">
        <table class="product-table">
          <thead>
            <tr><th>商品</th><th>分类</th><th>价格</th><th>库存</th><th>销量</th><th>状态</th><th><span class="sr-only">操作</span></th></tr>
          </thead>
          <tbody>
            <tr v-for="game in rows" :key="game.id">
              <td>
                <div class="table-product">
                  <img :src="gameCover(game)" :alt="game.title" />
                  <span><strong>{{ game.title }}</strong><small>{{ game.developer }}</small></span>
                </div>
              </td>
              <td>{{ game.categoryName }}</td>
              <td><strong>{{ money(game.price) }}</strong></td>
              <td><span :class="{ 'stock-low': game.stock < 100 }">{{ game.stock }}</span></td>
              <td>{{ game.soldCount }}</td>
              <td><span class="status-pill status-live">在售</span></td>
              <td>
                <div class="table-actions">
                  <button type="button" title="编辑商品" aria-label="编辑商品" @click="editGame(game.id)">
                    <Edit3 :size="17" />
                  </button>
                  <button
                    type="button"
                    title="下架商品"
                    aria-label="下架商品"
                    :disabled="statusBusy === game.id"
                    @click="disableGame(game.id)"
                  >
                    <ToggleLeft v-if="statusBusy === game.id" :size="18" />
                    <ToggleRight v-else :size="18" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!rows.length" class="table-empty">没有匹配的商品记录</div>
      </div>
    </div>
  </section>
</template>
