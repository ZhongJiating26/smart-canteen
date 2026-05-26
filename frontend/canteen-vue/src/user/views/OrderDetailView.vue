<template>
  <div class="mobile-screen state-center" id="order-detail-view">
    <header class="screen-header justify-between">
      <span class="header-icon clickable" @click="$router.push('/orders')">
        <span class="material-symbols-outlined">arrow_back</span>
      </span>
      <span style="font-weight: 700; font-size: 1.15rem;">订单详情</span>
      <span style="width: 36px;"></span>
    </header>

    <div class="order-detail-container" style="flex:1;overflow-y:auto;padding-bottom:96px;display:flex;flex-direction:column;gap:16px;">
      <div class="order-stepper-card">
        <div class="timeline-stepper">
          <div class="timeline-line">
            <div class="timeline-line-fill" :style="{ width: timelineProgress }"></div>
          </div>

          <div class="timeline-step" :class="stepClass('PENDING_PAY')">
            <div class="timeline-step-circle"><span class="material-symbols-outlined" style="font-size:1.15rem;">check</span></div>
            <span class="timeline-step-label">已下单</span>
            <span class="timeline-step-time">{{ formatTime(order?.createTime) }}</span>
          </div>
          <div class="timeline-step" :class="stepClass('PAID')">
            <div class="timeline-step-circle"><span class="material-symbols-outlined" style="font-size:1.15rem;">payments</span></div>
            <span class="timeline-step-label">已支付</span>
            <span class="timeline-step-time">{{ formatTime(order?.payTime) }}</span>
          </div>
          <div class="timeline-step" :class="stepClass('PREPARING')">
            <div class="timeline-step-circle"><span class="material-symbols-outlined" style="font-size:1.15rem;">soup_kitchen</span></div>
            <span class="timeline-step-label">制作中</span>
            <span class="timeline-step-time">{{ order?.status === 'PREPARING' ? '现在' : formatTime(order?.acceptTime) }}</span>
          </div>
          <div class="timeline-step" :class="stepClass('READY')">
            <div class="timeline-step-circle"><span class="material-symbols-outlined" style="font-size:1.15rem;">restaurant</span></div>
            <span class="timeline-step-label">可取餐</span>
            <span class="timeline-step-time">{{ order?.status === 'READY' ? '现在' : formatTime(order?.completeTime) }}</span>
          </div>
          <div class="timeline-step" :class="stepClass('COMPLETED')">
            <div class="timeline-step-circle"><span class="material-symbols-outlined" style="font-size:1.15rem;">inbox</span></div>
            <span class="timeline-step-label">已取餐</span>
            <span class="timeline-step-time">{{ formatTime(order?.completeTime) }}</span>
          </div>
        </div>

        <span class="stepper-status-pill">{{ statusPillText }}</span>
      </div>

      <div class="order-status-detail-card">
        <div v-if="order?.status === 'PENDING_PAY'" style="text-align:center;">
          <md-circular-progress indeterminate style="--md-circular-progress-size:48px;"></md-circular-progress>
          <h3 style="margin-top:16px;font-weight:700;">等待支付中</h3>
          <p style="color:var(--color-text-secondary);margin-top:8px;">请尽快完成支付以提交您的订单。</p>
          <div class="order-pickup-box" style="margin-top:16px;">
            <div class="order-pickup-row">
              <span class="order-pickup-label">订单号</span>
              <span class="order-pickup-value" style="color:var(--color-brand-rust);">{{ order.orderNo }}</span>
            </div>
            <div class="order-pickup-row">
              <span class="order-pickup-label">取餐档口</span>
              <span class="order-pickup-value">{{ order.merchantName }}</span>
            </div>
          </div>
        </div>

        <div v-else-if="order?.status === 'PAID' || order?.status === 'PREPARING'" style="text-align:center;">
          <md-circular-progress indeterminate style="--md-circular-progress-size:48px;"></md-circular-progress>
          <h3 style="margin-top:16px;font-weight:700;">{{ order.status === 'PREPARING' ? '正在配餐制作中...' : '等待商家接单中...' }}</h3>
          <p style="color:var(--color-text-secondary);margin-top:8px;">{{ order.status === 'PREPARING' ? '后厨正在全力制作您的美味餐点，请稍候。' : '商家正在确认您的订单，将很快为您接单。' }}</p>
          <div class="order-pickup-box" style="margin-top:16px;">
            <div class="order-pickup-row">
              <span class="order-pickup-label">取餐号</span>
              <span class="order-pickup-value" style="color:var(--color-brand-rust);">{{ order.pickupNo || order.orderNo }}</span>
            </div>
            <div class="order-pickup-row">
              <span class="order-pickup-label">取餐档口</span>
              <span class="order-pickup-value">{{ order.merchantName }}</span>
            </div>
          </div>
        </div>

        <div v-else-if="order?.status === 'READY'" style="text-align:center;">
          <div class="order-qrcode-wrapper" style="display:flex;justify-content:center;align-items:center;margin-bottom:12px;">
            <img 
              v-if="qrcodeContent" 
              :src="'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=' + encodeURIComponent(qrcodeContent)" 
              style="width:160px;height:160px;background-color:white;padding:12px;border-radius:16px;border:1px solid var(--color-border);" 
              alt="取餐二维码" 
            />
            <div v-else style="width:160px;height:160px;display:flex;align-items:center;justify-content:center;background-color:white;border-radius:16px;border:1px solid var(--color-border);">
              <md-circular-progress indeterminate style="--md-circular-progress-size:36px;"></md-circular-progress>
            </div>
          </div>
          <h3 class="order-status-qr-code">{{ order.pickupNo || order.orderNo }}</h3>
          <div class="order-pickup-stall-line">
            <span class="material-symbols-outlined" style="color:var(--color-brand-rust);">storefront</span>
            <span style="font-weight:700;">{{ order.merchantName }}</span>
          </div>
          <p class="order-status-qr-subtitle">请在档口前出示此取餐码以核销取餐。</p>
          <div style="margin-top: 16px; display: flex; justify-content: center;">
            <md-filled-button 
              @click="showCodeDialog = true" 
              style="--md-filled-button-container-color: var(--color-brand-rust); --md-filled-button-label-text-color: white; font-weight: bold;"
            >
              显示取餐编号
            </md-filled-button>
          </div>
        </div>

        <div v-else-if="order?.status === 'COMPLETED'" style="text-align:center;">
          <div class="order-status-large-icon-circle completed">
            <span class="material-symbols-outlined" style="font-weight:bold;">check</span>
          </div>
          <h3 class="order-status-main-title">感谢您的光临！</h3>
          <p style="color:var(--color-text-secondary);margin-top:8px;">您的订单已成功核销并取餐完成。</p>
          <div class="order-pickup-box" style="margin-top:16px;">
            <div class="order-pickup-row">
              <span class="order-pickup-label">取餐号</span>
              <span class="order-pickup-value" style="color:var(--color-brand-rust);">{{ order.pickupNo || order.orderNo }}</span>
            </div>
            <div class="order-pickup-row">
              <span class="order-pickup-label">取餐时间</span>
              <span class="order-pickup-value">{{ formatTime(order.completeTime) }}</span>
            </div>
          </div>
        </div>

        <div v-else-if="order?.status === 'CANCELLED'" style="text-align:center;">
          <span class="material-symbols-outlined" style="font-size:3rem;color:var(--color-text-muted);">cancel</span>
          <h3 style="margin-top:16px;font-weight:700;">订单已取消</h3>
          <p style="color:var(--color-text-secondary);margin-top:8px;">{{ order.cancelReason || '该订单已被取消。' }}</p>
        </div>
      </div>

      <div class="confirm-order-card" style="margin:0 16px;">
        <h3 class="confirm-order-card-title">商品汇总</h3>
        <div class="confirm-order-items-list">
          <div v-for="item in (order?.items || [])" :key="item.id" class="confirm-order-item">
            <div class="confirm-order-item-info">
              <span class="confirm-order-item-name">{{ item.quantity }}x {{ item.dishName }}</span>
              <span class="confirm-order-item-qty">单价 ￥{{ Number(item.unitPrice).toFixed(2) }}</span>
            </div>
            <span class="confirm-order-item-price">￥{{ Number(item.subtotal).toFixed(2) }}</span>
          </div>
        </div>
        <div class="confirm-order-divider"></div>
        <div class="confirm-order-total-row">
          <span class="confirm-order-total-label">合计</span>
          <span class="confirm-order-total-price">￥{{ Number(order?.totalAmount || 0).toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- Custom Dialog for Pickup Code ID -->
    <div v-if="showCodeDialog" class="modal-overlay" @click.self="showCodeDialog = false">
      <div class="modal-content">
        <h3 class="modal-title">取餐编号</h3>
        <p class="modal-desc">若无法扫描二维码，可向商家提供此取餐编号以核销。</p>
        <div class="modal-code-box">{{ order?.pickupQrcodeId || '加载中...' }}</div>
        <md-filled-button 
          @click="showCodeDialog = false" 
          style="--md-filled-button-container-color: var(--color-brand-rust); --md-filled-button-label-text-color: white; margin-top: 20px; width: 100%; font-weight: bold;"
        >
          关闭
        </md-filled-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, inject } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail } from '@common/api/orderApi'
import { getAvailableMenu } from '@common/api/menuApi'
import { getQrcode } from '@common/api/pickupApi'
import { usePolling } from '@common/composables/usePolling'
import { OrderStatus } from '@common/constants/enums'
import { formatTime as fmt } from '@common/utils/format'

const route = useRoute()
const toast = inject('toast')
const orderId = computed(() => route.params.orderId)
const order = ref(null)
const loading = ref(false)
const qrcodeContent = ref('')
const showCodeDialog = ref(false)

async function fetchQrCode() {
  if (!orderId.value) return
  try {
    const qrData = await getQrcode(orderId.value)
    if (qrData?.qrcodeContent) {
      qrcodeContent.value = qrData.qrcodeContent
    }
  } catch (e) {
    // silent
  }
}

const statusOrder = [OrderStatus.PENDING_PAY, OrderStatus.PAID, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.COMPLETED]

const timelineProgress = computed(() => {
  if (!order.value) return '0%'
  const idx = statusOrder.indexOf(order.value.status)
  if (idx < 0) return '0%'
  return (idx / (statusOrder.length - 1)) * 100 + '%'
})

const statusPillText = computed(() => {
  const map = {
    PENDING_PAY: '等待支付中',
    PAID: '等待商家接单',
    PREPARING: '正在配餐制作',
    READY: '请到档口取餐',
    COMPLETED: '订单已取餐完成',
    CANCELLED: '订单已取消'
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

async function fetchOrder() {
  try {
    if (Object.keys(merchantMap).length === 0) {
      await fetchMerchantNames()
    }
    const data = await getOrderDetail(orderId.value)
    if (data) {
      order.value = {
        ...data,
        merchantName: merchantMap[data.merchantId] || data.merchantName || `档口 #${data.merchantId}`
      }
    }
  } catch (e) {
    // silent on polling
  }
}

const { start, stop } = usePolling(fetchOrder, 5000)

onMounted(() => {
  fetchOrder()
  start()
})

// Stop polling when status becomes terminal, and fetch QR code when READY
watch(() => order.value?.status, async (status) => {
  if (status === 'COMPLETED' || status === 'CANCELLED') {
    stop()
  }
  if (status === 'READY' && !qrcodeContent.value) {
    await fetchQrCode()
  }
}, { immediate: true })
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background-color: var(--color-surface, #ffffff);
  padding: 24px;
  border-radius: 24px;
  width: 80%;
  max-width: 320px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: modal-fade-in 0.3s ease-out;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--color-text-primary, #1c1b1f);
}

.modal-desc {
  font-size: 0.85rem;
  color: var(--color-text-secondary, #49454f);
  text-align: center;
  margin-bottom: 20px;
  line-height: 1.4;
}

.modal-code-box {
  background-color: var(--color-surface-variant, #f4f0ec);
  color: var(--color-brand-rust, #8f4c38);
  font-family: monospace;
  font-size: 1.5rem;
  font-weight: 700;
  padding: 12px 24px;
  border-radius: 12px;
  letter-spacing: 2px;
  border: 1px dashed var(--color-brand-rust, #8f4c38);
}

@keyframes modal-fade-in {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
