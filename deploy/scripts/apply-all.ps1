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