$ErrorActionPreference = "Stop"

$K8sRoot = Resolve-Path (Join-Path $PSScriptRoot "..\k8s")

kubectl apply -f (Join-Path $K8sRoot "00-namespace.yaml")
kubectl apply -f (Join-Path $K8sRoot "01-secret.yaml")
kubectl apply -f (Join-Path $K8sRoot "02-configmap.yaml")

kubectl apply -f (Join-Path $K8sRoot "apps\user-service.yaml")
kubectl apply -f (Join-Path $K8sRoot "apps\menu-service.yaml")
kubectl apply -f (Join-Path $K8sRoot "apps\pickup-service.yaml")
kubectl apply -f (Join-Path $K8sRoot "apps\order-service.yaml")
kubectl apply -f (Join-Path $K8sRoot "apps\gateway-service.yaml")
kubectl apply -f (Join-Path $K8sRoot "apps\frontend.yaml")

kubectl apply -f (Join-Path $K8sRoot "ingress.yaml")
