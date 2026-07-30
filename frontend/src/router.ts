import { createRouter, createWebHashHistory } from "vue-router";
import StoreView from "./views/StoreView.vue";
import OrdersView from "./views/OrdersView.vue";
import AdminView from "./views/AdminView.vue";

export default createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/", name: "store", component: StoreView },
    { path: "/orders", name: "orders", component: OrdersView },
    { path: "/admin", name: "admin", component: AdminView },
    { path: "/:pathMatch(.*)*", redirect: "/" }
  ],
  scrollBehavior() {
    return { top: 0 };
  }
});
