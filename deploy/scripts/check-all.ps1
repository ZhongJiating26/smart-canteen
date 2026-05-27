$ErrorActionPreference = "Stop"

Write-Host "Checking infrastructure pods..."
kubectl wait --for=condition=Ready pod -l app=mysql -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=redis -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=nacos -n infra --timeout=240s
kubectl wait --for=condition=Ready pod -l app=rocketmq-namesrv -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=rocketmq-broker -n infra --timeout=180s
kubectl wait --for=condition=Ready pod -l app=seata -n infra --timeout=180s

Write-Host "Checking application deployments..."
kubectl rollout status deployment/user-service -n canteen --timeout=180s
kubectl rollout status deployment/menu-service -n canteen --timeout=180s
kubectl rollout status deployment/pickup-service -n canteen --timeout=180s
kubectl rollout status deployment/order-service -n canteen --timeout=180s
kubectl rollout status deployment/gateway-service -n canteen --timeout=180s
kubectl rollout status deployment/frontend-nginx -n canteen --timeout=180s

Write-Host "Current cluster resources:"
kubectl get pods -n infra
kubectl get pods -n canteen
kubectl get svc -n infra
kubectl get svc -n canteen
kubectl get pvc -n infra
kubectl get ingress -n canteen
