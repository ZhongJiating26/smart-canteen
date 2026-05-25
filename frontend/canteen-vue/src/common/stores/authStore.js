import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as userApi from '@common/api/userApi'
import { getToken, setToken, clearAllTokens } from '@common/utils/storage'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(getToken('accessToken') || '')
  const refreshTokenVal = ref(getToken('refreshToken') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!accessToken.value)
  const role = computed(() => userInfo.value?.role || '')
  const userId = computed(() => userInfo.value?.userId || '')
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || '')

  function setAuthData(data) {
    if (data.accessToken) {
      accessToken.value = data.accessToken
      setToken('accessToken', data.accessToken)
    }
    if (data.refreshToken) {
      refreshTokenVal.value = data.refreshToken
      setToken('refreshToken', data.refreshToken)
    }
    if (data.userInfo) {
      userInfo.value = data.userInfo
    }
  }

  async function passwordLogin(username, password) {
    const data = await userApi.loginByPassword(username, password)
    setAuthData(data)
    return data
  }

  async function codeLogin(phone, verifyCode) {
    const data = await userApi.loginByCode(phone, verifyCode)
    setAuthData(data)
    return data
  }

  async function register(data) {
    return await userApi.register(data)
  }

  async function doLogout() {
    try {
      if (accessToken.value) {
        await userApi.logout()
      }
    } catch (e) {
      // ignore
    }
    accessToken.value = ''
    refreshTokenVal.value = ''
    userInfo.value = null
    clearAllTokens()
  }

  async function fetchProfile() {
    const data = await userApi.getProfile()
    userInfo.value = data
    return data
  }

  async function updateProfile(profileData) {
    await userApi.updateProfile(profileData)
    await fetchProfile()
  }

  return {
    accessToken,
    refreshTokenVal,
    userInfo,
    isLoggedIn,
    role,
    userId,
    username,
    nickname,
    setAuthData,
    passwordLogin,
    codeLogin,
    register,
    doLogout,
    fetchProfile,
    updateProfile
  }
})
