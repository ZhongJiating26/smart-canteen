<template>
  <div class="workspace-sub-view" id="view-orders">
    <div class="orders-columns-container">
      <div v-for="column in columns" :key="column.status" class="order-column" :id="column.id">
        <div class="column-header">
          <span class="column-title">{{ column.title }}</span>
          <span class="column-count-badge">{{ column.orders.length }}</span>
        </div>
        <div class="column-cards-list">
          <div v-for="order in column.orders" :key="order.orderId" class="merchant-order-card">
            <div class="order-card-header">
              <span class="order-card-id">{{ displayNo(order) }}</span>
              <span class="order-card-time">{{ formatTime(timeOf(order)) }}</span>
            </div>
            <div class="order-card-items-list">
              <div v-for="item in order.items || []" :key="item.dishId" class="order-card-item">
                <span>{{ item.dishName }}</span>
                <span class="order-card-item-qty">x{{ item.quantity }}</span>
              </div>
              <div v-if="!order.items?.length" class="order-card-item">
                <span>订单菜品详情加载中</span>
              </div>
            </div>
            <div v-if="order.remark" class="order-card-remarks">
              <span class="material-symbols-outlined remarks-icon">chat_bubble</span>
              <span class="remarks-text">备注：{{ order.remark }}</span>
            </div>
            <div class="order-card-total-row">
              <span class="order-card-total-label">Total Amount</span>
              <span class="order-card-total-price">${{ Number(order.totalAmount).toFixed(2) }}</span>
            </div>
            <div class="order-card-actions">
              <template v-if="column.status === 'PAID'">
                <button type="button" class="order-card-btn btn-outline" @click="pendingReject = order">Reject</button>
                <button type="button" class="order-card-btn btn-filled" @click="handleAccept(order)">Accept</button>
              </template>
              <button v-else-if="column.status === 'PREPARING'" type="button" class="order-card-btn btn-filled" @click="handleDispatch(order)">Mark Ready</button>
              <button v-else type="button" class="order-card-btn btn-filled-light" @click="handleCall(order)">再次叫号</button>
            </div>
          </div>
          <div v-if="!column.orders.length" class="empty-state">
            <span class="material-symbols-outlined">{{ column.emptyIcon }}</span>
            <p>{{ column.emptyText }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="dialog-scrim" :class="{ show: pendingReject }" @click="pendingReject = null"></div>
    <div class="m3-dialog" :class="{ show: pendingReject }">
      <h3 class="m3-dialog-title">Reject Order?</h3>
      <p class="m3-dialog-content">Are you sure you want to reject this order? This action cannot be undone.</p>
      <div class="m3-dialog-actions">
        <button type="button" class="m3-dialog-btn btn-text" @click="pendingReject = null">Cancel</button>
        <button type="button" class="m3-dialog-btn btn-filled" @click="confirmReject">Confirm Reject</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject, onMounted, watch } from 'vue'
import { getMerchantOrders, getOrderDetail, acceptOrder, rejectOrder, dispatchOrder } from '@common/api/orderApi'
import { manualCall } from '@common/api/pickupApi'
import { usePolling } from '@common/composables/usePolling'
import { formatTime as fmt } from '@common/utils/format'

const toast = inject('toast')
const refreshOrders = inject('refreshOrders')
const pendingBadge = inject('pendingBadge')
const pendingOrders = ref([])
const preparingOrders = ref([])
const readyOrders = ref([])
const pendingReject = ref(null)

const columns = computed(() => [
  { id: 'col-pending', title: '待接单', status: 'PAID', orders: pendingOrders.value, emptyIcon: 'inbox', emptyText: 'No pending orders' },
  { id: 'col-preparing', title: '制作中', status: 'PREPARING', orders: preparingOrders.value, emptyIcon: 'soup_kitchen', emptyText: 'No preparing orders' },
  { id: 'col-ready', title: '待取餐', status: 'READY', orders: readyOrders.value, emptyIcon: 'check_circle', emptyText: 'No ready orders' }
])

function displayNo(order) {
  return order.pickupNo && order.status !== 'PAID' ? order.pickupNo : order.orderNo || `#${order.orderId}`
}

function timeOf(order) {
  return order.status === 'PREPARING' ? order.acceptTime : order.createTime
}

function formatTime(value) {
  return value ? fmt(value) : ''
}

async function fetchOrders() {
  try {
    const summaries = await getMerchantOrders()
    const details = await Promise.all((summaries || []).map(async order => {
      try {
        return { ...order, ...(await getOrderDetail(order.orderId)) }
      } catch (e) {
        return order
      }
    }))
    pendingOrders.value = details.filter(order => order.status === 'PAID')
    preparingOrders.value = details.filter(order => order.status === 'PREPARING')
    readyOrders.value = details.filter(order => order.status === 'READY')
    if (pendingBadge) pendingBadge.value = pendingOrders.value.length
  } catch (e) {
    toast.show(e.message || '订单加载失败', 'error')
  }
}

async function handleAccept(order) {
  try {
    await acceptOrder(order.orderId)
    toast.show(`Order ${displayNo(order)} accepted! Preparing food...`, 'soup_kitchen')
    fetchOrders()
  } catch (e) {
    toast.show(e.message || '接单失败', 'error')
  }
}

async function confirmReject() {
  if (!pendingReject.value) return
  try {
    await rejectOrder(pendingReject.value.orderId)
    toast.show(`Order ${displayNo(pendingReject.value)} has been rejected`, 'cancel')
    pendingReject.value = null
    fetchOrders()
  } catch (e) {
    toast.show(e.message || '拒单失败', 'error')
  }
}

async function handleDispatch(order) {
  try {
    await dispatchOrder(order.orderId)
    toast.show(`Order ${displayNo(order)} is ready! Notifying user...`, 'notifications_active')
    fetchOrders()
  } catch (e) {
    toast.show(e.message || '出餐失败', 'error')
  }
}

async function handleCall(order) {
  try {
    await manualCall(order.merchantId, order.orderId)
    toast.show(`叫号成功！已再次通知顾客取餐 (${displayNo(order)})`, 'notifications_active')
  } catch (e) {
    toast.show(e.message || '叫号失败', 'error')
  }
}

const { start } = usePolling(fetchOrders, 5000)

watch(refreshOrders, fetchOrders)
onMounted(() => {
  fetchOrders()
  start()
})
</script>
