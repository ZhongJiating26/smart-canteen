import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'

const routes = [
  {
    path: '/login',
    name: 'AdminLogin',
    component: () => import('./views/AdminLoginView.vue'),
    meta: { guest: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('./views/DashboardView.vue'),
    meta: { auth: true, role: 'ADMIN' }
  },
  {
    path: '/users',
    name: 'UserManagement',
    component: () => import('./views/UserManagementView.vue'),
    meta: { auth: true, role: 'ADMIN' }
  },
  {
    path: '/merchants',
    name: 'MerchantManagement',
    component: () => import('./views/MerchantManagementView.vue'),
    meta: { auth: true, role: 'ADMIN' }
  },
  {
    path: '/config',
    name: 'SystemConfig',
    component: () => import('./views/SystemConfigView.vue'),
    meta: { auth: true, role: 'ADMIN' }
  },
  {
    path: '/',
    redirect: '/dashboard'
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
