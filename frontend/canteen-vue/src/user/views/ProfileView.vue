<template>
  <div class="mobile-screen state-center" id="profile-view">
    <header class="screen-header">
      <span class="brand-title">Campus Eats</span>
    </header>

    <div class="profile-scroll-container">
      <div class="profile-card">
        <div class="profile-avatar-wrapper">
          <img class="profile-avatar" :src="authStore.userInfo?.avatar || '/images/profile_avatar.png'" alt="Profile" />
          <div class="profile-verified-badge">
            <span class="material-symbols-outlined">check</span>
          </div>
        </div>
        <h2 class="profile-name">{{ authStore.nickname || 'User' }}</h2>
        <div class="profile-info-row">
          <span class="material-symbols-outlined profile-info-icon">phone</span>
          <span class="profile-info-text">{{ authStore.userInfo?.phone || authStore.username || '-' }}</span>
        </div>
        <div class="profile-info-row">
          <span class="material-symbols-outlined profile-info-icon">badge</span>
          <span class="profile-info-text">ID: {{ authStore.userInfo?.studentNo || 'N/A' }}</span>
        </div>
        <button type="button" class="profile-edit-btn" @click="$router.push('/profile/edit')">
          <span class="material-symbols-outlined">edit</span>
          <span>Edit Profile</span>
        </button>
      </div>

      <div class="profile-stats-row">
        <div class="profile-stat-card">
          <span class="profile-stat-number">12</span>
          <span class="profile-stat-label">Orders</span>
        </div>
        <div class="profile-stat-card">
          <span class="profile-stat-number">$142</span>
          <span class="profile-stat-label">Spent</span>
        </div>
      </div>

      <div class="settings-card">
        <h3 class="settings-card-title">Account Settings</h3>
        <div class="settings-list">
          <div class="settings-item clickable" @click="toast.show('Settings features coming soon!', 'settings')">
            <div class="settings-item-left">
              <div class="settings-icon-wrapper"><span class="material-symbols-outlined">settings</span></div>
              <div class="settings-item-text">
                <span class="settings-item-title">Settings</span>
                <span class="settings-item-subtitle">App preferences and privacy</span>
              </div>
            </div>
            <span class="material-symbols-outlined settings-chevron">chevron_right</span>
          </div>
          <div class="settings-item clickable" @click="toast.show('Help & Support features coming soon!', 'help')">
            <div class="settings-item-left">
              <div class="settings-icon-wrapper"><span class="material-symbols-outlined">help</span></div>
              <div class="settings-item-text">
                <span class="settings-item-title">Help & Support</span>
                <span class="settings-item-subtitle">FAQs and contact support</span>
              </div>
            </div>
            <span class="material-symbols-outlined settings-chevron">chevron_right</span>
          </div>
          <div class="settings-item clickable" @click="showLogout = true">
            <div class="settings-item-left">
              <div class="settings-icon-wrapper logout-icon-wrapper"><span class="material-symbols-outlined">logout</span></div>
              <div class="settings-item-text">
                <span class="settings-item-title logout-title">Logout</span>
                <span class="settings-item-subtitle">Sign out of your account</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Logout Dialog -->
    <div v-if="showLogout" class="dialog-scrim show" @click="showLogout = false"></div>
    <div v-if="showLogout" class="m3-dialog show">
      <h3 class="m3-dialog-title">Log out?</h3>
      <p class="m3-dialog-content">Are you sure you want to log out of your account?</p>
      <div class="m3-dialog-actions">
        <button type="button" class="m3-dialog-btn btn-text" @click="showLogout = false">Cancel</button>
        <button type="button" class="m3-dialog-btn btn-filled" @click="handleLogout">Log out</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'

const authStore = useAuthStore()
const router = useRouter()
const toast = inject('toast')
const showLogout = ref(false)

async function handleLogout() {
  showLogout.value = false
  await authStore.doLogout()
  toast.show('Logged out successfully!', 'check_circle')
  router.replace('/login')
}

onMounted(async () => {
  try {
    await authStore.fetchProfile()
  } catch (e) {
    // ignore
  }
})
</script>
