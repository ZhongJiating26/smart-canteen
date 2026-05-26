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
              <span class="order-card-total-label">总金额</span>
              <span class="order-card-total-price">￥{{ Number(order.totalAmount).toFixed(2) }}</span>
            </div>
            <div class="order-card-actions">
              <template v-if="column.status === 'PAID'">
                <button type="button" class="order-card-btn btn-outline" @click="pendingReject = order">拒绝接单</button>
                <button type="button" class="order-card-btn btn-filled" @click="handleAccept(order)">确认接单</button>
              </template>
              <button v-else-if="column.status === 'PREPARING'" type="button" class="order-card-btn btn-filled" @click="handleDispatch(order)">完成制作</button>
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
      <h3 class="m3-dialog-title">拒绝订单？</h3>
      <p class="m3-dialog-content">您确定要拒绝该订单吗？此操作无法撤销。</p>
      <div class="m3-dialog-actions">
        <button type="button" class="m3-dialog-btn btn-text" @click="pendingReject = null">取消</button>
        <button type="button" class="m3-dialog-btn btn-filled" @click="confirmReject">确认拒绝</button>
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
  { id: 'col-pending', title: '待接单', status: 'PAID', orders: pendingOrders.value, emptyIcon: 'inbox', emptyText: '暂无待接单订单' },
  { id: 'col-preparing', title: '制作中', status: 'PREPARING', orders: preparingOrders.value, emptyIcon: 'soup_kitchen', emptyText: '暂无制作中订单' },
  { id: 'col-ready', title: '待取餐', status: 'READY', orders: readyOrders.value, emptyIcon: 'check_circle', emptyText: '暂无待取餐订单' }
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
    toast.show(`订单 ${displayNo(order)} 已接单！正在制作中...`, 'soup_kitchen')
    fetchOrders()
  } catch (e) {
    toast.show(e.message || '接单失败', 'error')
  }
}

async function confirmReject() {
  if (!pendingReject.value) return
  try {
    await rejectOrder(pendingReject.value.orderId)
    toast.show(`订单 ${displayNo(pendingReject.value)} 已拒绝`, 'cancel')
    pendingReject.value = null
    fetchOrders()
  } catch (e) {
    toast.show(e.message || '拒单失败', 'error')
  }
}

async function handleDispatch(order) {
  try {
    await dispatchOrder(order.orderId)
    toast.show(`订单 ${displayNo(order)} 制作完成！正在通知顾客取餐...`, 'notifications_active')
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

const { start } = usePolling(fetchOrders, 1000)

watch(refreshOrders, fetchOrders)
onMounted(() => {
  fetchOrders()
  start()
})
</script>
