<template>
  <div class="workspace-sub-view active" id="view-dashboard">
    <div class="metrics-grid">
      <div v-for="card in metricCards" :key="card.label" class="metric-card" :class="{ urgent: card.urgent }">
        <div class="metric-header">
          <span class="metric-title">{{ card.label }}</span>
          <span class="material-symbols-outlined metric-icon" :class="card.color">{{ card.icon }}</span>
        </div>
        <div class="metric-value">{{ card.value }}</div>
        <div class="metric-desc">{{ card.description }}</div>
      </div>
    </div>

    <div class="dashboard-split">
      <div class="dashboard-widget flex-2">
        <h3 class="widget-title">业务微服务状态</h3>
        <div class="status-grid">
          <div v-for="service in services" :key="service" class="status-row">
            <div class="service-meta">
              <span class="status-indicator online"></span>
              <span class="service-name">{{ service }}</span>
            </div>
            <span class="status-text">100% Operational</span>
          </div>
        </div>
        <div class="widget-footer">最近更新时间：<strong>{{ updateTime }}</strong></div>
      </div>
      <div class="dashboard-widget flex-1">
        <h3 class="widget-title">快捷入口</h3>
        <div class="quick-links">
          <button v-for="link in quickLinks" :key="link.path" type="button" class="quick-link-btn" :class="{ urgent: link.urgent }" @click="$router.push(link.path)">
            <span class="material-symbols-outlined">{{ link.icon }}</span>
            <span>{{ link.label }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import request from '@common/api/request'

const stats = ref({ totalUsers: 0, totalMerchants: 0, todayOrders: 0, abnormalOrders: 0 })
const updateTime = ref('刚刚')
const services = [
  'order-service (订单服务)',
  'menu-service (菜单/库存服务)',
  'pickup-service (排队取餐码服务)',
  'payment-gateway (支付网关中间件)'
]
const quickLinks = [
  { path: '/users', icon: 'group', label: '用户密码重置' },
  { path: '/merchants', icon: 'add_business', label: '新建商家窗口' },
  { path: '/config', icon: 'settings_suggest', label: '更改支付超时' },
  { path: '/abnormal', icon: 'healing', label: '异常库存补偿', urgent: true }
]
const metricCards = computed(() => [
  { label: '用户总数', icon: 'group', color: 'text-rust', value: stats.value.totalUsers, description: '注册学生/教职工/管理员' },
  { label: '商家窗口数', icon: 'storefront', color: 'text-orange', value: stats.value.totalMerchants, description: '在线运行的主题美食窗口' },
  { label: '今日订单数', icon: 'receipt_long', color: 'text-green', value: stats.value.todayOrders, description: '今日实时支付成功的订单数' },
  { label: '异常订单数', icon: 'warning', color: 'text-error', value: stats.value.abnormalOrders, description: '需要人工介入释放或取消的订单', urgent: true }
])

onMounted(async () => {
  const [users, merchants, abnormal] = await Promise.all([
    request.get('/admin/users', { params: { page: 1, size: 1 } }).catch(() => ({ total: 0 })),
    request.get('/admin/merchants', { params: { page: 1, size: 1 } }).catch(() => ({ total: 0 })),
    request.get('/admin/orders/abnormal').catch(() => [])
  ])
  stats.value.totalUsers = users?.total || 0
  stats.value.totalMerchants = merchants?.total || 0
  stats.value.abnormalOrders = (abnormal?.records || abnormal || []).length
  updateTime.value = new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit' }).format(new Date())
})
</script>
