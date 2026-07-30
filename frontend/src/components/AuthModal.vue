<script setup lang="ts">
import { ref, watch } from "vue";
import { Eye, EyeOff, LoaderCircle, X } from "lucide-vue-next";
import { useSessionStore } from "../stores/session";
import { useUiStore } from "../stores/ui";

const session = useSessionStore();
const ui = useUiStore();
const mode = ref<"login" | "register">("login");
const username = ref("");
const password = ref("");
const showPassword = ref(false);

watch(
  () => ui.authOpen,
  (open) => document.body.classList.toggle("locked", open)
);

async function submit() {
  try {
    await session.authenticate(mode.value, username.value.trim(), password.value);
    password.value = "";
  } catch (error) {
    ui.toast((error as Error).message, "error");
  }
}

async function quickAdmin() {
  mode.value = "login";
  username.value = "admin";
  password.value = "admin123";
  await submit();
}
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="ui.authOpen" class="modal-backdrop" @mousedown.self="ui.authOpen = false">
        <section class="modal auth-modal" role="dialog" aria-modal="true" aria-labelledby="auth-title">
          <button class="modal-close icon-button" type="button" aria-label="关闭" @click="ui.authOpen = false">
            <X :size="20" />
          </button>
          <div class="auth-brand">
            <span class="brand-mark" aria-hidden="true"><span /><span /></span>
            <span>GameMall</span>
          </div>
          <p class="section-kicker">PLAYER ACCOUNT</p>
          <h2 id="auth-title">{{ mode === "login" ? "欢迎回来" : "创建玩家账户" }}</h2>
          <p class="modal-subtitle">登录后继续管理购物车与订单。</p>
          <div class="auth-tabs">
            <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
            <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
          </div>
          <form class="form-stack" @submit.prevent="submit">
            <label>
              用户名
              <input v-model="username" required minlength="3" maxlength="32" autocomplete="username" placeholder="请输入用户名" />
            </label>
            <label>
              密码
              <span class="password-field">
                <input
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  required
                  minlength="6"
                  :autocomplete="mode === 'login' ? 'current-password' : 'new-password'"
                  placeholder="至少 6 位"
                />
                <button type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
                  <EyeOff v-if="showPassword" :size="18" />
                  <Eye v-else :size="18" />
                </button>
              </span>
            </label>
            <button class="button button-primary button-wide" type="submit" :disabled="session.submitting">
              <LoaderCircle v-if="session.submitting" class="spin" :size="18" />
              {{ session.submitting ? "请稍候..." : mode === "login" ? "登录" : "注册并登录" }}
            </button>
          </form>
          <button class="quick-admin" type="button" :disabled="session.submitting" @click="quickAdmin">
            使用演示管理员账号
          </button>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>
