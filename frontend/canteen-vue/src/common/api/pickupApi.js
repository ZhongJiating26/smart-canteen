import request from './request'

export function getPreparingQueue(merchantId) {
  return request.get(`/pickup/preparing/${merchantId}`)
}

export function getWaitingQueue(merchantId) {
  return request.get(`/pickup/waiting/${merchantId}`)
}

export function getMyPosition(orderId) {
  return request.get(`/pickup/my-position/${orderId}`)
}

export function getQrcode(orderId) {
  return request.get(`/pickup/qrcode/${orderId}`)
}

export function manualCall(merchantId, orderId) {
  return request.post('/pickup/call', { merchantId, orderId })
}

export function verifyPickup(data) {
  return request.post('/pickup/verify', data)
}

export function getSnapshot(merchantId) {
  return request.get(`/pickup/snapshot/${merchantId}`)
}
