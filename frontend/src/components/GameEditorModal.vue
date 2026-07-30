<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { LoaderCircle, Save, X } from "lucide-vue-next";
import { adminApi, catalogApi } from "../api";
import { useCatalogStore } from "../stores/catalog";
import { useUiStore } from "../stores/ui";
import type { GamePayload } from "../types";

const ui = useUiStore();
const catalog = useCatalogStore();
const loading = ref(false);
const submitting = ref(false);
const editing = computed(() => ui.editingGameId !== null);

const form = reactive({
  categoryId: 0,
  title: "",
  developer: "",
  publisher: "",
  price: 0,
  stock: 0,
  coverUrl: "",
  description: "",
  releaseDate: "",
  tagIds: [] as number[]
});

function resetForm() {
  Object.assign(form, {
    categoryId: catalog.categories[0]?.id || 0,
    title: "",
    developer: "",
    publisher: "",
    price: 0,
    stock: 0,
    coverUrl: "",
    description: "",
    releaseDate: "",
    tagIds: []
  });
}

watch(
  () => ui.gameEditorOpen,
  async (open) => {
    document.body.classList.toggle("locked", open);
    if (!open) return;
    resetForm();
    if (ui.editingGameId === null) return;
    loading.value = true;
    try {
      const game = await catalogApi.detail(ui.editingGameId);
      Object.assign(form, {
        categoryId: game.categoryId,
        title: game.title,
        developer: game.developer || "",
        publisher: game.publisher || "",
        price: Number(game.price),
        stock: game.stock,
        coverUrl: game.coverUrl || "",
        description: game.description || "",
        releaseDate: game.releaseDate?.slice(0, 16) || "",
        tagIds: game.tags.map((tag) => tag.id)
      });
    } catch (error) {
      ui.toast((error as Error).message, "error");
      ui.gameEditorOpen = false;
    } finally {
      loading.value = false;
    }
  }
);

function toggleTag(id: number) {
  const index = form.tagIds.indexOf(id);
  if (index >= 0) form.tagIds.splice(index, 1);
  else if (form.tagIds.length < 8) form.tagIds.push(id);
  else ui.toast("每个商品最多选择 8 个标签", "error");
}

async function submit() {
  submitting.value = true;
  const payload: GamePayload = {
    ...form,
    price: Number(form.price),
    stock: Number(form.stock),
    releaseDate: form.releaseDate ? `${form.releaseDate}:00` : null,
    tagIds: [...form.tagIds]
  };
  try {
    if (ui.editingGameId !== null) {
      await adminApi.update(ui.editingGameId, payload);
      ui.toast("商品信息已更新");
    } else {
      await adminApi.create(payload);
      ui.toast("商品已创建");
    }
    ui.gameEditorOpen = false;
    await catalog.loadGames();
  } catch (error) {
    ui.toast((error as Error).message, "error");
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="ui.gameEditorOpen" class="modal-backdrop" @mousedown.self="ui.gameEditorOpen = false">
        <section class="modal editor-modal" role="dialog" aria-modal="true" aria-labelledby="editor-title">
          <button class="modal-close icon-button" type="button" aria-label="关闭" @click="ui.gameEditorOpen = false">
            <X :size="20" />
          </button>
          <p class="section-kicker">PRODUCT EDITOR</p>
          <h2 id="editor-title">{{ editing ? "编辑游戏商品" : "新增游戏商品" }}</h2>
          <p class="modal-subtitle">商品变更会同步清理相关 Redis 缓存。</p>
          <div v-if="loading" class="modal-loading"><LoaderCircle class="spin" :size="28" />读取商品信息...</div>
          <form v-else class="editor-form" @submit.prevent="submit">
            <div class="form-grid">
              <label class="wide">游戏名称<input v-model="form.title" required maxlength="100" placeholder="例如：Neon Ronin" /></label>
              <label>分类
                <select v-model.number="form.categoryId" required>
                  <option v-for="category in catalog.categories" :key="category.id" :value="category.id">{{ category.name }}</option>
                </select>
              </label>
              <label>开发商<input v-model="form.developer" maxlength="100" placeholder="开发团队" /></label>
              <label>发行商<input v-model="form.publisher" maxlength="100" placeholder="发行公司" /></label>
              <label>价格（元）<input v-model.number="form.price" required type="number" min="0" step="0.01" /></label>
              <label>可售库存<input v-model.number="form.stock" required type="number" min="0" step="1" /></label>
              <label>发行时间<input v-model="form.releaseDate" type="datetime-local" /></label>
              <label class="wide">封面地址<input v-model="form.coverUrl" type="url" placeholder="https://..." /></label>
              <label class="wide">商品介绍<textarea v-model="form.description" rows="4" maxlength="2000" placeholder="介绍玩法、世界观与核心特色" /></label>
            </div>
            <fieldset class="tag-selector">
              <legend>细分标签 <small>最多选择 8 个</small></legend>
              <div v-for="(tags, group) in catalog.tagGroups" :key="group" class="editor-tag-group">
                <strong>{{ group }}</strong>
                <div>
                  <button
                    v-for="tag in tags"
                    :key="tag.id"
                    type="button"
                    :class="{ active: form.tagIds.includes(tag.id) }"
                    @click="toggleTag(tag.id)"
                  >
                    {{ tag.name }}
                  </button>
                </div>
              </div>
            </fieldset>
            <div class="editor-actions">
              <button class="button button-secondary" type="button" @click="ui.gameEditorOpen = false">取消</button>
              <button class="button button-primary" type="submit" :disabled="submitting">
                <LoaderCircle v-if="submitting" class="spin" :size="17" />
                <Save v-else :size="17" />
                {{ submitting ? "正在保存..." : "保存商品" }}
              </button>
            </div>
          </form>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>
