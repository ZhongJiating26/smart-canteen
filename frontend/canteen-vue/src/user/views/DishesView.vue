<template>
  <div class="mobile-screen state-center" id="dishes-view">
    <header class="screen-header justify-between">
      <span class="header-icon clickable" @click="$router.push('/menu')">
        <span class="material-symbols-outlined">arrow_back</span>
      </span>
      <span class="screen-title">{{ stallName }}</span>
      <div style="width: 36px;"></div>
    </header>

    <div class="screen-content scrollable menu-content" style="padding-bottom: 160px;">
      <div class="stall-hero-banner">
        <img :src="heroImage" alt="Stall Banner" />
        <span class="hero-cuisine-tag">{{ cuisine }}</span>
      </div>

      <div class="stall-hours">营业时间: {{ formatOperatingHours(operatingHours) }}</div>

      <div v-if="!operating" class="closed-warning-banner" style="background-color: #ffdad6; color: #410002; padding: 12px 16px; margin: 0 4px 20px; border-radius: 12px; display: flex; align-items: center; gap: 8px; font-weight: bold; font-size: 0.9rem;">
        <span class="material-symbols-outlined" style="color: #ba1a1a;">warning</span>
        <span>当前不在营业时间内，无法加入购物车</span>
      </div>

      <div class="dish-category-row-container">
        <div class="dish-category-row">
          <button
            v-for="cat in categories"
            :key="cat.categoryId"
            type="button"
            class="dish-chip"
            :class="{ active: activeCategory === cat.categoryId }"
            @click="activeCategory = cat.categoryId"
          >
            {{ cat.categoryName }}
          </button>
        </div>
      </div>

      <div class="food-list">
        <div
          v-for="dish in filteredDishes"
          :key="dish.dishId"
          class="food-card"
          @click="goToDetail(dish)"
        >
          <img class="food-img" :src="dishImage(dish)" :alt="dish.dishName" />
          <div class="food-info">
            <div class="food-header">
              <h4 class="food-title">{{ dish.dishName }}</h4>
              <span class="food-price">${{ Number(dish.price).toFixed(2) }}</span>
            </div>
            <div class="food-desc-actions-card">
              <p class="food-desc">{{ dish.description || '美味校园菜品。' }}</p>
              <div class="food-actions">
                <button
                  v-if="cartStore.getQuantity(dish.dailyMenuItemId) === 0"
                  type="button"
                  class="btn-add-cart-circle"
                  :disabled="!operating"
                  @click.stop="addToCart(dish)"
                >
                  <span class="material-symbols-outlined">add</span>
                </button>
                <div v-else class="quantity-control" @click.stop>
                  <button type="button" class="btn-qty-minus" :disabled="!operating" @click="animateQty($event, dish.dailyMenuItemId, -1)">-</button>
                  <span class="qty-number-box">{{ cartStore.getQuantity(dish.dailyMenuItemId) }}</span>
                  <button type="button" class="btn-qty-plus" :disabled="!operating" @click="animateQty($event, dish.dailyMenuItemId, 1)">+</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="filteredDishes.length === 0 && !loading" style="text-align:center;padding:40px;color:var(--color-text-secondary);">
          <span class="material-symbols-outlined" style="font-size:3rem;">restaurant</span>
          <p style="font-weight:700;">暂无可用菜品</p>
        </div>
      </div>
    </div>

    <div
      class="cart-floating-bar"
      :class="{ hidden: cartStore.isEmpty }"
      @click="cartStore.openCart()"
    >
      <div class="cart-floating-left">
        <span class="material-symbols-outlined cart-fab-icon">shopping_cart</span>
        <span class="cart-fab-label">购物车 ({{ cartStore.totalCount }})</span>
      </div>
      <div class="cart-floating-price">${{ cartStore.totalPrice.toFixed(2) }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAvailableMenu } from '@common/api/menuApi'
import { useCartStore } from '../stores/cartStore'

const route = useRoute()
const router = useRouter()
const toast = inject('toast')
const cartStore = useCartStore()

const stallId = computed(() => route.params.stallId)
const stallName = ref(route.query.name || '档口菜单')

const dishes = ref([])
const categories = ref([])
const activeCategory = ref(null)
const loading = ref(false)
const cuisine = ref('校园美食')
const operating = ref(true)
const operatingHours = ref('')

const heroImages = {
  'Noodle House': '/images/noodle_house_hero.png',
  'Western Grill': '/images/western_grill_hero.png',
  'Healthy Bowls': '/images/healthy_bowls_hero.png'
}

const heroImage = computed(() => heroImages[stallName.value] || '/images/noodle_house_hero.png')

const filteredDishes = computed(() => {
  if (!activeCategory.value) return dishes.value
  return dishes.value.filter(d => d.categoryId === activeCategory.value)
})

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

function dishImage(dish) {
  return dishImgMap[dish.dishName] || '/images/tofu_broccoli.png'
}

function formatOperatingHours(json) {
  if (!json) return '未配置'
  try {
    const list = JSON.parse(json)
    if (!list || !list.length) return '未配置'
    return list.map(range => `${range.startTime}-${range.endTime}`).join(', ')
  } catch (e) {
    return '格式错误'
  }
}

function goToDetail(dish) {
  router.push({
    name: 'DishDetail',
    params: { dishId: dish.dishId },
    query: { stallName: stallName.value, dailyMenuItemId: dish.dailyMenuItemId }
  })
}

function addToCart(dish) {
  if (!operating.value) {
    toast.show('当前不在营业时间内，无法加入购物车', 'warning')
    return
  }
  cartStore.setStall(stallId.value, stallName.value)
  cartStore.addItem(dish.dailyMenuItemId, dish.price, dish.dishName, dishImage(dish))
  toast.show(`已将 ${dish.dishName} 加入购物车！`, 'shopping_cart')
}

function animateQty(event, itemId, change) {
  if (change === 1) {
    const dish = dishes.value.find(d => d.dailyMenuItemId === itemId)
    if (dish) {
      cartStore.addItem(itemId, dish.price, dish.dishName, dishImage(dish))
    }
  } else {
    cartStore.removeItem(itemId)
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

async function fetchDishes() {
  loading.value = true
  try {
    const data = await getAvailableMenu({ merchantId: stallId.value })
    if (data?.merchants?.[0]) {
      const m = data.merchants[0]
      stallName.value = m.merchantName || stallName.value
      cuisine.value = m.cuisine || cuisine.value
      operating.value = m.operating !== false
      operatingHours.value = m.operatingHours || ''
      if (m.categories) {
        categories.value = m.categories
        const allDishes = []
        m.categories.forEach(cat => {
          if (cat.dishes) {
            cat.dishes.forEach(d => {
              d.categoryId = cat.categoryId
              d.categoryName = cat.categoryName
              allDishes.push(d)
            })
          }
        })
        dishes.value = allDishes
        if (m.categories.length > 0) {
          activeCategory.value = m.categories[0].categoryId
        }
      }
    }
  } catch (e) {
    toast.show(e.message || '加载菜品失败', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(fetchDishes)
</script>
