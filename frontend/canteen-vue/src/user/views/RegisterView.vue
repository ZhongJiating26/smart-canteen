<template>
  <div class="mobile-screen state-center" id="register-view">
    <header class="screen-header justify-between">
      <span class="material-symbols-outlined header-icon clickable" @click="$router.push('/login')">arrow_back</span>
      <span class="screen-title">Register</span>
      <div style="width: 36px;"></div>
    </header>

    <div class="screen-content scrollable">
      <div class="register-banner">
        <p class="register-subtitle">Create an account to start your campus culinary journey.</p>
      </div>

      <div class="form-card py-24">
        <form class="auth-form" @submit.prevent="handleRegister">
          <div class="form-section">
            <h3 class="section-header">IDENTIFICATION</h3>

            <md-outlined-text-field
              :value="phone"
              @input="phone = $event.target.value"
              label="Phone Number *"
              placeholder="e.g. 13800000000"
              type="tel"
              required
            >
              <span slot="leading-icon" class="material-symbols-outlined">phone</span>
            </md-outlined-text-field>

            <div class="verification-row">
              <md-outlined-text-field
                :value="verifyCode"
                @input="verifyCode = $event.target.value.replace(/[^0-9]/g, '').slice(0, 6)"
                label="Verification Code *"
                placeholder="6-digit code"
                required
                class="flex-grow"
              >
                <span slot="leading-icon" class="material-symbols-outlined">gpp_good</span>
              </md-outlined-text-field>
              <button type="button" class="btn-send-code inline-btn" :disabled="countdown.isRunning.value" @click="sendCodeHandler">
                {{ countdown.isRunning.value ? `${countdown.countdown.value}s` : 'Send Code' }}
              </button>
            </div>

            <md-outlined-text-field
              :value="studentNo"
              @input="studentNo = $event.target.value"
              label="Student/Staff ID (Optional)"
              placeholder="e.g. 20230001"
            >
              <span slot="leading-icon" class="material-symbols-outlined">badge</span>
            </md-outlined-text-field>
          </div>

          <div class="form-section">
            <h3 class="section-header">PROFILE</h3>
            <md-outlined-text-field
              :value="nickname"
              @input="nickname = $event.target.value"
              label="Nickname (Optional)"
              placeholder="What should we call you?"
            >
              <span slot="leading-icon" class="material-symbols-outlined">mood</span>
            </md-outlined-text-field>
          </div>

          <div class="form-section">
            <h3 class="section-header">SECURITY</h3>
            <md-outlined-text-field
              :value="password"
              @input="password = $event.target.value"
              :type="showPwd ? 'text' : 'password'"
              label="Password *"
              placeholder="Min. 8 characters"
              required
            >
              <span slot="leading-icon" class="material-symbols-outlined">lock</span>
              <span slot="trailing-icon" class="material-symbols-outlined eye-toggle" @click="showPwd = !showPwd">{{ showPwd ? 'visibility_off' : 'visibility' }}</span>
            </md-outlined-text-field>

            <md-outlined-text-field
              :value="confirmPassword"
              @input="confirmPassword = $event.target.value"
              :type="showConfirmPwd ? 'text' : 'password'"
              label="Confirm Password *"
              placeholder="Repeat your password"
              required
            >
              <span slot="leading-icon" class="material-symbols-outlined">lock</span>
              <span slot="trailing-icon" class="material-symbols-outlined eye-toggle" @click="showConfirmPwd = !showConfirmPwd">{{ showConfirmPwd ? 'visibility_off' : 'visibility' }}</span>
            </md-outlined-text-field>
          </div>

          <button type="submit" class="btn-primary mt-12" :disabled="loading">
            {{ loading ? 'Registering...' : 'Register' }}
          </button>
        </form>

        <p class="agreement-text">
          By clicking Register, you agree to our <a href="#" class="accent-link">Terms</a> and <a href="#" class="accent-link">Privacy Policy</a>.
        </p>
      </div>

      <div class="redirect-login">
        <span>Already have an account? <router-link to="/login" class="accent-link bold">Log in</router-link></span>
      </div>

      <div class="bottom-food-icons">
        <div class="food-badge-item"><span class="material-symbols-outlined badge-icon">restaurant</span></div>
        <div class="food-badge-item"><span class="material-symbols-outlined badge-icon">local_cafe</span></div>
        <div class="food-badge-item"><span class="material-symbols-outlined badge-icon">lunch_dining</span></div>
        <div class="food-badge-item"><span class="material-symbols-outlined badge-icon">local_pizza</span></div>
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

const phone = ref('')
const verifyCode = ref('')
const studentNo = ref('')
const nickname = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPwd = ref(false)
const showConfirmPwd = ref(false)
const loading = ref(false)

function showToast(msg, icon) {
  toast.show(msg, icon)
}

async function sendCodeHandler() {
  const p = phone.value.trim()
  if (!p) {
    showToast('Please enter your Phone Number first!', 'warning')
    return
  }
  try {
    await sendCode(p)
    countdown.start()
    showToast('Verification code sent! Code: 123456', 'sms')
  } catch (e) {
    showToast(e.message || 'Failed to send code', 'error')
  }
}

async function handleRegister() {
  const p = phone.value.trim()
  const code = verifyCode.value.trim()

  if (!p || !code || !password.value || !confirmPassword.value) {
    showToast('Please fill out all required fields!', 'warning')
    return
  }
  if (!/^\d{6}$/.test(code)) {
    showToast('Please enter a valid 6-digit verification code!', 'warning')
    return
  }
  if (password.value.length < 8) {
    showToast('Password must be at least 8 characters long!', 'warning')
    return
  }
  if (password.value !== confirmPassword.value) {
    showToast('Passwords do not match!', 'warning')
    return
  }

  loading.value = true
  try {
    await authStore.register({
      phone: p,
      studentNo: studentNo.value.trim() || undefined,
      password: password.value,
      payPassword: '123456',
      verifyCode: code,
      nickname: nickname.value.trim() || undefined
    })
    showToast('Registration successful! Redirecting to login...', 'check_circle')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (e) {
    showToast(e.message || 'Registration failed', 'error')
  } finally {
    loading.value = false
  }
}
</script>
