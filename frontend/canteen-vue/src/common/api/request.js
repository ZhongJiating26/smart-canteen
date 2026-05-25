import axios from 'axios'
import { getToken, setToken, clearAllTokens } from '@common/utils/storage'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

let isRefreshing = false
let failedQueue = []

function isLoginRequest(config) {
  return ['/user/login/password', '/user/login/code'].includes(config?.url)
}

function redirectToLogin() {
  window.location.hash = '#/login'
}

function processQueue(error, token = null) {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

request.interceptors.request.use(
  (config) => {
    const token = getToken('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    const idempotentKey = sessionStorage.getItem('idempotent_key')
    if (idempotentKey) {
      config.headers['X-Idempotent-Key'] = idempotentKey
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body.code === 200) {
      return body.data
    }
    if (body.message) {
      return Promise.reject(new Error(body.message))
    }
    return Promise.reject(new Error('Request failed'))
  },
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !isLoginRequest(originalRequest) && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return request(originalRequest)
        }).catch(err => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = getToken('refreshToken')
      if (!refreshToken) {
        isRefreshing = false
        clearAllTokens()
        redirectToLogin()
        return Promise.reject(error)
      }

      try {
        const response = await axios.post('/api/user/refresh', { refreshToken })

        if (response.data.code === 200) {
          const { accessToken, refreshToken: newRefreshToken } = response.data.data
          setToken('accessToken', accessToken)
          setToken('refreshToken', newRefreshToken)

          processQueue(null, accessToken)

          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return request(originalRequest)
        }
      } catch (refreshError) {
        processQueue(refreshError, null)
        clearAllTokens()
        redirectToLogin()
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    if (error.response?.data?.message) {
      return Promise.reject(new Error(error.response.data.message))
    }
    if (error.message === 'Network Error') {
      return Promise.reject(new Error('网络连接失败'))
    }
    return Promise.reject(error)
  }
)

export default request
