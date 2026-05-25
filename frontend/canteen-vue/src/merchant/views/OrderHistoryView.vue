<template>
  <div class="workspace-sub-view" id="view-history">
    <div class="history-list-wrapper">
      <div class="history-table-header">
        <span>Order ID</span>
        <span>Customer</span>
        <span>Items</span>
        <span>Total Price</span>
        <span>Complete Time</span>
        <span>Status</span>
      </div>
      <div class="history-items-container">
        <div v-for="order in historyOrders" :key="order.orderId" class="history-row">
          <span class="history-id">{{ order.orderNo || '#' + order.orderId }}</span>
          <span class="history-customer">{{ customerName(order) }}</span>
          <span class="history-items">{{ itemText(order) }}</span>
          <span class="history-price">${{ Number(order.totalAmount).toFixed(2) }}</span>
          <span class="history-time">{{ formatTime(order.completeTime || order.cancelTime) }}</span>
          <span class="history-status-badge" :class="order.status === 'COMPLETED' ? 'completed' : 'cancelled'">
            {{ order.status === 'COMPLETED' ? 'Completed' : 'Cancelled' }}
          </span>
        </div>
        <div v-if="!historyOrders.length" class="empty-state">
          <span class="material-symbols-outlined">history</span>
          <p>暂无历史订单</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted, watch } from 'vue'
import { getMerchantOrders, getOrderDetail } from '@common/api/orderApi'
import { formatTime as fmt } from '@common/utils/format'

const refreshOrders = inject('refreshOrders')
const historyOrders = ref([])

function formatTime(dateStr) {
  return dateStr ? fmt(dateStr) : '-'
}

function customerName(order) {
  return order.customer || (order.userId ? `User #${order.userId}` : '-')
}

function itemText(order) {
  return (order.items || []).map(item => `${item.dishName} x${item.quantity}`).join(', ') || '-'
}

async function fetchHistory() {
  const lists = await Promise.all(['COMPLETED', 'CANCELLED'].map(status => getMerchantOrders({ status })))
  const summaries = lists.flat()
  historyOrders.value = await Promise.all(summaries.map(async order => {
    try {
      return { ...order, ...(await getOrderDetail(order.orderId)) }
    } catch (e) {
      return order
    }
  }))
}

watch(refreshOrders, fetchHistory)
onMounted(fetchHistory)
</script>
