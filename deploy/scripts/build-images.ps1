$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$Registry = "smart-canteen-registry.localhost:5000"
$Version = "1.0.0"

Set-Location $ProjectRoot

$Services = @(
  "gateway-service",
  "user-service",
  "menu-service",
  "order-service",
  "pickup-service"
)

foreach ($Service in $Services) {
  docker build `
    -f backend/Dockerfile `
    --build-arg SERVICE_NAME=$Service `
    -t "$Registry/$Service`:$Version" `
    backend
}

docker build `
  -f frontend/canteen-vue/Dockerfile `
  -t "$Registry/frontend-nginx`:$Version" `
  frontend/canteen-vue
