use gamemall;

insert into users(username, password_hash, nickname, role, status, created_at, updated_at)
values
('admin', '$2b$12$Zeu.ak3nBb2.xu6LpNW0huyOMrcvkc4GqY0GXXFve.sSi19wLWpBe', '运营管理员', 'ADMIN', 1, now(), now());

insert into categories(name, sort_order)
values
('动作', 10),
('角色扮演', 20),
('策略', 30),
('独立游戏', 40),
('体育竞速', 50);

insert into games(category_id, title, developer, publisher, price, stock, sold_count, cover_url, description, status, release_date, created_at, updated_at)
values
(1, 'Neon Ronin', 'Blue Harbor Studio', 'GameMall Publishing', 98.00, 120, 321, 'https://picsum.photos/seed/neon-ronin/600/360', '赛博朋克风动作冒险游戏，强调高速战斗和关卡探索。', 1, '2025-03-18 00:00:00', now(), now()),
(2, 'Dragon Ledger', 'Northwind RPG Lab', 'GameMall Publishing', 168.00, 80, 266, 'https://picsum.photos/seed/dragon-ledger/600/360', '开放世界角色扮演游戏，围绕公会经营、装备收集和剧情选择展开。', 1, '2024-11-02 00:00:00', now(), now()),
(3, 'Colony Tactics', 'Hex Grid Works', 'Strategy Forge', 76.00, 200, 188, 'https://picsum.photos/seed/colony-tactics/600/360', '回合制殖民地策略游戏，包含资源建设、科技树和战役模式。', 1, '2025-07-09 00:00:00', now(), now()),
(4, 'Rain Courier', 'Tiny Planet', 'Indie Lane', 42.00, 500, 95, 'https://picsum.photos/seed/rain-courier/600/360', '叙事向独立游戏，玩家在雨夜城市中完成递送任务并拼合人物故事。', 1, '2023-10-21 00:00:00', now(), now()),
(5, 'Turbo Apex', 'Redline Team', 'GameMall Publishing', 118.00, 60, 410, 'https://picsum.photos/seed/turbo-apex/600/360', '街机竞速游戏，支持车辆改装、排行榜和多人对战。', 1, '2025-01-12 00:00:00', now(), now());
