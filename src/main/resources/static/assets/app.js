const state = {
    token: localStorage.getItem("gm_token") || "",
    user: readStoredUser(),
    categories: [],
    games: [],
    cart: [],
    orders: [],
    selectedCategory: "",
    keyword: "",
    orderFilter: "all",
    authMode: "login"
};

const GAME_ART = {
    "Neon Ronin": "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=900&q=84",
    "Dragon Ledger": "https://images.unsplash.com/photo-1578662996442-48f60103fc96?auto=format&fit=crop&w=900&q=84",
    "Colony Tactics": "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=900&q=84",
    "Rain Courier": "https://images.unsplash.com/photo-1519608487953-e999c86e7455?auto=format&fit=crop&w=900&q=84",
    "Turbo Apex": "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=900&q=84"
};

const $ = (id) => document.getElementById(id);
const money = (value) => `¥${Number(value || 0).toFixed(2)}`;
const formatTime = (value) => value ? String(value).replace("T", " ").slice(0, 19) : "-";
const statusLabel = (status) => ({5: "创建中", 10: "待支付", 20: "已支付", 30: "已取消", 40: "已关闭"}[status] || "未知状态");
const statusTone = (status) => status === 10 ? "pending" : ([30, 40].includes(status) ? "closed" : "");

function readStoredUser() {
    try {
        return JSON.parse(localStorage.getItem("gm_user") || "null");
    } catch (_) {
        return null;
    }
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>'"]/g, (char) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;"
    }[char]));
}

function coverFor(game) {
    return GAME_ART[game.title] || game.coverUrl || "/assets/gamemall-hero.png";
}

function refreshIcons() {
    if (window.lucide) window.lucide.createIcons({attrs: {"stroke-width": 1.8}});
}

function toast(message, type = "success") {
    const item = document.createElement("div");
    item.className = `toast ${type}`;
    item.innerHTML = `<i data-lucide="${type === "error" ? "circle-alert" : "circle-check"}"></i><span>${escapeHtml(message)}</span>`;
    $("toastRegion").appendChild(item);
    refreshIcons();
    setTimeout(() => item.remove(), 2800);
}

async function api(path, options = {}) {
    const headers = {"Content-Type": "application/json", ...(options.headers || {})};
    if (state.token) headers.Authorization = `Bearer ${state.token}`;
    let response;
    try {
        response = await fetch(path, {...options, headers});
    } catch (_) {
        throw new Error("服务暂时无法连接，请稍后重试");
    }
    const raw = await response.text();
    let body = {};
    try {
        body = raw ? JSON.parse(raw) : {};
    } catch (_) {
        throw new Error(`服务响应异常（HTTP ${response.status}）`);
    }
    if (response.status === 401 && state.token) {
        clearSession(false);
        throw new Error("登录状态已过期，请重新登录");
    }
    if (!response.ok || body.code !== 0) {
        throw new Error(body.message || `请求失败（HTTP ${response.status}）`);
    }
    return body.data;
}

function requireLogin() {
    if (state.token) return true;
    openModal("authModal");
    toast("请先登录后继续", "error");
    return false;
}

function isAdmin() {
    return Boolean(state.user && state.user.role === "ADMIN");
}

function setView(view) {
    if (view === "admin" && !isAdmin()) {
        toast("该页面仅对管理员开放", "error");
        return;
    }
    document.querySelectorAll(".page-view").forEach((node) => node.classList.toggle("active", node.id === `${view}View`));
    document.querySelectorAll("[data-view-target]").forEach((node) => node.classList.toggle("active", node.dataset.viewTarget === view));
    $("accountMenu").classList.remove("show");
    window.scrollTo({top: 0, behavior: "smooth"});
    if (view === "orders") loadOrders();
    if (view === "admin") renderAdmin();
}

function openCart() {
    $("cartDrawer").classList.add("show");
    $("drawerBackdrop").classList.add("show");
    $("cartDrawer").setAttribute("aria-hidden", "false");
    document.body.classList.add("locked");
    loadCart(false);
}

function closeCart() {
    $("cartDrawer").classList.remove("show");
    $("drawerBackdrop").classList.remove("show");
    $("cartDrawer").setAttribute("aria-hidden", "true");
    if (!$("modalBackdrop").classList.contains("show")) document.body.classList.remove("locked");
}

function openModal(id) {
    closeCart();
    $("modalBackdrop").classList.add("show");
    document.querySelectorAll(".modal").forEach((node) => node.classList.toggle("active", node.id === id));
    document.body.classList.add("locked");
    setTimeout(() => {
        const input = $(`${id}`)?.querySelector("input:not([type='hidden'])");
        if (input) input.focus();
    }, 80);
}

function closeModal() {
    $("modalBackdrop").classList.remove("show");
    document.querySelectorAll(".modal").forEach((node) => node.classList.remove("active"));
    document.body.classList.remove("locked");
}

function updateSession() {
    const loggedIn = Boolean(state.user && state.token);
    $("accountName").textContent = loggedIn ? state.user.username : "登录";
    $("accountRole").textContent = loggedIn ? (isAdmin() ? "管理员" : "玩家") : "访客";
    $("accountAvatar").textContent = loggedIn ? state.user.username.slice(0, 1).toUpperCase() : "游";
    $("accountMenuIdentity").innerHTML = loggedIn
        ? `<strong>${escapeHtml(state.user.username)}</strong><span>${isAdmin() ? "运营管理员" : "GameMall 玩家"}</span>`
        : "<strong>访客</strong><span>尚未登录</span>";
    $("logoutBtn").classList.toggle("hidden", !loggedIn);
    document.querySelectorAll(".admin-only").forEach((node) => node.classList.toggle("hidden", !isAdmin()));
    refreshIcons();
}

function saveSession(data) {
    state.token = data.token;
    state.user = {userId: data.userId, username: data.username, role: data.role};
    localStorage.setItem("gm_token", state.token);
    localStorage.setItem("gm_user", JSON.stringify(state.user));
    updateSession();
}

function clearSession(showToast = true) {
    state.token = "";
    state.user = null;
    state.cart = [];
    state.orders = [];
    localStorage.removeItem("gm_token");
    localStorage.removeItem("gm_user");
    updateSession();
    renderCart();
    renderOrders();
    if (showToast) toast("已退出登录");
}

async function loadCategories() {
    state.categories = await api("/api/categories");
    renderCategories();
    $("adminCategory").innerHTML = state.categories
        .map((category) => `<option value="${category.id}">${escapeHtml(category.name)}</option>`)
        .join("");
}

function renderCategories() {
    const categories = [{id: "", name: "全部游戏"}, ...state.categories];
    $("categoryTabs").innerHTML = categories.map((category) => `
        <button class="category-tab ${String(category.id) === String(state.selectedCategory) ? "active" : ""}"
                type="button" data-category-id="${category.id}">${escapeHtml(category.name)}</button>`).join("");
}

function renderGameSkeletons() {
    $("gamesGrid").innerHTML = Array.from({length: 8}, () => '<div class="skeleton-card"></div>').join("");
}

async function loadGames() {
    renderGameSkeletons();
    const params = new URLSearchParams({page: "1", size: "50"});
    if (state.selectedCategory) params.set("categoryId", state.selectedCategory);
    if (state.keyword) params.set("keyword", state.keyword);
    try {
        const data = await api(`/api/games?${params}`);
        state.games = data.records || [];
        $("catalogCount").textContent = `共 ${data.total || 0} 款游戏`;
        $("activeSearch").classList.toggle("hidden", !state.keyword);
        $("activeSearchText").textContent = state.keyword ? `搜索：${state.keyword}` : "";
        renderGames();
        renderAdmin();
    } catch (error) {
        $("gamesGrid").innerHTML = emptyState("wifi-off", "商品加载失败", error.message);
        toast(error.message, "error");
    }
}

function emptyState(icon, title, description, action = "") {
    return `<div class="empty-state"><i data-lucide="${icon}"></i><h3>${escapeHtml(title)}</h3><p>${escapeHtml(description)}</p>${action}</div>`;
}

function renderGames() {
    if (!state.games.length) {
        $("gamesGrid").innerHTML = emptyState("search-x", "没有找到游戏", "尝试更换分类或搜索词");
        refreshIcons();
        return;
    }
    $("gamesGrid").innerHTML = state.games.map((game) => `
        <article class="game-card">
            <button class="game-cover-button" type="button" data-game-detail="${game.id}" aria-label="查看 ${escapeHtml(game.title)} 详情">
                <img src="${escapeHtml(coverFor(game))}" alt="${escapeHtml(game.title)} 封面" loading="lazy">
                <span class="cover-chip">${escapeHtml(game.categoryName || "独立游戏")}</span>
            </button>
            <div class="game-card-body">
                <div class="game-title-row"><h3 title="${escapeHtml(game.title)}">${escapeHtml(game.title)}</h3><i class="stock-dot ${game.stock < 100 ? "low" : ""}" title="${game.stock > 0 ? "有库存" : "已售罄"}"></i></div>
                <p class="game-developer">${escapeHtml(game.developer || "GameMall Studio")}</p>
                <div class="game-meta">
                    <span><i data-lucide="trending-up"></i> 已售 ${game.soldCount || 0}</span>
                    <span><i data-lucide="boxes"></i> 库存 ${game.stock || 0}</span>
                </div>
                <div class="game-buy-row">
                    <span class="game-price">${money(game.price)}</span>
                    <button class="add-cart-button" type="button" data-add-cart="${game.id}" title="加入购物车" aria-label="将 ${escapeHtml(game.title)} 加入购物车" ${game.stock <= 0 ? "disabled" : ""}>
                        <i data-lucide="shopping-cart"></i>
                    </button>
                </div>
            </div>
        </article>`).join("");
    refreshIcons();
}

async function openGameDetail(id) {
    try {
        const game = await api(`/api/games/${id}`);
        $("detailContent").innerHTML = `
            <div class="detail-cover">
                <img src="${escapeHtml(coverFor(game))}" alt="${escapeHtml(game.title)} 封面">
                <div class="detail-heading"><span>${escapeHtml(categoryName(game.categoryId))}</span><h2 id="detailTitle">${escapeHtml(game.title)}</h2></div>
            </div>
            <div class="detail-body">
                <div class="detail-layout">
                    <div>
                        <p class="detail-description">${escapeHtml(game.description || "这款游戏暂未添加详细介绍。")}</p>
                        <div class="detail-facts">
                            <div><span>开发商</span><strong title="${escapeHtml(game.developer || "-")}">${escapeHtml(game.developer || "-")}</strong></div>
                            <div><span>发行商</span><strong title="${escapeHtml(game.publisher || "-")}">${escapeHtml(game.publisher || "-")}</strong></div>
                            <div><span>发行日期</span><strong>${formatTime(game.releaseDate).slice(0, 10)}</strong></div>
                        </div>
                    </div>
                    <div class="detail-purchase">
                        <span>数字版售价</span>
                        <strong>${money(game.price)}</strong>
                        <div class="detail-stock"><i></i>${game.stock > 0 ? `现货可售 · ${game.stock} 件` : "暂时售罄"}</div>
                        <button class="button button-primary button-wide" type="button" data-detail-add="${game.id}" ${game.stock <= 0 ? "disabled" : ""}>
                            <i data-lucide="shopping-bag"></i> 加入购物车
                        </button>
                    </div>
                </div>
            </div>`;
        openModal("detailModal");
        refreshIcons();
    } catch (error) {
        toast(error.message, "error");
    }
}

function categoryName(categoryId) {
    return state.categories.find((item) => Number(item.id) === Number(categoryId))?.name || "数字游戏";
}

async function addCart(gameId) {
    if (!requireLogin()) return;
    try {
        await api("/api/cart", {method: "POST", body: JSON.stringify({gameId: Number(gameId), quantity: 1})});
        await loadCart(false);
        closeModal();
        toast("已加入购物车");
    } catch (error) {
        toast(error.message, "error");
    }
}

async function loadCart(notify = false) {
    if (!state.token) {
        state.cart = [];
        renderCart();
        return;
    }
    try {
        state.cart = await api("/api/cart");
        renderCart();
        if (notify) toast("购物车已更新");
    } catch (error) {
        renderCart();
        toast(error.message, "error");
    }
}

function renderCart() {
    const quantity = state.cart.reduce((sum, item) => sum + Number(item.quantity || 0), 0);
    const total = state.cart.reduce((sum, item) => sum + Number(item.subtotal || 0), 0);
    $("headerCartCount").textContent = quantity;
    $("mobileCartCount").textContent = quantity;
    $("cartQuantity").textContent = `${quantity} 件`;
    $("cartTotal").textContent = money(total);
    $("checkoutBtn").disabled = !state.cart.length;
    if (!state.token) {
        $("cartList").innerHTML = emptyState("log-in", "登录后查看购物车", "你的商品会安全保存在账户中", '<button class="button button-primary" type="button" data-cart-login>立即登录</button>');
    } else if (!state.cart.length) {
        $("cartList").innerHTML = emptyState("shopping-bag", "购物车还是空的", "去商城挑一款喜欢的游戏吧");
    } else {
        $("cartList").innerHTML = state.cart.map((item) => `
            <div class="cart-line">
                <img src="${escapeHtml(coverFor(item))}" alt="${escapeHtml(item.title)} 封面">
                <div class="cart-line-copy">
                    <strong>${escapeHtml(item.title)}</strong>
                    <span>${money(item.price)} · 库存 ${item.stock}</span>
                    <div class="qty-stepper">
                        <button type="button" data-cart-qty="${item.gameId}" data-quantity="${item.quantity - 1}" aria-label="减少数量">−</button>
                        <span>${item.quantity}</span>
                        <button type="button" data-cart-qty="${item.gameId}" data-quantity="${item.quantity + 1}" aria-label="增加数量">+</button>
                    </div>
                </div>
                <div class="cart-line-price">${money(item.subtotal)}</div>
                <button class="cart-remove" type="button" data-cart-remove="${item.gameId}" aria-label="移除 ${escapeHtml(item.title)}"><i data-lucide="trash-2"></i></button>
            </div>`).join("");
    }
    refreshIcons();
}

async function changeCartQuantity(gameId, quantity) {
    try {
        if (quantity <= 0) await api(`/api/cart/${gameId}`, {method: "DELETE"});
        else await api(`/api/cart/${gameId}?quantity=${quantity}`, {method: "PUT"});
        await loadCart(false);
    } catch (error) {
        toast(error.message, "error");
    }
}

async function checkout() {
    if (!requireLogin() || !state.cart.length) return;
    $("checkoutBtn").disabled = true;
    try {
        const idempotencyKey = `web-${Date.now()}-${crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(16).slice(2)}`;
        const detail = await api("/api/orders", {
            method: "POST",
            body: JSON.stringify({fromCart: true, idempotencyKey})
        });
        await Promise.all([loadCart(false), loadOrders(false), loadGames()]);
        closeCart();
        setView("orders");
        toast("订单创建成功，库存已锁定");
        if (detail?.order?.id) openOrderDetail(detail.order.id);
    } catch (error) {
        toast(error.message, "error");
    } finally {
        $("checkoutBtn").disabled = !state.cart.length;
    }
}

async function loadOrders(notify = false) {
    if (!state.token) {
        state.orders = [];
        renderOrders();
        return;
    }
    try {
        state.orders = await api("/api/orders?page=1&size=50");
        renderOrders();
        if (notify) toast("订单状态已刷新");
    } catch (error) {
        renderOrders();
        toast(error.message, "error");
    }
}

function filteredOrders() {
    if (state.orderFilter === "all") return state.orders;
    if (state.orderFilter === "closed") return state.orders.filter((order) => [30, 40].includes(order.status));
    return state.orders.filter((order) => String(order.status) === String(state.orderFilter));
}

function renderOrders() {
    $("allOrderCount").textContent = state.orders.length;
    $("pendingOrderCount").textContent = state.orders.filter((order) => order.status === 10).length;
    $("paidOrderCount").textContent = state.orders.filter((order) => order.status === 20).length;
    $("closedOrderCount").textContent = state.orders.filter((order) => [30, 40].includes(order.status)).length;
    if (!state.token) {
        $("ordersList").innerHTML = emptyState("shield-check", "登录后查看订单", "订单记录仅对账户本人可见", '<button class="button button-primary" type="button" data-orders-login>立即登录</button>');
    } else if (!filteredOrders().length) {
        $("ordersList").innerHTML = emptyState("receipt-text", "这里还没有订单", "购买游戏后，订单会显示在这里");
    } else {
        $("ordersList").innerHTML = filteredOrders().map((order) => `
            <article class="order-card">
                <div class="order-card-head">
                    <div class="order-number"><strong>${escapeHtml(order.orderNo)}</strong><span>创建于 ${formatTime(order.createdAt)}</span></div>
                    <span class="status-pill ${statusTone(order.status)}">${statusLabel(order.status)}</span>
                </div>
                <div class="order-main">
                    <span class="order-main-icon"><i data-lucide="gamepad-2"></i></span>
                    <div class="order-main-copy"><strong>${money(order.totalAmount)}</strong><span>${order.status === 10 ? "待完成支付" : "数字游戏订单"}</span></div>
                </div>
                <div class="order-card-foot">
                    <span class="order-expire">${order.status === 10 ? `支付有效期至 ${formatTime(order.expireAt)}` : `状态更新于 ${formatTime(order.updatedAt)}`}</span>
                    <div class="order-actions">
                        <button class="button button-secondary" type="button" data-order-detail="${order.id}">查看明细</button>
                        ${order.status === 10 ? `<button class="button button-danger" type="button" data-order-cancel="${order.id}">取消</button><button class="button button-primary" type="button" data-order-pay="${order.id}">模拟支付</button>` : ""}
                    </div>
                </div>
            </article>`).join("");
    }
    refreshIcons();
}

async function openOrderDetail(id) {
    try {
        const detail = await api(`/api/orders/${id}`);
        const order = detail.order;
        $("orderDetailContent").innerHTML = `
            <div class="order-detail-head">
                <p class="section-kicker">ORDER DETAIL</p>
                <h2 id="orderDetailTitle">订单明细</h2>
                <div class="order-detail-meta"><span>${escapeHtml(order.orderNo)}</span><span class="status-pill ${statusTone(order.status)}">${statusLabel(order.status)}</span></div>
            </div>
            <div class="order-items">
                ${detail.items.map((item) => `<div class="order-item"><div><strong>${escapeHtml(item.gameTitle)}</strong><span>${money(item.price)} × ${item.quantity}</span></div><strong>${money(item.subtotal)}</strong></div>`).join("")}
            </div>
            <div class="order-detail-total"><span>订单合计</span><strong>${money(order.totalAmount)}</strong></div>`;
        openModal("orderModal");
        refreshIcons();
    } catch (error) {
        toast(error.message, "error");
    }
}

async function payOrder(id) {
    try {
        await api(`/api/orders/${id}/pay`, {method: "POST"});
        closeModal();
        await Promise.all([loadOrders(false), loadGames()]);
        toast("支付成功，游戏已进入购买记录");
    } catch (error) {
        toast(error.message, "error");
    }
}

async function cancelOrder(id) {
    try {
        await api(`/api/orders/${id}/cancel`, {method: "POST"});
        closeModal();
        await Promise.all([loadOrders(false), loadGames()]);
        toast("订单已取消，库存已回补");
    } catch (error) {
        toast(error.message, "error");
    }
}

function renderAdmin() {
    if (!isAdmin()) return;
    const keyword = $("adminKeyword")?.value.trim().toLowerCase() || "";
    const rows = state.games.filter((game) => [game.title, game.developer, game.categoryName].some((value) => String(value || "").toLowerCase().includes(keyword)));
    const stock = state.games.reduce((sum, game) => sum + Number(game.stock || 0), 0);
    const sold = state.games.reduce((sum, game) => sum + Number(game.soldCount || 0), 0);
    $("adminGameCount").textContent = state.games.length;
    $("adminStockCount").textContent = stock.toLocaleString("zh-CN");
    $("adminSoldCount").textContent = sold.toLocaleString("zh-CN");
    $("adminLowStockCount").textContent = state.games.filter((game) => game.stock < 100).length;
    $("adminTableCount").textContent = `${rows.length} 条商品记录`;
    $("adminGameRows").innerHTML = rows.length ? rows.map((game) => {
        const stockPercent = Math.min(100, Math.max(4, Number(game.stock || 0) / 5));
        return `<tr>
            <td><div class="product-cell"><img src="${escapeHtml(coverFor(game))}" alt=""><div><strong title="${escapeHtml(game.title)}">${escapeHtml(game.title)}</strong><span>${escapeHtml(game.developer || "-")}</span></div></div></td>
            <td>${escapeHtml(game.categoryName || "-")}</td>
            <td><strong>${money(game.price)}</strong></td>
            <td><div class="inventory"><span>${game.stock}</span><span class="inventory-bar"><i class="${game.stock < 100 ? "low" : ""}" style="width:${stockPercent}%"></i></span></div></td>
            <td>${game.soldCount || 0}</td>
            <td><span class="status-pill">在售</span></td>
            <td><div class="table-actions"><button type="button" data-admin-edit="${game.id}" title="编辑商品"><i data-lucide="pencil"></i></button><button class="danger" type="button" data-admin-offline="${game.id}" title="下架商品"><i data-lucide="archive"></i></button></div></td>
        </tr>`;
    }).join("") : '<tr><td colspan="7"><div class="empty-state"><i data-lucide="package-search"></i><h3>没有匹配的商品</h3></div></td></tr>';
    refreshIcons();
}

function resetGameForm() {
    $("gameForm").reset();
    $("gameId").value = "";
    $("gameFormTitle").textContent = "新增游戏";
}

function openNewGame() {
    resetGameForm();
    if (state.categories.length) $("adminCategory").value = state.categories[0].id;
    openModal("gameModal");
}

async function editGame(id) {
    try {
        const game = await api(`/api/games/${id}`);
        $("gameId").value = game.id;
        $("gameTitle").value = game.title || "";
        $("adminCategory").value = game.categoryId;
        $("developer").value = game.developer || "";
        $("publisher").value = game.publisher || "";
        $("price").value = game.price || 0;
        $("stock").value = game.stock || 0;
        $("releaseDate").value = game.releaseDate ? String(game.releaseDate).slice(0, 10) : "";
        $("coverUrl").value = game.coverUrl || "";
        $("description").value = game.description || "";
        $("gameFormTitle").textContent = "编辑游戏";
        openModal("gameModal");
    } catch (error) {
        toast(error.message, "error");
    }
}

async function saveGame(event) {
    event.preventDefault();
    if (!isAdmin()) return;
    const id = $("gameId").value;
    const date = $("releaseDate").value;
    const payload = {
        categoryId: Number($("adminCategory").value),
        title: $("gameTitle").value.trim(),
        developer: $("developer").value.trim(),
        publisher: $("publisher").value.trim(),
        price: Number($("price").value),
        stock: Number($("stock").value),
        coverUrl: $("coverUrl").value.trim(),
        description: $("description").value.trim(),
        releaseDate: date ? `${date} 00:00:00` : null
    };
    try {
        await api(id ? `/api/admin/games/${id}` : "/api/admin/games", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });
        closeModal();
        await loadGames();
        toast(id ? "商品信息已更新" : "新商品已创建");
    } catch (error) {
        toast(error.message, "error");
    }
}

async function offlineGame(id) {
    if (!window.confirm("确认下架该商品？下架后前台将不再展示。")) return;
    try {
        await api(`/api/admin/games/${id}/status?status=0`, {method: "PATCH"});
        await loadGames();
        toast("商品已下架");
    } catch (error) {
        toast(error.message, "error");
    }
}

function switchAuthMode(mode) {
    state.authMode = mode;
    document.querySelectorAll("[data-auth-mode]").forEach((node) => node.classList.toggle("active", node.dataset.authMode === mode));
    $("authTitle").textContent = mode === "login" ? "欢迎回来" : "创建玩家账号";
    $("authSubtitle").textContent = mode === "login" ? "登录后继续管理购物车与订单。" : "注册后即可收藏商品并创建订单。";
    $("authSubmitBtn").textContent = mode === "login" ? "登录" : "注册并登录";
    $("quickAdminBtn").classList.toggle("hidden", mode !== "login");
    $("password").autocomplete = mode === "login" ? "current-password" : "new-password";
}

async function submitAuth(event) {
    event.preventDefault();
    await performAuth($("username").value.trim(), $("password").value, state.authMode);
}

async function performAuth(username, password, mode = "login") {
    try {
        const data = await api(`/api/auth/${mode}`, {
            method: "POST",
            body: JSON.stringify({username, password})
        });
        saveSession(data);
        closeModal();
        await Promise.all([loadCart(false), loadOrders(false)]);
        renderAdmin();
        toast(mode === "login" ? "登录成功" : "账号创建成功");
    } catch (error) {
        toast(error.message, "error");
    }
}

function bindEvents() {
    document.querySelectorAll("[data-view-target]").forEach((node) => node.addEventListener("click", () => setView(node.dataset.viewTarget)));
    $("heroExploreBtn").addEventListener("click", () => $("catalog").scrollIntoView({behavior: "smooth"}));
    $("heroFeaturedBtn").addEventListener("click", () => {
        const featured = state.games.find((game) => game.title === "Neon Ronin") || state.games[0];
        if (featured) openGameDetail(featured.id);
    });
    $("headerSearchForm").addEventListener("submit", (event) => {
        event.preventDefault();
        state.keyword = $("headerKeyword").value.trim();
        setView("store");
        loadGames().then(() => $("catalog").scrollIntoView({behavior: "smooth"}));
    });
    $("categoryTabs").addEventListener("click", (event) => {
        const button = event.target.closest("[data-category-id]");
        if (!button) return;
        state.selectedCategory = button.dataset.categoryId;
        renderCategories();
        loadGames();
    });
    $("gamesGrid").addEventListener("click", (event) => {
        const detail = event.target.closest("[data-game-detail]");
        const add = event.target.closest("[data-add-cart]");
        if (detail) openGameDetail(detail.dataset.gameDetail);
        if (add) addCart(add.dataset.addCart);
    });
    $("detailContent").addEventListener("click", (event) => {
        const button = event.target.closest("[data-detail-add]");
        if (button) addCart(button.dataset.detailAdd);
    });
    $("clearSearchBtn").addEventListener("click", () => {
        state.keyword = "";
        $("headerKeyword").value = "";
        loadGames();
    });
    $("mobileSearchBtn").addEventListener("click", () => openModal("searchModal"));
    $("mobileSearchForm").addEventListener("submit", (event) => {
        event.preventDefault();
        state.keyword = $("mobileKeyword").value.trim();
        $("headerKeyword").value = state.keyword;
        closeModal();
        loadGames().then(() => $("catalog").scrollIntoView({behavior: "smooth"}));
    });

    [$("cartOpenBtn"), $("mobileCartBtn")].forEach((node) => node.addEventListener("click", openCart));
    $("cartCloseBtn").addEventListener("click", closeCart);
    $("drawerBackdrop").addEventListener("click", closeCart);
    $("cartList").addEventListener("click", (event) => {
        const qty = event.target.closest("[data-cart-qty]");
        const remove = event.target.closest("[data-cart-remove]");
        const login = event.target.closest("[data-cart-login]");
        if (qty) changeCartQuantity(qty.dataset.cartQty, Number(qty.dataset.quantity));
        if (remove) changeCartQuantity(remove.dataset.cartRemove, 0);
        if (login) openModal("authModal");
    });
    $("checkoutBtn").addEventListener("click", checkout);

    $("reloadOrdersBtn").addEventListener("click", () => loadOrders(true));
    $("orderTabs").addEventListener("click", (event) => {
        const button = event.target.closest("[data-order-filter]");
        if (!button) return;
        state.orderFilter = button.dataset.orderFilter;
        $("orderTabs").querySelectorAll("button").forEach((node) => node.classList.toggle("active", node === button));
        renderOrders();
    });
    $("ordersList").addEventListener("click", (event) => {
        const detail = event.target.closest("[data-order-detail]");
        const pay = event.target.closest("[data-order-pay]");
        const cancel = event.target.closest("[data-order-cancel]");
        const login = event.target.closest("[data-orders-login]");
        if (detail) openOrderDetail(detail.dataset.orderDetail);
        if (pay) payOrder(pay.dataset.orderPay);
        if (cancel) cancelOrder(cancel.dataset.orderCancel);
        if (login) openModal("authModal");
    });

    $("newGameBtn").addEventListener("click", openNewGame);
    $("adminKeyword").addEventListener("input", renderAdmin);
    $("adminGameRows").addEventListener("click", (event) => {
        const edit = event.target.closest("[data-admin-edit]");
        const offline = event.target.closest("[data-admin-offline]");
        if (edit) editGame(edit.dataset.adminEdit);
        if (offline) offlineGame(offline.dataset.adminOffline);
    });
    $("gameForm").addEventListener("submit", saveGame);

    [$("accountBtn"), $("mobileAccountBtn")].forEach((node) => node.addEventListener("click", (event) => {
        event.stopPropagation();
        if (!state.token) openModal("authModal");
        else $("accountMenu").classList.toggle("show");
    }));
    $("menuOrdersBtn").addEventListener("click", () => state.token ? setView("orders") : openModal("authModal"));
    $("menuAdminBtn").addEventListener("click", () => setView("admin"));
    $("logoutBtn").addEventListener("click", () => {
        clearSession();
        setView("store");
    });
    document.addEventListener("click", (event) => {
        if (!event.target.closest("#accountMenu") && !event.target.closest("#accountBtn") && !event.target.closest("#mobileAccountBtn")) {
            $("accountMenu").classList.remove("show");
        }
    });

    document.querySelectorAll("[data-auth-mode]").forEach((node) => node.addEventListener("click", () => switchAuthMode(node.dataset.authMode)));
    $("authForm").addEventListener("submit", submitAuth);
    $("quickAdminBtn").addEventListener("click", () => {
        $("username").value = "admin";
        $("password").value = "admin123";
        performAuth("admin", "admin123", "login");
    });
    document.querySelectorAll("[data-close-modal]").forEach((node) => node.addEventListener("click", closeModal));
    $("modalBackdrop").addEventListener("click", (event) => {
        if (event.target === $("modalBackdrop")) closeModal();
    });
    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeModal();
            closeCart();
            $("accountMenu").classList.remove("show");
        }
    });
}

async function init() {
    bindEvents();
    updateSession();
    renderCart();
    renderOrders();
    refreshIcons();
    try {
        await loadCategories();
        await loadGames();
        await Promise.all([loadCart(false), loadOrders(false)]);
    } catch (error) {
        toast(error.message, "error");
    }
}

init();
