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
        <span v-if="item.view === 'AbnormalOrders' && abnormalCount" class="badge-dot-count">{{ abnormalCount }}</span>
      </div>
    </div>
    <div class="drawer-footer">
      <div class="nav-item logout-item" @click="logout">
        <div class="nav-icon-wrapper">
          <span class="material-symbols-outlined text-error">logout</span>
        </div>
        <span class="nav-label text-error">Logout</span>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'
import request from '@common/api/request'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isExpanded = ref(true)
const abnormalCount = ref(0)

const navItems = [
  { view: 'Dashboard', label: '仪表盘', icon: 'dashboard' },
  { view: 'UserManagement', label: '用户管理', icon: 'group' },
  { view: 'MerchantManagement', label: '商家窗口管理', icon: 'storefront' },
  { view: 'SystemConfig', label: '系统配置', icon: 'settings' },
  { view: 'OperationLogs', label: '操作日志', icon: 'list_alt' },
  { view: 'AbnormalOrders', label: '异常订单处理', icon: 'warning' }
]

const currentView = computed(() => route.name)

function goTo(view) {
  const paths = {
    Dashboard: '/dashboard',
    UserManagement: '/users',
    MerchantManagement: '/merchants',
    SystemConfig: '/config',
    OperationLogs: '/logs',
    AbnormalOrders: '/abnormal'
  }
  router.push(paths[view])
}

async function logout() {
  await authStore.doLogout()
  router.replace('/login')
}

onMounted(async () => {
  try {
    const data = await request.get('/admin/orders/abnormal')
    abnormalCount.value = (data?.records || data || []).length
  } catch (e) {
    abnormalCount.value = 0
  }
})
</script>
