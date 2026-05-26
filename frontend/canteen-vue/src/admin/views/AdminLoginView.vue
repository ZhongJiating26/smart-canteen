<template>
  <div id="admin-login-view" class="admin-login-container active">
    <div class="admin-login-box">
      <div class="login-header">
        <div class="logo-box"><span class="material-symbols-outlined logo-icon">shield_person</span></div>
        <span class="brand-title">Campus Eats</span>
        <p class="login-subtitle">管理员控制台登录</p>
      </div>
      <div class="form-card">
        <h1 class="card-title">登录</h1>
        <form class="auth-form" @submit.prevent="handleLogin">
          <md-outlined-text-field :value="username" @input="username = $event.target.value" label="管理员账号 / 邮箱" required style="width:100%;">
            <span slot="trailing-icon" class="material-symbols-outlined">account_circle</span>
          </md-outlined-text-field>
          <md-outlined-text-field :value="password" @input="password = $event.target.value" :type="showPwd ? 'text' : 'password'" label="密码" required style="width:100%;">
            <span slot="trailing-icon" class="material-symbols-outlined eye-toggle" @click="showPwd = !showPwd">{{ showPwd ? 'visibility_off' : 'visibility' }}</span>
          </md-outlined-text-field>
          <button type="submit" class="btn-primary" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
        </form>
      </div>
    </div>
    <footer class="screen-footer"><p>© 2024 Campus Eats 生态系统。保留所有权利。</p></footer>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'

const authStore = useAuthStore()
const router = useRouter()
const toast = inject('toast')
const username = ref('')
const password = ref('')
const showPwd = ref(false)
const loading = ref(false)

async function handleLogin() {
  if (!username.value.trim() || !password.value) {
    toast.show('请填写所有字段', 'warning')
    return
  }
  loading.value = true
  try {
    await authStore.passwordLogin(username.value.trim(), password.value)
    toast.show('登录成功！', 'check_circle')
    router.replace('/dashboard')
  } catch (e) {
    toast.show(e.message || '登录失败', 'error')
  } finally { loading.value = false }
}
</script>
