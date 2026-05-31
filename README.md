# GameMall

GameMall 是一个面向数字游戏商品交易场景的类 Steam 商城后端项目，覆盖游戏展示、分类检索、购物车、订单创建、支付模拟、库存管理和后台商品运营。

## 技术栈

- Spring Boot 2.7.18 / Java 17
- MyBatis / MySQL 8
- Redis 缓存
- Spring Security / JWT
- Swagger OpenAPI

## 快速启动

1. 在 MySQL 执行 `src/main/resources/db/schema.sql` 和 `src/main/resources/db/data.sql`。
2. 确认 Redis 运行在 `localhost:6379`。Redis 未启动时，商品接口仍会回退查询数据库。
3. 修改 `src/main/resources/application.yml` 中的数据源，当前配置为：
   - host: `localhost`
   - database: `gamemall`
   - username: `root`
   - password: `123456`
4. 启动项目：

```bash
mvn spring-boot:run
```

Swagger 地址：`http://localhost:8080/swagger-ui.html`

## 默认账号

- 管理员：`admin / admin123`
- 普通用户：调用 `/api/auth/register` 注册

## 核心接口

- `POST /api/auth/register` 用户注册
- `POST /api/auth/login` 用户登录，返回 JWT
- `GET /api/categories` 分类列表
- `GET /api/games` 游戏分页检索，支持 `categoryId`、`keyword`
- `GET /api/games/hot` 热门游戏，Redis 缓存
- `GET /api/games/{id}` 商品详情，Redis 缓存
- `POST /api/cart` 加入购物车
- `GET /api/cart` 查看购物车
- `POST /api/orders` 创建订单并扣减库存
- `POST /api/orders/{id}/pay` 模拟支付
- `POST /api/orders/{id}/cancel` 取消订单并回滚库存
- `POST /api/admin/games` 后台新增游戏，需 ADMIN JWT
- `PUT /api/admin/games/{id}` 后台编辑游戏，需 ADMIN JWT
- `PATCH /api/admin/games/{id}/status` 商品上下架，需 ADMIN JWT

## 订单状态

- `10` 待支付
- `20` 已支付
- `30` 已取消
- `40` 已关闭

下单时通过 MySQL 条件更新扣减库存：`stock >= quantity`，避免超卖。待支付订单超过配置的 `gamemall.order.timeout-minutes` 后，会由定时任务取消并回滚库存。
