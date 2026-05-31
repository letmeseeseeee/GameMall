create database if not exists gamemall default character set utf8mb4 collate utf8mb4_unicode_ci;
use gamemall;

drop table if exists order_items;
drop table if exists orders;
drop table if exists cart_items;
drop table if exists games;
drop table if exists categories;
drop table if exists users;

create table users (
    id bigint primary key auto_increment,
    username varchar(32) not null unique,
    password_hash varchar(100) not null,
    nickname varchar(64) not null,
    role varchar(16) not null default 'USER',
    status tinyint not null default 1 comment '1 enabled, 0 disabled',
    created_at datetime not null,
    updated_at datetime not null,
    index idx_users_username (username)
) engine=InnoDB default charset=utf8mb4;

create table categories (
    id bigint primary key auto_increment,
    name varchar(64) not null unique,
    sort_order int not null default 0
) engine=InnoDB default charset=utf8mb4;

create table games (
    id bigint primary key auto_increment,
    category_id bigint not null,
    title varchar(128) not null,
    developer varchar(128),
    publisher varchar(128),
    price decimal(10,2) not null,
    stock int not null default 0,
    sold_count int not null default 0,
    cover_url varchar(500),
    description text,
    status tinyint not null default 1 comment '1 online, 0 offline',
    release_date datetime,
    created_at datetime not null,
    updated_at datetime not null,
    constraint fk_games_category foreign key (category_id) references categories(id),
    index idx_games_category (category_id),
    index idx_games_status_sold (status, sold_count)
) engine=InnoDB default charset=utf8mb4;

create table cart_items (
    id bigint primary key auto_increment,
    user_id bigint not null,
    game_id bigint not null,
    quantity int not null,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_cart_user_game (user_id, game_id),
    constraint fk_cart_user foreign key (user_id) references users(id),
    constraint fk_cart_game foreign key (game_id) references games(id)
) engine=InnoDB default charset=utf8mb4;

create table orders (
    id bigint primary key auto_increment,
    order_no varchar(40) not null unique,
    user_id bigint not null,
    total_amount decimal(10,2) not null,
    status tinyint not null comment '10 pending payment, 20 paid, 30 cancelled, 40 closed',
    payment_status tinyint not null comment '0 unpaid, 1 paid, 2 refunded',
    paid_at datetime null,
    expire_at datetime not null,
    created_at datetime not null,
    updated_at datetime not null,
    constraint fk_orders_user foreign key (user_id) references users(id),
    index idx_orders_user (user_id, id),
    index idx_orders_expire (status, expire_at)
) engine=InnoDB default charset=utf8mb4;

create table order_items (
    id bigint primary key auto_increment,
    order_id bigint not null,
    game_id bigint not null,
    game_title varchar(128) not null,
    price decimal(10,2) not null,
    quantity int not null,
    subtotal decimal(10,2) not null,
    constraint fk_order_items_order foreign key (order_id) references orders(id),
    constraint fk_order_items_game foreign key (game_id) references games(id),
    index idx_order_items_order (order_id)
) engine=InnoDB default charset=utf8mb4;
