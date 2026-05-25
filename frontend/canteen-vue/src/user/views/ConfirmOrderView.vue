<template>
  <div class="mobile-screen state-center" id="confirm-order-view">
    <header class="screen-header justify-start gap-12" style="background-color: var(--color-bg-card);">
      <span class="material-symbols-outlined header-icon clickable" @click="$router.back()">arrow_back</span>
      <span class="dish-detail-header-title">Confirm Order</span>
    </header>

    <div class="confirm-order-container">
      <div class="confirm-order-pickup-card">
        <div class="confirm-order-icon-badge">
          <span class="material-symbols-outlined">storefront</span>
        </div>
        <span class="confirm-order-pickup-text">Pickup Stall: {{ cartStore.stallName }}</span>
      </div>

      <div class="confirm-order-card">
        <h3 class="confirm-order-card-title">YOUR SELECTION</h3>
        <div class="confirm-order-items-list">
          <div v-for="item in cartStore.itemList" :key="item.id" class="confirm-order-item">
            <img class="confirm-order-item-img" :src="item.imgUrl || '/images/default.png'" :alt="item.name" />
            <div class="confirm-order-item-info">
              <span class="confirm-order-item-name">{{ item.name }}</span>
              <span class="confirm-order-item-qty">Qty: {{ item.quantity }}</span>
            </div>
            <span class="confirm-order-item-price">${{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
        <div class="confirm-order-divider"></div>
        <div class="confirm-order-total-row">
          <span class="confirm-order-total-label">Order Total</span>
          <span class="confirm-order-total-price">${{ cartStore.totalPrice.toFixed(2) }}</span>
        </div>
      </div>

      <div class="confirm-order-remarks-section">
        <label class="confirm-order-remarks-label">Special Remarks</label>
        <div class="confirm-order-remarks-card">
          <textarea v-model="remarks" placeholder="E.g. No peanuts, extra spicy..." rows="3"></textarea>
          <span class="material-symbols-outlined confirm-order-remarks-icon">edit_note</span>
        </div>
      </div>

      <div class="confirm-order-payment-card">
        <div class="confirm-order-payment-left">
          <span class="material-symbols-outlined">badge</span>
          <span class="confirm-order-payment-text">Payment: Campus Card (Credits)</span>
        </div>
        <span class="confirm-order-payment-change" @click="showPaymentInfo">Change</span>
      </div>
    </div>

    <div class="confirm-order-bottom-bar">
      <button type="button" class="btn-primary confirm-order-submit-btn" :disabled="submitting" @click="submitOrder">
        <span>{{ submitting ? 'Submitting...' : 'Submit Order' }}</span>
        <span class="material-symbols-outlined">send</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cartStore'
import { createOrder } from '@common/api/orderApi'

const cartStore = useCartStore()
const router = useRouter()
const toast = inject('toast')
const remarks = ref('')
const submitting = ref(false)

function showPaymentInfo() {
  toast.show('Only Campus Card payment is supported', 'info')
}

async function submitOrder() {
  if (cartStore.isEmpty) {
    toast.show('Cart is empty!', 'warning')
    return
  }

  submitting.value = true
  try {
    const items = cartStore.itemList.map(item => ({
      dailyMenuItemId: item.id,
      quantity: item.quantity
    }))

    const idempotentKey = crypto.randomUUID()
    sessionStorage.setItem('idempotent_key', idempotentKey)

    const result = await createOrder({
      merchantId: cartStore.stallId,
      remark: remarks.value,
      items
    })

    sessionStorage.removeItem('idempotent_key')
    cartStore.clearCart()

    if (result?.orderId) {
      router.push({ name: 'Payment', query: { orderId: result.orderId, totalAmount: result.totalAmount } })
    }
  } catch (e) {
    toast.show(e.message || 'Order creation failed', 'error')
  } finally {
    submitting.value = false
  }
}
</script>
