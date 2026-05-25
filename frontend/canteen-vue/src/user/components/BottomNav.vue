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
      <span class="nav-label">Menu</span>
    </div>
    <div
      class="nav-tab"
      :class="{ active: currentTab === 'orders' }"
      @click="goTo('orders')"
    >
      <div class="nav-pill">
        <span class="material-symbols-outlined">receipt</span>
      </div>
      <span class="nav-label">Orders</span>
    </div>
    <div
      class="nav-tab"
      :class="{ active: currentTab === 'profile' }"
      @click="goTo('profile')"
    >
      <div class="nav-pill">
        <span class="material-symbols-outlined">person</span>
      </div>
      <span class="nav-label">Profile</span>
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
