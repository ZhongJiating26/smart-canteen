const TOKEN_PREFIX = 'canteen_'

export function getToken(key) {
  return localStorage.getItem(TOKEN_PREFIX + key)
}

export function setToken(key, value) {
  localStorage.setItem(TOKEN_PREFIX + key, value)
}

export function removeToken(key) {
  localStorage.removeItem(TOKEN_PREFIX + key)
}

export function clearAllTokens() {
  Object.keys(localStorage)
    .filter(k => k.startsWith(TOKEN_PREFIX))
    .forEach(k => localStorage.removeItem(k))
}
