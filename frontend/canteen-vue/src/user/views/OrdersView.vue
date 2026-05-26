<template>
  <div class="mobile-screen state-center" id="orders-view">
    <header class="screen-header">
      <span class="brand-title">您的订单</span>
    </header>

    <div class="orders-filter-bar">
      <div
        v-for="tab in filterTabs"
        :key="tab.key"
        class="orders-filter-tab"
        :class="{ active: activeFilter === tab.key }"
        @click="activeFilter = tab.key"
      >
        {{ tab.label }}
      </div>
    </div>

    <div class="orders-list-container">
      <div v-if="filteredOrders.length === 0 && !loading" style="text-align:center;padding:40px 20px;color:var(--color-text-secondary);">
        <span class="material-symbols-outlined" style="font-size:3rem;margin-bottom:12px;color:var(--color-text-muted);">receipt</span>
        <p style="font-weight:700;">暂无订单</p>
        <p style="font-size:0.85rem;margin-top:4px;">该状态下的订单将会在此处显示。</p>
      </div>

      <div
        v-for="order in filteredOrders"
        :key="order.orderId"
        class="order-card"
        @click="openDetail(order)"
      >
        <div class="order-card-top-row">
          <img :src="orderItemImage(order)" class="order-card-stall-img" :alt="order.merchantName" />
          <div class="order-card-stall-info">
            <div class="order-card-stall-name">{{ order.merchantName }}</div>
            <div class="order-card-time">{{ formatTime(order.createTime) }}</div>
          </div>
          <div class="order-card-status-wrapper">
            <span class="order-card-status" :class="statusClass(order.status)">
              <span v-if="order.status === 'PREPARING'" class="material-symbols-outlined animated-sync" style="font-size:0.95rem;">sync</span>
              <span v-else-if="order.status === 'READY'" class="material-symbols-outlined" style="font-size:0.95rem;">check_circle</span>
              {{ statusLabel(order.status) }}
            </span>
          </div>
        </div>

        <div v-if="order.status !== 'COMPLETED' && order.status !== 'CANCELLED'" class="order-card-details-box">
          <div class="order-card-items-summary">{{ itemsSummary(order) }}</div>
          <div class="order-card-divider"></div>
          <div class="order-card-total-row">
            <span class="order-card-total-label">合计</span>
            <span class="order-card-total-price">${{ Number(order.totalAmount).toFixed(2) }}</span>
          </div>
        </div>
        <div v-else class="order-card-items-row">
          <span class="order-card-items-summary">{{ itemsSummary(order) }}</span>
          <span class="order-card-completed-price">${{ Number(order.totalAmount).toFixed(2) }}</span>
        </div>

        <div v-if="order.status === 'READY'" class="order-card-actions">
          <button type="button" class="btn-card-primary" @click.stop="openDetail(order)">查看取餐码</button>
        </div>
        <div v-else-if="order.status === 'PENDING_PAY' || order.status === 'PAID'" class="order-card-actions">
          <button type="button" class="btn-card-outline" @click.stop="cancelOrderHandler(order)">取消订单</button>
          <button v-if="order.status === 'PENDING_PAY'" type="button" class="btn-card-primary" @click.stop="goToPay(order)">去支付</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getMyOrders, getOrderDetail, cancelOrder } from '@common/api/orderApi'
import { getAvailableMenu } from '@common/api/menuApi'
import { usePolling } from '@common/composables/usePolling'
import { formatDateTime } from '@common/utils/format'
import { OrderStatus } from '@common/constants/enums'

const router = useRouter()
const toast = inject('toast')
const orders = ref([])
const activeFilter = ref('all')
const loading = ref(false)

const filterTabs = [
  { key: 'all', label: '全部' },
  { key: OrderStatus.PENDING_PAY, label: '待支付' },
  { key: OrderStatus.PAID, label: '待接单' },
  { key: OrderStatus.PREPARING, label: '制作中' },
  { key: OrderStatus.READY, label: '可取餐' },
  { key: OrderStatus.COMPLETED, label: '已完成' }
]

const filteredOrders = computed(() => {
  if (activeFilter.value === 'all') return orders.value
  return orders.value.filter(o => o.status === activeFilter.value)
})

function statusClass(status) {
  const map = { PENDING_PAY: 'placed', PAID: 'placed', PREPARING: 'preparing', READY: 'ready', COMPLETED: 'completed', CANCELLED: 'completed' }
  return map[status] || ''
}

function statusLabel(status) {
  const map = { PENDING_PAY: '待支付', PAID: '待接单', PREPARING: '制作中', READY: '待取餐', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

function itemsSummary(order) {
  if (!order.items) return '无商品'
  return order.items.map(i => `${i.quantity}x ${i.dishName || '菜品'}`).join(', ')
}

function orderItemImage(order) {
  if (order.items?.[0]?.dishImage) return order.items[0].dishImage
  return '/images/sour_beef.png'
}

function formatTime(dateStr) {
  return formatDateTime(dateStr)
}

function openDetail(order) {
  router.push({ name: 'OrderDetail', params: { orderId: order.orderId } })
}

function goToPay(order) {
  router.push({ name: 'Payment', query: { orderId: order.orderId, totalAmount: order.totalAmount, from: 'orders' } })
}

async function cancelOrderHandler(order) {
  try {
    await cancelOrder(order.orderId, '用户主动取消')
    toast.show(`订单 ${order.orderNo || order.orderId} 已成功取消。`, 'check_circle')
    fetchOrders()
  } catch (e) {
    toast.show(e.message || '取消失败', 'error')
  }
}

const merchantMap = {}

async function fetchMerchantNames() {
  try {
    const data = await getAvailableMenu()
    if (data?.merchants) {
      data.merchants.forEach(m => {
        merchantMap[m.merchantId] = m.merchantName
      })
    }
  } catch (e) {
    // silent
  }
}

async function fetchOrders() {
  loading.value = true
  try {
    if (Object.keys(merchantMap).length === 0) {
      await fetchMerchantNames()
    }
    const data = await getMyOrders({ page: 1, size: 50 })
    if (data?.records) {
      const records = data.records
      const details = await Promise.all(records.map(async order => {
        try {
          const detail = await getOrderDetail(order.orderId)
          return {
            ...order,
            ...detail,
            merchantName: merchantMap[order.merchantId] || detail.merchantName || `档口 #${order.merchantId}`
          }
        } catch (e) {
          return {
            ...order,
            merchantName: merchantMap[order.merchantId] || `档口 #${order.merchantId}`
          }
        }
      }))
      orders.value = details
    }
  } catch (e) {
    // silent
  } finally {
    loading.value = false
  }
}

const { start, stop } = usePolling(fetchOrders, 5000)

onMounted(() => {
  fetchOrders()
  start()
})
</script>
