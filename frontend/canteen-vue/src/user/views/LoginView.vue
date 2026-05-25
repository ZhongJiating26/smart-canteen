<template>
  <div class="mobile-screen state-center" id="login-view">
    <header class="screen-header">
      <span class="brand-title">Campus Eats</span>
    </header>

    <div class="screen-content">
      <div class="form-card">
        <h1 class="card-title">Sign In</h1>
        <p class="card-subtitle">Access your meal plans and orders</p>

        <div class="tab-bar">
          <div class="tab-item" :class="{ active: loginMode === 'password' }" @click="loginMode = 'password'">Password</div>
          <div class="tab-item" :class="{ active: loginMode === 'code' }" @click="loginMode = 'code'">Code</div>
        </div>

        <form class="auth-form" @submit.prevent="handleLogin">
          <md-outlined-text-field
            :value="account"
            @input="account = $event.target.value"
            label="Account (Phone/ID)"
            required
          >
            <span slot="trailing-icon" class="material-symbols-outlined">account_circle</span>
          </md-outlined-text-field>

          <md-outlined-text-field
            v-if="loginMode === 'password'"
            :value="password"
            @input="password = $event.target.value"
            :type="showPwd ? 'text' : 'password'"
            label="Password"
            required
          >
            <span slot="trailing-icon" class="material-symbols-outlined eye-toggle" @click="showPwd = !showPwd">{{ showPwd ? 'visibility_off' : 'visibility' }}</span>
          </md-outlined-text-field>

          <div v-if="loginMode === 'code'" class="verification-row">
            <md-outlined-text-field
              :value="code"
              @input="code = $event.target.value.replace(/[^0-9]/g, '').slice(0, 6)"
              label="Verification Code"
              class="flex-grow"
            >
              <span slot="leading-icon" class="material-symbols-outlined">gpp_good</span>
            </md-outlined-text-field>
            <button type="button" class="btn-send-code" :disabled="countdown.isRunning.value" @click="sendVerifyCode">
              {{ countdown.isRunning.value ? `${countdown.countdown.value}s` : 'Send Code' }}
            </button>
          </div>

          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? 'Loading...' : 'Login' }}
          </button>
        </form>

        <div class="form-links">
          <a href="#" class="link-item" @click.prevent="showToast('Password reset feature coming soon', 'info')">Forgot Password?</a>
          <span class="link-text">
            New user? <router-link to="/register" class="link-item bold">Register</router-link>
          </span>
        </div>
      </div>
    </div>

    <footer class="screen-footer">
      <p>© 2024 Campus Eats Ecosystem. All rights reserved.</p>
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
    showToast('Please enter your phone number first!', 'warning')
    return
  }
  try {
    await sendCode(phone)
    countdown.start()
    showToast('Verification code sent! Code: 123456', 'sms')
  } catch (e) {
    showToast(e.message || 'Failed to send code', 'error')
  }
}

async function handleLogin() {
  const phone = account.value.trim()
  if (!phone) {
    showToast('Please enter your Account details!', 'warning')
    return
  }

  loading.value = true
  try {
    if (loginMode.value === 'password') {
      if (!password.value) {
        showToast('Please enter your password!', 'warning')
        loading.value = false
        return
      }
      await authStore.passwordLogin(phone, password.value)
    } else {
      if (!code.value || !/^\d{6}$/.test(code.value)) {
        showToast('Verification code must be 6 digits!', 'warning')
        loading.value = false
        return
      }
      await authStore.codeLogin(phone, code.value)
    }
    showToast('Sign in successful! Welcome to Campus Eats.', 'check_circle')
    router.replace('/menu')
  } catch (e) {
    showToast(e.message || 'Login failed', 'error')
  } finally {
    loading.value = false
  }
}
</script>
