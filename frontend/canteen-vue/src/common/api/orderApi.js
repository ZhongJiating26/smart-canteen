import request from './request'

export function createOrder(data) {
  return request.post('/order/create', data)
}

export function payOrder(orderId, payPassword) {
  return request.post(`/order/${orderId}/pay`, { payPassword })
}

export function cancelOrder(orderId, reason) {
  return request.post(`/order/${orderId}/cancel`, { reason })
}

export function getMyOrders(params) {
  return request.get('/order/my', { params })
}

export function getOrderDetail(orderId) {
  return request.get(`/order/${orderId}`)
}

export function getOrderStatus(orderId) {
  return request.get(`/order/${orderId}/status`)
}

export function acceptOrder(orderId) {
  return request.post(`/order/merchant/${orderId}/accept`)
}

export function rejectOrder(orderId, reason) {
  return request.post(`/order/merchant/${orderId}/reject`, { reason })
}

export function dispatchOrder(orderId) {
  return request.post(`/order/merchant/${orderId}/ready`)
}

export function getMerchantOrders(params) {
  return request.get('/order/merchant/list', { params })
}

export function getMyStatistics() {
  return request.get('/order/my/statistics')
}
