<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import {
  Clock3,
  CreditCard,
  LoaderCircle,
  PackageOpen,
  ReceiptText,
  RefreshCw,
  RotateCcw,
  X
} from "lucide-vue-next";
import { orderApi } from "../api";
import { formatTime, money, orderStatus } from "../lib";
import { useSessionStore } from "../stores/session";
import { useUiStore } from "../stores/ui";
import type { Order, OrderDetail } from "../types";

const session = useSessionStore();
const ui = useUiStore();
const orders = ref<Order[]>([]);
const loading = ref(false);
const actionId = ref<number | null>(null);
const filter = ref<"all" | "pending" | "paid" | "closed">("all");
const detail = ref<OrderDetail | null>(null);
const detailLoading = ref(false);

const filteredOrders = computed(() => {
  if (filter.value === "pending") return orders.value.filter((order) => order.status === 10);
  if (filter.value === "paid") return orders.value.filter((order) => order.status === 20);
  if (filter.value === "closed") return orders.value.filter((order) => [30, 40].includes(order.status));
  return orders.value;
});

const counts = computed(() => ({
  all: orders.value.length,
  pending: orders.value.filter((order) => order.status === 10).length,
  paid: orders.value.filter((order) => order.status === 20).length,
  closed: orders.value.filter((order) => [30, 40].includes(order.status)).length
}));

async function loadOrders() {
  if (!session.loggedIn) return;
  loading.value = true;
  try {
    orders.value = await orderApi.list();
  } catch (error) {
    ui.toast((error as Error).message, "error");
  } finally {
    loading.value = false;
  }
}

async function openDetail(id: number) {
  detail.value = null;
  detailLoading.value = true;
  try {
    detail.value = await orderApi.detail(id);
  } catch (error) {
    ui.toast((error as Error).message, "error");
  } finally {
    detailLoading.value = false;
  }
}

async function pay(order: Order) {
  actionId.value = order.id;
  try {
    await orderApi.pay(order.id);
    ui.toast("支付成功，游戏已加入账户");
    await loadOrders();
    if (detail.value?.order.id === order.id) await openDetail(order.id);
  } catch (error) {
    ui.toast((error as Error).message, "error");
  } finally {
    actionId.value = null;
  }
}

async function cancel(order: Order) {
  actionId.value = order.id;
  try {
    await orderApi.cancel(order.id);
    ui.toast("订单已取消，库存已回补");
    await loadOrders();
    if (detail.value?.order.id === order.id) detail.value = null;
  } catch (error) {
    ui.toast((error as Error).message, "error");
  } finally {
    actionId.value = null;
  }
}

onMounted(loadOrders);
watch(() => session.loggedIn, loadOrders);
</script>

<template>
  <section class="account-page">
    <div class="page-container narrow-container">
      <div class="page-title-row">
        <div>
          <p class="section-kicker">MY PURCHASES</p>
          <h1>订单中心</h1>
          <p>查看购买记录、支付状态与库存处理结果。</p>
        </div>
        <button v-if="session.loggedIn" class="button button-secondary" type="button" :disabled="loading" @click="loadOrders">
          <RefreshCw :class="{ spin: loading }" :size="17" />刷新
        </button>
      </div>

      <div v-if="!session.loggedIn" class="empty-state account-empty">
        <ReceiptText :size="38" />
        <h3>登录后查看订单</h3>
        <p>订单数据仅对所属用户开放。</p>
        <button class="button button-primary" type="button" @click="ui.authOpen = true">登录账户</button>
      </div>
      <template v-else>
        <div class="order-tabs">
          <button v-for="item in [
            { key: 'all', label: '全部' },
            { key: 'pending', label: '待支付' },
            { key: 'paid', label: '已支付' },
            { key: 'closed', label: '已关闭' }
          ]" :key="item.key" type="button" :class="{ active: filter === item.key }" @click="filter = item.key as typeof filter">
            {{ item.label }} <span>{{ counts[item.key as keyof typeof counts] }}</span>
          </button>
        </div>

        <div v-if="loading" class="orders-loading"><LoaderCircle class="spin" :size="26" />正在读取订单...</div>
        <div v-else-if="filteredOrders.length" class="orders-list">
          <article v-for="order in filteredOrders" :key="order.id" class="order-row">
            <div class="order-primary">
              <span class="order-icon"><ReceiptText :size="22" /></span>
              <div>
                <small>订单号</small>
                <button type="button" @click="openDetail(order.id)">{{ order.orderNo }}</button>
                <span>{{ formatTime(order.createdAt) }}</span>
              </div>
            </div>
            <div class="order-amount"><small>订单金额</small><strong>{{ money(order.totalAmount) }}</strong></div>
            <div><span class="status-pill" :class="`status-${order.status}`">{{ orderStatus(order.status) }}</span></div>
            <div class="order-actions">
              <button class="text-button" type="button" @click="openDetail(order.id)">详情</button>
              <button v-if="order.status === 10" class="button button-primary compact" type="button" :disabled="actionId === order.id" @click="pay(order)">
                <CreditCard :size="16" />支付
              </button>
              <button v-if="order.status === 10" class="icon-button subtle" type="button" title="取消订单" :disabled="actionId === order.id" @click="cancel(order)">
                <RotateCcw :size="16" />
              </button>
            </div>
          </article>
        </div>
        <div v-else class="empty-state">
          <PackageOpen :size="38" />
          <h3>当前没有订单</h3>
          <p>完成购物车结算后，订单会出现在这里。</p>
          <RouterLink class="button button-secondary" to="/">去逛逛</RouterLink>
        </div>
      </template>
    </div>

    <Teleport to="body">
      <Transition name="fade">
        <div v-if="detailLoading || detail" class="modal-backdrop" @mousedown.self="detail = null; detailLoading = false">
          <section class="modal order-modal" role="dialog" aria-modal="true" aria-labelledby="order-detail-title">
            <button class="modal-close icon-button" type="button" aria-label="关闭" @click="detail = null; detailLoading = false">
              <X :size="20" />
            </button>
            <div v-if="detailLoading" class="modal-loading"><LoaderCircle class="spin" :size="28" />读取订单明细...</div>
            <template v-else-if="detail">
              <p class="section-kicker">ORDER DETAIL</p>
              <h2 id="order-detail-title">订单明细</h2>
              <div class="order-detail-head">
                <div><small>订单号</small><strong>{{ detail.order.orderNo }}</strong></div>
                <span class="status-pill" :class="`status-${detail.order.status}`">{{ orderStatus(detail.order.status) }}</span>
              </div>
              <div class="order-timeline">
                <Clock3 :size="18" />
                <span>创建于 {{ formatTime(detail.order.createdAt) }}</span>
                <span v-if="detail.order.status === 10">支付截止 {{ formatTime(detail.order.expireAt) }}</span>
              </div>
              <div class="order-detail-lines">
                <div v-for="item in detail.items" :key="item.id">
                  <span><strong>{{ item.gameTitle }}</strong><small>{{ money(item.price) }} × {{ item.quantity }}</small></span>
                  <b>{{ money(item.subtotal) }}</b>
                </div>
              </div>
              <div class="order-detail-total"><span>订单合计</span><strong>{{ money(detail.order.totalAmount) }}</strong></div>
              <div v-if="detail.order.status === 10" class="order-detail-actions">
                <button class="button button-secondary" type="button" @click="cancel(detail.order)">取消订单</button>
                <button class="button button-primary" type="button" @click="pay(detail.order)"><CreditCard :size="17" />模拟支付</button>
              </div>
            </template>
          </section>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>
