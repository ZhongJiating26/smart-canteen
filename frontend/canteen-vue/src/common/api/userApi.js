import request from './request'

export function sendCode(phone) {
  return request.post('/user/send-code', { phone })
}

export function register(data) {
  return request.post('/user/register', data)
}

export function loginByPassword(username, password) {
  return request.post('/user/login/password', { username, password })
}

export function loginByCode(phone, verifyCode) {
  return request.post('/user/login/code', { phone, verifyCode })
}

export function refreshToken() {
  return request.post('/user/refresh')
}

export function logout() {
  return request.post('/user/logout')
}

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function verifyPayPassword(userId, payPassword) {
  return request.post('/user/verify-pay-password', { userId, payPassword })
}
