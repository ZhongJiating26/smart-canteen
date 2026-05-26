<template>
  <div class="workspace-sub-view active" id="view-merchants">
    <div class="filter-action-bar">
      <div class="filter-left">
        <div class="search-box"><span class="material-symbols-outlined search-icon">search</span><input v-model="searchQuery" type="text" placeholder="输入窗口名称/负责人..." /></div>
        <div class="select-wrapper">
          <select v-model="statusFilter"><option value="">所有状态</option><option value="1">营运中</option><option value="0">停用中</option></select>
        </div>
      </div>
      <button type="button" class="btn-primary" @click="openAdd"><span class="material-symbols-outlined">add</span><span>新增美食窗口</span></button>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead><tr><th>窗口 ID</th><th>关联账号</th><th>窗口美食名称</th><th>档口负责人</th><th>联系电话</th><th>主营分类</th><th>营业时间</th><th>营业状态</th><th style="text-align:right">操作</th></tr></thead>
        <tbody>
          <tr v-for="merchant in filteredMerchants" :key="merchant.merchantId">
            <td><strong>{{ merchant.merchantId }}</strong></td>
            <td>#{{ merchant.userId }}</td>
            <td>{{ merchant.name }}</td>
            <td>{{ merchant.contactName }}</td>
            <td>{{ merchant.contactPhone }}</td>
            <td>{{ merchant.category }}</td>
            <td>{{ formatOperatingHours(merchant.operatingHours) }}</td>
            <td><span class="status-badge" :class="merchant.status === 1 ? 'active' : 'disabled'">{{ merchant.status === 1 ? '正常营运' : '停用中' }}</span></td>
            <td style="text-align:right">
              <button class="btn-table-action" @click="openEdit(merchant)">编辑</button>
              <button class="btn-table-action" :class="merchant.status === 1 ? 'action-danger' : 'action-success'" @click="toggleStatus(merchant)">{{ merchant.status === 1 ? '停用' : '启用' }}</button>
            </td>
          </tr>
          <tr v-if="!filteredMerchants.length"><td colspan="8" class="empty-cell">暂无匹配商家窗口</td></tr>
        </tbody>
      </table>
    </div>

    <div class="dialog-scrim" :class="{ show: showDialog }" @click="closeDialog"></div>
    <div class="m3-dialog" :class="{ show: showDialog }">
      <h3 class="m3-dialog-title">{{ editingMerchant ? '编辑美食窗口配置' : '新增商家美食窗口' }}</h3>
      <div class="m3-dialog-content">
        <form class="m3-dialog-form" @submit.prevent="saveMerchant">
          <md-outlined-text-field :value="form.name" @input="form.name = $event.target.value" label="窗口美食名称" required></md-outlined-text-field>
          <md-outlined-text-field :value="form.contactName" @input="form.contactName = $event.target.value" label="档口负责人" required></md-outlined-text-field>
          <md-outlined-text-field :value="form.contactPhone" @input="form.contactPhone = $event.target.value" label="负责人联系电话" required></md-outlined-text-field>
          <div class="form-row-grid">
            <div class="select-label-wrapper"><span class="input-label">主营经营分类</span><div class="select-wrapper"><select v-model="form.category"><option v-for="category in categories" :key="category">{{ category }}</option></select></div></div>
            <div v-if="editingMerchant" class="select-label-wrapper"><span class="input-label">窗口营运状态</span><div class="select-wrapper"><select v-model.number="form.status"><option :value="1">营运中</option><option :value="0">停用整顿中</option></select></div></div>
          </div>
          <template v-if="!editingMerchant">
            <div class="account-divider"><strong>商家登录账号配置</strong></div>
            <md-outlined-text-field :value="form.phone" @input="form.phone = $event.target.value" label="登录账户手机号" required></md-outlined-text-field>
            <md-outlined-text-field :value="form.password" @input="form.password = $event.target.value" label="初始登录密码" type="password" required></md-outlined-text-field>
          </template>
          
          <div class="operating-hours-section">
            <div class="account-divider"><strong>营业时间段配置</strong></div>
            <div v-for="(range, idx) in form.operatingHoursList" :key="idx" class="operating-hours-row" style="display:flex;align-items:center;gap:12px;margin-bottom:12px;">
              <md-outlined-text-field
                type="time"
                :value="range.startTime"
                @input="range.startTime = $event.target.value"
                label="开始时间"
                required
                style="flex: 1; --md-outlined-text-field-container-shape: 8px;"
              ></md-outlined-text-field>
              <span style="color: var(--color-text-secondary); font-weight: 500;">至</span>
              <md-outlined-text-field
                type="time"
                :value="range.endTime"
                @input="range.endTime = $event.target.value"
                label="结束时间"
                required
                style="flex: 1; --md-outlined-text-field-container-shape: 8px;"
              ></md-outlined-text-field>
              <button type="button" class="btn-table-action action-danger" style="padding:8px 16px;height:56px;border-radius:8px;margin-top:0;" @click="removeOperatingHour(idx)">
                删除
              </button>
            </div>
            <button type="button" class="btn-table-action action-success" style="padding:6px 12px;display:flex;align-items:center;gap:4px;" @click="addOperatingHour">
              <span class="material-symbols-outlined" style="font-size:16px;">add</span><span>添加时间段</span>
            </button>
          </div>
        </form>
      </div>
      <div class="m3-dialog-actions">
        <button class="m3-dialog-btn btn-text" @click="closeDialog">取消</button>
        <button class="m3-dialog-btn btn-filled" @click="saveMerchant">{{ editingMerchant ? '保存修改' : '确认创建' }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, inject } from 'vue'
import request from '@common/api/request'

const toast = inject('toast')
const merchants = ref([])
const searchQuery = ref('')
const statusFilter = ref('')
const showDialog = ref(false)
const editingMerchant = ref(null)
const categories = ['面点主食', '沙拉轻食', '西式烧烤', '中式快餐', '甜品饮品']
const form = reactive({ name: '', contactName: '', contactPhone: '', category: categories[0], phone: '', password: '123456', status: 1, operatingHoursList: [] })
const filteredMerchants = computed(() => merchants.value.filter(merchant => {
  const query = searchQuery.value.toLowerCase()
  return (!query || (merchant.name || '').toLowerCase().includes(query) || (merchant.contactName || '').toLowerCase().includes(query))
    && (!statusFilter.value || String(merchant.status) === statusFilter.value)
}))
async function fetchMerchants() {
  try {
    const data = await request.get('/admin/merchants', { params: { page: 1, size: 100 } })
    merchants.value = data?.records || []
  } catch (e) { merchants.value = [] }
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
function addOperatingHour() {
  form.operatingHoursList.push({ startTime: '09:00', endTime: '21:00' })
}
function removeOperatingHour(index) {
  form.operatingHoursList.splice(index, 1)
}
function openAdd() {
  editingMerchant.value = null
  Object.assign(form, { name: '', contactName: '', contactPhone: '', category: categories[0], phone: '', password: '123456', status: 1, operatingHoursList: [{ startTime: '09:00', endTime: '21:00' }] })
  showDialog.value = true
}
function openEdit(merchant) {
  editingMerchant.value = merchant
  Object.assign(form, merchant)
  let list = [];
  try {
    list = JSON.parse(merchant.operatingHours || '[]');
  } catch (e) {
    list = [];
  }
  form.operatingHoursList = list;
  showDialog.value = true
}
function closeDialog() { showDialog.value = false }
async function saveMerchant() {
  try {
    const payload = {
      name: form.name,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      category: form.category,
      status: form.status,
      operatingHours: JSON.stringify(form.operatingHoursList)
    }
    if (editingMerchant.value) {
      await request.put(`/admin/merchants/${editingMerchant.value.merchantId}`, payload)
      toast.show('窗口配置已保存', 'check_circle')
    } else {
      await request.post('/admin/merchants', { ...payload, phone: form.phone, password: form.password })
      toast.show('商家窗口已创建', 'check_circle')
    }
    closeDialog()
    fetchMerchants()
  } catch (e) { toast.show(e.message || '保存失败', 'error') }
}
async function toggleStatus(merchant) {
  try {
    await request.put(`/admin/merchants/${merchant.merchantId}`, { name: merchant.name, contactName: merchant.contactName, contactPhone: merchant.contactPhone, category: merchant.category, status: merchant.status === 1 ? 0 : 1 })
    toast.show('营业状态已更新', 'check_circle')
    fetchMerchants()
  } catch (e) { toast.show(e.message || '更新失败', 'error') }
}
onMounted(fetchMerchants)
</script>
