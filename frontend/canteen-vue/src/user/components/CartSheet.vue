<template>
  <div
    v-if="cartStore.isOpen"
    class="cart-sheet-scrim show"
    @click="cartStore.closeCart()"
  ></div>
  <div
    class="cart-sheet"
    :class="{ show: cartStore.isOpen, hidden: !cartStore.isOpen }"
  >
    <div class="cart-sheet-drag-handle"></div>

    <div class="cart-sheet-header">
      <div class="cart-sheet-title-box">
        <h3 class="cart-sheet-title">Your Cart</h3>
        <span class="cart-sheet-subtitle">{{ cartStore.stallName }}</span>
      </div>
      <button type="button" class="cart-sheet-close-btn" @click="cartStore.closeCart()">
        <span class="material-symbols-outlined">close</span>
      </button>
    </div>

    <div class="cart-sheet-content">
      <div class="cart-sheet-items-list">
        <div v-if="cartStore.isEmpty" style="text-align:center;padding:40px 0;color:var(--color-text-secondary);">
          Your cart is empty
        </div>
        <div
          v-for="item in cartStore.itemList"
          :key="item.id"
          class="cart-sheet-item"
        >
          <img class="cart-item-img" :src="item.imgUrl || '/images/default.png'" :alt="item.name" />
          <div class="cart-item-details">
            <span class="cart-item-name">{{ item.name }}</span>
            <span class="cart-item-options">{{ item.options }}</span>
          </div>
          <div class="cart-item-right">
            <span class="cart-item-price">${{ (item.price * item.quantity).toFixed(2) }}</span>
            <div class="cart-qty-control">
              <button type="button" class="cart-qty-btn" @click.stop="cartStore.removeItem(item.id)">-</button>
              <span class="cart-qty-num">{{ item.quantity }}</span>
              <button type="button" class="cart-qty-btn" @click.stop="cartStore.addItem(item.id, item.price, item.name, item.imgUrl, item.options)">+</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="cart-sheet-footer">
      <button type="button" class="btn-checkout" @click="goToCheckout">
        <div class="btn-checkout-left">
          <span class="material-symbols-outlined btn-checkout-icon">shopping_cart_checkout</span>
          <span>Go to Checkout</span>
        </div>
        <span class="btn-checkout-price">${{ cartStore.totalPrice.toFixed(2) }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cartStore'

const cartStore = useCartStore()
const router = useRouter()

function goToCheckout() {
  cartStore.closeCart()
  router.push('/confirm-order')
}
</script>
