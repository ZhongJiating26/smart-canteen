# 智能食堂点餐与取餐微服务系统混合部署方案说明书

| 文档名称 | K3s + Docker Compose 混合部署方案说明书 |
|---|---|
| 项目名称 | 智能食堂点餐与取餐微服务系统 |
| 部署环境 | Windows + Docker Desktop + K3d/K3s + Docker Compose |
| 部署方式 | MySQL、Redis、Nacos、RocketMQ、Seata 使用 Docker Compose；前端和后端微服务部署到 K3d/K3s |
| 适用阶段 | 课程作业部署、验收演示、本地集成测试 |

## 1. 文档目的

本文档说明系统在 Windows 本地环境中的混合部署方案。基础组件继续使用 Docker Compose 运行，便于本地调试和数据持久化；前端 Nginx、Spring Cloud Gateway 和业务微服务部署到 K3d/K3s，用于展示 Kubernetes Deployment、Service、ConfigMap、Secret、Ingress 和镜像交付流程。

本文档按实际操作顺序编写。除特别说明外，所有 PowerShell 命令均在项目根目录执行：

```powershell
cd E:\Projects\smart-canteen
```

## 2. 部署目标与最终效果

基础组件由 `deploy/docker-compose/compose.yaml` 启动：

- MySQL：宿主机 `localhost:3106`
- Redis：宿主机 `localhost:6179`
- Nacos：宿主机 `localhost:8148`
- RocketMQ NameServer：宿主机 `localhost:9176`
- RocketMQ Broker：宿主机 `localhost:11911`
- Seata：宿主机 `localhost:8191`

应用组件部署到 K3d/K3s 的 `canteen` 命名空间：

- `frontend-nginx`
- `gateway-service`
- `user-service`
- `menu-service`
- `order-service`
- `pickup-service`

当前 K3d 入口端口配置为 `8180:80`，因此浏览器最终访问地址如下：

| 访问对象 | 访问地址 | 验证目标 |
|---|---|---|
| 用户端 | `http://localhost:8180/` | 打开用户在线点餐页面 |
| 商家端 | `http://localhost:8180/merchant.html` | 打开商家接单、备餐、出餐页面 |
| 管理端 | `http://localhost:8180/admin.html` | 打开管理员维护页面 |
| 大屏端 | `http://localhost:8180/screen.html` | 打开叫号和待取餐大屏页面 |
| 后端 API | `http://localhost:8180/api/**` | 请求先进入 Ingress，再进入 Gateway |
| WebSocket | `ws://localhost:8180/ws/screen/{merchantId}` | 大屏实时叫号推送连接由 Gateway 转发 |

如果你希望直接使用 `http://localhost/`，需要把 `deploy/k3s/cluster.yaml` 中的端口从 `8180:80` 改成 `80:80`，并确认本机 80 端口没有被占用。

## 3. 总体架构

```text
Windows 主机
├── Docker Compose 基础组件
│   ├── MySQL      localhost:3106 -> 3306
│   ├── Redis      localhost:6179 -> 6379
│   ├── Nacos      localhost:8148 -> 8848, localhost:9148 -> 9848
│   ├── RocketMQ   localhost:9176 -> 9876, localhost:11911 -> 10911
│   └── Seata      localhost:8191 -> 8091
└── Docker Desktop / WSL2
    └── K3d 集群 smart-canteen
        ├── 本地镜像仓库 smart-canteen-registry.localhost:5000
        ├── Traefik Ingress Controller
        └── canteen 命名空间
            ├── Frontend Nginx Deployment
            ├── Gateway Service Deployment
            ├── User Service Deployment
            ├── Menu Service Deployment
            ├── Order Service Deployment
            └── Pickup Service Deployment
```

Ingress 路由关系：

```text
浏览器 / 客户端
    ↓
http://localhost:8180
    ↓
Traefik Ingress
    ├── /api/**  → gateway-service:9000
    ├── /ws/**   → gateway-service:9000
    └── /**      → frontend-nginx:80
```

K3s Pod 访问 Compose 基础组件时，不访问 `localhost`，而是使用 `host.k3d.internal` 访问 Windows 宿主机映射端口。相关配置位于 `deploy/k8s/02-configmap.yaml`。

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `NACOS_SERVER_ADDR` | `host.k3d.internal:8148` | Nacos 注册中心 |
| `MYSQL_HOST` / `MYSQL_PORT` | `host.k3d.internal` / `3106` | MySQL |
| `REDIS_HOST` / `REDIS_PORT` | `host.k3d.internal` / `6179` | Redis |
| `ROCKETMQ_NAME_SERVER` | `host.k3d.internal:9176` | RocketMQ NameServer |
| `ROCKETMQ_BROKER_A_ADDR` | `host.k3d.internal:11911` | RocketMQ Broker 地址覆盖 |
| `SEATA_SERVER_ADDR` | `host.k3d.internal:8191` | Seata Server |

## 4. 环境要求

| 软件 | 建议版本 | 验证命令 |
|---|---|---|
| Windows | Windows 10/11 64 位 | `winver` |
| Docker Desktop | 最新稳定版 | `docker version` |
| Docker Compose | Docker Desktop 内置 | `docker compose version` |
| kubectl | 与 K3s 兼容即可 | `kubectl version --client` |
| k3d | v5.x | `k3d version` |
| JDK | 17 | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Node.js | 18+ | `node -v` |
| npm | 与 Node.js 配套 | `npm -v` |

端口规划：

| 端口 | 所属对象 | 说明 |
|---:|---|---|
| 8180 | K3d LoadBalancer / Traefik Ingress | 系统统一 HTTP 入口 |
| 443 | K3d LoadBalancer / Traefik Ingress | 预留 HTTPS |
| 5000 | K3d 本地镜像仓库 | Docker push 和 K3s 拉取镜像 |
| 3106 | MySQL Compose 映射端口 | 业务数据库 |
| 6179 | Redis Compose 映射端口 | 缓存、限流、Token 黑名单 |
| 8148 | Nacos HTTP 端口 | 控制台、注册中心和配置中心 |
| 9148 | Nacos gRPC 端口 | Nacos 2.x 客户端连接需要 |
| 9176 | RocketMQ NameServer 端口 | RocketMQ 服务发现 |
| 11911 | RocketMQ Broker 端口 | 消息收发 |
| 8191 | Seata Server 端口 | 分布式事务服务端 |

部署前可以检查端口占用：

```powershell
netstat -ano | findstr ":8180"
netstat -ano | findstr ":5000"
netstat -ano | findstr ":3106"
netstat -ano | findstr ":8148"
```

没有输出表示对应端口当前未被占用。

## 5. 部署目录

```text
smart-canteen/
├── backend/
│   └── Dockerfile
├── frontend/
│   └── canteen-vue/
│       ├── Dockerfile
│       └── nginx.conf
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
│   │   └── ingress.yaml
│   ├── scripts/
│   │   ├── build-images.ps1
│   │   ├── push-images.ps1
│   │   ├── apply-all.ps1
│   │   └── check-all.ps1
│   └── sql/
└── docs/
```

## 6. 第一步：检查本地工具

在项目根目录执行：

```powershell
docker version
docker compose version
kubectl version --client
k3d version
java -version
mvn -version
node -v
npm -v
```

预期结果：

- Docker 命令正常输出 Client 和 Server 信息。
- `kubectl` 能输出客户端版本。
- `k3d version` 能输出 k3d 版本。
- JDK 为 17。
- Maven、Node.js、npm 均能输出版本号。

如果 `k3d` 不存在，可安装：

```powershell
winget install -e --id k3d.k3d
```

如果 Docker Server 无法连接，需要先打开 Docker Desktop，并确认 Docker Desktop 已启用 WSL2 backend。

实际结果：
![image-20260528233829862](E:\Projects\smart-canteen\docs\assets\image-20260528233829862.png)

![image-20260528233850342](E:\Projects\smart-canteen\docs\assets\image-20260528233850342.png)

## 7. 第二步：启动 Docker Compose 基础组件

在项目根目录执行：

```powershell
docker compose -p canteen -f deploy/docker-compose/compose.yaml up -d
docker compose -p canteen -f deploy/docker-compose/compose.yaml ps
```

预期结果：

- `canteen-mysql` 状态为 running 或 healthy。
- `canteen-redis` 状态为 running。
- `canteen-nacos` 状态为 running。
- `canteen-rocketmq-namesrv` 状态为 running。
- `canteen-rocketmq-broker` 状态为 running。
- `canteen-seata` 状态为 running。

![image-20260528234021235](E:\Projects\smart-canteen\docs\assets\image-20260528234021235.png)

MySQL 首次启动会自动执行以下 SQL：

1. `deploy/sql/canteen_user.sql`
2. `deploy/sql/canteen_menu.sql`
3. `deploy/sql/canteen_order.sql`
4. `deploy/sql/canteen_pickup.sql`
5. `deploy/sql/seata_undo_log.sql`
6. `deploy/sql/init_data.sql`

验证 MySQL：

```powershell
docker exec -it canteen-mysql mysql -uroot -proot123456 -e "SHOW DATABASES;"
```

预期至少看到：

```text
canteen_user
canteen_menu
canteen_order
canteen_pickup
```

![image-20260528234040984](E:\Projects\smart-canteen\docs\assets\image-20260528234040984.png)

验证 Redis：

```powershell
docker exec -it canteen-redis redis-cli -a root123456 ping
```

预期输出：

```text
PONG
```

![image-20260528234055655](E:\Projects\smart-canteen\docs\assets\image-20260528234055655.png)

验证 Nacos 控制台：

```text
http://localhost:8148/nacos
```

能打开 Nacos 登录页面即表示 Nacos HTTP 端口正常。默认账号密码通常为 `nacos` / `nacos`。

![image-20260528234121196](E:\Projects\smart-canteen\docs\assets\image-20260528234121196.png)

如果需要清空 MySQL 和 Redis 数据并重新初始化，执行：

```powershell
docker compose -p canteen -f deploy/docker-compose/compose.yaml down -v
docker compose -p canteen -f deploy/docker-compose/compose.yaml up -d
```

## 8. 第三步：创建 K3d/K3s 集群

集群配置文件为 `deploy/k3s/cluster.yaml`。当前关键配置如下：

```yaml
ports:
  - port: 8180:80
    nodeFilters:
      - loadbalancer
  - port: 443:443
    nodeFilters:
      - loadbalancer

registries:
  create:
    name: smart-canteen-registry.localhost
    host: 0.0.0.0
    hostPort: "5000"
```

说明：

- `8180:80` 表示 Windows 宿主机 `localhost:8180` 会转发到 K3d LoadBalancer 的 80 端口。
- `hostPort: "5000"` 会创建本地镜像仓库。宿主机推送镜像时使用 `localhost:5000/...`，K3s Pod 拉取镜像时使用 `smart-canteen-registry.localhost:5000/...`。两者对应同一个 registry，仓库路径保持一致即可。
- 配置文件默认不设置代理。如果你的网络环境必须使用代理拉取 K3s 镜像，可按本机代理地址自行给 `cluster.yaml` 增加 `HTTP_PROXY`、`HTTPS_PROXY`、`NO_PROXY`。

创建集群：

```powershell
k3d cluster create --config deploy/k3s/cluster.yaml
```

![image-20260528235716074](E:\Projects\smart-canteen\docs\assets\image-20260528235716074.png)

验证集群：

```powershell
kubectl cluster-info
kubectl get nodes
kubectl get pods -A
```

预期结果：

- `kubectl cluster-info` 能显示 Kubernetes control plane 地址。
- `kubectl get nodes` 至少看到 1 个 server 节点和 1 个 agent 节点，状态为 `Ready`。
- `kubectl get pods -A` 中 `kube-system` 命名空间里的 Traefik、CoreDNS 等 Pod 最终应为 `Running` 或 `Completed`。

![image-20260529000121669](E:\Projects\smart-canteen\docs\assets\image-20260529000121669.png)

![image-20260528235829423](E:\Projects\smart-canteen\docs\assets\image-20260528235829423.png)

验证本地镜像仓库：

```powershell
docker ps --filter "name=smart-canteen-registry"
```

预期看到一个 registry 容器正在运行，并暴露 `0.0.0.0:5000->5000/tcp`。

![image-20260529000144670](E:\Projects\smart-canteen\docs\assets\image-20260529000144670.png)

如果创建失败并提示端口被占用，先删除失败集群：

```powershell
k3d cluster delete smart-canteen
```

然后修改 `deploy/k3s/cluster.yaml` 中被占用的端口，例如把 `8180:80` 改为 `8280:80`，后续访问地址也同步改为 `http://localhost:8280/`。

## 9. 第四步：构建应用镜像

脚本已经基于 `$PSScriptRoot` 定位项目根目录，因此可以在项目根目录直接执行：

```powershell
.\deploy\scripts\build-images.ps1
```

脚本会构建以下镜像：

| 服务 | 镜像 |
|---|---|
| Gateway | `smart-canteen-registry.localhost:5000/gateway-service:1.0.0` |
| User Service | `smart-canteen-registry.localhost:5000/user-service:1.0.0` |
| Menu Service | `smart-canteen-registry.localhost:5000/menu-service:1.0.0` |
| Order Service | `smart-canteen-registry.localhost:5000/order-service:1.0.0` |
| Pickup Service | `smart-canteen-registry.localhost:5000/pickup-service:1.0.0` |
| Frontend Nginx | `smart-canteen-registry.localhost:5000/frontend-nginx:1.0.0` |

验证本地镜像是否构建成功：

```powershell
docker images "smart-canteen-registry.localhost:5000/*"
```

预期能看到上表中的 6 个镜像，tag 均为 `1.0.0`。

![image-20260529005741626](E:\Projects\smart-canteen\docs\assets\image-20260529005741626.png)

如果前端镜像构建失败，重点检查：

- `frontend/canteen-vue/package-lock.json` 是否存在。
- Docker Desktop 是否能访问 npm registry。

如果后端镜像构建失败，重点检查：

- JDK 和 Maven 版本是否满足要求。
- Maven 依赖下载是否被网络或代理阻断。
- 报错服务名是否和 `backend/pom.xml` 中 module 名一致。

## 10. 第五步：推送镜像到 K3d 本地仓库

执行：

```powershell
.\deploy\scripts\push-images.ps1
```

![image-20260529010312949](E:\Projects\smart-canteen\docs\assets\image-20260529010312949.png)

脚本会先把 `smart-canteen-registry.localhost:5000/...` 镜像临时打上 `localhost:5000/...` 标签，然后通过 `localhost:5000` 推送到本地 registry。这样可以避免 Docker 客户端把 `smart-canteen-registry.localhost:5000` 当成 HTTPS registry 访问。

验证仓库内镜像：

```powershell
(Invoke-RestMethod http://localhost:5000/v2/_catalog).repositories
```

![image-20260529010522365](E:\Projects\smart-canteen\docs\assets\image-20260529010522365.png)

预期返回的 `repositories` 中包含：

```text
gateway-service
user-service
menu-service
order-service
pickup-service
frontend-nginx
```

也可以查看某个镜像 tag：

```powershell
Invoke-RestMethod http://localhost:5000/v2/gateway-service/tags/list
```

预期返回 `1.0.0`。

## 11. 第六步：部署应用到 K3s

确保以下条件已经满足：

- Docker Compose 基础组件已启动。
- K3d 集群已创建。
- 6 个应用镜像已通过 `localhost:5000` 推送到本地 registry；K8s 部署清单继续使用 `smart-canteen-registry.localhost:5000/...` 拉取同一批镜像。

执行自动部署脚本：

```powershell
.\deploy\scripts\apply-all.ps1
```

![image-20260529010605232](E:\Projects\smart-canteen\docs\assets\image-20260529010605232.png)

脚本会依次应用：

1. `deploy/k8s/00-namespace.yaml`
2. `deploy/k8s/01-secret.yaml`
3. `deploy/k8s/02-configmap.yaml`
4. `deploy/k8s/apps/user-service.yaml`
5. `deploy/k8s/apps/menu-service.yaml`
6. `deploy/k8s/apps/pickup-service.yaml`
7. `deploy/k8s/apps/order-service.yaml`
8. `deploy/k8s/apps/gateway-service.yaml`
9. `deploy/k8s/apps/frontend.yaml`
10. `deploy/k8s/ingress.yaml`

手动部署时，在项目根目录执行：

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

部署后先看 Pod：

```powershell
kubectl get pods -n canteen -o wide
```

预期最终看到：

```text
frontend-nginx-...    1/1 Running
gateway-service-...   1/1 Running
user-service-...      1/1 Running
menu-service-...      1/1 Running
order-service-...     1/1 Running
pickup-service-...    1/1 Running
```

![image-20260529010720633](E:\Projects\smart-canteen\docs\assets\image-20260529010720633.png)

如果某个 Pod 是 `ImagePullBackOff`，通常是镜像没有推送成功或镜像名不一致。检查：

```powershell
kubectl describe pod -n canteen <pod-name>
Invoke-RestMethod http://localhost:5000/v2/_catalog
```

如果某个 Pod 是 `CrashLoopBackOff`，查看日志：

```powershell
kubectl logs -n canteen deployment/<deployment-name>
```

常见原因是 MySQL、Redis、Nacos、RocketMQ 或 Seata 未启动，或 `host.k3d.internal` 在当前环境无法解析。

## 12. 第七步：完整部署验证

执行快速检查脚本：

```powershell
.\deploy\scripts\check-all.ps1
```

脚本会检查：

- Docker Compose 基础组件状态。
- 6 个 Deployment 的 rollout 状态。
- `canteen` 命名空间下的 Pod、Service、Ingress。

也可以手动执行：

```powershell
docker compose -p canteen -f deploy/docker-compose/compose.yaml ps
kubectl rollout status deployment/user-service -n canteen --timeout=180s
kubectl rollout status deployment/menu-service -n canteen --timeout=180s
kubectl rollout status deployment/pickup-service -n canteen --timeout=180s
kubectl rollout status deployment/order-service -n canteen --timeout=180s
kubectl rollout status deployment/gateway-service -n canteen --timeout=180s
kubectl rollout status deployment/frontend-nginx -n canteen --timeout=180s
kubectl get pods -n canteen
kubectl get svc -n canteen
kubectl get ingress -n canteen
```

预期结果：

- Compose 容器均为 running。
- 所有 rollout status 都显示 `successfully rolled out`。
- `kubectl get pods -n canteen` 中所有 Pod 为 `Running`。
- `kubectl get svc -n canteen` 中有 `frontend-nginx`、`gateway-service`、`user-service`、`menu-service`、`order-service`、`pickup-service`。
- `kubectl get ingress -n canteen` 中有 `canteen-ingress`。

![image-20260529011055928](E:\Projects\smart-canteen\docs\assets\image-20260529011055928.png)

检查服务注册：

```text
http://localhost:8148/nacos
```

进入 Nacos 后，在服务列表中应能看到：

```text
gateway-service
user-service
menu-service
order-service
pickup-service
```

![image-20260529011118692](E:\Projects\smart-canteen\docs\assets\image-20260529011118692.png)

检查页面访问：

```text
http://localhost:8180/
http://localhost:8180/merchant.html
http://localhost:8180/admin.html
http://localhost:8180/screen.html
```

目标效果：

- 四个页面都能正常打开。
- 页面中的静态资源没有 404。
- 浏览器开发者工具 Network 中，前端请求 `/api/**` 时请求地址应为 `localhost:8180`，并由 Ingress 转发到 Gateway。

![image-20260529011141361](E:\Projects\smart-canteen\docs\assets\image-20260529011141361.png)

![image-20260529011226104](E:\Projects\smart-canteen\docs\assets\image-20260529011226104.png)

![image-20260529011254204](E:\Projects\smart-canteen\docs\assets\image-20260529011254204.png)

![image-20260529011310631](E:\Projects\smart-canteen\docs\assets\image-20260529011310631.png)

检查 Gateway 是否可达：

```powershell
Invoke-WebRequest http://localhost:8180/api/menu/merchants -UseBasicParsing
```

![image-20260529011340481](E:\Projects\smart-canteen\docs\assets\image-20260529011340481.png)

如果接口需要登录或业务参数，返回 401、403 或业务错误也说明请求已经到达后端；如果返回 404 或连接失败，则优先检查 Ingress 和 Gateway 日志。

查看关键日志：

```powershell
kubectl logs -n canteen deployment/gateway-service
kubectl logs -n canteen deployment/user-service
kubectl logs -n canteen deployment/menu-service
kubectl logs -n canteen deployment/order-service
kubectl logs -n canteen deployment/pickup-service
docker logs canteen-nacos
docker logs canteen-rocketmq-broker
docker logs canteen-seata
```

## 13. 代码变更后的更新部署

部署成功后，如果只修改了某个后端模块或前端代码，不需要重新创建 K3d 集群，也不需要重启 Docker Compose 基础组件。只需要重新构建对应镜像、推送到本地 registry，然后让 Kubernetes 滚动重启对应 Deployment。

### 13.1 更新单个后端服务

假设修改的是 `user-service`，在项目根目录执行：

```powershell
docker build -f backend/Dockerfile --build-arg SERVICE_NAME=user-service -t smart-canteen-registry.localhost:5000/user-service:1.0.0 backend
docker tag smart-canteen-registry.localhost:5000/user-service:1.0.0 localhost:5000/user-service:1.0.0
docker push localhost:5000/user-service:1.0.0
kubectl rollout restart deployment/user-service -n canteen
kubectl rollout status deployment/user-service -n canteen --timeout=180s
```

预期结果：

- `docker build` 成功生成新镜像。
- `docker push` 成功把新镜像推送到本地 registry。
- `kubectl rollout status` 输出 `deployment "user-service" successfully rolled out`。
- `kubectl get pods -n canteen` 中旧 Pod 被替换为新 Pod，最终状态为 `Running`。

其他后端服务只需要替换服务名：

| 修改的模块 | 替换的服务名 |
|---|---|
| Gateway | `gateway-service` |
| 用户服务 | `user-service` |
| 菜单服务 | `menu-service` |
| 订单服务 | `order-service` |
| 取餐服务 | `pickup-service` |

例如修改 `order-service` 后执行：

```powershell
docker build -f backend/Dockerfile --build-arg SERVICE_NAME=order-service -t smart-canteen-registry.localhost:5000/order-service:1.0.0 backend
docker tag smart-canteen-registry.localhost:5000/order-service:1.0.0 localhost:5000/order-service:1.0.0
docker push localhost:5000/order-service:1.0.0
kubectl rollout restart deployment/order-service -n canteen
kubectl rollout status deployment/order-service -n canteen --timeout=180s
```

如果修改的是 `smart-canteen-common` 公共模块，需要重新构建所有依赖公共模块的后端服务。可以直接执行：

```powershell
.\deploy\scripts\build-images.ps1
.\deploy\scripts\push-images.ps1
kubectl rollout restart deployment/gateway-service -n canteen
kubectl rollout restart deployment/user-service -n canteen
kubectl rollout restart deployment/menu-service -n canteen
kubectl rollout restart deployment/order-service -n canteen
kubectl rollout restart deployment/pickup-service -n canteen
```

然后检查滚动更新：

```powershell
kubectl rollout status deployment/gateway-service -n canteen --timeout=180s
kubectl rollout status deployment/user-service -n canteen --timeout=180s
kubectl rollout status deployment/menu-service -n canteen --timeout=180s
kubectl rollout status deployment/order-service -n canteen --timeout=180s
kubectl rollout status deployment/pickup-service -n canteen --timeout=180s
```

### 13.2 更新前端

如果修改了 `frontend/canteen-vue` 下的页面、组件、样式或接口调用代码，在项目根目录执行：

```powershell
docker build -f frontend/canteen-vue/Dockerfile -t smart-canteen-registry.localhost:5000/frontend-nginx:1.0.0 frontend/canteen-vue
docker tag smart-canteen-registry.localhost:5000/frontend-nginx:1.0.0 localhost:5000/frontend-nginx:1.0.0
docker push localhost:5000/frontend-nginx:1.0.0
kubectl rollout restart deployment/frontend-nginx -n canteen
kubectl rollout status deployment/frontend-nginx -n canteen --timeout=180s
```

预期结果：

- 前端镜像重新构建成功。
- `frontend-nginx` Deployment 滚动重启完成。
- 重新访问 `http://localhost:8180/`、`http://localhost:8180/merchant.html`、`http://localhost:8180/admin.html` 或 `http://localhost:8180/screen.html` 能看到最新页面。

如果浏览器仍显示旧页面，使用 `Ctrl + F5` 强制刷新，或清理浏览器缓存后再访问。

### 13.3 为什么需要 rollout restart

当前部署使用固定镜像 tag `1.0.0`。即使镜像内容已经重新推送，Kubernetes 看到 Deployment 中的镜像名没有变化，也不会自动重建 Pod。因此每次使用同一个 tag 覆盖推送镜像后，都需要执行：

```powershell
kubectl rollout restart deployment/<deployment-name> -n canteen
```

如果后续希望避免手动重启，也可以改成每次构建使用新的 tag，例如 `1.0.1`、`20260529-001`，然后修改对应 K8s YAML 或执行 `kubectl set image`。

## 14. 常见问题处理

| 问题 | 可能原因 | 处理方式 |
|---|---|---|
| 访问 `http://localhost:8180/` 失败 | K3d 集群未创建、Traefik 未就绪、端口被占用 | `kubectl get pods -A`，确认 Traefik Running；检查 `deploy/k3s/cluster.yaml` 端口 |
| Pod 为 `ImagePullBackOff` | 镜像未推送到 `smart-canteen-registry.localhost:5000` 或镜像名不一致 | 重新执行 `build-images.ps1` 和 `push-images.ps1`，检查 K8s YAML 中 image |
| Pod 无法连接 MySQL / Redis / Nacos | 基础组件未启动，或 `host.k3d.internal` 不可解析 | 检查 Compose 状态；必要时把 `deploy/k8s/02-configmap.yaml` 中 host 改为宿主机局域网 IP |
| `unknown database` | MySQL volume 已存在，初始化 SQL 未重新执行 | `docker compose -p canteen -f deploy/docker-compose/compose.yaml down -v` 后重新启动 |
| 页面中的用户昵称、菜品名称乱码 | MySQL volume 中已有错误编码写入的初始化数据 | 确认 `deploy/sql/*.sql` 已包含 `SET NAMES utf8mb4;`，然后执行 `docker compose -p canteen -f deploy/docker-compose/compose.yaml down -v` 重新初始化 |
| Nacos 无服务实例 | 后端 Pod 未正常启动，或 Nacos 地址错误 | 查看后端日志，确认 `NACOS_SERVER_ADDR=host.k3d.internal:8148` 已注入 |
| RocketMQ 发送失败 | Broker 回传地址不可达 | 确认 `ROCKETMQ_BROKER_A_ADDR=host.k3d.internal:11911` 已注入 Order Service |
| Seata 连接失败 | Seata 容器未启动或地址错误 | 检查 `docker logs canteen-seata` 和 `SEATA_SERVER_ADDR` |
| Ingress 返回 404 | Ingress 未应用或路径未命中 | `kubectl get ingress -n canteen`，再检查 `deploy/k8s/ingress.yaml` |
| 前端页面打开但 API 失败 | Gateway 未就绪或后端服务未注册 | 检查 Gateway 日志、Nacos 服务列表和业务服务日志 |

## 15. 清理环境

只删除 K3s 应用资源：

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

停止基础组件但保留数据：

```powershell
docker compose -p canteen -f deploy/docker-compose/compose.yaml down
```

停止基础组件并清理 MySQL、Redis volume：

```powershell
docker compose -p canteen -f deploy/docker-compose/compose.yaml down -v
```

删除 K3d 集群和本地 registry：

```powershell
k3d cluster delete smart-canteen
```

清理本地应用镜像：

```powershell
docker rmi smart-canteen-registry.localhost:5000/gateway-service:1.0.0
docker rmi smart-canteen-registry.localhost:5000/user-service:1.0.0
docker rmi smart-canteen-registry.localhost:5000/menu-service:1.0.0
docker rmi smart-canteen-registry.localhost:5000/order-service:1.0.0
docker rmi smart-canteen-registry.localhost:5000/pickup-service:1.0.0
docker rmi smart-canteen-registry.localhost:5000/frontend-nginx:1.0.0
```

## 16. 总结

本方案将基础组件保留在 Docker Compose 中运行，将前端和后端应用部署到 K3d/K3s。部署完成后，浏览器通过 `http://localhost:8180/` 进入系统，Ingress 将静态页面转发到前端 Nginx，将 `/api/**` 和 `/ws/**` 转发到 Gateway，Gateway 再通过 Nacos 服务发现调用各业务微服务。
