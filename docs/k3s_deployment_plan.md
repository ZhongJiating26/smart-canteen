# 智能食堂点餐与取餐微服务系统混合部署方案说明书

| 文档名称 | K3s + Docker Compose 混合部署方案说明书 |
|---|---|
| 项目名称 | 智能食堂点餐与取餐微服务系统 |
| 部署环境 | Windows + Docker Desktop + K3d/K3s + Docker Compose |
| 部署方式 | MySQL、Redis、Nacos、RocketMQ、Seata 使用 Docker Compose；前端和后端微服务部署到 K3d/K3s |
| 适用阶段 | 课程作业部署、验收演示、本地集成测试 |

## 1. 文档目的

本文档说明系统在 Windows 本地环境中的混合部署方案。开发和演示阶段继续使用 Docker Compose 运行 MySQL、Redis、Nacos、RocketMQ、Seata 等基础组件；K3d/K3s 仅承载前端 Nginx、Spring Cloud Gateway 和四个业务微服务。

这样可以保留基础组件单机部署的稳定性和可调试性，同时用 K3s 展示前后端应用的容器化编排、Service、Deployment、ConfigMap、Secret 与 Ingress。

## 2. 部署目标

- 基础组件由 `deploy/docker-compose/compose.yaml` 启动，数据持久化由 Docker volume 管理。
- 前端、Gateway、User Service、Menu Service、Order Service、Pickup Service 部署在 K3s 的 `canteen` 命名空间。
- Traefik Ingress 作为统一入口，暴露 `http://localhost/`。
- `/`、`/merchant.html`、`/admin.html`、`/screen.html` 转发到前端 Nginx。
- `/api/**` 和 `/ws/**` 转发到 Gateway。
- K3s 中的后端 Pod 通过宿主机映射端口访问 Compose 基础组件。

最终访问地址如下：

| 访问对象 | 访问地址 | 说明 |
|---|---|---|
| 用户端 | `http://localhost/` | 用户在线点餐页面 |
| 商家端 | `http://localhost/merchant.html` | 商家接单、备餐、出餐页面 |
| 管理端 | `http://localhost/admin.html` | 管理员维护菜品、菜单、用户等信息 |
| 大屏端 | `http://localhost/screen.html` | 食堂电视大屏展示叫号和待取餐队列 |
| 后端 API | `http://localhost/api/**` | 统一经过 Gateway 转发 |
| WebSocket | `ws://localhost/ws/screen/{merchantId}` | 大屏实时叫号推送 |

## 3. 总体部署架构

```text
Windows 主机
├── Docker Compose 基础组件
│   ├── MySQL      localhost:3106 -> 3306
│   ├── Redis      localhost:6179 -> 6379
│   ├── Nacos      localhost:8148 -> 8848
│   ├── RocketMQ   localhost:9176 -> 9876, localhost:11911 -> 10911
│   └── Seata      localhost:8191 -> 8091
└── Docker Desktop / WSL2
    └── K3d 集群 smart-canteen
        ├── Traefik Ingress Controller
        └── canteen 命名空间
            ├── Frontend Nginx Deployment
            ├── Gateway Service Deployment
            ├── User Service Deployment
            ├── Menu Service Deployment
            ├── Order Service Deployment
            └── Pickup Service Deployment
```

Ingress 路由关系如下：

```text
浏览器 / 客户端
    ↓
http://localhost:80
    ↓
Traefik Ingress
    ├── /、/merchant.html、/admin.html、/screen.html → frontend-nginx:80
    ├── /api/**                                  → gateway-service:9000
    └── /ws/**                                   → gateway-service:9000
```

K3s Pod 访问 Compose 基础组件的地址由 `deploy/k8s/02-configmap.yaml` 管理：

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `NACOS_SERVER_ADDR` | `host.k3d.internal:8148` | Nacos 注册中心 |
| `MYSQL_HOST` / `MYSQL_PORT` | `host.k3d.internal` / `3106` | MySQL |
| `REDIS_HOST` / `REDIS_PORT` | `host.k3d.internal` / `6179` | Redis |
| `ROCKETMQ_NAME_SERVER` | `host.k3d.internal:9176` | RocketMQ NameServer |
| `ROCKETMQ_BROKER_A_ADDR` | `host.k3d.internal:11911` | RocketMQ Broker 地址覆盖 |
| `SEATA_SERVER_ADDR` | `host.k3d.internal:8191` | Seata Server |

> `host.k3d.internal` 是 K3d 容器访问宿主机的默认主机名。如果所在环境不支持该名称，可将 ConfigMap 中的地址改成宿主机局域网 IP。

## 4. 运行环境要求

| 软件 | 建议版本 | 用途 |
|---|---|---|
| Windows | Windows 10/11 64 位 | 宿主机操作系统 |
| WSL2 | 已启用 | Docker Desktop Linux 容器运行基础 |
| Docker Desktop | 最新稳定版 | 容器和 K3d 运行环境 |
| Docker Compose | Docker Desktop 内置版本即可 | 启动基础组件 |
| kubectl | 与 K3s 兼容即可 | 操作 Kubernetes 集群 |
| k3d | v5.x | 在 Docker 中创建 K3s 集群 |
| JDK | 17 | Spring Boot 3.x 微服务构建 |
| Maven | 3.8+ | 后端服务打包 |
| Node.js | 18+ | 前端 Vue3 项目构建 |

端口规划：

| 端口 | 所属对象 | 说明 |
|---:|---|---|
| 80 | K3d LoadBalancer / Traefik Ingress | 系统统一入口 |
| 443 | K3d LoadBalancer / Traefik Ingress | 预留 HTTPS |
| 5000 | 本地镜像仓库 | 存放本项目镜像 |
| 3106 | MySQL Compose 映射端口 | 业务数据库 |
| 6179 | Redis Compose 映射端口 | 缓存、限流、Token 黑名单 |
| 8148 | Nacos Compose 映射端口 | 注册中心和配置中心 |
| 9176 | RocketMQ NameServer Compose 映射端口 | RocketMQ 服务发现 |
| 11911 | RocketMQ Broker Compose 映射端口 | 消息收发 |
| 8191 | Seata Compose 映射端口 | 分布式事务服务端 |

## 5. 部署目录规划

```text
smart-canteen/
├── backend/
├── frontend/
├── deploy/
│   ├── docker-compose/
│   │   └── compose.yaml
│   ├── k3s/
│   │   └── cluster.yaml
│   ├── k8s/
│   │   ├── 00-namespace.yaml
│   │   ├── 01-secret.yaml
│   │   ├── 02-configmap.yaml
│   │   ├── apps/
│   │   │   ├── user-service.yaml
│   │   │   ├── menu-service.yaml
│   │   │   ├── pickup-service.yaml
│   │   │   ├── order-service.yaml
│   │   │   ├── gateway-service.yaml
│   │   │   └── frontend.yaml
│   │   └── ingress.yaml
│   ├── scripts/
│   │   ├── build-images.ps1
│   │   ├── push-images.ps1
│   │   ├── apply-all.ps1
│   │   └── check-all.ps1
│   └── sql/
└── docs/
```

`deploy/k8s/infra` 已不再需要；MySQL、Redis、Nacos、RocketMQ、Seata 不再维护 Kubernetes StatefulSet / PVC 清单。

## 6. 启动 Docker Compose 基础组件

进入项目根目录后执行：

```powershell
docker compose -f deploy/docker-compose/compose.yaml up -d
docker compose -f deploy/docker-compose/compose.yaml ps
```

MySQL 初始化 SQL 已在 Compose 中按顺序挂载：

1. `deploy/sql/canteen_user.sql`
2. `deploy/sql/canteen_menu.sql`
3. `deploy/sql/canteen_order.sql`
4. `deploy/sql/canteen_pickup.sql`
5. `deploy/sql/seata_undo_log.sql`
6. `deploy/sql/init_data.sql`

首次启动时 MySQL 会自动执行这些 SQL。若需要清空演示数据重新初始化：

```powershell
docker compose -f deploy/docker-compose/compose.yaml down -v
docker compose -f deploy/docker-compose/compose.yaml up -d
```

常用验证命令：

```powershell
docker exec -it canteen-mysql mysql -uroot -proot123456 -e "SHOW DATABASES;"
docker exec -it canteen-redis redis-cli -a root123456 ping
```

浏览器访问 Nacos 控制台：

```text
http://localhost:8148/nacos
```

## 7. 创建 K3d/K3s 集群

K3d 集群配置文件：`deploy/k3s/cluster.yaml`。

```yaml
apiVersion: k3d.io/v1alpha5
kind: Simple
metadata:
  name: smart-canteen
servers: 1
agents: 1
ports:
  - port: 80:80
    nodeFilters:
      - loadbalancer
  - port: 443:443
    nodeFilters:
      - loadbalancer
registries:
  create:
    name: smart-canteen-registry
    host: 0.0.0.0
    hostPort: "5000"
options:
  k3d:
    wait: true
    timeout: 120s
```

创建集群：

```powershell
k3d cluster create --config deploy/k3s/cluster.yaml
kubectl cluster-info
kubectl get nodes
kubectl get pods -A
```

如果 80 端口被占用，可删除集群后将 `cluster.yaml` 中端口改成 `8080:80`，此时访问地址改为 `http://localhost:8080/`。

## 8. 应用资源配置

K3s 中只创建 `canteen` 命名空间：

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: canteen
```

Secret 只保存应用侧需要的密码和 JWT 密钥：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: canteen-app-secret
  namespace: canteen
type: Opaque
stringData:
  MYSQL_USERNAME: root
  MYSQL_PASSWORD: root123456
  REDIS_PASSWORD: root123456
  JWT_SECRET: er85>:UPGNUwm3sDhTTUm{0Oo+9MOE.=
```

ConfigMap 指向 Compose 暴露在宿主机上的端口：

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: canteen-common-config
  namespace: canteen
data:
  NACOS_SERVER_ADDR: host.k3d.internal:8148
  MYSQL_HOST: host.k3d.internal
  MYSQL_PORT: "3106"
  REDIS_HOST: host.k3d.internal
  REDIS_PORT: "6179"
  ROCKETMQ_NAME_SERVER: host.k3d.internal:9176
  ROCKETMQ_BROKER_A_ADDR: host.k3d.internal:11911
  SEATA_SERVER_ADDR: host.k3d.internal:8191
```

后端 Deployment 通过 `envFrom` 引入该 ConfigMap 和 Secret。各服务仍以 Kubernetes Service 暴露集群内部端口，Gateway 通过 Nacos 的 `lb://service-name` 路由到业务服务。

## 9. 构建与推送镜像

构建后端和前端镜像：

```powershell
cd deploy/scripts
.\build-images.ps1
```

推送到 K3d 本地镜像仓库：

```powershell
.\push-images.ps1
```

当前应用镜像名称如下：

| 服务 | 镜像 |
|---|---|
| Gateway | `smart-canteen-registry:5000/gateway-service:1.0.0` |
| User Service | `smart-canteen-registry:5000/user-service:1.0.0` |
| Menu Service | `smart-canteen-registry:5000/menu-service:1.0.0` |
| Order Service | `smart-canteen-registry:5000/order-service:1.0.0` |
| Pickup Service | `smart-canteen-registry:5000/pickup-service:1.0.0` |
| Frontend Nginx | `smart-canteen-registry:5000/frontend-nginx:1.0.0` |

## 10. 部署前端和后端到 K3s

确保 Compose 基础组件已经启动后，执行：

```powershell
cd deploy/scripts
.\apply-all.ps1
```

脚本执行内容：

1. 应用 `00-namespace.yaml`、`01-secret.yaml`、`02-configmap.yaml`。
2. 部署 User、Menu、Pickup、Order、Gateway、Frontend。
3. 应用 Ingress。

手动部署时可执行：

```powershell
kubectl apply -f deploy/k8s/00-namespace.yaml
kubectl apply -f deploy/k8s/01-secret.yaml
kubectl apply -f deploy/k8s/02-configmap.yaml
kubectl apply -f deploy/k8s/apps/user-service.yaml
kubectl apply -f deploy/k8s/apps/menu-service.yaml
kubectl apply -f deploy/k8s/apps/pickup-service.yaml
kubectl apply -f deploy/k8s/apps/order-service.yaml
kubectl apply -f deploy/k8s/apps/gateway-service.yaml
kubectl apply -f deploy/k8s/apps/frontend.yaml
kubectl apply -f deploy/k8s/ingress.yaml
```

建议启动顺序如下：

```text
Docker Compose 基础组件
  → User Service / Menu Service / Pickup Service
    → Order Service
      → Gateway
        → Frontend Nginx
          → Ingress
```

## 11. 部署验证

快速检查：

```powershell
cd deploy/scripts
.\check-all.ps1
```

或手动执行：

```powershell
docker compose -f deploy/docker-compose/compose.yaml ps
kubectl get pods -n canteen
kubectl get svc -n canteen
kubectl get ingress -n canteen
```

检查服务注册：

```text
http://localhost:8148/nacos
```

进入 Nacos 后应能看到：

```text
gateway-service
user-service
menu-service
order-service
pickup-service
```

检查应用访问：

```text
http://localhost/
http://localhost/merchant.html
http://localhost/admin.html
http://localhost/screen.html
```

查看日志：

```powershell
kubectl logs -n canteen deployment/gateway-service
kubectl logs -n canteen deployment/order-service
docker logs canteen-nacos
docker logs canteen-rocketmq-broker
docker logs canteen-seata
```

## 12. 常见问题

| 问题 | 可能原因 | 处理方式 |
|---|---|---|
| Pod 无法连接 MySQL / Redis / Nacos | `host.k3d.internal` 不可解析或端口未开放 | 将 `deploy/k8s/02-configmap.yaml` 中地址改为宿主机局域网 IP 后重新 apply |
| `unknown database` | MySQL volume 已存在，初始化 SQL 未重新执行 | 执行 `docker compose -f deploy/docker-compose/compose.yaml down -v` 后重新启动基础组件 |
| Nacos 无服务实例 | 后端 Pod 未启动或 `NACOS_SERVER_ADDR` 配置错误 | 查看 Pod 日志并确认 `host.k3d.internal:8148` 可访问 |
| RocketMQ 发送失败 | Broker 地址回传为 Compose 内部地址 | 确认 `ROCKETMQ_BROKER_A_ADDR=host.k3d.internal:11911` 已注入 Order Service |
| Seata 连接失败 | Seata Compose 容器未启动或端口错误 | 检查 `docker logs canteen-seata` 和 `SEATA_SERVER_ADDR` |
| Ingress 访问 404 | Ingress 未应用或服务未就绪 | 检查 `kubectl get ingress -n canteen` 和前端 / Gateway Service |

## 13. 清理环境

删除 K3s 应用资源：

```powershell
kubectl delete -f deploy/k8s/ingress.yaml
kubectl delete -f deploy/k8s/apps/frontend.yaml
kubectl delete -f deploy/k8s/apps/gateway-service.yaml
kubectl delete -f deploy/k8s/apps/order-service.yaml
kubectl delete -f deploy/k8s/apps/pickup-service.yaml
kubectl delete -f deploy/k8s/apps/menu-service.yaml
kubectl delete -f deploy/k8s/apps/user-service.yaml
kubectl delete -f deploy/k8s/02-configmap.yaml
kubectl delete -f deploy/k8s/01-secret.yaml
kubectl delete -f deploy/k8s/00-namespace.yaml
```

停止基础组件：

```powershell
docker compose -f deploy/docker-compose/compose.yaml down
```

如需清理基础组件数据：

```powershell
docker compose -f deploy/docker-compose/compose.yaml down -v
```

删除 K3d 集群：

```powershell
k3d cluster delete smart-canteen
```

## 14. 总结

本方案将基础组件保留在 Docker Compose 中运行，将前端和后端应用部署到 K3d/K3s。它减少了本地 StatefulSet、PVC 和基础件排障成本，同时保留了应用层 Kubernetes 部署、服务发现、统一 Ingress 入口和容器镜像交付流程。
