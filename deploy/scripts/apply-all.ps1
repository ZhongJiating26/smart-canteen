kubectl apply -f ../k8s/00-namespace.yaml
kubectl apply -f ../k8s/01-secret.yaml
kubectl apply -f ../k8s/02-configmap.yaml

kubectl apply -f ../k8s/apps/user-service.yaml
kubectl apply -f ../k8s/apps/menu-service.yaml
kubectl apply -f ../k8s/apps/pickup-service.yaml
kubectl apply -f ../k8s/apps/order-service.yaml
kubectl apply -f ../k8s/apps/gateway-service.yaml
kubectl apply -f ../k8s/apps/frontend.yaml

kubectl apply -f ../k8s/ingress.yaml
