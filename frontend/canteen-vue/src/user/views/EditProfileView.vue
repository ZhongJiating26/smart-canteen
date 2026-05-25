<template>
  <div class="mobile-screen state-center" id="edit-profile-view">
    <header class="screen-header justify-between">
      <span class="material-symbols-outlined header-icon clickable" style="color:var(--color-brand-rust);font-size:1.6rem;" @click="$router.push('/profile')">arrow_back</span>
      <span class="screen-title" style="font-weight:700;">Edit Profile</span>
      <span style="width:24px;"></span>
    </header>

    <div class="edit-profile-scroll-container">
      <div class="edit-avatar-section">
        <input type="file" accept="image/*" style="display:none;" ref="fileInput" @change="onFileChange($event)" />
        <div class="edit-avatar-wrapper clickable" @click="fileInput?.click()">
          <img class="edit-avatar" :src="avatarSrc" alt="Profile Avatar" />
          <div class="edit-avatar-badge">
            <span class="material-symbols-outlined">camera_alt</span>
          </div>
        </div>
        <span class="edit-avatar-label">Tap to change avatar</span>
      </div>

      <div class="edit-profile-card">
        <div class="edit-form-group">
          <md-outlined-text-field :value="nickname" @input="nickname = $event.target.value" label="Nickname" required style="width:100%;"></md-outlined-text-field>
        </div>
        <div class="edit-form-group">
          <md-outlined-text-field :value="studentNo" @input="studentNo = $event.target.value" label="Student/Staff ID" style="width:100%;"></md-outlined-text-field>
        </div>
        <div class="edit-form-group">
          <md-outlined-text-field :value="phone" @input="phoneChanged = $event.target.value !== originalPhone; phone = $event.target.value" label="Phone Number" type="tel" required style="width:100%;"></md-outlined-text-field>
        </div>

        <div v-if="phoneChanged" class="verification-row edit-code-row" style="margin-top:12px;display:flex;align-items:center;gap:12px;">
          <md-outlined-text-field :value="verifyCode" @input="verifyCode = $event.target.value.replace(/[^0-9]/g, '').slice(0, 6)" label="Verification Code" style="flex-grow:1;" placeholder="6-digit code"></md-outlined-text-field>
          <button type="button" class="btn-send-code inline-btn" :disabled="countdown.isRunning.value" @click="sendCodeHandler" style="height:56px;margin:0;">{{ countdown.isRunning.value ? `${countdown.countdown.value}s` : 'Send Code' }}</button>
        </div>
      </div>

      <div class="edit-profile-actions">
        <button type="button" class="btn-primary" :disabled="saving" @click="saveProfile">{{ saving ? 'Saving...' : 'Save Changes' }}</button>
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
const countdown = useCountdown()

const nickname = ref('')
const studentNo = ref('')
const phone = ref('')
const originalPhone = ref('')
const phoneChanged = ref(false)
const verifyCode = ref('')
const avatarSrc = ref('/images/profile_avatar.png')
const saving = ref(false)
const fileInput = ref(null)

function onFileChange(e) {
  const file = e.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (event) => { avatarSrc.value = event.target.result }
    reader.readAsDataURL(file)
  }
}

async function sendCodeHandler() {
  if (!phone.value.trim()) {
    toast.show('Please enter your Phone Number first!', 'warning')
    return
  }
  try {
    await sendCode(phone.value.trim())
    countdown.start()
    toast.show('Verification code sent! Code: 123456', 'sms')
  } catch (e) {
    toast.show(e.message || 'Failed to send code', 'error')
  }
}

async function saveProfile() {
  if (!nickname.value.trim()) {
    toast.show('Nickname cannot be empty!', 'warning')
    return
  }
  if (!phone.value.trim()) {
    toast.show('Phone number cannot be empty!', 'warning')
    return
  }
  if (phoneChanged.value && !verifyCode.value.trim()) {
    toast.show('Please enter the verification code!', 'warning')
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
      data.newPhoneVerifyCode = verifyCode.value.trim()
    }
    if (studentNo.value.trim()) {
      data.studentNo = studentNo.value.trim()
    }
    await authStore.updateProfile(data)
    toast.show('Profile updated successfully!', 'check_circle')
    router.push('/profile')
  } catch (e) {
    toast.show(e.message || 'Failed to save profile', 'error')
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
      avatarSrc.value = u.avatar || '/images/profile_avatar.png'
    }
  } catch (e) { /* ignore */ }
})
</script>
