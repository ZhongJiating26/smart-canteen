<template>
  <div v-if="authStore.isLoggedIn" class="admin-main-container active">
    <SideDrawer />
    <div class="workspace">
      <header class="workspace-header">
        <div class="header-left">
          <h2 class="workspace-title">{{ currentTitle }}</h2>
          <span class="system-status-chip online">系统在线</span>
        </div>
        <div class="header-right">
          <button type="button" class="icon-action-btn" title="刷新数据" @click="refreshKey += 1">
            <span class="material-symbols-outlined">refresh</span>
          </button>
          <div class="admin-profile-badge">
            <span class="material-symbols-outlined profile-avatar-icon">shield_person</span>
            <div class="profile-meta">
              <span class="profile-name">{{ authStore.nickname || '管理员 (根账号)' }}</span>
              <span class="profile-role">系统管理员</span>
            </div>
          </div>
        </div>
      </header>
      <div class="workspace-content">
        <router-view :key="`${route.fullPath}-${refreshKey}`" />
      </div>
    </div>
  </div>
  <router-view v-else v-slot="{ Component }">
    <component :is="Component" />
  </router-view>

  <div v-if="toast.visible.value" class="toast show" id="admin-toast">
    <span class="material-symbols-outlined">{{ toast.iconName.value }}</span>
    <span>{{ toast.message.value }}</span>
  </div>
</template>

<script setup>
import { provide, computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'
import { useToast } from '@common/composables/useToast'
import SideDrawer from './components/SideDrawer.vue'

const authStore = useAuthStore()
const route = useRoute()
const toast = useToast()
const refreshKey = ref(0)

const titleMap = {
  Dashboard: '仪表盘',
  UserManagement: '用户管理',
  MerchantManagement: '商家窗口管理',
  SystemConfig: '系统配置'
}

const currentTitle = computed(() => {
  return titleMap[route.name] || '管理控制台'
})

provide('toast', toast)
</script>
