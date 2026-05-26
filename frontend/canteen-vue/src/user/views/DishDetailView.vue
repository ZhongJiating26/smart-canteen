<template>
  <div class="mobile-screen state-center" id="dish-detail-view">
    <header class="screen-header justify-start gap-12" style="background-color: var(--color-bg-card);">
      <span class="header-icon clickable" @click="$router.back()">
        <span class="material-symbols-outlined">arrow_back</span>
      </span>
      <span class="dish-detail-header-title">{{ stallName }}</span>
    </header>

    <div class="dish-detail-container">
      <div class="dish-detail-hero">
        <img :src="dishImage" alt="Dish Image" />
      </div>

      <div class="dish-detail-sheet">
        <!-- Warning Banner if not operating -->
        <div v-if="!operating" class="closed-warning-banner" style="background-color: #ffdad6; color: #410002; padding: 12px 16px; margin-bottom: 16px; border-radius: 12px; display: flex; align-items: center; gap: 8px; font-weight: bold; font-size: 0.9rem;">
          <span class="material-symbols-outlined" style="color: #ba1a1a;">warning</span>
          <span>当前不在营业时间内，无法加入购物车</span>
        </div>

        <div class="dish-detail-info-group">
          <div class="dish-detail-main-info">
            <div class="dish-detail-info-left">
              <span class="dish-detail-category">{{ categoryName }}</span>
              <h1 class="dish-detail-title">{{ dish?.dishName || '加载中...' }}</h1>
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
            <span>今日供应量</span>
            <span class="availability-count">仅剩 {{ stockLeft }} 份</span>
          </div>
          <div class="availability-progress-bar">
            <div class="availability-progress-fill" :style="{ width: stockPercent + '%' }"></div>
          </div>
        </div>

        <div class="dish-detail-about-section">
          <h2>关于本菜品</h2>
          <p>{{ dish?.description || '来自校园厨房的美味菜品。' }}</p>
        </div>
      </div>
    </div>

    <div class="dish-detail-bottom-bar">
      <div v-if="cartQty > 0" class="quantity-control" style="align-self:flex-end;gap:8px;">
        <button type="button" class="btn-qty-minus" :disabled="!operating" @click="animateQty($event, -1)">-</button>
        <span class="qty-number-box">{{ cartQty }}</span>
        <button type="button" class="btn-qty-plus" :disabled="!operating" @click="animateQty($event, 1)">+</button>
      </div>
      <button
        v-else
        type="button"
        class="btn-primary dish-detail-add-btn"
        :disabled="!operating"
        @click="addToCart"
      >
        <span class="material-symbols-outlined">shopping_cart</span>
        <span>加入购物车</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import { getDishDetail, getAvailableMenu } from '@common/api/menuApi'
import { useCartStore } from '../stores/cartStore'

const route = useRoute()
const toast = inject('toast')
const cartStore = useCartStore()

const dishId = computed(() => route.params.dishId)
const dailyMenuItemId = computed(() => route.query.dailyMenuItemId)
const stallName = ref(route.query.stallName || '档口')
const dish = ref(null)
const categoryName = ref('主食')
const stockLeft = ref(0)
const stockTotal = ref(20)
const operating = ref(true)
const operatingHours = ref('')

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

const cartQty = computed(() => cartStore.getQuantity(dailyMenuItemId.value))

function addToCart() {
  if (!dish.value) return
  if (!operating.value) {
    toast.show('当前不在营业时间内，无法加入购物车', 'warning')
    return
  }
  if (!dailyMenuItemId.value) {
    toast.show('请从当前可用菜单中添加该菜品。', 'warning')
    return
  }
  cartStore.setStall(dish.value.merchantId, stallName.value)
  cartStore.addItem(dailyMenuItemId.value, dish.value.price, dish.value.dishName, dishImage.value)
  toast.show(`已将 ${dish.value.dishName} 加入购物车！`, 'shopping_cart')
}

function animateQty(event, change) {
  if (!dish.value) return
  if (change === 1) {
    cartStore.addItem(dailyMenuItemId.value, dish.value.price, dish.value.dishName, dishImage.value)
  } else {
    cartStore.removeItem(dailyMenuItemId.value)
  }

  const button = event.currentTarget
  const control = button.parentElement
  if (!control) return
  const numBox = control.querySelector('.qty-number-box')
  if (!numBox) return

  button.classList.remove('qty-btn-anim')
  numBox.classList.remove('qty-num-anim')

  void button.offsetWidth
  void numBox.offsetWidth

  button.classList.add('qty-btn-anim')
  numBox.classList.add('qty-num-anim')

  const onEnd = () => {
    button.classList.remove('qty-btn-anim')
    numBox.classList.remove('qty-num-anim')
    button.removeEventListener('animationend', onEnd)
  }
  button.addEventListener('animationend', onEnd)
}

async function fetchDish() {
  try {
    const data = await getDishDetail(dishId.value)
    if (data) {
      dish.value = { ...data, dishName: data.dishName || data.name }
      stockLeft.value = data.remainingStock || 10
      stockTotal.value = data.totalStock || 20
      if (data.categoryName) categoryName.value = data.categoryName
      
      if (data.merchantId) {
        const menuData = await getAvailableMenu({ merchantId: data.merchantId })
        if (menuData?.merchants?.[0]) {
          operating.value = menuData.merchants[0].operating !== false
          operatingHours.value = menuData.merchants[0].operatingHours || ''
        }
      }
    }
  } catch (e) {
    toast.show(e.message || '加载菜品详情失败', 'error')
  }
}

onMounted(fetchDish)
</script>
