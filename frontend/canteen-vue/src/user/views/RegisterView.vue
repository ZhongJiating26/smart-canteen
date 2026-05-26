<template>
  <div class="mobile-screen state-center" id="register-view">
    <header class="screen-header justify-between">
      <span class="header-icon clickable" @click="$router.push('/login')">
        <span class="material-symbols-outlined">arrow_back</span>
      </span>
      <span class="screen-title">注册</span>
      <div style="width: 36px;"></div>
    </header>

    <div class="screen-content scrollable">
      <div class="register-banner">
        <p class="register-subtitle">创建账户，开启您的校园美食之旅。</p>
      </div>

      <div class="form-card py-24">
        <form class="auth-form" @submit.prevent="handleRegister">
          <div class="form-section">
            <h3 class="section-header">身份验证</h3>

            <md-outlined-text-field
              :value="phone"
              @input="phone = $event.target.value"
              label="手机号码 *"
              placeholder="例如 13800000000"
              type="tel"
              required
            >
              <span slot="leading-icon" class="material-symbols-outlined">phone</span>
            </md-outlined-text-field>

            <div class="verification-row">
              <md-outlined-text-field
                :value="verifyCode"
                @input="verifyCode = $event.target.value.replace(/[^0-9]/g, '').slice(0, 6)"
                label="验证码 *"
                placeholder="6位验证码"
                required
                class="flex-grow"
              >
                <span slot="leading-icon" class="material-symbols-outlined">gpp_good</span>
              </md-outlined-text-field>
              <button type="button" class="btn-send-code inline-btn" :disabled="countdown.isRunning.value" @click="sendCodeHandler">
                {{ countdown.isRunning.value ? `${countdown.countdown.value}s` : '发送验证码' }}
              </button>
            </div>

            <md-outlined-text-field
              :value="studentNo"
              @input="studentNo = $event.target.value"
              label="学号/工号 (选填)"
              placeholder="例如 20230001"
            >
              <span slot="leading-icon" class="material-symbols-outlined">badge</span>
            </md-outlined-text-field>
          </div>

          <div class="form-section">
            <h3 class="section-header">个人信息</h3>
            <md-outlined-text-field
              :value="nickname"
              @input="nickname = $event.target.value"
              label="昵称 (选填)"
              placeholder="我们该如何称呼您？"
            >
              <span slot="leading-icon" class="material-symbols-outlined">mood</span>
            </md-outlined-text-field>
          </div>

          <div class="form-section">
            <h3 class="section-header">安全设置</h3>
            <md-outlined-text-field
              :value="password"
              @input="password = $event.target.value"
              :type="showPwd ? 'text' : 'password'"
              label="密码 *"
              placeholder="最少 8 位字符"
              required
            >
              <span slot="leading-icon" class="material-symbols-outlined">lock</span>
              <span slot="trailing-icon" class="material-symbols-outlined eye-toggle" @click="showPwd = !showPwd">{{ showPwd ? 'visibility_off' : 'visibility' }}</span>
            </md-outlined-text-field>

            <md-outlined-text-field
              :value="confirmPassword"
              @input="confirmPassword = $event.target.value"
              :type="showConfirmPwd ? 'text' : 'password'"
              label="确认密码 *"
              placeholder="请再次输入密码"
              required
            >
              <span slot="leading-icon" class="material-symbols-outlined">lock</span>
              <span slot="trailing-icon" class="material-symbols-outlined eye-toggle" @click="showConfirmPwd = !showConfirmPwd">{{ showConfirmPwd ? 'visibility_off' : 'visibility' }}</span>
            </md-outlined-text-field>
          </div>

          <button type="submit" class="btn-primary mt-12" :disabled="loading">
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>

        <p class="agreement-text">
          点击注册即表示您同意我们的<a href="#" class="accent-link">服务条款</a>和<a href="#" class="accent-link">隐私政策</a>。
        </p>
      </div>

      <div class="redirect-login">
        <span>已有账号？ <router-link to="/login" class="accent-link bold">立即登录</router-link></span>
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
    showToast('请先输入手机号码！', 'warning')
    return
  }
  try {
    await sendCode(p)
    countdown.start()
    showToast('验证码已发送！验证码：123456', 'sms')
  } catch (e) {
    showToast(e.message || '发送验证码失败', 'error')
  }
}

async function handleRegister() {
  const p = phone.value.trim()
  const code = verifyCode.value.trim()

  if (!p || !code || !password.value || !confirmPassword.value) {
    showToast('请填写所有必填字段！', 'warning')
    return
  }
  if (!/^\d{6}$/.test(code)) {
    showToast('请输入有效的6位验证码！', 'warning')
    return
  }
  if (password.value.length < 8) {
    showToast('密码长度必须至少为8位！', 'warning')
    return
  }
  if (password.value !== confirmPassword.value) {
    showToast('两次输入的密码不一致！', 'warning')
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
    showToast('注册成功！正在跳转至登录页面...', 'check_circle')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (e) {
    showToast(e.message || '注册失败', 'error')
  } finally {
    loading.value = false
  }
}
</script>
