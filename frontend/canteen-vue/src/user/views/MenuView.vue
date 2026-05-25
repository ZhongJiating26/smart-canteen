<template>
  <div class="mobile-screen state-center" id="menu-view">
    <header class="screen-header">
      <span class="brand-title">Campus Eats</span>
    </header>

    <div class="screen-content scrollable menu-content">
      <div class="promo-banner">
        <h2 class="promo-title">Hungry, {{ authStore.nickname || 'Friend' }}?</h2>
        <p class="promo-desc">Find your favorite campus stall and skip the queue.</p>
        <div class="promo-chips">
          <span class="promo-chip">
            <span class="material-symbols-outlined icon-small">local_fire_department</span>
            Popular Now
          </span>
          <span class="promo-chip">
            <span class="material-symbols-outlined icon-small">schedule</span>
            Fast Pickup
          </span>
        </div>
      </div>

      <div class="search-container">
        <md-outlined-text-field
          :value="searchQuery"
          @input="searchQuery = $event.target.value"
          placeholder="Search for stalls or dishes"
        >
          <span slot="leading-icon" class="material-symbols-outlined">search</span>
          <span slot="trailing-icon" class="material-symbols-outlined filter-icon">tune</span>
        </md-outlined-text-field>
      </div>

      <div class="category-scroll-container">
        <div class="category-chips-row">
          <div
            v-for="cat in stallCategories"
            :key="cat.id"
            class="chip"
            :class="{ active: activeCategory === cat.id }"
            @click="activeCategory = cat.id"
          >
            <span class="material-symbols-outlined chip-icon">{{ cat.icon }}</span>
            {{ cat.label }}
          </div>
        </div>
      </div>

      <div class="stall-list">
        <div
          v-for="merchant in filteredMerchants"
          :key="merchant.merchantId"
          class="stall-card clickable-stall"
          @click="goToDishes(merchant.merchantId, merchant.merchantName)"
        >
          <img class="stall-img" :src="merchantImage(merchant.merchantName)" :alt="merchant.merchantName" />
          <div class="stall-info">
            <h4 class="stall-title">{{ merchant.merchantName }}</h4>
            <p class="stall-cuisine">{{ merchant.cuisine || 'Campus Cuisine' }}</p>
          </div>
          <div class="stall-status-col">
            <span class="stall-sales-tag">920+ Sales</span>
            <span class="stall-queue-tag">{{ merchant.queueCount || 0 }} orders</span>
          </div>
        </div>

        <div v-if="filteredMerchants.length === 0 && !loading" style="text-align:center;padding:40px;color:var(--color-text-secondary);">
          <span class="material-symbols-outlined" style="font-size:3rem;margin-bottom:12px;color:var(--color-text-muted);">storefront</span>
          <p style="font-weight:700;">No stalls found</p>
          <p style="font-size:0.85rem;margin-top:4px;">Try adjusting your search or filter.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'
import { getAvailableMenu } from '@common/api/menuApi'

const authStore = useAuthStore()
const router = useRouter()
const toast = inject('toast')

const merchants = ref([])
const searchQuery = ref('')
const activeCategory = ref('all')
const loading = ref(false)

const stallCategories = [
  { id: 'all', label: 'All Stalls', icon: 'grid_view' },
  { id: 'noodles', label: 'Noodles', icon: 'ramen_dining' },
  { id: 'western', label: 'Western', icon: 'lunch_dining' },
  { id: 'healthy', label: 'Healthy', icon: 'eco' },
  { id: 'rice', label: 'Rice Bowls', icon: 'rice_bowl' },
  { id: 'drinks', label: 'Drinks', icon: 'local_cafe' },
  { id: 'desserts', label: 'Desserts', icon: 'cookie' }
]

const merchantCategoryMap = {
  'Noodle House': 'noodles',
  'Western Grill': 'western',
  'Healthy Bowls': 'healthy'
}

const filteredMerchants = computed(() => {
  let result = merchants.value
  const cat = activeCategory.value
  if (cat !== 'all') {
    result = result.filter(m => (merchantCategoryMap[m.merchantName] || 'all') === cat)
  }
  const q = searchQuery.value.toLowerCase().trim()
  if (q) {
    result = result.filter(m => m.merchantName.toLowerCase().includes(q))
  }
  return result
})

const merchantImages = {
  'Noodle House': '/images/sour_beef.png',
  'Western Grill': '/images/chicken_meal.png',
  'Healthy Bowls': '/images/tofu_broccoli.png'
}

function merchantImage(name) {
  return merchantImages[name] || '/images/tofu_broccoli.png'
}

function goToDishes(merchantId, merchantName) {
  router.push({ name: 'Dishes', params: { stallId: merchantId }, query: { name: merchantName } })
}

async function fetchMenu() {
  loading.value = true
  try {
    const data = await getAvailableMenu()
    if (data && data.merchants) {
      merchants.value = data.merchants
    }
  } catch (e) {
    toast.show(e.message || 'Failed to load menu', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(fetchMenu)
</script>
