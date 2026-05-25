<template>
  <nav class="nav-rail collapsed" id="merchant-nav-rail" :class="{ expanded: isExpanded }">
    <div class="rail-header">
      <button type="button" class="rail-toggle-btn" @click="isExpanded = !isExpanded">
        <span class="material-symbols-outlined">menu</span>
      </button>
      <span class="rail-brand-name">Campus Eats</span>
    </div>

    <div class="rail-body">
      <div
        v-for="item in navItems"
        :key="item.view"
        class="rail-item"
        :class="{ active: currentView === item.view }"
        @click="goTo(item.view)"
      >
        <div class="rail-icon-wrapper">
          <span class="material-symbols-outlined">{{ item.icon }}</span>
        </div>
        <span class="rail-label">{{ item.label }}</span>
        <span v-if="item.badge && pendingCount > 0" class="badge-dot show"></span>
      </div>
    </div>

    <div class="rail-footer">
      <div class="rail-item logout-item" @click="handleLogout">
        <div class="rail-icon-wrapper"><span class="material-symbols-outlined">logout</span></div>
        <span class="rail-label" style="color:var(--color-brand-rust);">Logout</span>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = inject('toast')
const isExpanded = ref(false)
const pendingCount = inject('pendingBadge', ref(0))

const navItems = [
  { view: 'OrderKanban', label: '订单处理', icon: 'pending_actions', badge: true },
  { view: 'DishManagement', label: '菜品管理', icon: 'restaurant_menu' },
  { view: 'DailyMenu', label: '每日菜单', icon: 'calendar_today' },
  { view: 'OrderHistory', label: '历史订单', icon: 'history' },
  { view: 'StoreConfig', label: '店铺信息', icon: 'store' }
]

const currentView = computed(() => route.name)

function goTo(view) {
  const pathMap = {
    OrderKanban: '/orders',
    DishManagement: '/dishes',
    DailyMenu: '/daily-menu',
    OrderHistory: '/history',
    StoreConfig: '/store'
  }
  router.push(pathMap[view])
}

async function handleLogout() {
  await authStore.doLogout()
  toast.show('Logged out!', 'check_circle')
  router.push('/login')
}

defineExpose({ pendingCount })
</script>
