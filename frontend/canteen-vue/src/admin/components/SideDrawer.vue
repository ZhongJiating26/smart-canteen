<template>
  <nav class="side-drawer" :class="{ collapsed: !isExpanded, expanded: isExpanded }">
    <div class="drawer-header">
      <button type="button" class="drawer-toggle-btn" @click="isExpanded = !isExpanded">
        <span class="material-symbols-outlined">menu</span>
      </button>
      <span class="drawer-brand-name">Campus Eats</span>
    </div>

    <div class="drawer-body">
      <div
        v-for="item in navItems"
        :key="item.view"
        class="nav-item"
        :class="{ active: currentView === item.view }"
        @click="goTo(item.view)"
      >
        <div class="nav-icon-wrapper">
          <span class="material-symbols-outlined">{{ item.icon }}</span>
        </div>
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </div>
    <div class="drawer-footer">
      <div class="nav-item logout-item" @click="logout">
        <div class="nav-icon-wrapper">
          <span class="material-symbols-outlined text-error">logout</span>
        </div>
        <span class="nav-label text-error">退出登录</span>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isExpanded = ref(true)

const navItems = [
  { view: 'Dashboard', label: '仪表盘', icon: 'dashboard' },
  { view: 'UserManagement', label: '用户管理', icon: 'group' },
  { view: 'MerchantManagement', label: '商家窗口管理', icon: 'storefront' },
  { view: 'SystemConfig', label: '系统配置', icon: 'settings' }
]

const currentView = computed(() => route.name)

function goTo(view) {
  const paths = {
    Dashboard: '/dashboard',
    UserManagement: '/users',
    MerchantManagement: '/merchants',
    SystemConfig: '/config'
  }
  router.push(paths[view])
}

async function logout() {
  await authStore.doLogout()
  router.replace('/login')
}
</script>
