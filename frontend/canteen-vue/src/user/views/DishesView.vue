<template>
  <div class="mobile-screen state-center" id="dishes-view">
    <header class="screen-header justify-between">
      <span class="material-symbols-outlined header-icon clickable" @click="$router.push('/menu')">arrow_back</span>
      <span class="screen-title">{{ stallName }}</span>
      <div style="width: 36px;"></div>
    </header>

    <div class="screen-content scrollable menu-content" style="padding-bottom: 160px;">
      <div class="stall-hero-banner">
        <img :src="heroImage" alt="Stall Banner" />
        <span class="hero-cuisine-tag">{{ cuisine }}</span>
      </div>

      <div class="stall-hours">Open until 9:00 PM</div>

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
              <p class="food-desc">{{ dish.description || 'Delicious campus dish.' }}</p>
              <div class="food-actions">
                <button
                  v-if="cartStore.getQuantity(dish.dishId) === 0"
                  type="button"
                  class="btn-add-cart-circle"
                  @click.stop="addToCart(dish)"
                >
                  <span class="material-symbols-outlined">add</span>
                </button>
                <div v-else class="quantity-control" @click.stop>
                  <button type="button" class="btn-qty-minus" @click="cartStore.removeItem(dish.dishId)">-</button>
                  <span class="qty-number-box">{{ cartStore.getQuantity(dish.dishId) }}</span>
                  <button type="button" class="btn-qty-plus" @click="cartStore.addItem(dish.dishId, dish.price, dish.dishName, dishImage(dish))">+</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="filteredDishes.length === 0 && !loading" style="text-align:center;padding:40px;color:var(--color-text-secondary);">
          <span class="material-symbols-outlined" style="font-size:3rem;">restaurant</span>
          <p style="font-weight:700;">No dishes available</p>
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
        <span class="cart-fab-label">Cart ({{ cartStore.totalCount }})</span>
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
const stallName = ref(route.query.name || 'Stall Menu')

const dishes = ref([])
const categories = ref([])
const activeCategory = ref(null)
const loading = ref(false)
const cuisine = ref('Campus Cuisine')

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

function goToDetail(dish) {
  router.push({ name: 'DishDetail', params: { dishId: dish.dishId }, query: { stallName: stallName.value } })
}

function addToCart(dish) {
  cartStore.setStall(stallId.value, stallName.value)
  cartStore.addItem(dish.dishId, dish.price, dish.dishName, dishImage(dish))
  toast.show(`Added ${dish.dishName} to cart!`, 'shopping_cart')
}

async function fetchDishes() {
  loading.value = true
  try {
    const data = await getAvailableMenu({ merchantId: stallId.value })
    if (data?.merchants?.[0]) {
      const m = data.merchants[0]
      stallName.value = m.merchantName || stallName.value
      cuisine.value = m.cuisine || cuisine.value
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
    toast.show(e.message || 'Failed to load dishes', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(fetchDishes)
</script>
