<template>
  <div class="mobile-screen state-center" id="order-detail-view">
    <header class="screen-header justify-between">
      <span class="material-symbols-outlined header-icon clickable" @click="$router.push('/orders')">arrow_back</span>
      <span style="font-weight: 700; font-size: 1.15rem;">Order Details</span>
      <span style="width: 24px;"></span>
    </header>

    <div class="order-detail-container" style="flex:1;overflow-y:auto;padding-bottom:96px;display:flex;flex-direction:column;gap:16px;">
      <div class="order-stepper-card">
        <div class="timeline-stepper">
          <div class="timeline-line">
            <div class="timeline-line-fill" :style="{ width: timelineProgress }"></div>
          </div>

          <div class="timeline-step" :class="stepClass('PENDING_PAY')">
            <div class="timeline-step-circle"><span class="material-symbols-outlined" style="font-size:1.15rem;">check</span></div>
            <span class="timeline-step-label">Ordered</span>
            <span class="timeline-step-time">{{ formatTime(order?.createTime) }}</span>
          </div>
          <div class="timeline-step" :class="stepClass('PAID')">
            <div class="timeline-step-circle"><span class="material-symbols-outlined" style="font-size:1.15rem;">payments</span></div>
            <span class="timeline-step-label">Paid</span>
            <span class="timeline-step-time">{{ formatTime(order?.payTime) }}</span>
          </div>
          <div class="timeline-step" :class="stepClass('PREPARING')">
            <div class="timeline-step-circle"><span class="material-symbols-outlined" style="font-size:1.15rem;">soup_kitchen</span></div>
            <span class="timeline-step-label">Preparing</span>
            <span class="timeline-step-time">{{ order?.status === 'PREPARING' ? 'Now' : formatTime(order?.acceptTime) }}</span>
          </div>
          <div class="timeline-step" :class="stepClass('READY')">
            <div class="timeline-step-circle"><span class="material-symbols-outlined" style="font-size:1.15rem;">restaurant</span></div>
            <span class="timeline-step-label">Ready</span>
            <span class="timeline-step-time">{{ order?.status === 'READY' ? 'Now' : formatTime(order?.completeTime) }}</span>
          </div>
          <div class="timeline-step" :class="stepClass('COMPLETED')">
            <div class="timeline-step-circle"><span class="material-symbols-outlined" style="font-size:1.15rem;">inbox</span></div>
            <span class="timeline-step-label">Collected</span>
            <span class="timeline-step-time">{{ formatTime(order?.completeTime) }}</span>
          </div>
        </div>

        <span class="stepper-status-pill">{{ statusPillText }}</span>
      </div>

      <div class="order-status-detail-card">
        <div v-if="order?.status === 'PENDING_PAY'" style="text-align:center;">
          <md-circular-progress indeterminate style="--md-circular-progress-size:48px;"></md-circular-progress>
          <h3 style="margin-top:16px;font-weight:700;">Waiting for Payment</h3>
          <p style="color:var(--color-text-secondary);margin-top:8px;">Complete payment to submit your order.</p>
          <div class="order-pickup-box" style="margin-top:16px;">
            <div class="order-pickup-row">
              <span class="order-pickup-label">Order No</span>
              <span class="order-pickup-value" style="color:var(--color-brand-rust);">{{ order.orderNo }}</span>
            </div>
            <div class="order-pickup-row">
              <span class="order-pickup-label">Pickup Stall</span>
              <span class="order-pickup-value">{{ order.merchantName }}</span>
            </div>
          </div>
        </div>

        <div v-else-if="order?.status === 'PAID' || order?.status === 'PREPARING'" style="text-align:center;">
          <md-circular-progress indeterminate style="--md-circular-progress-size:48px;"></md-circular-progress>
          <h3 style="margin-top:16px;font-weight:700;">{{ order.status === 'PREPARING' ? 'Preparing Meal...' : 'Waiting for Accept...' }}</h3>
          <p style="color:var(--color-text-secondary);margin-top:8px;">{{ order.status === 'PREPARING' ? 'The chef is preparing your meal.' : 'The merchant will accept your order shortly.' }}</p>
          <div class="order-pickup-box" style="margin-top:16px;">
            <div class="order-pickup-row">
              <span class="order-pickup-label">Collection ID</span>
              <span class="order-pickup-value" style="color:var(--color-brand-rust);">{{ order.pickupNo || order.orderNo }}</span>
            </div>
            <div class="order-pickup-row">
              <span class="order-pickup-label">Pickup Stall</span>
              <span class="order-pickup-value">{{ order.merchantName }}</span>
            </div>
          </div>
        </div>

        <div v-else-if="order?.status === 'READY'" style="text-align:center;">
          <div class="order-qrcode-wrapper">
            <svg width="160" height="160" viewBox="0 0 29 29" style="background-color:white;padding:12px;border-radius:16px;border:1px solid var(--color-border);">
              <rect x="0" y="0" width="7" height="7" fill="black" /><rect x="1" y="1" width="5" height="5" fill="white" /><rect x="2" y="2" width="3" height="3" fill="black" />
              <rect x="22" y="0" width="7" height="7" fill="black" /><rect x="23" y="1" width="5" height="5" fill="white" /><rect x="24" y="2" width="3" height="3" fill="black" />
              <rect x="0" y="22" width="7" height="7" fill="black" /><rect x="1" y="23" width="5" height="5" fill="white" /><rect x="2" y="24" width="3" height="3" fill="black" />
            </svg>
          </div>
          <h3 class="order-status-qr-code">{{ order.pickupNo || order.orderNo }}</h3>
          <div class="order-pickup-stall-line">
            <span class="material-symbols-outlined" style="color:var(--color-brand-rust);">storefront</span>
            <span style="font-weight:700;">{{ order.merchantName }}</span>
          </div>
          <p class="order-status-qr-subtitle">Present this code at the counter to collect your order.</p>
        </div>

        <div v-else-if="order?.status === 'COMPLETED'" style="text-align:center;">
          <div class="order-status-large-icon-circle completed">
            <span class="material-symbols-outlined" style="font-weight:bold;">check</span>
          </div>
          <h3 class="order-status-main-title">Thank You!</h3>
          <p style="color:var(--color-text-secondary);margin-top:8px;">Your order has been successfully collected.</p>
          <div class="order-pickup-box" style="margin-top:16px;">
            <div class="order-pickup-row">
              <span class="order-pickup-label">Collection ID</span>
              <span class="order-pickup-value" style="color:var(--color-brand-rust);">{{ order.pickupNo || order.orderNo }}</span>
            </div>
            <div class="order-pickup-row">
              <span class="order-pickup-label">Collected at</span>
              <span class="order-pickup-value">{{ formatTime(order.completeTime) }}</span>
            </div>
          </div>
        </div>

        <div v-else-if="order?.status === 'CANCELLED'" style="text-align:center;">
          <span class="material-symbols-outlined" style="font-size:3rem;color:var(--color-text-muted);">cancel</span>
          <h3 style="margin-top:16px;font-weight:700;">Order Cancelled</h3>
          <p style="color:var(--color-text-secondary);margin-top:8px;">{{ order.cancelReason || 'This order has been cancelled.' }}</p>
        </div>
      </div>

      <div class="confirm-order-card" style="margin:0 16px;">
        <h3 class="confirm-order-card-title">ITEMS SUMMARY</h3>
        <div class="confirm-order-items-list">
          <div v-for="item in (order?.items || [])" :key="item.id" class="confirm-order-item">
            <div class="confirm-order-item-info">
              <span class="confirm-order-item-name">{{ item.quantity }}x {{ item.dishName }}</span>
              <span class="confirm-order-item-qty">${{ Number(item.unitPrice).toFixed(2) }} each</span>
            </div>
            <span class="confirm-order-item-price">${{ Number(item.subtotal).toFixed(2) }}</span>
          </div>
        </div>
        <div class="confirm-order-divider"></div>
        <div class="confirm-order-total-row">
          <span class="confirm-order-total-label">Total</span>
          <span class="confirm-order-total-price">${{ Number(order?.totalAmount || 0).toFixed(2) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail } from '@common/api/orderApi'
import { usePolling } from '@common/composables/usePolling'
import { OrderStatus } from '@common/constants/enums'
import { formatTime as fmt } from '@common/utils/format'

const route = useRoute()
const toast = inject('toast')
const orderId = computed(() => route.params.orderId)
const order = ref(null)
const loading = ref(false)

const statusOrder = [OrderStatus.PENDING_PAY, OrderStatus.PAID, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.COMPLETED]

const timelineProgress = computed(() => {
  if (!order.value) return '0%'
  const idx = statusOrder.indexOf(order.value.status)
  if (idx < 0) return '0%'
  return (idx / (statusOrder.length - 1)) * 100 + '%'
})

const statusPillText = computed(() => {
  const map = {
    PENDING_PAY: 'WAITING FOR PAYMENT',
    PAID: 'WAITING FOR ACCEPTANCE',
    PREPARING: 'PREPARING MEAL',
    READY: 'READY FOR PICKUP',
    COMPLETED: 'ORDER COMPLETED',
    CANCELLED: 'ORDER CANCELLED'
  }
  return map[order.value?.status] || ''
})

function stepClass(stepStatus) {
  if (!order.value) return 'pending'
  const currentIdx = statusOrder.indexOf(order.value.status)
  const stepIdx = statusOrder.indexOf(stepStatus)
  if (order.value.status === 'CANCELLED') return currentIdx >= stepIdx ? 'completed' : 'pending'
  if (stepIdx < currentIdx) return 'completed'
  if (stepIdx === currentIdx) return 'active'
  return 'pending'
}

function formatTime(dateStr) {
  return fmt(dateStr)
}

async function fetchOrder() {
  try {
    const data = await getOrderDetail(orderId.value)
    if (data) order.value = data
  } catch (e) {
    // silent on polling
  }
}

const { start, stop } = usePolling(fetchOrder, 5000)

onMounted(() => {
  fetchOrder()
  start()
})

// Stop polling when status becomes terminal
watch(() => order.value?.status, (status) => {
  if (status === 'COMPLETED' || status === 'CANCELLED') {
    stop()
  }
})
</script>
