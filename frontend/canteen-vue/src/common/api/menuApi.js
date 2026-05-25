import request from './request'

export function getCurrentMerchant() {
  return request.get('/menu/merchant/me')
}

export function getCategories(merchantId) {
  return request.get('/menu/categories', { params: { merchantId } })
}

export function createCategory(data) {
  return request.post('/menu/categories', data)
}

export function getDishes(params) {
  return request.get('/menu/dishes', { params })
}

export function getDishDetail(dishId) {
  return request.get(`/menu/dishes/${dishId}`)
}

export function createDish(data) {
  return request.post('/menu/dishes', data)
}

export function updateDish(dishId, data) {
  return request.put(`/menu/dishes/${dishId}`, data)
}

export function deleteDish(dishId) {
  return request.delete(`/menu/dishes/${dishId}`)
}

export function uploadDishImage(dishId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.put(`/menu/dishes/${dishId}/image`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getAvailableMenu(params) {
  return request.get('/menu/available', { params })
}

export function getDailyMenus(params) {
  return request.get('/menu/daily-menus', { params })
}

export function saveDailyMenu(data) {
  return request.post('/menu/daily-menus', data)
}

export function updateDailyMenuItems(menuId, data) {
  return request.put(`/menu/daily-menus/${menuId}/items`, data)
}

export function getInventoryAlerts() {
  return request.get('/menu/inventory/alerts')
}
