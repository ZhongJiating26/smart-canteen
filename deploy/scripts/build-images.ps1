Set-Location ../..

docker build -f backend/Dockerfile --build-arg SERVICE_NAME=gateway-service -t localhost:5000/gateway-service:1.0.0 backend
docker build -f backend/Dockerfile --build-arg SERVICE_NAME=user-service -t localhost:5000/user-service:1.0.0 backend
docker build -f backend/Dockerfile --build-arg SERVICE_NAME=menu-service -t localhost:5000/menu-service:1.0.0 backend
docker build -f backend/Dockerfile --build-arg SERVICE_NAME=order-service -t localhost:5000/order-service:1.0.0 backend
docker build -f backend/Dockerfile --build-arg SERVICE_NAME=pickup-service -t localhost:5000/pickup-service:1.0.0 backend
docker build -f frontend/canteen-vue/Dockerfile -t localhost:5000/frontend-nginx:1.0.0 frontend/canteen-vue
