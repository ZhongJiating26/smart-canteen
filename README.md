# Smart Canteen

### 提交规范：

```text
feat: 新增功能
fix: 修复问题
docs: 文档
test: 测试
refactor: 重构
chore: 配置或构建
```

示例：

```bash
git commit -m "feat(user): 实现密码登录"
git commit -m "feat(order): 实现创建订单并预留库存"
git commit -m "fix(pickup): 防止重复排队"
```

### mysql表导入顺序：

```
deploy/sql/canteen_user.sql
deploy/sql/canteen_menu.sql
deploy/sql/canteen_order.sql
deploy/sql/canteen_pickup.sql
deploy/sql/seata_undo_log.sql
deploy/sql/init_data.sql
```

