<script setup lang="ts">
import { computed, nextTick, ref } from "vue";
import {
  Blocks,
  ChevronDown,
  Gamepad2,
  Gauge,
  Search,
  Sparkles,
  Swords,
  X,
  ScrollText
} from "lucide-vue-next";
import GameCard from "../components/GameCard.vue";
import { useCatalogStore } from "../stores/catalog";
import { useUiStore } from "../stores/ui";

const catalog = useCatalogStore();
const ui = useUiStore();
const mobileFiltersOpen = ref(false);

const categoryIcon = {
  action: Swords,
  rpg: ScrollText,
  strategy: Blocks,
  indie: Sparkles,
  sports: Gauge
} as Record<string, typeof Gamepad2>;

const activeTagName = computed(
  () => catalog.tags.find((tag) => tag.id === catalog.selectedTag)?.name
);

async function explore() {
  await nextTick();
  document.querySelector("#catalog")?.scrollIntoView({ behavior: "smooth" });
}

async function openFeatured() {
  if (!catalog.games.length) await catalog.loadGames();
  const featured = catalog.games.find((game) => game.title === "Neon Ronin") || catalog.games[0];
  if (featured) ui.detailGameId = featured.id;
}
</script>

<template>
  <div>
    <section class="hero" aria-labelledby="hero-title">
      <img src="/assets/gamemall-hero.png" alt="未来海岸都市游戏场景" />
      <div class="hero-shade" />
      <div class="hero-content page-container">
        <p class="eyebrow">本周编辑精选</p>
        <h1 id="hero-title">GameMall</h1>
        <p class="hero-lead">
          发现下一段值得投入的旅程。动作、策略、角色扮演与独立佳作，即刻入库。
        </p>
        <div class="hero-actions">
          <button class="button button-primary" type="button" @click="explore">
            浏览游戏 <ChevronDown :size="18" />
          </button>
          <button class="button button-glass" type="button" @click="openFeatured">
            查看《Neon Ronin》
          </button>
        </div>
      </div>
      <div class="hero-release">
        <span>FEATURED RELEASE</span>
        <strong>Neon Ronin</strong>
        <small>赛博都市 · 高速动作</small>
      </div>
    </section>

    <section id="catalog" class="catalog-band">
      <div class="page-container">
        <div class="section-heading">
          <div>
            <p class="section-kicker">STORE CATALOG</p>
            <h2>探索游戏</h2>
          </div>
          <p>共 {{ catalog.total }} 款游戏</p>
        </div>

        <div class="genre-grid" aria-label="按游戏类型浏览">
          <button
            v-for="category in catalog.categories"
            :key="category.id"
            class="genre-card"
            :class="{ active: catalog.selectedCategory === category.id }"
            type="button"
            @click="catalog.chooseCategory(category.id)"
          >
            <span class="genre-icon">
              <component :is="categoryIcon[category.code] || Gamepad2" :size="24" />
            </span>
            <span class="genre-copy">
              <strong>{{ category.name }}</strong>
              <small>{{ category.description || "精选数字游戏" }}</small>
            </span>
            <span class="genre-count">{{ category.gameCount }}<small>款</small></span>
          </button>
        </div>

        <div class="catalog-list-heading">
          <div>
            <span>全部商品</span>
            <strong>{{ catalog.selectedCategoryName }}</strong>
          </div>
          <small>支持类型、标签与关键词组合筛选</small>
        </div>

        <div class="catalog-tools">
          <div class="category-tabs" aria-label="游戏分类">
            <button
              type="button"
              :class="{ active: catalog.selectedCategory === null }"
              @click="catalog.chooseCategory(null)"
            >
              全部游戏
            </button>
            <button
              v-for="category in catalog.categories"
              :key="category.id"
              type="button"
              :class="{ active: catalog.selectedCategory === category.id }"
              @click="catalog.chooseCategory(category.id)"
            >
              {{ category.name }}
            </button>
          </div>
          <button class="filter-button" type="button" @click="mobileFiltersOpen = !mobileFiltersOpen">
            <Search :size="17" />筛选
          </button>
        </div>

        <div class="tag-filter-row" :class="{ open: mobileFiltersOpen }">
          <span>细分标签</span>
          <div class="tag-filter-groups">
            <div v-for="(tags, group) in catalog.tagGroups" :key="group" class="tag-filter-group">
              <b>{{ group }}</b>
              <div>
                <button
                  v-for="tag in tags"
                  :key="tag.id"
                  type="button"
                  :class="{ active: catalog.selectedTag === tag.id }"
                  @click="catalog.chooseTag(tag.id)"
                >
                  {{ tag.name }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div
          v-if="catalog.keyword || catalog.selectedTag !== null"
          class="active-search"
        >
          <span>
            当前筛选：
            <strong v-if="catalog.keyword">“{{ catalog.keyword }}”</strong>
            <strong v-if="activeTagName">{{ activeTagName }}</strong>
          </span>
          <button type="button" aria-label="清除筛选" @click="catalog.clearFilters">
            <X :size="16" />清除
          </button>
        </div>

        <div v-if="catalog.loading" class="games-grid" aria-busy="true">
          <div v-for="index in 8" :key="index" class="skeleton-card" />
        </div>
        <div v-else-if="catalog.games.length" class="games-grid">
          <GameCard v-for="game in catalog.games" :key="game.id" :game="game" />
        </div>
        <div v-else class="empty-state">
          <Gamepad2 :size="36" />
          <h3>没有找到匹配的游戏</h3>
          <p>换一个分类、标签或搜索词再试试。</p>
          <button class="button button-secondary" type="button" @click="catalog.clearFilters">
            查看全部游戏
          </button>
        </div>
      </div>
    </section>
  </div>
</template>
