<template>
  <div class="mobile-screen state-center" id="menu-view">
    <header class="screen-header">
      <span class="brand-title">Campus Eats</span>
    </header>

    <div class="screen-content scrollable menu-content">
      <div class="promo-banner">
        <template v-if="authStore.isLoggedIn">
          <h2 class="promo-title">饿了吗，{{ authStore.nickname || '朋友' }}？</h2>
          <p class="promo-desc">选择你喜欢的校园档口，跳过排队，轻松自提。</p>
        </template>
        <template v-else>
          <h2 class="promo-title">请先登录</h2>
          <p class="promo-desc">登录后即可下单</p>
          <button class="promo-login-btn" @click="goToLogin">
            立即登录
          </button>
        </template>
        <div class="promo-chips">
          <span class="promo-chip">
            <span class="material-symbols-outlined icon-small">local_fire_department</span>
            当下热门
          </span>
          <span class="promo-chip">
            <span class="material-symbols-outlined icon-small">schedule</span>
            快速自提
          </span>
        </div>
      </div>

      <div class="search-container">
        <md-outlined-text-field
          :value="searchQuery"
          @input="searchQuery = $event.target.value"
          placeholder="搜索档口或菜品"
        >
          <span slot="leading-icon" class="material-symbols-outlined">search</span>
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
          :class="{ 'stall-card-inactive': !merchant.operating }"
          @click="goToDishes(merchant.merchantId, merchant.merchantName)"
        >
          <img class="stall-img" :src="merchantImage(merchant.merchantName)" :alt="merchant.merchantName" />
          <div class="stall-info">
            <div style="display:flex;align-items:center;gap:8px;">
              <h4 class="stall-title">{{ merchant.merchantName }}</h4>
            </div>
            <p class="stall-cuisine">{{ merchant.cuisine || '校园美食' }}</p>
          </div>
          <div class="stall-status-col">
            <span class="stall-sales-tag">月售 {{ merchant.monthlySales || 0 }}</span>
            <span v-if="merchant.operating" class="stall-queue-tag">{{ merchant.queueCount || 0 }} 单制作中</span>
            <span v-else class="stall-queue-tag closed-queue-tag" style="background-color: #ffdad6; color: #ba1a1a; font-weight: 700;">休息中</span>
          </div>
        </div>

        <div v-if="filteredMerchants.length === 0 && !loading" style="text-align:center;padding:40px;color:var(--color-text-secondary);">
          <span class="material-symbols-outlined" style="font-size:3rem;margin-bottom:12px;color:var(--color-text-muted);">storefront</span>
          <p style="font-weight:700;">未找到相关档口</p>
          <p style="font-size:0.85rem;margin-top:4px;">尝试调整您的搜索词或分类过滤。</p>
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
  { id: 'all', label: '所有档口', icon: 'grid_view' },
  { id: 'noodles', label: '美味面点', icon: 'ramen_dining' },
  { id: 'western', label: '西式美食', icon: 'lunch_dining' },
  { id: 'healthy', label: '健康轻食', icon: 'eco' },
  { id: 'rice', label: '美味盖饭', icon: 'rice_bowl' },
  { id: 'drinks', label: '特色饮品', icon: 'local_cafe' },
  { id: 'desserts', label: '精美甜点', icon: 'cookie' }
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
    result = result.filter(m => {
      // 匹配档口名称
      if (m.merchantName && m.merchantName.toLowerCase().includes(q)) {
        return true
      }
      // 匹配该档口下的任何菜品名称
      if (m.categories) {
        return m.categories.some(category => 
          category.dishes && category.dishes.some(dish => 
            dish.dishName && dish.dishName.toLowerCase().includes(q)
          )
        )
      }
      return false
    })
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

function goToLogin() {
  router.push({ name: 'Login' })
}

async function fetchMenu() {
  loading.value = true
  try {
    const data = await getAvailableMenu()
    if (data && data.merchants) {
      merchants.value = data.merchants
    }
  } catch (e) {
    toast.show(e.message || '加载菜单失败', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(fetchMenu)
</script>
