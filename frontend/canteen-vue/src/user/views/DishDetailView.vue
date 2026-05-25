<template>
  <div class="mobile-screen state-center" id="dish-detail-view">
    <header class="screen-header justify-start gap-12" style="background-color: var(--color-bg-card);">
      <span class="material-symbols-outlined header-icon clickable" @click="$router.back()">arrow_back</span>
      <span class="dish-detail-header-title">{{ stallName }}</span>
    </header>

    <div class="dish-detail-container">
      <div class="dish-detail-hero">
        <img :src="dishImage" alt="Dish Image" />
      </div>

      <div class="dish-detail-sheet">
        <div class="dish-detail-info-group">
          <div class="dish-detail-main-info">
            <div class="dish-detail-info-left">
              <span class="dish-detail-category">{{ categoryName }}</span>
              <h1 class="dish-detail-title">{{ dish?.dishName || 'Loading...' }}</h1>
              <div class="dish-detail-stall-row">
                <span class="material-symbols-outlined">restaurant</span>
                <span>{{ stallName }}</span>
              </div>
            </div>
          </div>
          <div class="dish-detail-price-row">
            <div class="dish-detail-price">${{ priceDisplay }}</div>
          </div>
        </div>

        <div class="dish-detail-availability-card">
          <div class="availability-header">
            <span>Availability</span>
            <span class="availability-count">{{ stockLeft }} left</span>
          </div>
          <div class="availability-progress-bar">
            <div class="availability-progress-fill" :style="{ width: stockPercent + '%' }"></div>
          </div>
        </div>

        <div class="dish-detail-about-section">
          <h2>About this dish</h2>
          <p>{{ dish?.description || 'A delicious dish from our campus kitchen.' }}</p>
        </div>
      </div>
    </div>

    <div class="dish-detail-bottom-bar">
      <div v-if="cartQty > 0" class="quantity-control" style="align-self:flex-end;gap:8px;">
        <button type="button" class="btn-qty-minus" @click="cartStore.removeItem(dishId)">-</button>
        <span class="qty-number-box">{{ cartQty }}</span>
        <button type="button" class="btn-qty-plus" @click="cartStore.addItem(dishId, dish?.price, dish?.dishName, dishImage)">+</button>
      </div>
      <button
        v-else
        type="button"
        class="btn-primary dish-detail-add-btn"
        @click="addToCart"
      >
        <span class="material-symbols-outlined">shopping_cart</span>
        <span>Add to Order</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import { getDishDetail } from '@common/api/menuApi'
import { useCartStore } from '../stores/cartStore'

const route = useRoute()
const toast = inject('toast')
const cartStore = useCartStore()

const dishId = computed(() => route.params.dishId)
const stallName = ref(route.query.stallName || 'Stall')
const dish = ref(null)
const categoryName = ref('MAIN DISH')
const stockLeft = ref(0)
const stockTotal = ref(20)

const dishImgMap = {
  'Tonkotsu Ramen': '/images/tonkotsu_ramen.png',
  'Spicy Dan Dan': '/images/spicy_dan_dan.png',
  'Beef Chow Mein': '/images/beef_chow_mein.png',
  'Pork Gyoza': '/images/pork_gyoza.png',
  'Chicken Breast Meal': '/images/chicken_meal.png',
  'Gourmet Beef Burger': '/images/sour_beef.png',
  'Crispy French Fries': '/images/tofu_broccoli.png',
  'Tofu Broccoli Salad': '/images/tofu_broccoli.png',
  'Avocado Chicken Salad': '/images/chicken_meal.png',
  'Sweet Sour Plum Soup': '/images/plum_soup.png'
}

const dishImage = computed(() => {
  if (!dish.value) return ''
  return dishImgMap[dish.value.dishName] || '/images/tofu_broccoli.png'
})

const priceDisplay = computed(() => {
  return dish.value ? Number(dish.value.price).toFixed(2) : '0.00'
})

const stockPercent = computed(() => {
  return Math.max(0, (stockLeft.value / stockTotal.value) * 100)
})

const cartQty = computed(() => cartStore.getQuantity(dishId.value))

function addToCart() {
  if (!dish.value) return
  cartStore.setStall(dish.value.merchantId, stallName.value)
  cartStore.addItem(dishId.value, dish.value.price, dish.value.dishName, dishImage.value)
  toast.show(`Added ${dish.value.dishName} to cart!`, 'shopping_cart')
}

async function fetchDish() {
  try {
    const data = await getDishDetail(dishId.value)
    if (data) {
      dish.value = data
      stockLeft.value = data.remainingStock || 10
      stockTotal.value = data.totalStock || 20
      if (data.categoryName) categoryName.value = data.categoryName
    }
  } catch (e) {
    toast.show(e.message || 'Failed to load dish', 'error')
  }
}

onMounted(fetchDish)
</script>
