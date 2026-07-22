# GameMall

GameMall 是一个面向数字游戏商品交易场景的类 Steam 全栈商城项目，覆盖游戏展示、分类检索、购物车、订单创建、支付模拟、库存管理、订单状态流转和后台商品运营。项目自带响应式 Web 界面，可直接完成从商品浏览到订单支付的完整演示。

## 技术栈

- Spring Boot 2.7.18 / Java 17
- MyBatis / MySQL 8
- Redis 缓存
- Spring Security / JWT
- Swagger OpenAPI
- 原生 HTML / CSS / JavaScript 响应式 Web UI

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

商城地址：`http://localhost:8080/`

## Web 界面

- 商城：首页主视觉、主类型入口、细分标签联合筛选、关键词搜索、商品列表和商品详情。
- 购物车：商品加购、数量调整、移除商品、金额汇总和订单提交。
- 订单中心：全部、待支付、已支付、已关闭订单筛选，支持查看明细、模拟支付和取消订单。
- 运营后台：仅管理员可见，展示商品、库存、销量和低库存指标，支持新增、编辑和下架商品。
- 响应式布局：桌面端使用完整导航和运营表格，移动端提供底部导航与单列商品流。

## 默认账号

- 管理员：`admin / admin123`
- 普通用户：调用 `/api/auth/register` 注册

## 核心接口

- `POST /api/auth/register` 用户注册
- `POST /api/auth/login` 用户登录，返回 JWT
- `GET /api/categories` 分类列表
- `GET /api/tags` 细分标签列表，按玩法、模式、题材和特色分组
- `GET /api/games` 游戏分页检索，支持 `categoryId`、`tagId`、`keyword` 联合筛选
- `GET /api/games/hot` 热门游戏，Redis 缓存
- `GET /api/games/{id}` 商品详情，Redis 缓存
- `POST /api/cart` 加入购物车
- `GET /api/cart` 查看购物车
- `POST /api/orders` 创建订单并扣减库存，支持 `idempotencyKey` 防重复提交
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

## 高并发处理

- 库存扣减使用单条 SQL 条件更新：`update games set stock = stock - ? where id = ? and status = 1 and stock >= ?`。
- 订单创建先写入 `CREATING` 状态，再在同一事务内扣库存、写明细、推进到 `PENDING_PAYMENT`。
- `orders` 表增加 `(user_id, idempotency_key)` 唯一索引，同一用户同一幂等键只会创建一笔订单。
- 并发重复请求如果读到 `CREATING` 中间态，会短暂等待订单完成后返回最终订单，避免半成品响应。
- 支付和取消都使用状态条件更新，只允许待支付订单流转，避免重复支付、重复取消和库存重复回滚。
- Hikari 连接池在 `application.yml` 中配置为最大 30 连接，适合本地中小规模压测。

## 游戏分类模型

- `categories` 保存每款游戏唯一的主类型，例如动作冒险、角色扮演和策略模拟。
- `tags` 保存玩法、模式、题材和特色等细分维度。
- `game_tags` 通过多对多关系允许一款游戏关联多个细分标签。
- 商品查询支持 `categoryId + tagId + keyword` 联合过滤，标签关联使用组合主键并建立反向查询索引。
- 从旧版数据库升级时执行 `src/main/resources/db/migration-v2-game-taxonomy.sql`，无需删除现有用户和订单数据。

压测记录见 [docs/performance-test.md](docs/performance-test.md)，学术研究风格报告见 [docs/academic-stress-report.md](docs/academic-stress-report.md)。
