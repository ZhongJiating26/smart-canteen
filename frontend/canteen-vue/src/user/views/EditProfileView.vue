<template>
  <div class="mobile-screen state-center" id="edit-profile-view">
    <header class="screen-header justify-between">
      <span class="header-icon clickable" style="color:var(--color-brand-rust);" @click="$router.push('/profile')">
        <span class="material-symbols-outlined">arrow_back</span>
      </span>
      <span class="screen-title" style="font-weight:700;">编辑个人资料</span>
      <span style="width: 36px;"></span>
    </header>

    <div class="edit-profile-scroll-container">
      <div class="edit-avatar-section">
        <input type="file" accept="image/*" style="display:none;" ref="fileInput" @change="onFileChange($event)" />
        <div class="edit-avatar-wrapper clickable" @click="fileInput?.click()">
          <img class="edit-avatar" :src="avatarSrc" alt="用户头像" />
          <div class="edit-avatar-badge">
            <span class="material-symbols-outlined">camera_alt</span>
          </div>
        </div>
        <span class="edit-avatar-label">点击更换头像</span>
      </div>

      <div class="edit-profile-card">
        <div class="edit-form-group">
          <md-outlined-text-field :value="nickname" @input="nickname = $event.target.value" label="昵称" required style="width:100%;"></md-outlined-text-field>
        </div>

        <div class="edit-form-group">
          <md-outlined-text-field :value="studentNo" @input="studentNoChanged = $event.target.value !== originalStudentNo; studentNo = $event.target.value" label="学工号" style="width:100%;"></md-outlined-text-field>
        </div>

        <div v-if="studentNoChanged" class="verification-row edit-code-row" style="margin-top:12px;display:flex;align-items:center;gap:12px;margin-bottom:16px;">
          <md-outlined-text-field :value="studentNoVerifyCode" @input="studentNoVerifyCode = $event.target.value.replace(/[^0-9]/g, '').slice(0, 6)" label="学工号修改验证码" style="flex-grow:1;" placeholder="验证码已发送至当前手机号"></md-outlined-text-field>
          <button type="button" class="btn-send-code inline-btn" :disabled="studentNoCountdown.isRunning.value" @click="sendCodeToCurrentPhone" style="height:56px;margin:0;">{{ studentNoCountdown.isRunning.value ? `${studentNoCountdown.countdown.value}s` : '发送验证码' }}</button>
        </div>

        <div class="edit-form-group">
          <md-outlined-text-field :value="phone" @input="phoneChanged = $event.target.value !== originalPhone; phone = $event.target.value" label="手机号" type="tel" required style="width:100%;"></md-outlined-text-field>
        </div>

        <div v-if="phoneChanged" class="phone-verification-section" style="margin-top:12px;">
          <div class="verification-row edit-code-row" style="display:flex;align-items:center;gap:12px;margin-bottom:12px;">
            <md-outlined-text-field :value="oldPhoneVerifyCode" @input="oldPhoneVerifyCode = $event.target.value.replace(/[^0-9]/g, '').slice(0, 6)" label="原手机验证码" style="flex-grow:1;" placeholder="验证码已发送至原手机号"></md-outlined-text-field>
            <button type="button" class="btn-send-code inline-btn" :disabled="oldPhoneCountdown.isRunning.value" @click="sendCodeToOldPhone" style="height:56px;margin:0;">{{ oldPhoneCountdown.isRunning.value ? `${oldPhoneCountdown.countdown.value}s` : '发送验证码' }}</button>
          </div>
          <div class="verification-row edit-code-row" style="display:flex;align-items:center;gap:12px;">
            <md-outlined-text-field :value="newPhoneVerifyCode" @input="newPhoneVerifyCode = $event.target.value.replace(/[^0-9]/g, '').slice(0, 6)" label="新手机验证码" style="flex-grow:1;" placeholder="验证码已发送至新手机号"></md-outlined-text-field>
            <button type="button" class="btn-send-code inline-btn" :disabled="newPhoneCountdown.isRunning.value" @click="sendCodeToNewPhone" style="height:56px;margin:0;">{{ newPhoneCountdown.isRunning.value ? `${newPhoneCountdown.countdown.value}s` : '发送验证码' }}</button>
          </div>
        </div>
      </div>

      <div class="edit-profile-actions">
        <button type="button" class="btn-primary" :disabled="saving" @click="saveProfile">{{ saving ? '保存中...' : '保存修改' }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'
import { useCountdown } from '@common/composables/useCountdown'
import { sendCode } from '@common/api/userApi'

const authStore = useAuthStore()
const router = useRouter()
const toast = inject('toast')

const nickname = ref('')
const studentNo = ref('')
const originalStudentNo = ref('')
const studentNoChanged = ref(false)
const studentNoVerifyCode = ref('')

const phone = ref('')
const originalPhone = ref('')
const phoneChanged = ref(false)
const oldPhoneVerifyCode = ref('')
const newPhoneVerifyCode = ref('')

const avatarSrc = ref('/images/profile_avatar.png')
const saving = ref(false)
const fileInput = ref(null)

const oldPhoneCountdown = useCountdown()
const newPhoneCountdown = useCountdown()
const studentNoCountdown = useCountdown()

function onFileChange(e) {
  const file = e.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (event) => { avatarSrc.value = event.target.result }
    reader.readAsDataURL(file)
  }
}

async function sendCodeToCurrentPhone() {
  try {
    await sendCode(originalPhone.value)
    studentNoCountdown.start()
    toast.show('验证码已发送至当前手机！验证码为：123456', 'sms')
  } catch (e) {
    toast.show(e.message || '发送验证码失败', 'error')
  }
}

async function sendCodeToOldPhone() {
  try {
    await sendCode(originalPhone.value)
    oldPhoneCountdown.start()
    toast.show('验证码已发送至原手机！验证码为：123456', 'sms')
  } catch (e) {
    toast.show(e.message || '发送验证码失败', 'error')
  }
}

async function sendCodeToNewPhone() {
  if (!phone.value.trim()) {
    toast.show('请先输入您的新手机号！', 'warning')
    return
  }
  try {
    await sendCode(phone.value.trim())
    newPhoneCountdown.start()
    toast.show('验证码已发送至新手机！验证码为：123456', 'sms')
  } catch (e) {
    toast.show(e.message || '发送验证码失败', 'error')
  }
}

async function saveProfile() {
  if (!nickname.value.trim()) {
    toast.show('昵称不能为空！', 'warning')
    return
  }
  if (!phone.value.trim()) {
    toast.show('手机号不能为空！', 'warning')
    return
  }
  if (phoneChanged.value && (!oldPhoneVerifyCode.value.trim() || !newPhoneVerifyCode.value.trim())) {
    toast.show('请输入原手机号和新手机号的验证码！', 'warning')
    return
  }
  if (studentNoChanged.value && !studentNoVerifyCode.value.trim()) {
    toast.show('请输入修改学工号的验证码！', 'warning')
    return
  }

  saving.value = true
  try {
    const data = {
      nickname: nickname.value.trim(),
      avatar: avatarSrc.value
    }
    if (phoneChanged.value) {
      data.phone = phone.value.trim()
      data.oldPhoneVerifyCode = oldPhoneVerifyCode.value.trim()
      data.newPhoneVerifyCode = newPhoneVerifyCode.value.trim()
    }
    if (studentNo.value.trim()) {
      data.studentNo = studentNo.value.trim()
      if (studentNoChanged.value) {
        data.studentNoVerifyCode = studentNoVerifyCode.value.trim()
      }
    }
    await authStore.updateProfile(data)
    toast.show('个人资料更新成功！', 'check_circle')
    router.push('/profile')
  } catch (e) {
    toast.show(e.message || '保存个人资料失败', 'error')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    await authStore.fetchProfile()
    const u = authStore.userInfo
    if (u) {
      nickname.value = u.nickname || ''
      phone.value = u.phone || u.username || ''
      originalPhone.value = phone.value
      studentNo.value = u.studentNo || ''
      originalStudentNo.value = studentNo.value
      avatarSrc.value = u.avatar || '/images/profile_avatar.png'
    }
  } catch (e) { /* ignore */ }
})
</script>
