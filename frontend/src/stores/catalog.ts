import { defineStore } from "pinia";
import { catalogApi } from "../api";
import type { Category, GameSummary, GameTag } from "../types";
import { useUiStore } from "./ui";

export const useCatalogStore = defineStore("catalog", {
  state: () => ({
    categories: [] as Category[],
    tags: [] as GameTag[],
    games: [] as GameSummary[],
    total: 0,
    selectedCategory: null as number | null,
    selectedTag: null as number | null,
    keyword: "",
    loading: false,
    initialized: false
  }),
  getters: {
    selectedCategoryName(state) {
      return (
        state.categories.find((item) => item.id === state.selectedCategory)?.name || "全部游戏"
      );
    },
    tagGroups(state) {
      return state.tags.reduce<Record<string, GameTag[]>>((groups, tag) => {
        (groups[tag.groupName] ||= []).push(tag);
        return groups;
      }, {});
    }
  },
  actions: {
    async initialize() {
      if (this.initialized) return;
      try {
        const [categories, tags] = await Promise.all([
          catalogApi.categories(),
          catalogApi.tags()
        ]);
        this.categories = categories;
        this.tags = tags;
        this.initialized = true;
        await this.loadGames();
      } catch (error) {
        useUiStore().toast((error as Error).message, "error");
      }
    },
    async loadGames() {
      this.loading = true;
      const params: Record<string, string | number> = { page: 1, size: 50 };
      if (this.selectedCategory) params.categoryId = this.selectedCategory;
      if (this.selectedTag) params.tagId = this.selectedTag;
      if (this.keyword.trim()) params.keyword = this.keyword.trim();
      try {
        const result = await catalogApi.games(params);
        this.games = result.records;
        this.total = result.total;
      } catch (error) {
        useUiStore().toast((error as Error).message, "error");
      } finally {
        this.loading = false;
      }
    },
    async chooseCategory(id: number | null) {
      this.selectedCategory = id;
      await this.loadGames();
    },
    async chooseTag(id: number | null) {
      this.selectedTag = this.selectedTag === id ? null : id;
      await this.loadGames();
    },
    async search(keyword: string) {
      this.keyword = keyword;
      await this.loadGames();
    },
    async clearFilters() {
      this.keyword = "";
      this.selectedTag = null;
      this.selectedCategory = null;
      await this.loadGames();
    }
  }
});
