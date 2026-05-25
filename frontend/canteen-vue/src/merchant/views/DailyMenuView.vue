<template>
  <div class="workspace-sub-view" id="view-daily-menu">
    <div class="dishes-header-row" style="margin-bottom:20px;">
      <div class="segmented-button-group">
        <button type="button" class="segmented-button" :class="{ active: activeCategory === null }" @click="activeCategory = null">全部</button>
        <button v-for="category in categories" :key="category.categoryId" type="button" class="segmented-button" :class="{ active: activeCategory === category.categoryId }" @click="activeCategory = category.categoryId">{{ category.name }}</button>
      </div>
      <button type="button" class="btn-create-dish" @click="openAddDialog">
        <span class="material-symbols-outlined">add</span><span>添加菜品</span>
      </button>
    </div>

    <div class="dishes-workspace-layout">
      <div class="dishes-list-column">
        <div class="m3-list-card">
          <div v-for="item in filteredItems" :key="item.dishId" class="m3-list-item" :class="{ active: selectedItem?.dishId === item.dishId }" @click="selectedItem = item">
            <img class="list-item-avatar" :src="dishImage(item.dish)" :alt="item.dish?.name" />
            <div class="list-item-text">
              <span class="list-item-title">{{ item.dish?.name || '未知菜品' }}</span>
              <span class="list-item-subtitle">库存: {{ item.remainingStock ?? item.dailyStock }} · ${{ price(item.dish?.price) }}</span>
            </div>
            <span v-if="item.isListed !== 1" class="list-item-status-tag">下架</span>
          </div>
          <div v-if="!filteredItems.length" class="empty-state">
            <span class="material-symbols-outlined">restaurant_menu</span><p>暂无每日供应菜品</p>
          </div>
        </div>
      </div>
      <div class="dishes-detail-column">
        <div class="dish-detail-card">
          <div v-if="selectedItem" class="dish-detail-content">
            <div class="dish-detail-main-block">
              <div class="dish-detail-left-col" style="gap:12px;">
                <span class="detail-display-category-badge">{{ categoryName(selectedItem.dish?.categoryId) }}</span>
                <h1 class="detail-display-title">{{ selectedItem.dish?.name }}</h1>
                <div class="detail-display-price">${{ price(selectedItem.dish?.price) }}</div>
                <div class="detail-display-stock">当前剩余库存：<strong>{{ selectedItem.remainingStock ?? selectedItem.dailyStock }}</strong> 件</div>
              </div>
              <div class="dish-detail-right-col">
                <div class="detail-image-wrapper"><img :src="dishImage(selectedItem.dish)" class="detail-image" :alt="selectedItem.dish?.name" /></div>
              </div>
            </div>
            <div class="daily-stock-summary">
              <span class="dish-section-label">每日供应库存设置</span>
              <p>自动更新库存：每日重置为 <strong>{{ selectedItem.dailyStock }}</strong> 件</p>
              <button type="button" class="m3-dialog-btn btn-text" @click="stockOpen = true">修改规则</button>
            </div>
            <div class="slide-to-remove-container">
              <div class="slide-text">点击移出每日菜单</div>
              <button type="button" class="slide-handle" @click="removeSelected">
                <span class="material-symbols-outlined">delete</span>
              </button>
            </div>
          </div>
          <div v-else class="detail-placeholder">
            <span class="material-symbols-outlined">restaurant_menu</span><p>请在左侧选择一个今日供应的菜品以查看详情</p>
          </div>
        </div>
      </div>
    </div>

    <div class="dialog-scrim" :class="{ show: addOpen }" @click="addOpen = false"></div>
    <div class="m3-dialog daily-add-dialog" :class="{ show: addOpen }">
      <h3 class="m3-dialog-title dialog-title-icon">
        <span class="material-symbols-outlined">add_to_photos</span><span>添加每日供应菜品</span>
      </h3>
      <div class="daily-dialog-body">
        <div class="daily-picker-column">
          <div v-for="dish in availableDishes" :key="dish.dishId" class="daily-picker-row dialog-dish-row" @click="toggleCandidate(dish.dishId)">
            <img :src="dishImage(dish)" class="list-item-avatar" :alt="dish.name" />
            <div class="list-item-text">
              <span class="list-item-title">{{ dish.name }}</span>
              <span class="list-item-subtitle">{{ categoryName(dish.categoryId) }} · ${{ price(dish.price) }}</span>
            </div>
            <md-checkbox :checked="checkedDishIds.includes(dish.dishId)"></md-checkbox>
          </div>
          <div v-if="!availableDishes.length" class="empty-state"><p>没有可添加的菜品</p></div>
        </div>
        <div class="daily-preview-column">
          <div class="daily-preview-header"><span>已勾选菜品</span><span class="selection-count">{{ checkedDishIds.length }}</span></div>
          <div v-for="dish in checkedDishes" :key="dish.dishId" class="dialog-preview-row">
            <span>{{ dish.name }}</span>
            <button class="icon-button" type="button" @click="toggleCandidate(dish.dishId)"><span class="material-symbols-outlined">close</span></button>
          </div>
        </div>
      </div>
      <div class="m3-dialog-actions dialog-footer">
        <button type="button" class="m3-dialog-btn btn-text" @click="addOpen = false">取消</button>
        <button type="button" class="m3-dialog-btn btn-filled" @click="addChecked">确认添加</button>
      </div>
    </div>

    <div class="dialog-scrim dialog-over-scrim" :class="{ show: stockOpen }" @click="stockOpen = false"></div>
    <div class="m3-dialog dialog-over" :class="{ show: stockOpen }">
      <h3 class="m3-dialog-title dialog-title-icon">
        <span class="material-symbols-outlined">settings_backup_restore</span><span>每日供应自动更新库存</span>
      </h3>
      <p class="m3-dialog-content">每日菜单启用时，将该菜品库存重置为指定数量。</p>
      <md-outlined-text-field :value="stockValue" @input="stockValue = $event.target.value" label="库存数量 (件)" type="number"></md-outlined-text-field>
      <div class="m3-dialog-actions dialog-footer">
        <button type="button" class="m3-dialog-btn btn-text" @click="stockOpen = false">取消</button>
        <button type="button" class="m3-dialog-btn btn-filled" @click="saveStock">确认修改</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { getCurrentMerchant, getCategories, getDishes, getDailyMenus, saveDailyMenu, updateDailyMenuItems } from '@common/api/menuApi'

const toast = inject('toast')
const merchant = ref(null)
const categories = ref([])
const dishes = ref([])
const menu = ref(null)
const items = ref([])
const activeCategory = ref(null)
const selectedItem = ref(null)
const addOpen = ref(false)
const stockOpen = ref(false)
const checkedDishIds = ref([])
const stockValue = ref(50)

const filteredItems = computed(() => activeCategory.value === null ? items.value : items.value.filter(item => item.dish?.categoryId === activeCategory.value))
const currentIds = computed(() => new Set(items.value.map(item => item.dishId)))
const availableDishes = computed(() => dishes.value.filter(dish => !currentIds.value.has(dish.dishId) && dish.status === 1))
const checkedDishes = computed(() => dishes.value.filter(dish => checkedDishIds.value.includes(dish.dishId)))

watch(stockOpen, open => {
  if (open && selectedItem.value) stockValue.value = selectedItem.value.dailyStock
})

function price(value) {
  return Number(value || 0).toFixed(2)
}

function dishImage(dish) {
  return dish?.hasImage ? `/api/menu/files/${dish.dishId}/image` : '/images/beef_chow_mein.png'
}

function categoryName(id) {
  return categories.value.find(category => category.categoryId === id)?.name || '未分类'
}

function attachDishes(source) {
  const byId = Object.fromEntries(dishes.value.map(dish => [dish.dishId, dish]))
  return (source || []).filter(item => item.isListed === 1).map(item => ({ ...item, dish: byId[item.dishId] }))
}

async function loadMenu() {
  const menus = await getDailyMenus({ merchantId: merchant.value.merchantId, mealPeriod: 'LUNCH' })
  menu.value = menus?.[0] || null
  items.value = attachDishes(menu.value?.items)
  selectedItem.value = items.value.find(item => item.dishId === selectedItem.value?.dishId) || items.value[0] || null
}

function requestItems() {
  return items.value.map(item => ({ dishId: item.dishId, dailyStock: Number(item.dailyStock) || 0, alertThreshold: Number(item.alertThreshold) || 10, isListed: 1 }))
}

async function persistItems() {
  if (!items.value.length) {
    toast.show('每日菜单至少需要保留一个菜品', 'warning')
    return false
  }
  const saved = menu.value
    ? await updateDailyMenuItems(menu.value.menuId, { items: requestItems() })
    : await saveDailyMenu({ merchantId: merchant.value.merchantId, mealPeriod: 'LUNCH', status: 1, items: requestItems() })
  menu.value = saved
  items.value = attachDishes(saved.items)
  return true
}

function openAddDialog() {
  checkedDishIds.value = []
  addOpen.value = true
}

function toggleCandidate(id) {
  checkedDishIds.value = checkedDishIds.value.includes(id) ? checkedDishIds.value.filter(item => item !== id) : [...checkedDishIds.value, id]
}

async function addChecked() {
  if (!checkedDishIds.value.length) return toast.show('未勾选任何菜品！', 'warning')
  items.value.push(...checkedDishIds.value.map(id => ({ dishId: id, dailyStock: 50, remainingStock: 50, alertThreshold: 10, isListed: 1, dish: dishes.value.find(dish => dish.dishId === id) })))
  try {
    await persistItems()
    addOpen.value = false
    toast.show(`成功添加 ${checkedDishIds.value.length} 个菜品到每日菜单！`, 'check_circle')
    await loadMenu()
  } catch (e) {
    toast.show(e.message || '添加失败', 'error')
  }
}

async function saveStock() {
  const amount = Number(stockValue.value)
  if (!Number.isInteger(amount) || amount < 0) return toast.show('请输入有效的库存数量！', 'warning')
  selectedItem.value.dailyStock = amount
  try {
    await persistItems()
    stockOpen.value = false
    toast.show(`“${selectedItem.value.dish.name}”的供应库存规则修改成功！`, 'check_circle')
    await loadMenu()
  } catch (e) {
    toast.show(e.message || '库存修改失败', 'error')
  }
}

async function removeSelected() {
  if (items.value.length === 1) return toast.show('每日菜单至少需要保留一个菜品', 'warning')
  const name = selectedItem.value.dish.name
  items.value = items.value.filter(item => item.dishId !== selectedItem.value.dishId)
  try {
    await persistItems()
    toast.show(`已将“${name}”移出每日菜单`, 'check_circle')
    await loadMenu()
  } catch (e) {
    toast.show(e.message || '移出失败', 'error')
  }
}

onMounted(async () => {
  try {
    merchant.value = await getCurrentMerchant()
    const [categoryData, dishData] = await Promise.all([getCategories(merchant.value.merchantId), getDishes({ page: 1, size: 100 })])
    categories.value = categoryData || []
    dishes.value = dishData?.records || []
    await loadMenu()
  } catch (e) {
    toast.show(e.message || '每日菜单加载失败', 'error')
  }
})
</script>
