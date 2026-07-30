<script setup lang="ts">
import { CircleAlert, CircleCheck, X } from "lucide-vue-next";
import { useUiStore } from "../stores/ui";

const ui = useUiStore();
</script>

<template>
  <div class="toast-region" aria-live="polite">
    <TransitionGroup name="toast">
      <div v-for="item in ui.toasts" :key="item.id" class="toast" :class="item.type">
        <CircleAlert v-if="item.type === 'error'" :size="19" />
        <CircleCheck v-else :size="19" />
        <span>{{ item.text }}</span>
        <button type="button" aria-label="关闭通知" @click="ui.removeToast(item.id)">
          <X :size="15" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>
