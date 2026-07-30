import type { GameSummary } from "./types";

export const GAME_ART: Record<string, string> = {
  "Neon Ronin":
    "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=900&q=84",
  "Dragon Ledger":
    "https://images.unsplash.com/photo-1578662996442-48f60103fc96?auto=format&fit=crop&w=900&q=84",
  "Colony Tactics":
    "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=900&q=84",
  "Rain Courier":
    "https://images.unsplash.com/photo-1519608487953-e999c86e7455?auto=format&fit=crop&w=900&q=84",
  "Turbo Apex":
    "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=900&q=84",
  "Steel Harbor":
    "https://images.unsplash.com/photo-1569701813229-33284b643e3c?auto=format&fit=crop&w=900&q=84",
  "Echoes of Aster":
    "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=900&q=84",
  "Verdant Assembly":
    "https://images.unsplash.com/photo-1511497584788-876760111969?auto=format&fit=crop&w=900&q=84",
  "Paper Moon Hotel":
    "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=900&q=84",
  "Circuit Breakers":
    "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?auto=format&fit=crop&w=900&q=84"
};

export const categoryIcons: Record<string, string> = {
  action: "swords",
  rpg: "scroll-text",
  strategy: "blocks",
  indie: "sparkles",
  sports: "gauge"
};

export function gameCover(game: Pick<GameSummary, "title" | "coverUrl">) {
  return GAME_ART[game.title] || game.coverUrl || "/assets/gamemall-hero.png";
}

export function money(value: number | string | undefined) {
  return `¥${Number(value || 0).toFixed(2)}`;
}

export function formatTime(value?: string) {
  return value ? value.replace("T", " ").slice(0, 19) : "-";
}

export function orderStatus(status: number) {
  return (
    {
      5: "创建中",
      10: "待支付",
      20: "已支付",
      30: "已取消",
      40: "已关闭"
    }[status] || "未知状态"
  );
}
