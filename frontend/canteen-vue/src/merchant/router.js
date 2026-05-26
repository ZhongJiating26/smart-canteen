import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'

const routes = [
  {
    path: '/login',
    name: 'MerchantLogin',
    component: () => import('./views/MerchantLoginView.vue'),
    meta: { guest: true }
  },
  {
    path: '/orders',
    name: 'OrderKanban',
    component: () => import('./views/OrderKanbanView.vue'),
    meta: { auth: true, role: 'MERCHANT' }
  },
  {
    path: '/dishes',
    name: 'DishManagement',
    component: () => import('./views/DishManagementView.vue'),
    meta: { auth: true, role: 'MERCHANT' }
  },
  {
    path: '/daily-menu',
    name: 'DailyMenu',
    component: () => import('./views/DailyMenuView.vue'),
    meta: { auth: true, role: 'MERCHANT' }
  },
  {
    path: '/history',
    name: 'OrderHistory',
    component: () => import('./views/OrderHistoryView.vue'),
    meta: { auth: true, role: 'MERCHANT' }
  },
  {
    path: '/',
    redirect: '/orders'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.auth && !authStore.isLoggedIn) {
    return next('/login')
  }

  if (to.meta.auth && !authStore.role) {
    try {
      await authStore.fetchProfile()
    } catch (e) {
      await authStore.doLogout()
      return next('/login')
    }
  }

  if (to.meta.role && authStore.role !== to.meta.role) {
    await authStore.doLogout()
    return next('/login')
  }
  if (to.meta.guest && authStore.isLoggedIn) {
    return next('/')
  }
  next()
})

export default router
