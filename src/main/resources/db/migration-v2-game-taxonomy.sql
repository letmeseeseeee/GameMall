use gamemall;

alter table categories add column code varchar(32) null after name;
alter table categories add column description varchar(255) null after code;

update categories set name = '动作冒险', code = 'action', description = '强调即时操作、战斗反馈与关卡探索' where id = 1;
update categories set code = 'rpg', description = '通过角色成长、装备构筑和剧情选择推进冒险' where id = 2;
update categories set name = '策略模拟', code = 'strategy', description = '围绕资源管理、战术决策和系统经营展开' where id = 3;
update categories set name = '独立叙事', code = 'indie', description = '聚焦独特表达、氛围塑造与作者化体验' where id = 4;
update categories set code = 'sports', description = '包含竞速、竞技运动与多人对抗体验' where id = 5;

alter table categories modify column code varchar(32) not null;
alter table categories modify column description varchar(255) not null;
alter table categories add unique key uk_categories_code (code);

create table tags (
    id bigint primary key auto_increment,
    name varchar(32) not null unique,
    group_name varchar(32) not null,
    sort_order int not null default 0,
    index idx_tags_group_sort (group_name, sort_order)
) engine=InnoDB default charset=utf8mb4;

create table game_tags (
    game_id bigint not null,
    tag_id bigint not null,
    primary key (game_id, tag_id),
    constraint fk_game_tags_game foreign key (game_id) references games(id) on delete cascade,
    constraint fk_game_tags_tag foreign key (tag_id) references tags(id) on delete cascade,
    index idx_game_tags_tag_game (tag_id, game_id)
) engine=InnoDB default charset=utf8mb4;

insert into tags(name, group_name, sort_order)
values
('动作战斗', '玩法', 10), ('开放世界', '玩法', 20), ('回合制', '玩法', 30),
('基地建设', '玩法', 40), ('叙事探索', '玩法', 50), ('竞速', '玩法', 60),
('战术射击', '玩法', 70), ('资源管理', '玩法', 80),
('单人', '模式', 110), ('多人', '模式', 120), ('在线合作', '模式', 130),
('赛博朋克', '题材', 210), ('奇幻', '题材', 220), ('科幻', '题材', 230), ('都市', '题材', 240),
('剧情丰富', '特色', 310), ('独立制作', '特色', 320), ('快节奏', '特色', 330), ('车辆改装', '特色', 340);

insert into games(category_id, title, developer, publisher, price, stock, sold_count, cover_url, description, status, release_date, created_at, updated_at)
select c.id, seed.title, seed.developer, seed.publisher, seed.price, seed.stock, seed.sold_count, seed.cover_url,
       seed.description, 1, seed.release_date, now(), now()
from categories c
join (
    select '动作冒险' category_name, 'Steel Harbor' title, 'Breakwater Interactive' developer, 'Iron Tide' publisher,
           128.00 price, 150 stock, 174 sold_count, 'https://picsum.photos/seed/steel-harbor/600/360' cover_url,
           '以港口争夺为核心的战术动作游戏，支持合作突袭和多人竞技。' description, '2025-09-26 00:00:00' release_date
    union all select '角色扮演', 'Echoes of Aster', 'Silver Map Studio', 'Northwind Games', 148.00, 95, 203,
           'https://picsum.photos/seed/echoes-aster/600/360', '探索失落星界遗迹，在动态事件和分支任务中塑造角色命运。', '2025-05-16 00:00:00'
    union all select '策略模拟', 'Verdant Assembly', 'Mosslight Works', 'Strategy Forge', 88.00, 240, 137,
           'https://picsum.photos/seed/verdant-assembly/600/360', '建设生态殖民地，在有限资源与环境变化之间制定长期发展策略。', '2024-08-30 00:00:00'
    union all select '独立叙事', 'Paper Moon Hotel', 'Soft Corner', 'Indie Lane', 56.00, 320, 121,
           'https://picsum.photos/seed/paper-moon-hotel/600/360', '经营一间只在午夜出现的旅店，倾听旅客故事并改变他们的结局。', '2024-04-12 00:00:00'
    union all select '体育竞速', 'Circuit Breakers', 'Vector Mile', 'Redline Team', 108.00, 110, 289,
           'https://picsum.photos/seed/circuit-breakers/600/360', '未来都市街机竞速，包含组队赛事、车辆改装与赛季挑战。', '2025-06-20 00:00:00'
) seed on seed.category_name = c.name
where not exists (select 1 from games existing where existing.title = seed.title);

insert ignore into game_tags(game_id, tag_id)
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
