$ErrorActionPreference = "Stop"

Write-Host "Checking Docker Compose infrastructure..."
docker compose -f ../docker-compose/compose.yaml ps

Write-Host "Checking application deployments..."
kubectl rollout status deployment/user-service -n canteen --timeout=180s
kubectl rollout status deployment/menu-service -n canteen --timeout=180s
kubectl rollout status deployment/pickup-service -n canteen --timeout=180s
kubectl rollout status deployment/order-service -n canteen --timeout=180s
kubectl rollout status deployment/gateway-service -n canteen --timeout=180s
kubectl rollout status deployment/frontend-nginx -n canteen --timeout=180s

Write-Host "Current cluster resources:"
kubectl get pods -n canteen
kubectl get svc -n canteen
kubectl get ingress -n canteen
