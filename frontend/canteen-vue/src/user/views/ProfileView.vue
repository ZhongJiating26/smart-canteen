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
        <h2 class="profile-name">{{ authStore.nickname || '用户' }}</h2>
        <div class="profile-info-row">
          <span class="material-symbols-outlined profile-info-icon">phone</span>
          <span class="profile-info-text">{{ authStore.userInfo?.phone || authStore.username || '-' }}</span>
        </div>
        <div class="profile-info-row">
          <span class="material-symbols-outlined profile-info-icon">badge</span>
          <span class="profile-info-text">学工号: {{ authStore.userInfo?.studentNo || '无' }}</span>
        </div>
        <button type="button" class="profile-edit-btn" @click="$router.push('/profile/edit')">
          <span class="material-symbols-outlined">edit</span>
          <span>编辑资料</span>
        </button>
      </div>

      <div class="profile-stats-row">
        <div class="profile-stat-card">
          <span class="profile-stat-number">{{ stats.orderCount || 0 }}</span>
          <span class="profile-stat-label">订单数</span>
        </div>
        <div class="profile-stat-card">
          <span class="profile-stat-number">￥{{ Number(stats.totalSpend || 0).toFixed(2) }}</span>
          <span class="profile-stat-label">消费额</span>
        </div>
      </div>

      <div class="settings-card">
        <h3 class="settings-card-title">账户设置</h3>
        <div class="settings-list">
          <div class="settings-item clickable" @click="toast.show('设置功能即将推出！', 'settings')">
            <div class="settings-item-left">
              <div class="settings-icon-wrapper"><span class="material-symbols-outlined">settings</span></div>
              <div class="settings-item-text">
                <span class="settings-item-title">系统设置</span>
                <span class="settings-item-subtitle">应用偏好及隐私设置</span>
              </div>
            </div>
            <span class="material-symbols-outlined settings-chevron">chevron_right</span>
          </div>
          <div class="settings-item clickable" @click="toast.show('帮助与支持功能即将推出！', 'help')">
            <div class="settings-item-left">
              <div class="settings-icon-wrapper"><span class="material-symbols-outlined">help</span></div>
              <div class="settings-item-text">
                <span class="settings-item-title">帮助与支持</span>
                <span class="settings-item-subtitle">常见问题与联系客服</span>
              </div>
            </div>
            <span class="material-symbols-outlined settings-chevron">chevron_right</span>
          </div>
          <div class="settings-item clickable" @click="showLogout = true">
            <div class="settings-item-left">
              <div class="settings-icon-wrapper logout-icon-wrapper"><span class="material-symbols-outlined">logout</span></div>
              <div class="settings-item-text">
                <span class="settings-item-title logout-title">退出登录</span>
                <span class="settings-item-subtitle">退出您当前的账号登录</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Logout Dialog -->
    <div v-if="showLogout" class="dialog-scrim show" @click="showLogout = false"></div>
    <div v-if="showLogout" class="m3-dialog show">
      <h3 class="m3-dialog-title">确定退出？</h3>
      <p class="m3-dialog-content">您确定要退出当前账户的登录吗？</p>
      <div class="m3-dialog-actions">
        <button type="button" class="m3-dialog-btn btn-text" @click="showLogout = false">取消</button>
        <button type="button" class="m3-dialog-btn btn-filled" @click="handleLogout">确定退出</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'
import { getMyStatistics } from '@common/api/orderApi'

const authStore = useAuthStore()
const router = useRouter()
const toast = inject('toast')
const showLogout = ref(false)
const stats = ref({ orderCount: 0, totalSpend: 0 })

async function fetchStats() {
  try {
    const data = await getMyStatistics()
    if (data) {
      stats.value = data
    }
  } catch (e) {
    // ignore
  }
}

async function handleLogout() {
  showLogout.value = false
  await authStore.doLogout()
  toast.show('已成功退出登录！', 'check_circle')
  router.replace('/login')
}

onMounted(async () => {
  try {
    await authStore.fetchProfile()
  } catch (e) {
    // ignore
  }
  fetchStats()
})
</script>
