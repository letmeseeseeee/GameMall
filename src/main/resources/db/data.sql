use gamemall;

insert into users(username, password_hash, nickname, role, status, created_at, updated_at)
values
('admin', '$2b$12$Zeu.ak3nBb2.xu6LpNW0huyOMrcvkc4GqY0GXXFve.sSi19wLWpBe', '运营管理员', 'ADMIN', 1, now(), now());

insert into categories(name, code, description, sort_order)
values
('动作冒险', 'action', '强调即时操作、战斗反馈与关卡探索', 10),
('角色扮演', 'rpg', '通过角色成长、装备构筑和剧情选择推进冒险', 20),
('策略模拟', 'strategy', '围绕资源管理、战术决策和系统经营展开', 30),
('独立叙事', 'indie', '聚焦独特表达、氛围塑造与作者化体验', 40),
('体育竞速', 'sports', '包含竞速、竞技运动与多人对抗体验', 50);

insert into games(category_id, title, developer, publisher, price, stock, sold_count, cover_url, description, status, release_date, created_at, updated_at)
values
(1, 'Neon Ronin', 'Blue Harbor Studio', 'GameMall Publishing', 98.00, 120, 321, 'https://picsum.photos/seed/neon-ronin/600/360', '赛博朋克风动作冒险游戏，强调高速战斗和关卡探索。', 1, '2025-03-18 00:00:00', now(), now()),
(2, 'Dragon Ledger', 'Northwind RPG Lab', 'GameMall Publishing', 168.00, 80, 266, 'https://picsum.photos/seed/dragon-ledger/600/360', '开放世界角色扮演游戏，围绕公会经营、装备收集和剧情选择展开。', 1, '2024-11-02 00:00:00', now(), now()),
(3, 'Colony Tactics', 'Hex Grid Works', 'Strategy Forge', 76.00, 200, 188, 'https://picsum.photos/seed/colony-tactics/600/360', '回合制殖民地策略游戏，包含资源建设、科技树和战役模式。', 1, '2025-07-09 00:00:00', now(), now()),
(4, 'Rain Courier', 'Tiny Planet', 'Indie Lane', 42.00, 500, 95, 'https://picsum.photos/seed/rain-courier/600/360', '叙事向独立游戏，玩家在雨夜城市中完成递送任务并拼合人物故事。', 1, '2023-10-21 00:00:00', now(), now()),
(5, 'Turbo Apex', 'Redline Team', 'GameMall Publishing', 118.00, 60, 410, 'https://picsum.photos/seed/turbo-apex/600/360', '街机竞速游戏，支持车辆改装、排行榜和多人对战。', 1, '2025-01-12 00:00:00', now(), now());

insert into games(category_id, title, developer, publisher, price, stock, sold_count, cover_url, description, status, release_date, created_at, updated_at)
values
(1, 'Steel Harbor', 'Breakwater Interactive', 'Iron Tide', 128.00, 150, 174, 'https://picsum.photos/seed/steel-harbor/600/360', '以港口争夺为核心的战术动作游戏，支持合作突袭和多人竞技。', 1, '2025-09-26 00:00:00', now(), now()),
(2, 'Echoes of Aster', 'Silver Map Studio', 'Northwind Games', 148.00, 95, 203, 'https://picsum.photos/seed/echoes-aster/600/360', '探索失落星界遗迹，在动态事件和分支任务中塑造角色命运。', 1, '2025-05-16 00:00:00', now(), now()),
(3, 'Verdant Assembly', 'Mosslight Works', 'Strategy Forge', 88.00, 240, 137, 'https://picsum.photos/seed/verdant-assembly/600/360', '建设生态殖民地，在有限资源与环境变化之间制定长期发展策略。', 1, '2024-08-30 00:00:00', now(), now()),
(4, 'Paper Moon Hotel', 'Soft Corner', 'Indie Lane', 56.00, 320, 121, 'https://picsum.photos/seed/paper-moon-hotel/600/360', '经营一间只在午夜出现的旅店，倾听旅客故事并改变他们的结局。', 1, '2024-04-12 00:00:00', now(), now()),
(5, 'Circuit Breakers', 'Vector Mile', 'Redline Team', 108.00, 110, 289, 'https://picsum.photos/seed/circuit-breakers/600/360', '未来都市街机竞速，包含组队赛事、车辆改装与赛季挑战。', 1, '2025-06-20 00:00:00', now(), now());

insert into tags(name, group_name, sort_order)
values
('动作战斗', '玩法', 10), ('开放世界', '玩法', 20), ('回合制', '玩法', 30),
('基地建设', '玩法', 40), ('叙事探索', '玩法', 50), ('竞速', '玩法', 60),
('战术射击', '玩法', 70), ('资源管理', '玩法', 80),
('单人', '模式', 110), ('多人', '模式', 120), ('在线合作', '模式', 130),
('赛博朋克', '题材', 210), ('奇幻', '题材', 220), ('科幻', '题材', 230), ('都市', '题材', 240),
('剧情丰富', '特色', 310), ('独立制作', '特色', 320), ('快节奏', '特色', 330), ('车辆改装', '特色', 340);

insert into game_tags(game_id, tag_id)
select g.id, t.id from games g join tags t on
    (g.title = 'Neon Ronin' and t.name in ('动作战斗', '单人', '赛博朋克', '快节奏')) or
    (g.title = 'Dragon Ledger' and t.name in ('开放世界', '单人', '奇幻', '剧情丰富')) or
    (g.title = 'Colony Tactics' and t.name in ('回合制', '基地建设', '单人', '科幻', '资源管理')) or
    (g.title = 'Rain Courier' and t.name in ('叙事探索', '单人', '都市', '剧情丰富', '独立制作')) or
    (g.title = 'Turbo Apex' and t.name in ('竞速', '多人', '车辆改装', '快节奏')) or
    (g.title = 'Steel Harbor' and t.name in ('动作战斗', '战术射击', '多人', '在线合作', '科幻')) or
    (g.title = 'Echoes of Aster' and t.name in ('开放世界', '单人', '奇幻', '剧情丰富')) or
    (g.title = 'Verdant Assembly' and t.name in ('基地建设', '资源管理', '单人', '科幻')) or
    (g.title = 'Paper Moon Hotel' and t.name in ('叙事探索', '单人', '都市', '剧情丰富', '独立制作')) or
    (g.title = 'Circuit Breakers' and t.name in ('竞速', '多人', '车辆改装', '赛博朋克', '快节奏'));
