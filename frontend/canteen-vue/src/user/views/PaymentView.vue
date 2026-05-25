<template>
  <div class="mobile-screen state-center" id="payment-view">
    <header class="screen-header justify-between">
      <span class="material-symbols-outlined header-icon clickable" @click="$router.back()">close</span>
      <span style="font-weight: 700; font-size: 1.15rem;">Pay</span>
      <span style="width: 24px;"></span>
    </header>

    <div class="payment-container">
      <div class="payment-top-section">
        <div class="payment-bill-card">
          <div class="payment-bill-header">
            <div class="payment-bill-title-box">
              <span class="payment-bill-label">Total Amount</span>
              <span class="payment-bill-amount">${{ totalAmount }}</span>
            </div>
            <span class="payment-badge-pending">Pending</span>
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
          <h4 class="payment-password-title">Enter Payment Password</h4>
          <p class="payment-password-subtitle">Verify your identity to authorize the transaction</p>

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
            {{ paying ? 'Processing...' : 'Confirm Payment' }}
          </button>
          <button type="button" class="payment-cancel-btn" @click="handleCancelPay">Cancel</button>
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
    toast.show('Order not found', 'error')
    return
  }
  paying.value = true
  try {
    await payOrder(orderId.value, password.value)
    toast.show('Payment successful! Order submitted!', 'check_circle')
    password.value = ''
    router.replace({ name: 'OrderDetail', params: { orderId: orderId.value } })
  } catch (e) {
    toast.show(e.message || 'Payment failed', 'error')
    password.value = ''
  } finally {
    paying.value = false
  }
}

function handleCancelPay() {
  router.back()
}
</script>
