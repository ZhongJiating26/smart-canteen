<template>
  <div id="merchant-login-view" class="tablet-screen active">
    <div class="login-split-container">
      <div class="login-brand-side">
        <div class="brand-badge">Merchant</div>
        <h1 class="brand-logo-text">Campus Eats</h1>
        <p class="brand-tagline">Manage your campus stall orders, dishes, and status in real time.</p>
        <div class="brand-stats-badges">
          <div class="stat-badge"><span class="material-symbols-outlined">bolt</span><span>Instant Sync</span></div>
          <div class="stat-badge"><span class="material-symbols-outlined">devices</span><span>Tablet Native</span></div>
        </div>
      </div>

      <div class="login-form-side">
        <div class="login-form-wrapper">
          <h2 class="form-title">Portal Sign In</h2>
          <p class="form-subtitle">Enter your merchant credentials to manage your stall</p>

          <div class="merchant-tab-bar">
            <button type="button" class="merchant-tab" :class="{ active: loginMode === 'password' }" @click="loginMode = 'password'">Password</button>
            <button type="button" class="merchant-tab" :class="{ active: loginMode === 'code' }" @click="loginMode = 'code'">SMS Code</button>
          </div>

          <form class="merchant-form" @submit.prevent="handleLogin">
            <div class="input-group">
              <md-outlined-text-field :value="account" @input="account = $event.target.value" label="Stall ID / Phone" required style="width:100%;">
                <span class="material-symbols-outlined" slot="leading-icon">person</span>
              </md-outlined-text-field>
            </div>

            <div v-if="loginMode === 'password'" class="input-group">
              <md-outlined-text-field :value="password" @input="password = $event.target.value" :type="showPwd ? 'text' : 'password'" label="Password" required style="width:100%;">
                <span class="material-symbols-outlined" slot="leading-icon">lock</span>
                <span class="material-symbols-outlined eye-toggle clickable" slot="trailing-icon" @click="showPwd = !showPwd">{{ showPwd ? 'visibility_off' : 'visibility' }}</span>
              </md-outlined-text-field>
            </div>

            <div v-else class="input-group">
              <div class="sms-code-row">
                <md-outlined-text-field :value="code" @input="code = $event.target.value.replace(/[^0-9]/g, '').slice(0, 6)" label="Verification Code" style="flex-grow:1;" placeholder="6-digit code">
                  <span class="material-symbols-outlined" slot="leading-icon">sms</span>
                </md-outlined-text-field>
                <button type="button" class="btn-send-code" :disabled="countdown.isRunning.value" @click="sendCodeHandler">{{ countdown.isRunning.value ? `${countdown.countdown.value}s` : 'Send Code' }}</button>
              </div>
            </div>

            <button type="submit" class="btn-login-submit" :disabled="loading">{{ loading ? 'Signing in...' : 'Sign In' }}</button>
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
  if (!account.value.trim()) { toast.show('Please enter your phone first!', 'warning'); return }
  try {
    await sendCode(account.value.trim())
    countdown.start()
    toast.show('Verification code sent! Code: 123456', 'sms')
  } catch (e) { toast.show(e.message || 'Failed to send code', 'error') }
}

async function handleLogin() {
  if (!account.value.trim()) { toast.show('Please enter your account!', 'warning'); return }
  loading.value = true
  try {
    if (loginMode.value === 'password') {
      if (!password.value) { toast.show('Please enter your password!', 'warning'); loading.value = false; return }
      await authStore.passwordLogin(account.value.trim(), password.value)
    } else {
      if (!code.value || !/^\d{6}$/.test(code.value)) { toast.show('Code must be 6 digits!', 'warning'); loading.value = false; return }
      await authStore.codeLogin(account.value.trim(), code.value)
    }
    toast.show('Sign in successful!', 'check_circle')
    router.replace('/orders')
  } catch (e) {
    toast.show(e.message || 'Login failed', 'error')
  } finally { loading.value = false }
}
</script>
