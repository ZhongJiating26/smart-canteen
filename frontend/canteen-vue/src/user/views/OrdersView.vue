<template>
  <div class="mobile-screen state-center" id="orders-view">
    <header class="screen-header">
      <span class="brand-title">Your Orders</span>
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
        <p style="font-weight:700;">No orders yet</p>
        <p style="font-size:0.85rem;margin-top:4px;">Items in this status will appear here.</p>
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
            <span class="order-card-total-label">TOTAL</span>
            <span class="order-card-total-price">${{ Number(order.totalAmount).toFixed(2) }}</span>
          </div>
        </div>
        <div v-else class="order-card-items-row">
          <span class="order-card-items-summary">{{ itemsSummary(order) }}</span>
          <span class="order-card-completed-price">${{ Number(order.totalAmount).toFixed(2) }}</span>
        </div>

        <div v-if="order.status === 'READY'" class="order-card-actions">
          <button type="button" class="btn-card-primary" @click.stop="openDetail(order)">View Pickup Code</button>
        </div>
        <div v-else-if="order.status === 'PENDING_PAY' || order.status === 'PAID'" class="order-card-actions">
          <button type="button" class="btn-card-outline" @click.stop="cancelOrderHandler(order)">Cancel</button>
          <button v-if="order.status === 'PENDING_PAY'" type="button" class="btn-card-primary" @click.stop="goToPay(order)">Pay</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getMyOrders, cancelOrder } from '@common/api/orderApi'
import { usePolling } from '@common/composables/usePolling'
import { formatDateTime } from '@common/utils/format'
import { OrderStatus } from '@common/constants/enums'

const router = useRouter()
const toast = inject('toast')
const orders = ref([])
const activeFilter = ref('all')
const loading = ref(false)

const filterTabs = [
  { key: 'all', label: 'All' },
  { key: OrderStatus.PENDING_PAY, label: 'Pending' },
  { key: OrderStatus.PAID, label: 'Waiting' },
  { key: OrderStatus.PREPARING, label: 'Preparing' },
  { key: OrderStatus.READY, label: 'Ready' },
  { key: OrderStatus.COMPLETED, label: 'Completed' }
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
  const map = { PENDING_PAY: 'Pending', PAID: 'Waiting', PREPARING: 'Preparing', READY: 'Ready', COMPLETED: 'Completed', CANCELLED: 'Cancelled' }
  return map[status] || status
}

function itemsSummary(order) {
  if (!order.items) return 'No items'
  return order.items.map(i => `${i.quantity}x ${i.dishName || 'Item'}`).join(', ')
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
  router.push({ name: 'Payment', query: { orderId: order.orderId, totalAmount: order.totalAmount } })
}

async function cancelOrderHandler(order) {
  try {
    await cancelOrder(order.orderId, 'User cancelled')
    toast.show(`Order ${order.orderNo || order.orderId} has been cancelled.`, 'check_circle')
    fetchOrders()
  } catch (e) {
    toast.show(e.message || 'Failed to cancel', 'error')
  }
}

async function fetchOrders() {
  loading.value = true
  try {
    const data = await getMyOrders({ page: 1, size: 50 })
    if (data?.records) {
      orders.value = data.records
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
