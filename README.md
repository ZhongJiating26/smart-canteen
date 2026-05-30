# Smart Canteen 智能食堂系统

面向校园/园区食堂场景的在线点餐、商家备餐、实时叫号与扫码取餐系统。项目采用前后端分离和微服务架构，包含用户端、商家端、管理端、大屏端四个前端入口，以及网关、用户、菜单、订单、取餐排队等后端服务。

## 项目概览

系统覆盖从浏览菜单、下单支付、商家接单、制作出餐、叫号展示到取餐核销的完整流程：

- 用户端：注册登录、浏览商家和菜品、购物车、下单、模拟支付、查看订单和取餐二维码。
- 商家端：菜品维护、每日菜单、订单看板、接单、出餐、手动叫号、取餐核销。
- 管理端：用户管理、商家管理、业务配置维护。
- 大屏端：实时展示制作中队列、待取餐队列和叫号提醒。
- 后端能力：JWT 认证、网关鉴权与限流、库存预占/恢复、订单超时处理、WebSocket 推送、分布式事务。

## 技术栈

### 后端

- Java 17
- Spring Boot 3.3.5
- Spring Cloud 2023.0.3
- Spring Cloud Alibaba 2023.0.3.3
- Spring Cloud Gateway
- Nacos
- MyBatis-Plus
- MySQL 8
- Redis 7
- RocketMQ 5
- Seata
- Knife4j

### 前端

- Vue 3
- Vite
- Vue Router
- Pinia
- Axios
- Material Web / Material Symbols
- Nginx

### 部署

- Docker / Docker Compose
- K3d / K3s
- Kubernetes Deployment / Service / ConfigMap / Secret / Ingress

## 系统架构

```text
浏览器 / 大屏终端
    |
    | 静态页面、/api/**、/ws/**
    v
K3s Ingress / Nginx / Gateway
    |
    +--> gateway-service :9000
            |
            +--> user-service   :9001  用户、认证、Token
            +--> menu-service   :9002  商家、菜品、菜单、库存
            +--> order-service  :9003  下单、支付、订单状态、超时
            +--> pickup-service :9004  取餐号、队列、叫号、核销、WebSocket

基础组件：
MySQL / Redis / Nacos / RocketMQ / Seata
```

## 目录结构

```text
smart-canteen/
├── backend/                    # Spring Cloud 微服务后端
│   ├── smart-canteen-common/   # 通用 DTO、枚举、异常、工具
│   ├── gateway-service/        # API 网关、鉴权、限流、路由
│   ├── user-service/           # 用户、认证、管理员用户接口
│   ├── menu-service/           # 商家、菜品、菜单、库存
│   ├── order-service/          # 订单创建、支付、取消、完成、超时
│   ├── pickup-service/         # 取餐排队、叫号、核销、WebSocket
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   └── canteen-vue/            # Vue3 + Vite 多入口前端
│       ├── src/user/           # 用户端
│       ├── src/merchant/       # 商家端
│       ├── src/admin/          # 管理端
│       ├── src/screen/         # 大屏端
│       ├── src/common/         # 公共 API、状态、工具
│       └── vite.config.js
├── deploy/
│   ├── docker-compose/         # MySQL、Redis、Nacos、RocketMQ、Seata
│   ├── k3s/                    # K3d 集群配置
│   ├── k8s/                    # Kubernetes 资源清单
│   ├── scripts/                # Windows PowerShell 部署脚本
│   └── sql/                    # 数据库建表与初始化数据
├── docs/                       # 需求、设计、测试、部署文档
├── prototype/                  # 原型与实现计划
└── README.md
```

## 环境要求

本地开发建议安装：

- JDK 17
- Maven 3.8+
- Node.js 18+
- npm
- Docker Desktop
- Docker Compose

混合部署还需要：

- kubectl
- k3d

## 快速启动

### 1. 启动基础组件

项目提供 Docker Compose 启动 MySQL、Redis、Nacos、RocketMQ、Seata：

```bash
docker compose -p canteen -f deploy/docker-compose/compose.yaml up -d
docker compose -p canteen -f deploy/docker-compose/compose.yaml ps
```

默认端口：

| 组件 | 宿主机端口 | 默认账号/密码 |
|---|---:|---|
| MySQL | 3106 | root / root123456 |
| Redis | 6179 | root123456 |
| Nacos | 8148 | nacos / nacos |
| RocketMQ NameServer | 9176 | - |
| RocketMQ Broker | 11911 | - |
| RocketMQ Dashboard | 8188 | - |
| Seata | 8191 | - |

首次启动 MySQL 容器时会自动执行 `deploy/sql/` 下的初始化脚本。若需要手动导入，顺序见下方“数据库脚本”。

### 2. 启动后端服务

后端服务默认读取本机基础组件地址。使用 Compose 默认端口时，可在 `backend/` 目录下设置环境变量后启动服务：

```bash
cd backend

export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=3106
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=root123456
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6179
export REDIS_PASSWORD=root123456
export NACOS_SERVER_ADDR=127.0.0.1:8148
export ROCKETMQ_NAME_SERVER=127.0.0.1:9176
export ROCKETMQ_BROKER_A_ADDR=127.0.0.1:11911
export SEATA_SERVER_ADDR=127.0.0.1:8191
export JWT_SECRET=smart-canteen-local-development-secret

mvn -pl gateway-service -am spring-boot:run
mvn -pl user-service -am spring-boot:run
mvn -pl menu-service -am spring-boot:run
mvn -pl order-service -am spring-boot:run
mvn -pl pickup-service -am spring-boot:run
```

各服务端口：

| 服务 | 端口 | 说明 |
|---|---:|---|
| gateway-service | 9000 | API 网关 |
| user-service | 9001 | 用户与认证 |
| menu-service | 9002 | 商家、菜品、库存 |
| order-service | 9003 | 订单 |
| pickup-service | 9004 | 取餐排队与叫号 |

> 各服务需要分别在独立终端中运行。前端开发环境通过 Vite 代理访问 `http://127.0.0.1:9000`。

### 3. 启动前端

```bash
cd frontend/canteen-vue
npm install
npm run dev
```

访问入口：

| 入口 | 地址 |
|---|---|
| 用户端 | `http://localhost:5173/` |
| 商家端 | `http://localhost:5173/merchant.html` |
| 管理端 | `http://localhost:5173/admin.html` |
| 大屏端 | `http://localhost:5173/screen.html` |

## 构建与测试

### 后端

```bash
cd backend
mvn test
mvn -DskipTests package
```

单个模块测试示例：

```bash
cd backend
mvn -pl order-service -am test
```

### 前端

```bash
cd frontend/canteen-vue
npm run build
npm run preview
```

当前前端未配置单元测试脚本。

## Docker 与 K3s 部署

本项目支持“基础组件 Docker Compose + 应用服务 K3s/K3d”的混合部署模式：

- Compose 运行 MySQL、Redis、Nacos、RocketMQ、Seata。
- K3s/K3d 运行前端 Nginx、Gateway 和业务微服务。
- Ingress 统一暴露静态页面、`/api/**` 和 `/ws/**`。

Windows PowerShell 脚本位于 `deploy/scripts/`：

```powershell
deploy\scripts\build-images.ps1
deploy\scripts\push-images.ps1
deploy\scripts\apply-all.ps1
deploy\scripts\check-all.ps1
```

部署完成后的默认入口：

| 入口 | 地址 |
|---|---|
| 用户端 | `http://localhost:8180/` |
| 商家端 | `http://localhost:8180/merchant.html` |
| 管理端 | `http://localhost:8180/admin.html` |
| 大屏端 | `http://localhost:8180/screen.html` |
| API | `http://localhost:8180/api/**` |
| WebSocket | `ws://localhost:8180/ws/screen/{merchantId}` |

更完整的部署步骤见 [docs/A部署文档.md](docs/A部署文档.md)。

## 数据库脚本

SQL 文件位于 `deploy/sql/`。手动导入时请按以下顺序执行：

```text
deploy/sql/canteen_user.sql
deploy/sql/canteen_menu.sql
deploy/sql/canteen_order.sql
deploy/sql/canteen_pickup.sql
deploy/sql/seata_undo_log.sql
deploy/sql/init_data.sql
```

## 常用接口入口

本地开发时统一访问 Gateway：

```text
http://localhost:9000/api/**
ws://localhost:9000/ws/**
```

K3s 部署后统一访问 Ingress：

```text
http://localhost:8180/api/**
ws://localhost:8180/ws/**
```

主要路由前缀：

| 前缀 | 目标服务 |
|---|---|
| `/api/user/**` | user-service |
| `/api/admin/users/**` | user-service |
| `/api/menu/**` | menu-service |
| `/api/admin/merchants/**` | menu-service |
| `/api/order/**` | order-service |
| `/api/pickup/**` | pickup-service |
| `/ws/**` | pickup-service |

## 开发约定

### 提交规范

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

### 分支与代码建议

- 每个微服务维护自己的 Controller、Service、Mapper、Entity、DTO、VO。
- 跨服务通用类型放入 `smart-canteen-common`。
- 后端接口统一返回 `Result` / `PageResult`。
- 受保护接口通过 Gateway 校验 JWT，下游服务读取用户上下文。
- 订单、库存、队列相关改动需要同步补充或更新测试。

## 相关文档

- [docs/A智能食堂系统需求规格说明书.md](docs/A智能食堂系统需求规格说明书.md)
- [docs/A架构设计文档.md](docs/A架构设计文档.md)
- [docs/A智能食堂系统概要设计说明书.md](docs/A智能食堂系统概要设计说明书.md)
- [docs/A详细设计说明书.md](docs/A详细设计说明书.md)
- [docs/A智能食堂系统测试用例文档.md](docs/A智能食堂系统测试用例文档.md)
- [docs/A部署文档.md](docs/A部署文档.md)
