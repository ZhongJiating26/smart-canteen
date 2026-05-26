<template>
  <div v-if="visible" class="bottom-nav" id="global-bottom-nav">
    <div
      class="nav-tab"
      :class="{ active: currentTab === 'menu' }"
      @click="goTo('menu')"
    >
      <div class="nav-pill">
        <span class="material-symbols-outlined">restaurant</span>
      </div>
      <span class="nav-label">点餐</span>
    </div>
    <div
      class="nav-tab"
      :class="{ active: currentTab === 'orders' }"
      @click="goTo('orders')"
    >
      <div class="nav-pill">
        <span class="material-symbols-outlined">receipt</span>
      </div>
      <span class="nav-label">订单</span>
    </div>
    <div
      class="nav-tab"
      :class="{ active: currentTab === 'profile' }"
      @click="goTo('profile')"
    >
      <div class="nav-pill">
        <span class="material-symbols-outlined">person</span>
      </div>
      <span class="nav-label">我的</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const currentTab = computed(() => {
  const path = route.path
  if (path.startsWith('/order') || path === '/orders') return 'orders'
  if (path.startsWith('/profile')) return 'profile'
  return 'menu'
})

const hideOnRoutes = ['/login', '/register', '/confirm-order', '/payment']
const visible = computed(() => !hideOnRoutes.includes(route.path) && !route.path.startsWith('/order/'))

function goTo(tab) {
  const paths = { menu: '/menu', orders: '/orders', profile: '/profile' }
  router.push(paths[tab])
}
</script>
