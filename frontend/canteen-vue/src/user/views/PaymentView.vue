<template>
  <div class="mobile-screen state-center" id="payment-view">
    <header class="screen-header justify-between">
      <span class="header-icon clickable" @click="handleCancelPay">
        <span class="material-symbols-outlined">close</span>
      </span>
      <span style="font-weight: 700; font-size: 1.15rem;">支付</span>
      <span style="width: 36px;"></span>
    </header>

    <div class="payment-container">
      <div class="payment-top-section">
        <div class="payment-bill-card">
          <div class="payment-bill-header">
            <div class="payment-bill-title-box">
              <span class="payment-bill-label">合计金额</span>
              <span class="payment-bill-amount">${{ totalAmount }}</span>
            </div>
            <span class="payment-badge-pending">待支付</span>
          </div>
          <div class="payment-bill-divider"></div>
          <div class="payment-bill-items">
            <div v-for="item in cartItems" :key="item.id" class="payment-bill-item">
              <div class="payment-bill-item-left">
                <span class="payment-item-qty">{{ item.quantity }}x</span>
                <span class="payment-item-name">{{ item.name }}</span>
              </div>
              <span class="payment-item-price">${{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="payment-password-section">
          <h4 class="payment-password-title">请输入支付密码</h4>
          <p class="payment-password-subtitle">验证您的身份以授权交易</p>

          <div class="payment-dots-container">
            <span v-for="i in 6" :key="i" class="payment-dot" :class="{ filled: password.length >= i }"></span>
          </div>
        </div>
      </div>

      <div class="payment-bottom-section">
        <div class="payment-keypad">
          <button v-for="n in 9" :key="n" type="button" class="keypad-btn" @click="inputDigit(n)">{{ n }}</button>
          <div class="keypad-empty"></div>
          <button type="button" class="keypad-btn" @click="inputDigit(0)">0</button>
          <button type="button" class="keypad-btn keypad-backspace" @click="backspace">
            <span class="material-symbols-outlined">backspace</span>
          </button>
        </div>

        <div class="payment-actions">
          <button
            type="button"
            class="btn-primary payment-confirm-btn"
            :disabled="password.length < 6 || paying"
            @click="handlePay"
          >
            {{ paying ? '处理中...' : '确认支付' }}
          </button>
          <button type="button" class="payment-cancel-btn" @click="handleCancelPay">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { payOrder } from '@common/api/orderApi'
import { useCartStore } from '../stores/cartStore'

const route = useRoute()
const router = useRouter()
const toast = inject('toast')
const cartStore = useCartStore()

const orderId = computed(() => route.query.orderId)
const totalAmount = computed(() => Number(route.query.totalAmount || 0).toFixed(2))
const password = ref('')
const paying = ref(false)

const cartItems = computed(() => cartStore.itemList)

function inputDigit(n) {
  if (password.value.length < 6) {
    password.value += n.toString()
  }
}

function backspace() {
  password.value = password.value.slice(0, -1)
}

async function handlePay() {
  if (!orderId.value) {
    toast.show('未找到订单', 'error')
    return
  }
  paying.value = true
  try {
    await payOrder(orderId.value, password.value)
    toast.show('支付成功！订单已提交！', 'check_circle')
    password.value = ''
    router.replace({ name: 'OrderDetail', params: { orderId: orderId.value } })
  } catch (e) {
    toast.show(e.message || '支付失败', 'error')
    password.value = ''
  } finally {
    paying.value = false
  }
}

function handleCancelPay() {
  const fromPage = route.query.from
  const stallId = route.query.stallId
  if (fromPage === 'confirm' && stallId) {
    router.replace({ name: 'Dishes', params: { stallId } })
  } else if (fromPage === 'orders') {
    router.replace({ name: 'Orders' })
  } else {
    router.back()
  }
}
</script>
