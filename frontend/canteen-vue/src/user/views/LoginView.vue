<template>
  <div class="mobile-screen state-center" id="login-view">
    <header class="screen-header">
      <span class="brand-title">Campus Eats</span>
    </header>

    <div class="screen-content">
      <div class="form-card">
        <h1 class="card-title">用户登录</h1>
        <p class="card-subtitle">开启校园美味点餐之旅</p>

        <div class="tab-bar">
          <div class="tab-item" :class="{ active: loginMode === 'password' }" @click="loginMode = 'password'">密码登录</div>
          <div class="tab-item" :class="{ active: loginMode === 'code' }" @click="loginMode = 'code'">验证码登录</div>
        </div>

        <form class="auth-form" @submit.prevent="handleLogin">
          <md-outlined-text-field
            :value="account"
            @input="account = $event.target.value"
            label="账号 (手机号/学工号)"
            required
          >
            <span slot="trailing-icon" class="material-symbols-outlined">account_circle</span>
          </md-outlined-text-field>

          <md-outlined-text-field
            v-if="loginMode === 'password'"
            :value="password"
            @input="password = $event.target.value"
            :type="showPwd ? 'text' : 'password'"
            label="密码"
            required
          >
            <span slot="trailing-icon" class="material-symbols-outlined eye-toggle" @click="showPwd = !showPwd">{{ showPwd ? 'visibility_off' : 'visibility' }}</span>
          </md-outlined-text-field>

          <div v-if="loginMode === 'code'" class="verification-row">
            <md-outlined-text-field
              :value="code"
              @input="code = $event.target.value.replace(/[^0-9]/g, '').slice(0, 6)"
              label="验证码"
              class="flex-grow"
            >
              <span slot="leading-icon" class="material-symbols-outlined">gpp_good</span>
            </md-outlined-text-field>
            <button type="button" class="btn-send-code" :disabled="countdown.isRunning.value" @click="sendVerifyCode">
              {{ countdown.isRunning.value ? `${countdown.countdown.value}s` : '发送验证码' }}
            </button>
          </div>

          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <div class="form-links">
          <a href="#" class="link-item" @click.prevent="showToast('密码重置功能即将上线', 'info')">忘记密码？</a>
          <span class="link-text">
            新用户？<router-link to="/register" class="link-item bold">立即注册</router-link>
          </span>
        </div>
      </div>
    </div>

    <footer class="screen-footer">
      <p>© 2024 Campus Eats 生态系统。保留所有权利。</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'
import { useCountdown } from '@common/composables/useCountdown'
import { sendCode } from '@common/api/userApi'

const authStore = useAuthStore()
const router = useRouter()
const toast = inject('toast')
const countdown = useCountdown()

const loginMode = ref('password')
const account = ref('')
const password = ref('')
const code = ref('')
const showPwd = ref(false)
const loading = ref(false)

function showToast(msg, icon) {
  toast.show(msg, icon)
}

async function sendVerifyCode() {
  const phone = account.value.trim()
  if (!phone) {
    showToast('请先输入您的手机号！', 'warning')
    return
  }
  try {
    await sendCode(phone)
    countdown.start()
    showToast('验证码已发送！验证码为：123456', 'sms')
  } catch (e) {
    showToast(e.message || '发送验证码失败', 'error')
  }
}

async function handleLogin() {
  const phone = account.value.trim()
  if (!phone) {
    showToast('请输入账号！', 'warning')
    return
  }

  loading.value = true
  try {
    if (loginMode.value === 'password') {
      if (!password.value) {
        showToast('请输入密码！', 'warning')
        loading.value = false
        return
      }
      await authStore.passwordLogin(phone, password.value)
    } else {
      if (!code.value || !/^\d{6}$/.test(code.value)) {
        showToast('验证码必须为 6 位数字！', 'warning')
        loading.value = false
        return
      }
      await authStore.codeLogin(phone, code.value)
    }
    showToast('登录成功！欢迎来到 Campus Eats。', 'check_circle')
    router.replace('/menu')
  } catch (e) {
    showToast(e.message || '登录失败', 'error')
  } finally {
    loading.value = false
  }
}
</script>
