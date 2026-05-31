# Performance Test

测试日期：2026-06-01

## 测试环境

- Windows 10
- Java 17
- Spring Boot 2.7.18
- MySQL 8.0.33
- 本地服务：`http://localhost:8080`
- Maven 本地仓库：`F:/work/maven-repository`

## 自动化并发测试

命令：

```powershell
mvn -q test
```

覆盖场景：

- 200 个线程同时下单，库存 40，成功 40，拒绝 160，库存最低为 0，无超卖。
- 30 个线程使用同一用户同一 `idempotencyKey` 同时下单，只生成 1 笔订单，只扣 1 件库存。

## HTTP 订单压测

命令：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/stress-order.ps1 -Users 200 -Concurrency 64 -GameId 1 -Quantity 1
```

实测结果：

```text
users          : 200
concurrency    : 64
success        : 120
failed         : 80
elapsedSeconds : 5.97
throughput     : 33.48
```

数据库校验：

```text
game_id=1 stock=0
status=10 count=120
order_items=120
total_quantity=120
```

结论：游戏 1 初始库存为 120，200 次并发下单只成功 120 次，订单明细总数量为 120，未出现超卖。

## HTTP 幂等压测

场景：同一用户、同一 `idempotencyKey`、30 个并发请求同时创建游戏 2 订单。

结果：

```text
30 True, 201, 10, 1, success
```

数据库校验：

```text
game_id=2 stock=79
idempotent_orders=1
idempotent_items=1
```

结论：30 次重复提交返回同一笔订单，数据库只创建 1 笔订单、1 条明细，库存只扣减 1 件。
