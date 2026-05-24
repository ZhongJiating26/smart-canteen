# Seata 联调说明

## 当前接入点

- `order-service` 已在创建订单入口使用 `@GlobalTransactional(name = "create-order-reserve-inventory")`。
- `menu-service` 的库存预占 `InventoryServiceImpl.reserve` 已使用本地 `@Transactional`，可作为 Seata AT 分支事务参与者。
- `canteen_order` 和 `canteen_menu` 都需要执行 `deploy/sql/seata_undo_log.sql` 创建 `undo_log`。
- `order-service` 与 `menu-service` 已配置同一个事务分组：`smart-canteen-tx-group`。

## 本地启动参数

基础组件使用 `deploy/docker-compose/compose.yaml`：

```bash
docker compose -f deploy/docker-compose/compose.yaml up -d mysql redis nacos rocketmq-namesrv rocketmq-broker rocketmq-init seata
```

服务启动时打开 Seata：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.0.jdk/Contents/Home
export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=3106
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=root123456
export NACOS_SERVER_ADDR=127.0.0.1:8148
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6179
export REDIS_PASSWORD=root123456
export SEATA_ENABLED=true
export SEATA_SERVER_ADDR=127.0.0.1:8191
export ROCKETMQ_NAME_SERVER=127.0.0.1:9176
export ROCKETMQ_BROKER_IP=127.0.0.1
export ROCKETMQ_BROKER_PORT=11911
export ROCKETMQ_BROKER_HA_PORT=11909
export ROCKETMQ_TOPICS=order-timeout
```

按顺序启动：

```bash
cd backend
./menu-service/mvnw -q -f pom.xml -pl menu-service -am spring-boot:run
./order-service/mvnw -q -f pom.xml -pl order-service -am spring-boot:run
```

## 正常提交验证

1. 调用 order-service 创建订单接口。
2. 预期 `canteen_order.order` 和 `canteen_order.order_item` 有新数据。
3. 预期 `canteen_menu.daily_menu_item.remaining_stock` 减少、`reserved_stock` 增加。
4. Seata Server 控制台无未完成异常事务。

## 异常回滚验证

1. 使用库存不足的 `dailyMenuItemId` 或数量调用创建订单。
2. 预期接口返回库存不足。
3. 预期订单表没有新增脏订单。
4. 预期菜单库存不变化。

## 当前本机状态

本次代码侧配置已补齐并通过编译；当前 shell 中未检测到 `docker` / `podman` / `colima` 命令，因此本机容器级真联调尚未执行。
