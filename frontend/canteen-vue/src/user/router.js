import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('./views/LoginView.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('./views/RegisterView.vue'),
    meta: { guest: true }
  },
  {
    path: '/menu',
    name: 'Menu',
    component: () => import('./views/MenuView.vue'),
    meta: { title: 'Menu' }
  },
  {
    path: '/dishes/:stallId',
    name: 'Dishes',
    component: () => import('./views/DishesView.vue'),
    props: true
  },
  {
    path: '/dish/:dishId',
    name: 'DishDetail',
    component: () => import('./views/DishDetailView.vue'),
    props: true
  },
  {
    path: '/confirm-order',
    name: 'ConfirmOrder',
    component: () => import('./views/ConfirmOrderView.vue'),
    meta: { auth: true }
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('./views/PaymentView.vue'),
    meta: { auth: true }
  },
  {
    path: '/order/:orderId',
    name: 'OrderDetail',
    component: () => import('./views/OrderDetailView.vue'),
    props: true,
    meta: { auth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('./views/OrdersView.vue'),
    meta: { auth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('./views/ProfileView.vue'),
    meta: { auth: true }
  },
  {
    path: '/profile/edit',
    name: 'EditProfile',
    component: () => import('./views/EditProfileView.vue'),
    meta: { auth: true }
  },
  {
    path: '/',
    redirect: '/menu'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.auth && !authStore.isLoggedIn) {
    return next('/login')
  }
  if (to.meta.guest && authStore.isLoggedIn) {
    return next('/menu')
  }
  next()
})

export default router
