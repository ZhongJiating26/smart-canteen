<template>
  <div class="workspace-sub-view" id="view-dishes">
    <div class="dishes-header-row">
      <div class="segmented-button-group">
        <button type="button" class="segmented-button" :class="{ active: activeCategory === null }" @click="activeCategory = null">全部</button>
        <button v-for="category in categories" :key="category.categoryId" type="button" class="segmented-button" :class="{ active: activeCategory === category.categoryId }" @click="activeCategory = category.categoryId">
          {{ category.name }}
        </button>
      </div>
      <button type="button" class="btn-create-dish" @click="openCreate">
        <span class="material-symbols-outlined">add</span>
        <span>创建新菜品</span>
      </button>
    </div>

    <div class="dishes-workspace-layout">
      <div class="dishes-list-column">
        <div class="m3-list-card">
          <div v-for="dish in filteredDishes" :key="dish.dishId" class="m3-list-item" :class="{ active: selectedDish?.dishId === dish.dishId }" @click="selectedDish = dish">
            <img class="list-item-avatar" :src="dishImage(dish)" :alt="dish.name" />
            <div class="list-item-text">
              <span class="list-item-title">{{ dish.name }}</span>
              <span class="list-item-subtitle" :style="stockStyle(dish)">{{ stockText(dish) }} · ￥{{ price(dish.price) }}</span>
            </div>
            <span v-if="dish.status !== 1" class="list-item-status-tag">下架</span>
          </div>
          <div v-if="!filteredDishes.length" class="empty-state">
            <span class="material-symbols-outlined">restaurant</span>
            <p>暂无菜品</p>
          </div>
        </div>
      </div>
      <div class="dishes-detail-column">
        <div class="dish-detail-card">
          <div v-if="selectedDish" class="dish-detail-content">
            <div class="dish-detail-main-block">
              <div class="dish-detail-left-col" style="gap:12px;">
                <div style="display:flex;align-items:center;gap:8px;">
                  <span class="detail-display-category-badge">{{ categoryName(selectedDish.categoryId) }}</span>
                  <span class="detail-display-status-badge" :class="selectedDish.status === 1 ? 'on-shelf' : 'off-shelf'">
                    {{ selectedDish.status === 1 ? '已上架' : '已下架' }}
                  </span>
                </div>
                <h1 class="detail-display-title">{{ selectedDish.name }}</h1>
                <div class="detail-display-price">￥{{ price(selectedDish.price) }}</div>
                <div class="detail-display-stock">剩余库存：<strong>{{ remainingStock(selectedDish) }}</strong> 件</div>
              </div>
              <div class="dish-detail-right-col">
                <div class="detail-image-wrapper">
                  <img :src="dishImage(selectedDish)" :alt="selectedDish.name" class="detail-image" />
                </div>
              </div>
            </div>
            <div style="margin-top:12px;padding-top:16px;border-top:1px solid var(--color-border);">
              <h3 class="dish-section-label">菜品介绍</h3>
              <p class="detail-display-desc">{{ selectedDish.description || '暂无介绍。' }}</p>
            </div>
            <div class="detail-actions-row">
              <button type="button" class="btn-save-daily-menu detail-edit-button" @click="openEdit(selectedDish)">
                <span class="material-symbols-outlined">edit</span><span>修改</span>
              </button>
            </div>
          </div>
          <div v-else class="detail-placeholder">
            <span class="material-symbols-outlined">restaurant</span>
            <p>请在左侧选择一个菜品以查看或修改详情</p>
          </div>
        </div>
      </div>
    </div>

    <div class="dialog-scrim" :class="{ show: formOpen }" @click="closeForm"></div>
    <div class="m3-dialog dish-edit-dialog" :class="{ show: formOpen }">
      <h3 class="m3-dialog-title dialog-title-icon">
        <span class="material-symbols-outlined">{{ editingDish ? 'edit_note' : 'add_to_photos' }}</span>
        <span>{{ editingDish ? '修改菜品详情' : '创建新菜品' }}</span>
      </h3>
      <div class="dish-form-grid">
        <div class="dish-detail-left-col">
          <md-outlined-text-field :value="form.name" @input="form.name = $event.target.value" label="菜品名称"></md-outlined-text-field>
          <md-outlined-text-field :value="form.price" @input="form.price = $event.target.value" label="价格 (元)" type="number"></md-outlined-text-field>
          <md-outlined-text-field v-if="!editingDish" :value="form.stock" @input="form.stock = $event.target.value" label="初始库存" type="number"></md-outlined-text-field>
          <div v-else>
            <label class="form-field-label">每日供应库存</label>
            <div class="quantity-control" id="dish-detail-qty-control">
              <button type="button" class="btn-qty-minus" :disabled="form.stock <= 0" @click="form.stock--">-</button>
              <span class="qty-number-box">{{ form.stock }}</span>
              <button type="button" class="btn-qty-plus" @click="form.stock++">+</button>
            </div>
          </div>
          <div>
            <label class="form-field-label">菜品分类</label>
            <div class="segmented-button-group dialog-category-group">
              <button v-for="category in categories" :key="category.categoryId" type="button" class="segmented-button" :class="{ active: form.categoryId === category.categoryId }" @click="form.categoryId = category.categoryId">{{ category.name }}</button>
              <button type="button" class="segmented-button" @click="categoryOpen = true">+ 新增</button>
            </div>
          </div>
        </div>
        <div class="dish-detail-right-col">
          <label class="detail-image-wrapper editable">
            <img :src="imagePreview || dishImage(editingDish)" class="detail-image" alt="菜品图片预览" />
            <span class="image-upload-overlay"><span class="material-symbols-outlined">photo_camera</span><span>更换图片</span></span>
            <input type="file" accept="image/*" class="file-input" @change="handleImage" />
          </label>
        </div>
      </div>
      <div v-if="editingDish" class="dish-status-row">
        <div>
          <div class="config-label">上架状态</div>
          <div class="config-desc">控制顾客是否可以下单购买</div>
        </div>
        <md-switch :selected="form.available" @change="form.available = $event.target.selected"></md-switch>
      </div>
      <md-outlined-text-field :value="form.description" @input="form.description = $event.target.value" label="菜品介绍" type="textarea" rows="2"></md-outlined-text-field>
      <div class="m3-dialog-actions dialog-footer">
        <button type="button" class="m3-dialog-btn btn-text" @click="closeForm">取消修改</button>
        <button type="button" class="m3-dialog-btn btn-filled" :disabled="saving" @click="saveDish">保存修改</button>
      </div>
    </div>

    <div class="dialog-scrim dialog-over-scrim" :class="{ show: categoryOpen }" @click="categoryOpen = false"></div>
    <div class="m3-dialog dialog-over" :class="{ show: categoryOpen }">
      <h3 class="m3-dialog-title dialog-title-icon">
        <span class="material-symbols-outlined">create_new_folder</span><span>新增菜品分类</span>
      </h3>
      <md-outlined-text-field :value="newCategory" @input="newCategory = $event.target.value" label="分类名称" placeholder="例如: 甜品"></md-outlined-text-field>
      <div class="m3-dialog-actions dialog-footer">
        <button type="button" class="m3-dialog-btn btn-text" @click="categoryOpen = false">取消</button>
        <button type="button" class="m3-dialog-btn btn-filled" @click="saveCategory">确认创建</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, inject, onMounted, reactive, ref, watch } from 'vue'
import { getCurrentMerchant, getCategories, createCategory, getDishes, createDish, updateDish, deleteDish, uploadDishImage, getDailyMenus, saveDailyMenu, updateDailyMenuItems } from '@common/api/menuApi'

const toast = inject('toast')
const merchant = ref(null)
const dishes = ref([])
const categories = ref([])
const activeCategory = ref(null)
const selectedDish = ref(null)
const menuStock = ref({})
const formOpen = ref(false)
const categoryOpen = ref(false)
const editingDish = ref(null)
const imageFile = ref(null)
const imagePreview = ref('')
const newCategory = ref('')
const saving = ref(false)
const form = reactive({ name: '', price: '', stock: 50, categoryId: null, description: '', available: true })

const filteredDishes = computed(() => activeCategory.value === null ? dishes.value : dishes.value.filter(dish => dish.categoryId === activeCategory.value))

watch(activeCategory, () => {
  selectedDish.value = filteredDishes.value[0] || null
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

function remainingStock(dish) {
  return menuStock.value[dish?.dishId]?.remainingStock ?? 0
}

function stockText(dish) {
  const stock = remainingStock(dish)
  if (stock === 0) return '已售罄'
  if (stock <= 10) return `库存紧张 (${stock})`
  return `库存: ${stock}`
}

function stockStyle(dish) {
  const stock = remainingStock(dish)
  return stock === 0 ? 'color:var(--color-error);font-weight:600;' : stock <= 10 ? 'color:var(--color-warning);font-weight:600;' : ''
}

function resetForm() {
  Object.assign(form, { name: '', price: '', stock: 50, categoryId: categories.value[0]?.categoryId || null, description: '', available: true })
  imageFile.value = null
  imagePreview.value = ''
}

function openCreate() {
  editingDish.value = null
  resetForm()
  formOpen.value = true
}

function openEdit(dish) {
  editingDish.value = dish
  Object.assign(form, { name: dish.name, price: price(dish.price), stock: remainingStock(dish), categoryId: dish.categoryId, description: dish.description || '', available: dish.status === 1 })
  imageFile.value = null
  imagePreview.value = ''
  formOpen.value = true
}

function closeForm() {
  formOpen.value = false
  editingDish.value = null
}

function handleImage(event) {
  const file = event.target.files?.[0]
  if (!file) return
  imageFile.value = file
  imagePreview.value = URL.createObjectURL(file)
}

async function loadDishes() {
  const data = await getDishes({ page: 1, size: 100 })
  dishes.value = data?.records || []
  if (selectedDish.value) selectedDish.value = dishes.value.find(dish => dish.dishId === selectedDish.value.dishId) || null
  if (!selectedDish.value && dishes.value.length) selectedDish.value = dishes.value[0]
}

async function loadMenuStock() {
  const menus = await getDailyMenus({ merchantId: merchant.value.merchantId })
  menuStock.value = Object.fromEntries((menus || []).flatMap(menu => menu.items || []).map(item => [item.dishId, item]))
}

async function saveCategory() {
  const name = newCategory.value.trim()
  if (!name) return toast.show('分类名称不能为空！', 'warning')
  try {
    const category = await createCategory({ name, sort: categories.value.length + 1 })
    categories.value.push(category)
    form.categoryId = category.categoryId
    newCategory.value = ''
    categoryOpen.value = false
    toast.show(`成功创建分类：${name}`, 'check_circle')
  } catch (e) {
    toast.show(e.message || '分类创建失败', 'error')
  }
}

async function syncDailyStock(dishId) {
  const menus = await getDailyMenus({ merchantId: merchant.value.merchantId, mealPeriod: 'LUNCH' })
  const dailyMenu = menus?.[0]
  const existing = dailyMenu?.items || []
  const item = { dishId, dailyStock: Number(form.stock) || 0, alertThreshold: 10, isListed: 1 }
  const items = [...existing.filter(existingItem => existingItem.dishId !== dishId).map(existingItem => ({
    dishId: existingItem.dishId,
    dailyStock: existingItem.dailyStock,
    alertThreshold: existingItem.alertThreshold,
    isListed: existingItem.isListed
  })), item]
  if (dailyMenu) {
    await updateDailyMenuItems(dailyMenu.menuId, { items })
  } else {
    await saveDailyMenu({ merchantId: merchant.value.merchantId, mealPeriod: 'LUNCH', status: 1, items })
  }
}

async function saveDish() {
  if (!form.name.trim() || !form.categoryId || Number(form.price) < 0 || Number(form.stock) < 0) {
    toast.show('请完整填写菜品名称、价格、库存和分类！', 'warning')
    return
  }
  if (editingDish.value && form.available && editingDish.value.status !== 1) {
    toast.show('当前服务仅支持下架，重新上架需补充服务端操作接口', 'warning')
    return
  }
  saving.value = true
  try {
    const payload = { name: form.name.trim(), price: Number(form.price), description: form.description.trim(), categoryId: form.categoryId }
    let saved = editingDish.value ? await updateDish(editingDish.value.dishId, payload) : await createDish(payload)
    if (editingDish.value && !form.available && editingDish.value.status === 1) {
      await deleteDish(editingDish.value.dishId)
    }
    if (imageFile.value) saved = await uploadDishImage(saved.dishId, imageFile.value)
    await syncDailyStock(saved.dishId)
    toast.show(`成功${editingDish.value ? '修改' : '创建'}菜品：${saved.name}`, 'check_circle')
    closeForm()
    await Promise.all([loadDishes(), loadMenuStock()])
  } catch (e) {
    toast.show(e.message || '菜品保存失败', 'error')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    merchant.value = await getCurrentMerchant()
    categories.value = await getCategories(merchant.value.merchantId)
    await Promise.all([loadDishes(), loadMenuStock()])
  } catch (e) {
    toast.show(e.message || '菜品加载失败', 'error')
  }
})
</script>
