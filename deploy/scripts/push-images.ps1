$ErrorActionPreference = "Stop"

$ImageRegistry = "smart-canteen-registry.localhost:5000"
$PushRegistry = "localhost:5000"
$Version = "1.0.0"
$Images = @(
  "gateway-service",
  "user-service",
  "menu-service",
  "order-service",
  "pickup-service",
  "frontend-nginx"
)

foreach ($Image in $Images) {
  docker tag "$ImageRegistry/$Image`:$Version" "$PushRegistry/$Image`:$Version"
  docker push "$PushRegistry/$Image`:$Version"
}
