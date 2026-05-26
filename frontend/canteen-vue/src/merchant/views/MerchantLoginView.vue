<template>
  <div id="merchant-login-view" class="tablet-screen active">
    <div class="login-split-container">
      <div class="login-brand-side">
        <div class="brand-badge">商家端</div>
        <h1 class="brand-logo-text">Campus Eats</h1>
        <p class="brand-tagline">实时管理您的校园档口订单、菜品与营业状态。</p>
        <div class="brand-stats-badges">
          <div class="stat-badge"><span class="material-symbols-outlined">bolt</span><span>即时同步</span></div>
          <div class="stat-badge"><span class="material-symbols-outlined">devices</span><span>平板原生</span></div>
        </div>
      </div>

      <div class="login-form-side">
        <div class="login-form-wrapper">
          <h2 class="form-title">商家登录</h2>
          <p class="form-subtitle">输入您的商家账户凭证以管理档口</p>

          <div class="merchant-tab-bar">
            <button type="button" class="merchant-tab" :class="{ active: loginMode === 'password' }" @click="loginMode = 'password'">密码登录</button>
            <button type="button" class="merchant-tab" :class="{ active: loginMode === 'code' }" @click="loginMode = 'code'">验证码登录</button>
          </div>

          <form class="merchant-form" @submit.prevent="handleLogin">
            <div class="input-group">
              <md-outlined-text-field :value="account" @input="account = $event.target.value" label="档口账号 / 手机号" required style="width:100%;">
                <span class="material-symbols-outlined" slot="leading-icon">person</span>
              </md-outlined-text-field>
            </div>

            <div v-if="loginMode === 'password'" class="input-group">
              <md-outlined-text-field :value="password" @input="password = $event.target.value" :type="showPwd ? 'text' : 'password'" label="密码" required style="width:100%;">
                <span class="material-symbols-outlined" slot="leading-icon">lock</span>
                <span class="material-symbols-outlined eye-toggle clickable" slot="trailing-icon" @click="showPwd = !showPwd">{{ showPwd ? 'visibility_off' : 'visibility' }}</span>
              </md-outlined-text-field>
            </div>

            <div v-else class="input-group">
              <div class="sms-code-row">
                <md-outlined-text-field :value="code" @input="code = $event.target.value.replace(/[^0-9]/g, '').slice(0, 6)" label="验证码" style="flex-grow:1;" placeholder="6位验证码">
                  <span class="material-symbols-outlined" slot="leading-icon">sms</span>
                </md-outlined-text-field>
                <button type="button" class="btn-send-code" :disabled="countdown.isRunning.value" @click="sendCodeHandler">{{ countdown.isRunning.value ? `${countdown.countdown.value}s` : '发送验证码' }}</button>
              </div>
            </div>

            <button type="submit" class="btn-login-submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
          </form>
        </div>
      </div>
    </div>
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

async function sendCodeHandler() {
  if (!account.value.trim()) { toast.show('请先输入手机号码！', 'warning'); return }
  try {
    await sendCode(account.value.trim())
    countdown.start()
    toast.show('验证码已发送！验证码：123456', 'sms')
  } catch (e) { toast.show(e.message || '发送验证码失败', 'error') }
}

async function handleLogin() {
  if (!account.value.trim()) { toast.show('请输入账号！', 'warning'); return }
  loading.value = true
  try {
    if (loginMode.value === 'password') {
      if (!password.value) { toast.show('请输入密码！', 'warning'); loading.value = false; return }
      await authStore.passwordLogin(account.value.trim(), password.value)
    } else {
      if (!code.value || !/^\d{6}$/.test(code.value)) { toast.show('验证码必须为6位数字！', 'warning'); loading.value = false; return }
      await authStore.codeLogin(account.value.trim(), code.value)
    }
    toast.show('登录成功！', 'check_circle')
    router.replace('/orders')
  } catch (e) {
    toast.show(e.message || '登录失败', 'error')
  } finally { loading.value = false }
}
</script>
