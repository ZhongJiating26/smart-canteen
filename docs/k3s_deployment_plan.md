# 智能食堂点餐与取餐微服务系统 K3S 部署方案说明书

| 文档名称 | K3S 部署方案说明书 |
|---|---|
| 项目名称 | 智能食堂点餐与取餐微服务系统 |
| 部署环境 | Windows + Docker Desktop + K3d/K3s |
| 部署方式 | 前端、后端微服务、中间件统一容器化部署到 K3d 本地集群 |
| 适用阶段 | 课程作业部署、验收演示、本地集成测试 |
| 编写目的 | 说明系统在 Windows 环境下基于 K3d/K3s 的部署环境、配置步骤、Kubernetes 资源清单、端口规划、服务验证方法和常见问题处理 |

## 1. 文档目的

本文档用于说明“智能食堂点餐与取餐微服务系统”在 Windows 本地环境中的 K3S 部署方案。系统开发调试阶段已经通过 Docker Compose 单机运行 MySQL、Redis、RocketMQ、Seata、Nacos 等中间件，并通过 IntelliJ IDEA 本地启动后端微服务完成联调。部署阶段将前端应用、后端微服务和基础中间件统一迁移到 K3d/K3s 本地集群中运行。

本方案重点覆盖以下内容：

1. Windows 运行环境准备。
2. Docker Desktop、kubectl、k3d 等工具安装与验证。
3. K3d/K3s 本地集群创建方式。
4. Kubernetes Namespace、Secret、ConfigMap、Service、Deployment、StatefulSet、PVC、Ingress 等资源清单。
5. MySQL、Redis、Nacos、RocketMQ、Seata 等基础组件部署。
6. Gateway、User Service、Menu Service、Order Service、Pickup Service、Frontend Nginx 等业务组件部署。
7. 启动顺序、部署验证、日志查看和常见错误处理。

## 2. 部署目标

本系统部署后的整体目标如下：

- 用户手机端、商家平板端、管理员 Web 端和电视大屏端通过统一入口访问系统。
- 前端静态资源由 Nginx 容器托管。
- 外部 HTTP 请求统一进入 K3d/K3s 集群中的 Traefik Ingress。
- `/` 路径转发到前端 Nginx 服务。
- `/api/*` 路径转发到 Spring Cloud Gateway。
- `/ws/*` 路径转发到 Spring Cloud Gateway，并由 Gateway 代理到取餐排队服务。
- 后端服务通过 Nacos 完成注册与发现。
- 各微服务通过 Kubernetes Service 和 Nacos 进行服务调用。
- MySQL、Redis、Nacos、RocketMQ、Seata 等基础组件在 `infra` 命名空间中运行。
- 业务服务和前端服务在 `canteen` 命名空间中运行。

最终访问地址规划如下：

| 访问对象 | 访问地址 | 说明 |
|---|---|---|
| 用户端 | `http://localhost/` | 用户在线点餐页面，多入口前端默认入口 |
| 商家端 | `http://localhost/merchant.html` | 商家接单、备餐、出餐页面 |
| 管理端 | `http://localhost/admin.html` | 管理员维护菜品、菜单、用户等信息 |
| 大屏端 | `http://localhost/screen.html` | 食堂电视大屏展示叫号和待取餐队列 |
| 后端 API | `http://localhost/api/**` | 统一经过 Gateway 转发 |
| WebSocket | `ws://localhost/ws/screen/{merchantId}` | 大屏实时叫号推送，例如 `/ws/screen/2000001` |

## 3. 总体部署架构

系统采用本地 K3d/K3s 集群部署，逻辑结构如下：

```text
Windows 主机
└── Docker Desktop / WSL2
    └── K3d 集群 smart-canteen
        ├── Traefik Ingress Controller
        ├── infra 命名空间
        │   ├── MySQL StatefulSet + PVC
        │   ├── Redis StatefulSet + PVC
        │   ├── Nacos StatefulSet + PVC
        │   ├── RocketMQ NameServer StatefulSet
        │   ├── RocketMQ Broker StatefulSet + PVC
        │   └── Seata Server StatefulSet + PVC
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

## 4. 运行环境要求

### 4.1 硬件环境

| 项目 | 建议配置 |
|---|---|
| CPU | 4 核及以上 |
| 内存 | 8 GB 及以上，推荐 16 GB |
| 磁盘 | 至少 20 GB 可用空间 |
| 网络 | 本机可访问 Docker Desktop，80 和 5000 端口未被占用 |

### 4.2 软件环境

| 软件 | 建议版本 | 用途 |
|---|---|---|
| Windows | Windows 10/11 64 位 | 宿主机操作系统 |
| WSL2 | 已启用 | Docker Desktop Linux 容器运行基础 |
| Docker Desktop | 最新稳定版 | 容器运行环境 |
| kubectl | 与 K3s 兼容即可 | 操作 Kubernetes 集群 |
| k3d | v5.x | 在 Docker 中创建 K3s 集群 |
| JDK | 17 | Spring Boot 3.x 微服务构建 |
| Maven | 3.8+ | 后端服务打包 |
| Node.js | 18+ | 前端 Vue3 项目构建 |
| npm | 与前端项目匹配 | 前端依赖安装和构建 |

### 4.3 端口规划

| 端口 | 所属对象 | 访问范围 | 说明 |
|---:|---|---|---|
| 80 | K3d LoadBalancer / Traefik Ingress | Windows 主机访问 | 系统统一入口 |
| 443 | K3d LoadBalancer / Traefik Ingress | Windows 主机访问 | 预留 HTTPS |
| 5000 | 本地镜像仓库 | Windows 主机和 K3d 集群访问 | 存放本项目镜像 |
| 3306 | MySQL Service | 集群内部访问 | 业务数据库 |
| 6379 | Redis Service | 集群内部访问 | 缓存、限流、Token 黑名单 |
| 8848 | Nacos Service | 集群内部访问，可临时 port-forward | 注册中心和配置中心 |
| 9848 | Nacos gRPC | 集群内部访问 | Nacos 2.x 客户端通信 |
| 9849 | Nacos gRPC | 集群内部访问 | Nacos 2.x 集群通信预留 |
| 9876 | RocketMQ NameServer | 集群内部访问 | RocketMQ 服务发现 |
| 10911 | RocketMQ Broker | 集群内部访问 | 消息收发 |
| 10909 | RocketMQ Broker VIP | 集群内部访问 | Broker VIP 通道 |
| 8091 | Seata Server | 集群内部访问 | 分布式事务服务端 |
| 9000 | Gateway Service | 集群内部访问，经 Ingress 暴露 | API 网关 |
| 9001 | User Service | 集群内部访问 | 用户服务 |
| 9002 | Menu Service | 集群内部访问 | 菜品菜单服务 |
| 9003 | Order Service | 集群内部访问 | 订单服务 |
| 9004 | Pickup Service | 集群内部访问 | 取餐排队服务 |

## 5. 部署目录规划

建议在项目根目录中增加 `deploy` 目录，用于统一保存 K3d 配置、Kubernetes 清单和部署脚本。

```text
smart-canteen/
├── backend/
│   ├── gateway-service/
│   ├── user-service/
│   ├── menu-service/
│   ├── order-service/
│   ├── pickup-service/
│   └── smart-canteen-common/
├── frontend/
│   └── canteen-vue/
│       ├── index.html
│       ├── merchant.html
│       ├── admin.html
│       ├── screen.html
│       ├── nginx.conf
│       └── Dockerfile
└── deploy/
    ├── k3s/
    │   └── cluster.yaml
    ├── k8s/
    │   ├── 00-namespace.yaml
    │   ├── 01-secret.yaml
    │   ├── 02-configmap.yaml
    │   ├── infra/
    │   │   ├── mysql.yaml
    │   │   ├── redis.yaml
    │   │   ├── nacos.yaml
    │   │   ├── rocketmq.yaml
    │   │   └── seata.yaml
    │   ├── apps/
    │   │   ├── user-service.yaml
    │   │   ├── menu-service.yaml
    │   │   ├── pickup-service.yaml
    │   │   ├── order-service.yaml
    │   │   ├── gateway-service.yaml
    │   │   └── frontend.yaml
    │   └── ingress.yaml
    └── scripts/
        ├── build-images.ps1
        ├── push-images.ps1
        ├── apply-all.ps1
        └── check-all.ps1
```

## 6. Windows 环境准备

### 6.1 安装 Docker Desktop

使用 PowerShell 执行：

```powershell
winget install -e --id Docker.DockerDesktop
```

安装完成后打开 Docker Desktop，进入：

```text
Settings → General → Use the WSL 2 based engine
```

勾选后点击 Apply & Restart。验证 Docker 是否可用：

```powershell
docker version
docker ps
```

### 6.2 安装 kubectl

```powershell
winget install -e --id Kubernetes.kubectl
kubectl version --client
```

### 6.3 安装 k3d

```powershell
winget install -e --id k3d.k3d
k3d version
```

若 winget 无法安装，可使用 Chocolatey：

```powershell
choco install k3d
```

### 6.4 安装 Maven、JDK 和 Node.js

```powershell
winget install -e --id EclipseAdoptium.Temurin.17.JDK
winget install -e --id Apache.Maven
winget install -e --id OpenJS.NodeJS.LTS
```

验证：

```powershell
java -version
mvn -version
node -v
npm -v
```

## 7. 停止开发阶段 Docker Compose 环境

部署到 K3d/K3s 前，应停止原 Docker Compose 中运行的中间件，避免端口占用和数据混乱。

进入原 `docker-compose.yml` 所在目录：

```powershell
docker compose down
```

如果需要彻底清理测试数据：

```powershell
docker compose down -v
```

检查关键端口是否被占用：

```powershell
netstat -ano | findstr ":80"
netstat -ano | findstr ":3306"
netstat -ano | findstr ":6379"
netstat -ano | findstr ":8848"
netstat -ano | findstr ":9876"
```

如果 80 端口被 IIS、Nginx 或其他进程占用，需要关闭相关进程，或将 K3d 入口改为 8080。

## 8. 创建 K3d/K3s 集群

### 8.1 K3d 集群配置文件

新建文件：`deploy/k3s/cluster.yaml`。

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

### 8.2 创建集群

```powershell
k3d cluster create --config deploy/k3s/cluster.yaml
```

验证：

```powershell
kubectl cluster-info
kubectl get nodes
kubectl get pods -A
```

正常情况下，应能看到 `coredns`、`local-path-provisioner`、`metrics-server`、`traefik` 等系统组件处于 Running 状态。

若 80 端口被占用，可删除集群后将 `cluster.yaml` 中端口改成：

```yaml
ports:
  - port: 8080:80
    nodeFilters:
      - loadbalancer
```

此时访问地址改为：

```text
http://localhost:8080/
```

## 9. 命名空间配置

新建文件：`deploy/k8s/00-namespace.yaml`。

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: infra
---
apiVersion: v1
kind: Namespace
metadata:
  name: canteen
```

执行：

```powershell
kubectl apply -f deploy/k8s/00-namespace.yaml
kubectl get ns
```

## 10. Secret 配置

基础设施和业务服务位于不同命名空间，因此 Secret 需要分别创建在 `infra` 和 `canteen` 命名空间中。Kubernetes Secret 不能跨命名空间直接引用。

新建文件：`deploy/k8s/01-secret.yaml`。

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: canteen-secret
  namespace: infra
type: Opaque
stringData:
  MYSQL_ROOT_PASSWORD: root123456
  REDIS_PASSWORD: root123456
---
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
  JWT_SECRET: replace-with-a-shared-jwt-secret-at-least-32-bytes-long
```

执行：

```powershell
kubectl apply -f deploy/k8s/01-secret.yaml
kubectl get secret -n infra
kubectl get secret -n canteen
```

## 11. ConfigMap 配置

新建文件：`deploy/k8s/02-configmap.yaml`。

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: canteen-common-config
  namespace: canteen
data:
  NACOS_SERVER_ADDR: nacos.infra.svc.cluster.local:8848
  MYSQL_HOST: mysql.infra.svc.cluster.local
  MYSQL_PORT: "3306"
  REDIS_HOST: redis.infra.svc.cluster.local
  REDIS_PORT: "6379"
  ROCKETMQ_NAME_SERVER: rocketmq-namesrv.infra.svc.cluster.local:9876
  SEATA_SERVER_ADDR: seata.infra.svc.cluster.local:8091
```

执行：

```powershell
kubectl apply -f deploy/k8s/02-configmap.yaml
kubectl get configmap -n canteen
```

业务数据库表、Seata AT 模式所需的 `undo_log` 表以及演示数据已经保存在仓库的 `deploy/sql` 目录中。MySQL 初始化不能只创建数据库，必须挂载这些实际 SQL 文件，并按以下顺序创建初始化 ConfigMap：

```powershell
kubectl create configmap mysql-init-sql -n infra `
  --from-file=01-canteen-user.sql=deploy/sql/canteen_user.sql `
  --from-file=02-canteen-menu.sql=deploy/sql/canteen_menu.sql `
  --from-file=03-canteen-order.sql=deploy/sql/canteen_order.sql `
  --from-file=04-canteen-pickup.sql=deploy/sql/canteen_pickup.sql `
  --from-file=05-seata-undo-log.sql=deploy/sql/seata_undo_log.sql `
  --from-file=06-init-data.sql=deploy/sql/init_data.sql `
  --dry-run=client -o yaml | kubectl apply -f -

kubectl get configmap mysql-init-sql -n infra
```

说明：

- 当前 Nacos 使用 standalone 内嵌存储，不依赖 `nacos_config` 数据库。
- 当前 Seata Server 使用 file 存储，不依赖 `seata` 数据库；业务库中的 `undo_log` 表仍是订单与库存 AT 事务必须具备的表。
- MySQL 官方镜像只会在数据目录首次初始化时执行 SQL。若此前已经创建过 MySQL PVC，需要删除测试 PVC 后重新初始化。

## 12. MySQL 部署清单

MySQL 使用 StatefulSet + PVC 部署。客户端访问使用 `mysql.infra.svc.cluster.local:3306`。

新建文件：`deploy/k8s/infra/mysql.yaml`。

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql-headless
  namespace: infra
spec:
  clusterIP: None
  selector:
    app: mysql
  ports:
    - name: mysql
      port: 3306
      targetPort: 3306
---
apiVersion: v1
kind: Service
metadata:
  name: mysql
  namespace: infra
spec:
  type: ClusterIP
  selector:
    app: mysql
  ports:
    - name: mysql
      port: 3306
      targetPort: 3306
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: infra
spec:
  serviceName: mysql-headless
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
        - name: mysql
          image: mysql:8.0.44
          imagePullPolicy: IfNotPresent
          ports:
            - name: mysql
              containerPort: 3306
          env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: canteen-secret
                  key: MYSQL_ROOT_PASSWORD
            - name: TZ
              value: Asia/Shanghai
          args:
            - --character-set-server=utf8mb4
            - --collation-server=utf8mb4_unicode_ci
            - --default-time-zone=+08:00
          volumeMounts:
            - name: mysql-data
              mountPath: /var/lib/mysql
            - name: mysql-init
              mountPath: /docker-entrypoint-initdb.d
          readinessProbe:
            exec:
              command:
                - sh
                - -c
                - mysqladmin ping -uroot -p$MYSQL_ROOT_PASSWORD
            initialDelaySeconds: 30
            periodSeconds: 10
            timeoutSeconds: 5
      volumes:
        - name: mysql-init
          configMap:
            name: mysql-init-sql
  volumeClaimTemplates:
    - metadata:
        name: mysql-data
      spec:
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 5Gi
```

部署和验证：

```powershell
kubectl apply -f deploy/k8s/infra/mysql.yaml
kubectl get pod -n infra
kubectl get pvc -n infra
kubectl logs -n infra mysql-0
kubectl exec -it -n infra mysql-0 -- mysql -uroot -proot123456 -e "SHOW DATABASES;"
```

## 13. Redis 部署清单

Redis 使用 StatefulSet + PVC 部署，开启 AOF 持久化和密码认证。

新建文件：`deploy/k8s/infra/redis.yaml`。

```yaml
apiVersion: v1
kind: Service
metadata:
  name: redis-headless
  namespace: infra
spec:
  clusterIP: None
  selector:
    app: redis
  ports:
    - name: redis
      port: 6379
      targetPort: 6379
---
apiVersion: v1
kind: Service
metadata:
  name: redis
  namespace: infra
spec:
  type: ClusterIP
  selector:
    app: redis
  ports:
    - name: redis
      port: 6379
      targetPort: 6379
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: redis
  namespace: infra
spec:
  serviceName: redis-headless
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
        - name: redis
          image: redis:7.2
          imagePullPolicy: IfNotPresent
          ports:
            - name: redis
              containerPort: 6379
          command:
            - sh
            - -c
            - redis-server --appendonly yes --requirepass "$REDIS_PASSWORD"
          env:
            - name: REDIS_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: canteen-secret
                  key: REDIS_PASSWORD
          volumeMounts:
            - name: redis-data
              mountPath: /data
          readinessProbe:
            exec:
              command:
                - sh
                - -c
                - redis-cli -a "$REDIS_PASSWORD" ping | grep PONG
            initialDelaySeconds: 10
            periodSeconds: 10
            timeoutSeconds: 5
  volumeClaimTemplates:
    - metadata:
        name: redis-data
      spec:
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 1Gi
```

部署和验证：

```powershell
kubectl apply -f deploy/k8s/infra/redis.yaml
kubectl exec -it -n infra redis-0 -- redis-cli -a root123456 ping
```

预期输出：

```text
PONG
```

## 14. Nacos 部署清单

Nacos 用于服务注册、服务发现和配置管理。课程本地部署阶段采用 standalone 模式。Nacos 2.x 需要开放 8848、9848、9849 端口。

新建文件：`deploy/k8s/infra/nacos.yaml`。

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nacos-headless
  namespace: infra
spec:
  clusterIP: None
  selector:
    app: nacos
  ports:
    - name: http
      port: 8848
      targetPort: 8848
    - name: grpc-client
      port: 9848
      targetPort: 9848
    - name: grpc-raft
      port: 9849
      targetPort: 9849
---
apiVersion: v1
kind: Service
metadata:
  name: nacos
  namespace: infra
spec:
  type: ClusterIP
  selector:
    app: nacos
  ports:
    - name: http
      port: 8848
      targetPort: 8848
    - name: grpc-client
      port: 9848
      targetPort: 9848
    - name: grpc-raft
      port: 9849
      targetPort: 9849
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: nacos
  namespace: infra
spec:
  serviceName: nacos-headless
  replicas: 1
  selector:
    matchLabels:
      app: nacos
  template:
    metadata:
      labels:
        app: nacos
    spec:
      containers:
        - name: nacos
          image: nacos/nacos-server:v2.3.2
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 8848
            - name: grpc-client
              containerPort: 9848
            - name: grpc-raft
              containerPort: 9849
          env:
            - name: MODE
              value: standalone
            - name: PREFER_HOST_MODE
              value: hostname
            - name: NACOS_AUTH_ENABLE
              value: "false"
            - name: JVM_XMS
              value: 256m
            - name: JVM_XMX
              value: 512m
            - name: JVM_XMN
              value: 128m
            - name: TZ
              value: Asia/Shanghai
          volumeMounts:
            - name: nacos-data
              mountPath: /home/nacos/data
            - name: nacos-logs
              mountPath: /home/nacos/logs
          readinessProbe:
            httpGet:
              path: /nacos/v1/console/health/readiness
              port: 8848
            initialDelaySeconds: 60
            periodSeconds: 10
            timeoutSeconds: 5
  volumeClaimTemplates:
    - metadata:
        name: nacos-data
      spec:
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 1Gi
    - metadata:
        name: nacos-logs
      spec:
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 1Gi
```

部署和验证：

```powershell
kubectl apply -f deploy/k8s/infra/nacos.yaml
kubectl get pod -n infra
kubectl logs -n infra nacos-0
kubectl port-forward -n infra svc/nacos 8848:8848
```

浏览器访问：

```text
http://localhost:8848/nacos
```

## 15. RocketMQ 部署清单

RocketMQ 采用单 NameServer + 单 Broker 模式，满足课程作业中订单超时取消和延迟消息演示需求。

新建文件：`deploy/k8s/infra/rocketmq.yaml`。

```yaml
apiVersion: v1
kind: Service
metadata:
  name: rocketmq-namesrv
  namespace: infra
spec:
  type: ClusterIP
  selector:
    app: rocketmq-namesrv
  ports:
    - name: namesrv
      port: 9876
      targetPort: 9876
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: rocketmq-namesrv
  namespace: infra
spec:
  serviceName: rocketmq-namesrv
  replicas: 1
  selector:
    matchLabels:
      app: rocketmq-namesrv
  template:
    metadata:
      labels:
        app: rocketmq-namesrv
    spec:
      containers:
        - name: namesrv
          image: apache/rocketmq:5.2.0
          imagePullPolicy: IfNotPresent
          command:
            - sh
            - -c
            - mqnamesrv
          ports:
            - name: namesrv
              containerPort: 9876
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: rocketmq-broker-config
  namespace: infra
data:
  broker.conf: |
    brokerClusterName = DefaultCluster
    brokerName = broker-a
    brokerId = 0
    namesrvAddr = rocketmq-namesrv.infra.svc.cluster.local:9876
    brokerIP1 = rocketmq-broker.infra.svc.cluster.local
    listenPort = 10911
    autoCreateTopicEnable = true
    autoCreateSubscriptionGroup = true
    storePathRootDir = /home/rocketmq/store
    storePathCommitLog = /home/rocketmq/store/commitlog
    brokerRole = ASYNC_MASTER
    flushDiskType = ASYNC_FLUSH
---
apiVersion: v1
kind: Service
metadata:
  name: rocketmq-broker
  namespace: infra
spec:
  type: ClusterIP
  selector:
    app: rocketmq-broker
  ports:
    - name: broker
      port: 10911
      targetPort: 10911
    - name: vip
      port: 10909
      targetPort: 10909
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: rocketmq-broker
  namespace: infra
spec:
  serviceName: rocketmq-broker
  replicas: 1
  selector:
    matchLabels:
      app: rocketmq-broker
  template:
    metadata:
      labels:
        app: rocketmq-broker
    spec:
      containers:
        - name: broker
          image: apache/rocketmq:5.2.0
          imagePullPolicy: IfNotPresent
          command:
            - sh
            - -c
            - mqbroker -c /home/rocketmq/conf/broker.conf
          ports:
            - name: broker
              containerPort: 10911
            - name: vip
              containerPort: 10909
          volumeMounts:
            - name: broker-config
              mountPath: /home/rocketmq/conf
            - name: broker-store
              mountPath: /home/rocketmq/store
      volumes:
        - name: broker-config
          configMap:
            name: rocketmq-broker-config
  volumeClaimTemplates:
    - metadata:
        name: broker-store
      spec:
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 3Gi
```

部署和验证：

```powershell
kubectl apply -f deploy/k8s/infra/rocketmq.yaml
kubectl get pod -n infra
kubectl logs -n infra rocketmq-namesrv-0
kubectl logs -n infra rocketmq-broker-0
```

## 16. Seata 部署清单

Seata 用于订单创建和库存预占场景中的分布式事务协调。当前 `order-service` 的下单流程已使用 `@GlobalTransactional`，且 `order-service`、`menu-service` 默认启用 Seata。因此演示完整下单链路时，本组件和业务库中的 `undo_log` 表均为必需项。

新建文件：`deploy/k8s/infra/seata.yaml`。

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: seata-config
  namespace: infra
data:
  registry.conf: |
    registry {
      type = "file"
    }
    config {
      type = "file"
      file {
        name = "file.conf"
      }
    }
  file.conf: |
    store {
      mode = "file"
    }
---
apiVersion: v1
kind: Service
metadata:
  name: seata-headless
  namespace: infra
spec:
  clusterIP: None
  selector:
    app: seata
  ports:
    - name: seata
      port: 8091
      targetPort: 8091
---
apiVersion: v1
kind: Service
metadata:
  name: seata
  namespace: infra
spec:
  type: ClusterIP
  selector:
    app: seata
  ports:
    - name: seata
      port: 8091
      targetPort: 8091
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: seata
  namespace: infra
spec:
  serviceName: seata-headless
  replicas: 1
  selector:
    matchLabels:
      app: seata
  template:
    metadata:
      labels:
        app: seata
    spec:
      containers:
        - name: seata
          image: seataio/seata-server:1.7.1
          imagePullPolicy: IfNotPresent
          ports:
            - name: seata
              containerPort: 8091
          env:
            - name: SEATA_IP
              value: seata.infra.svc.cluster.local
            - name: SEATA_PORT
              value: "8091"
            - name: STORE_MODE
              value: file
            - name: SEATA_CONFIG_NAME
              value: file:/root/seata-config/registry
            - name: TZ
              value: Asia/Shanghai
          volumeMounts:
            - name: seata-config
              mountPath: /root/seata-config
            - name: seata-data
              mountPath: /root/seata
      volumes:
        - name: seata-config
          configMap:
            name: seata-config
  volumeClaimTemplates:
    - metadata:
        name: seata-data
      spec:
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 1Gi
```

部署和验证：

```powershell
kubectl apply -f deploy/k8s/infra/seata.yaml
kubectl get pod -n infra
kubectl logs -n infra seata-0
```

## 17. 后端运行配置

当前代码中的 `application.yml` 和 `bootstrap.yml` 已通过环境变量读取基础组件地址，无需为 K3s 另外新增一套 `application-k8s.yml`。业务 Deployment 统一引用第 11 节的 `canteen-common-config` 和 `canteen-app-secret` 即可。

### 17.1 已有环境变量契约

| 环境变量 | 使用服务 | K3s 值 |
|---|---|---|
| `NACOS_SERVER_ADDR` | 全部后端服务 | `nacos.infra.svc.cluster.local:8848` |
| `MYSQL_HOST`、`MYSQL_PORT` | User、Menu、Order、Pickup | `mysql.infra.svc.cluster.local`、`3306` |
| `MYSQL_USERNAME`、`MYSQL_PASSWORD` | User、Menu、Order、Pickup | 从 Secret 注入 |
| `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` | Gateway 和业务服务 | Redis Service 与 Secret |
| `JWT_SECRET` | Gateway、User | 从同一个 Secret 注入，保证签发和校验一致 |
| `ROCKETMQ_NAME_SERVER` | Order | `rocketmq-namesrv.infra.svc.cluster.local:9876` |
| `SEATA_SERVER_ADDR` | Menu、Order | `seata.infra.svc.cluster.local:8091` |

### 17.2 各服务数据库名称

| 服务 | Spring 应用名 | 端口 | 数据库 |
|---|---|---:|---|
| user-service | `user-service` | 9001 | `canteen_user` |
| menu-service | `menu-service` | 9002 | `canteen_menu` |
| order-service | `order-service` | 9003 | `canteen_order` |
| pickup-service | `pickup-service` | 9004 | `canteen_pickup` |

### 17.3 Gateway 路由契约

当前各 Controller 自身已经包含 `/api` 前缀，因此 Gateway 必须原样转发路径，不能添加 `StripPrefix`。`backend/gateway-service/src/main/resources/application.yml` 已包含实际路由，部署时保持该实现：

```yaml
server:
  port: 9000

spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**,/api/admin/users/**
        - id: menu-service
          uri: lb://menu-service
          predicates:
            - Path=/api/menu/**,/api/admin/merchants/**,/api/admin/menu/config/**
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/order/**,/api/admin/order/config/**
        - id: pickup-service
          uri: lb://pickup-service
          predicates:
            - Path=/api/pickup/**,/api/admin/pickup/config/**,/ws/**
```

WebSocket 由 Gateway 原样代理至 Pickup Service，真实处理路径为 `/ws/screen/{merchantId}`。

## 18. 后端 Dockerfile

仓库已经提供统一的 `backend/Dockerfile`，以 `backend` 作为构建上下文并通过 `SERVICE_NAME` 参数选择微服务。部署时复用该 Dockerfile，不为每个模块重复维护镜像构建文件。`EXPOSE` 仅是镜像说明性元数据，实际监听端口由各服务配置和 Kubernetes Service 决定。

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build
ARG SERVICE_NAME
WORKDIR /workspace
COPY . .
RUN mvn -q -pl ${SERVICE_NAME} -am -DskipTests package

FROM eclipse-temurin:17-jre
ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}
WORKDIR /app
COPY --from=build /workspace/${SERVICE_NAME}/target/${SERVICE_NAME}-1.0-SNAPSHOT.jar /app/service.jar
ENTRYPOINT ["java", "-jar", "/app/service.jar"]
```

## 19. 构建和推送后端镜像

新建脚本：`deploy/scripts/build-images.ps1`。

```powershell
Set-Location ../..

docker build -f backend/Dockerfile --build-arg SERVICE_NAME=gateway-service -t localhost:5000/gateway-service:1.0.0 backend
docker build -f backend/Dockerfile --build-arg SERVICE_NAME=user-service -t localhost:5000/user-service:1.0.0 backend
docker build -f backend/Dockerfile --build-arg SERVICE_NAME=menu-service -t localhost:5000/menu-service:1.0.0 backend
docker build -f backend/Dockerfile --build-arg SERVICE_NAME=order-service -t localhost:5000/order-service:1.0.0 backend
docker build -f backend/Dockerfile --build-arg SERVICE_NAME=pickup-service -t localhost:5000/pickup-service:1.0.0 backend
```

新建脚本：`deploy/scripts/push-images.ps1`。

```powershell
docker push localhost:5000/gateway-service:1.0.0
docker push localhost:5000/user-service:1.0.0
docker push localhost:5000/menu-service:1.0.0
docker push localhost:5000/order-service:1.0.0
docker push localhost:5000/pickup-service:1.0.0
docker push localhost:5000/frontend-nginx:1.0.0
```

宿主机执行 `docker push` 时使用 `localhost:5000`；Pod 拉取镜像时必须使用 K3d 集群网络内可解析的 Registry 地址：

```text
k3d-smart-canteen-registry:5000/<image>:1.0.0
```

其中 `k3d-smart-canteen-registry` 对应第 8 节创建的 managed registry 容器名。不能在 Deployment 的 `image` 字段中使用宿主机含义下的 `localhost:5000`。

执行：

```powershell
cd deploy/scripts
.\build-images.ps1
.\push-images.ps1
```

## 20. 业务服务 Kubernetes 清单

业务服务采用 Deployment 部署，Service 使用 ClusterIP 暴露到集群内部。所有 Deployment 的 `selector.matchLabels` 必须与 Pod 模板中的 `metadata.labels` 保持一致。

### 20.1 user-service.yaml

新建文件：`deploy/k8s/apps/user-service.yaml`。

```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: canteen
spec:
  type: ClusterIP
  selector:
    app: user-service
  ports:
    - name: http
      port: 9001
      targetPort: 9001
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: canteen
spec:
  replicas: 1
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
        - name: user-service
          image: k3d-smart-canteen-registry:5000/user-service:1.0.0
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 9001
          envFrom:
            - configMapRef:
                name: canteen-common-config
            - secretRef:
                name: canteen-app-secret
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 9001
            initialDelaySeconds: 40
            periodSeconds: 10
            timeoutSeconds: 5
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 9001
            initialDelaySeconds: 60
            periodSeconds: 20
            timeoutSeconds: 5
```

### 20.2 menu-service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: menu-service
  namespace: canteen
spec:
  type: ClusterIP
  selector:
    app: menu-service
  ports:
    - name: http
      port: 9002
      targetPort: 9002
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: menu-service
  namespace: canteen
spec:
  replicas: 1
  selector:
    matchLabels:
      app: menu-service
  template:
    metadata:
      labels:
        app: menu-service
    spec:
      containers:
        - name: menu-service
          image: k3d-smart-canteen-registry:5000/menu-service:1.0.0
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 9002
          envFrom:
            - configMapRef:
                name: canteen-common-config
            - secretRef:
                name: canteen-app-secret
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 9002
            initialDelaySeconds: 40
            periodSeconds: 10
```

### 20.3 pickup-service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: pickup-service
  namespace: canteen
spec:
  type: ClusterIP
  selector:
    app: pickup-service
  ports:
    - name: http
      port: 9004
      targetPort: 9004
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pickup-service
  namespace: canteen
spec:
  replicas: 1
  selector:
    matchLabels:
      app: pickup-service
  template:
    metadata:
      labels:
        app: pickup-service
    spec:
      containers:
        - name: pickup-service
          image: k3d-smart-canteen-registry:5000/pickup-service:1.0.0
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 9004
          envFrom:
            - configMapRef:
                name: canteen-common-config
            - secretRef:
                name: canteen-app-secret
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 9004
            initialDelaySeconds: 40
            periodSeconds: 10
```

### 20.4 order-service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: order-service
  namespace: canteen
spec:
  type: ClusterIP
  selector:
    app: order-service
  ports:
    - name: http
      port: 9003
      targetPort: 9003
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
  namespace: canteen
spec:
  replicas: 1
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
        - name: order-service
          image: k3d-smart-canteen-registry:5000/order-service:1.0.0
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 9003
          envFrom:
            - configMapRef:
                name: canteen-common-config
            - secretRef:
                name: canteen-app-secret
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 9003
            initialDelaySeconds: 50
            periodSeconds: 10
```

### 20.5 gateway-service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: gateway-service
  namespace: canteen
spec:
  type: ClusterIP
  selector:
    app: gateway-service
  ports:
    - name: http
      port: 9000
      targetPort: 9000
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway-service
  namespace: canteen
spec:
  replicas: 1
  selector:
    matchLabels:
      app: gateway-service
  template:
    metadata:
      labels:
        app: gateway-service
    spec:
      containers:
        - name: gateway-service
          image: k3d-smart-canteen-registry:5000/gateway-service:1.0.0
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 9000
          envFrom:
            - configMapRef:
                name: canteen-common-config
            - secretRef:
                name: canteen-app-secret
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 9000
            initialDelaySeconds: 60
            periodSeconds: 10
```

## 21. 前端打包和部署

### 21.1 前端访问路径规划

项目当前是一个 Vite 多入口应用，而不是四个独立前端工程。一次 `npm run build` 会产出用户端、商家端、管理员端和大屏端入口。

| 前端入口 | 部署访问路径 | 当前路由模式 |
|---|---|---|
| 用户端 `index.html` | `/`，例如 `/menu`、`/login` | History |
| 商家端 `merchant.html` | `/merchant.html` | Hash |
| 管理员端 `admin.html` | `/admin.html` | Hash |
| 大屏端 `screen.html` | `/screen.html` | 单页面入口 |

当前 `src/common/api/request.js` 已使用统一 API 前缀：

```javascript
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})
```

大屏 WebSocket 统一使用：

```javascript
const url = `${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/ws/screen/${merchantId}`
const ws = new WebSocket(url)
```

### 21.2 Nginx 配置

新建文件：`frontend/canteen-vue/nginx.conf`。用户端使用 History Router，因此未知静态路径回落到用户端入口；`/api` 和 `/ws` 请求在 Ingress 层已优先转发到 Gateway，不会进入前端容器。

```nginx
server {
    listen 80;
    server_name localhost;

    root /usr/share/nginx/html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 21.3 前端 Dockerfile

新建文件：`frontend/canteen-vue/Dockerfile`。

```dockerfile
FROM node:20-alpine AS build
WORKDIR /workspace
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist /usr/share/nginx/html
EXPOSE 80
```

### 21.4 构建前端镜像

```powershell
docker build -t localhost:5000/frontend-nginx:1.0.0 frontend/canteen-vue
docker push localhost:5000/frontend-nginx:1.0.0
```

### 21.5 前端 Kubernetes 清单

新建文件：`deploy/k8s/apps/frontend.yaml`。

```yaml
apiVersion: v1
kind: Service
metadata:
  name: frontend-nginx
  namespace: canteen
spec:
  type: ClusterIP
  selector:
    app: frontend-nginx
  ports:
    - name: http
      port: 80
      targetPort: 80
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend-nginx
  namespace: canteen
spec:
  replicas: 1
  selector:
    matchLabels:
      app: frontend-nginx
  template:
    metadata:
      labels:
        app: frontend-nginx
    spec:
      containers:
        - name: frontend-nginx
          image: k3d-smart-canteen-registry:5000/frontend-nginx:1.0.0
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 80
```

## 22. Ingress 路由清单

新建文件：`deploy/k8s/ingress.yaml`。

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: canteen-ingress
  namespace: canteen
spec:
  ingressClassName: traefik
  rules:
    - http:
        paths:
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: gateway-service
                port:
                  number: 9000
          - path: /ws
            pathType: Prefix
            backend:
              service:
                name: gateway-service
                port:
                  number: 9000
          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend-nginx
                port:
                  number: 80
```

说明：

- `/api` 和 `/ws` 必须放在 `/` 前面，避免被根路径提前匹配。
- `gateway-service` 的 Service 端口为 9000。
- `frontend-nginx` 的 Service 端口为 80。
- Ingress 不直接暴露 user、menu、order、pickup 服务，所有后端请求统一经过 Gateway。

## 23. 部署执行顺序

建议严格按照以下顺序部署，避免业务服务启动时找不到基础组件。

```powershell
kubectl apply -f deploy/k8s/00-namespace.yaml
kubectl apply -f deploy/k8s/01-secret.yaml
kubectl apply -f deploy/k8s/02-configmap.yaml
kubectl create configmap mysql-init-sql -n infra `
  --from-file=01-canteen-user.sql=deploy/sql/canteen_user.sql `
  --from-file=02-canteen-menu.sql=deploy/sql/canteen_menu.sql `
  --from-file=03-canteen-order.sql=deploy/sql/canteen_order.sql `
  --from-file=04-canteen-pickup.sql=deploy/sql/canteen_pickup.sql `
  --from-file=05-seata-undo-log.sql=deploy/sql/seata_undo_log.sql `
  --from-file=06-init-data.sql=deploy/sql/init_data.sql `
  --dry-run=client -o yaml | kubectl apply -f -

kubectl apply -f deploy/k8s/infra/mysql.yaml
kubectl apply -f deploy/k8s/infra/redis.yaml
kubectl apply -f deploy/k8s/infra/nacos.yaml
kubectl apply -f deploy/k8s/infra/rocketmq.yaml
kubectl apply -f deploy/k8s/infra/seata.yaml

kubectl wait --for=condition=Ready pod -l app=mysql -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=redis -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=nacos -n infra --timeout=240s
kubectl wait --for=condition=Ready pod -l app=rocketmq-namesrv -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=rocketmq-broker -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=seata -n infra --timeout=180s

kubectl apply -f deploy/k8s/apps/user-service.yaml
kubectl apply -f deploy/k8s/apps/menu-service.yaml
kubectl apply -f deploy/k8s/apps/pickup-service.yaml
kubectl apply -f deploy/k8s/apps/order-service.yaml
kubectl apply -f deploy/k8s/apps/gateway-service.yaml
kubectl apply -f deploy/k8s/apps/frontend.yaml

kubectl apply -f deploy/k8s/ingress.yaml
```

新建脚本：`deploy/scripts/apply-all.ps1`。

```powershell
kubectl apply -f ../k8s/00-namespace.yaml
kubectl apply -f ../k8s/01-secret.yaml
kubectl apply -f ../k8s/02-configmap.yaml
kubectl create configmap mysql-init-sql -n infra `
  --from-file=01-canteen-user.sql=../sql/canteen_user.sql `
  --from-file=02-canteen-menu.sql=../sql/canteen_menu.sql `
  --from-file=03-canteen-order.sql=../sql/canteen_order.sql `
  --from-file=04-canteen-pickup.sql=../sql/canteen_pickup.sql `
  --from-file=05-seata-undo-log.sql=../sql/seata_undo_log.sql `
  --from-file=06-init-data.sql=../sql/init_data.sql `
  --dry-run=client -o yaml | kubectl apply -f -

kubectl apply -f ../k8s/infra/mysql.yaml
kubectl apply -f ../k8s/infra/redis.yaml
kubectl apply -f ../k8s/infra/nacos.yaml
kubectl apply -f ../k8s/infra/rocketmq.yaml
kubectl apply -f ../k8s/infra/seata.yaml

kubectl wait --for=condition=Ready pod -l app=mysql -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=redis -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=nacos -n infra --timeout=240s
kubectl wait --for=condition=Ready pod -l app=rocketmq-namesrv -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=rocketmq-broker -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=seata -n infra --timeout=180s

kubectl apply -f ../k8s/apps/user-service.yaml
kubectl apply -f ../k8s/apps/menu-service.yaml
kubectl apply -f ../k8s/apps/pickup-service.yaml
kubectl apply -f ../k8s/apps/order-service.yaml
kubectl apply -f ../k8s/apps/gateway-service.yaml
kubectl apply -f ../k8s/apps/frontend.yaml

kubectl apply -f ../k8s/ingress.yaml
```

执行：

```powershell
cd deploy/scripts
.\apply-all.ps1
.\check-all.ps1
```

其中 `check-all.ps1` 会等待基础组件和业务 Deployment 就绪，并输出 Pod、Service、PVC 与 Ingress 当前状态，用于部署后的快速验收。

## 24. 部署验证

### 24.1 检查 Pod 状态

```powershell
kubectl get pod -A
kubectl get pod -n infra
kubectl get pod -n canteen
```

所有关键 Pod 应处于 Running 状态。

### 24.2 检查 Service

```powershell
kubectl get svc -n infra
kubectl get svc -n canteen
```

应能看到：

```text
mysql
redis
nacos
rocketmq-namesrv
rocketmq-broker
seata
user-service
menu-service
order-service
pickup-service
gateway-service
frontend-nginx
```

### 24.3 检查 PVC

```powershell
kubectl get pvc -n infra
```

MySQL、Redis、Nacos、RocketMQ Broker、Seata 应存在对应 PVC。

### 24.4 检查 Ingress

```powershell
kubectl get ingress -n canteen
kubectl describe ingress -n canteen canteen-ingress
```

### 24.5 检查日志

```powershell
kubectl logs -n infra mysql-0
kubectl logs -n infra redis-0
kubectl logs -n infra nacos-0
kubectl logs -n infra rocketmq-namesrv-0
kubectl logs -n infra rocketmq-broker-0
kubectl logs -n infra seata-0

kubectl logs -n canteen deploy/user-service
kubectl logs -n canteen deploy/menu-service
kubectl logs -n canteen deploy/pickup-service
kubectl logs -n canteen deploy/order-service
kubectl logs -n canteen deploy/gateway-service
kubectl logs -n canteen deploy/frontend-nginx
```

### 24.6 访问前端

浏览器访问：

```text
http://localhost/
http://localhost/merchant.html
http://localhost/admin.html
http://localhost/screen.html
```

### 24.7 访问后端 API

Ingress 只将实际业务路径交给 Gateway，因此通过公开查询接口验证 Gateway、Nacos 服务发现和 Menu Service 链路：

```powershell
curl "http://localhost/api/menu/available?merchantId=2000001&mealPeriod=LUNCH"
curl "http://localhost/api/pickup/snapshot/2000001"
```

需要单独查看健康端点时，可在集群内部访问具体 Service，例如：

```powershell
kubectl run curl-check -n canteen --rm -it --restart=Never --image=curlimages/curl -- http://gateway-service:9000/actuator/health
```

### 24.8 验证 Nacos 注册

临时转发 Nacos 控制台：

```powershell
kubectl port-forward -n infra svc/nacos 8848:8848
```

浏览器访问：

```text
http://localhost:8848/nacos
```

在服务列表中检查是否存在：

```text
gateway-service
user-service
menu-service
order-service
pickup-service
```

## 25. 常见问题与处理

### 25.1 ImagePullBackOff

查看原因：

```powershell
kubectl describe pod -n canteen <pod-name>
```

常见原因：

| 原因 | 处理方式 |
|---|---|
| 镜像没有 push 到本地 registry | 重新执行 `docker push localhost:5000/xxx:1.0.0` |
| Pod 使用了宿主机地址 `localhost:5000` | Deployment 改用 `k3d-smart-canteen-registry:5000/xxx:1.0.0` |
| 镜像名称写错 | 检查 Deployment 中 `image` 字段及 tag |
| K3d registry 未创建 | 删除并重建 K3d 集群 |

### 25.2 CrashLoopBackOff

查看日志：

```powershell
kubectl logs -n canteen deploy/user-service
```

常见原因：

| 报错表现 | 可能原因 | 处理方式 |
|---|---|---|
| cannot connect mysql | MySQL 地址、端口或密码错误 | 检查 ConfigMap 和 Secret |
| unknown database | 数据库未初始化 | 检查 mysql-init-sql 是否执行 |
| NOAUTH Authentication required | Redis 密码未配置 | 检查 `REDIS_PASSWORD` |
| failed to connect Nacos | Nacos 未就绪或地址错误 | 检查 Nacos Pod 和 `NACOS_SERVER_ADDR` |
| RocketMQ connect failed | NameServer 地址错误 | 检查 `ROCKETMQ_NAME_SERVER` |

### 25.3 前端刷新 404

原因：用户端 Vue Router 使用 history 模式，Nginx 缺少 `try_files` 配置。商家端与管理员端当前使用 Hash Router，不需要额外路由回退。

处理方式：确保 Nginx 配置中包含：

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### 25.4 Gateway 转发 404

当前 Controller 已定义 `/api` 路径，Gateway 应原样转发请求。若为该项目添加 `StripPrefix=1`，会导致后端收到 `/user/**`、`/menu/**` 路径并返回 404。

项目实际契约：

| 外部路径 | Gateway 转发路径 | Controller 路径 |
|---|---|---|
| `/api/user/login/password` | `/api/user/login/password` | `@RequestMapping("/api/user")` |
| `/api/menu/available` | `/api/menu/available` | `@RequestMapping("/api/menu")` |
| `/api/order/create` | `/api/order/create` | `@RequestMapping("/api/order")` |
| `/api/pickup/snapshot/2000001` | `/api/pickup/snapshot/2000001` | `@RequestMapping("/api/pickup")` |

### 25.5 80 端口被占用

查看占用进程：

```powershell
netstat -ano | findstr ":80"
```

处理方式一：关闭占用 80 端口的服务。

处理方式二：将 K3d 入口改为 8080。

```yaml
ports:
  - port: 8080:80
    nodeFilters:
      - loadbalancer
```

然后重建集群：

```powershell
k3d cluster delete smart-canteen
k3d cluster create --config deploy/k3s/cluster.yaml
```

### 25.6 MySQL 初始化 SQL 未执行

MySQL 官方镜像只会在数据目录为空时执行 `/docker-entrypoint-initdb.d` 下的初始化 SQL。如果 PVC 已经存在，后续修改 ConfigMap 不会自动重新执行初始化 SQL。

处理方式：

```powershell
kubectl delete -f deploy/k8s/infra/mysql.yaml
kubectl delete pvc -n infra mysql-data-mysql-0
kubectl apply -f deploy/k8s/infra/mysql.yaml
```

注意：该操作会删除数据库数据，只适合课程测试环境。

## 26. 清理与重新部署

### 26.1 删除业务服务

```powershell
kubectl delete -f deploy/k8s/ingress.yaml
kubectl delete -f deploy/k8s/apps/frontend.yaml
kubectl delete -f deploy/k8s/apps/gateway-service.yaml
kubectl delete -f deploy/k8s/apps/order-service.yaml
kubectl delete -f deploy/k8s/apps/pickup-service.yaml
kubectl delete -f deploy/k8s/apps/menu-service.yaml
kubectl delete -f deploy/k8s/apps/user-service.yaml
```

### 26.2 删除基础组件

```powershell
kubectl delete -f deploy/k8s/infra/seata.yaml
kubectl delete -f deploy/k8s/infra/rocketmq.yaml
kubectl delete -f deploy/k8s/infra/nacos.yaml
kubectl delete -f deploy/k8s/infra/redis.yaml
kubectl delete -f deploy/k8s/infra/mysql.yaml
```

### 26.3 删除 PVC

```powershell
kubectl delete pvc -n infra --all
```

### 26.4 删除 K3d 集群

```powershell
k3d cluster delete smart-canteen
```

## 27. 部署验收标准

部署完成后，应满足以下验收标准：

| 验收项 | 验收标准 |
|---|---|
| K3d 集群 | `kubectl get nodes` 显示节点 Ready |
| 基础组件 | MySQL、Redis、Nacos、RocketMQ、Seata Pod 均 Running |
| 业务服务 | Gateway、User、Menu、Order、Pickup Pod 均 Running |
| 服务注册 | Nacos 控制台可看到所有后端服务 |
| 前端访问 | 用户端、商家端、管理端、大屏端均可通过 localhost 访问 |
| API 转发 | `/api/**` 请求可经 Gateway 正确转发 |
| WebSocket | `/ws/screen/2000001` 可建立连接并推送叫号信息 |
| 数据持久化 | 删除并重建 Pod 后，MySQL 和 Redis 数据不丢失 |
| 端口配置 | Ingress 80、Gateway 9000、业务服务 9001-9004、基础组件端口均符合规划 |
| 标签选择器 | Service selector 与 Pod label 完全一致 |

## 28. 总结

本部署方案基于 Windows + Docker Desktop + K3d/K3s 实现智能食堂系统的本地集群化部署。基础组件部署在 `infra` 命名空间，业务服务部署在 `canteen` 命名空间，通过 Kubernetes Service 实现集群内部通信，通过 Traefik Ingress 暴露统一入口。系统对外只暴露 `localhost:80`，前端、API 和 WebSocket 均通过路径转发完成统一访问。

该方案既保留了课程开发调试阶段的单机便利性，又能够体现 K3s、Ingress、Service、Deployment、StatefulSet、PVC、ConfigMap、Secret 等 Kubernetes 核心资源的使用，满足课程作业对 K3S 部署方案的要求。

## 29. 参考资料

1. Docker Desktop 官方文档：Windows WSL2 后端说明。
2. k3d 官方文档：本地 K3s 集群创建、端口映射和本地镜像仓库配置。
3. K3s 官方文档：Traefik Ingress Controller 和网络服务说明。
4. Kubernetes 官方文档：StatefulSet、PersistentVolume、ConfigMap、Secret、Service、Ingress 资源说明。
5. 项目架构设计文档：《A智能食堂系统架构设计文档》。
