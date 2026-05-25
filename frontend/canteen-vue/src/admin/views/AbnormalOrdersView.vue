<template>
  <div class="workspace-sub-view active" id="view-abnormal">
    <div class="filter-action-bar">
      <div class="filter-left">
        <div class="search-box"><span class="material-symbols-outlined search-icon">search</span><input v-model="searchQuery" type="text" placeholder="输入订单号 (#) 搜索..." /></div>
        <div class="select-wrapper">
          <select v-model="causeFilter"><option value="">所有异常类型</option><option value="PAY_TIMEOUT">支付超时锁定</option><option value="STALL_TIMEOUT">商家接单超时</option><option value="STOCK_LOCKED">库存状态回滚失败</option></select>
        </div>
      </div>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead><tr><th>订单号</th><th>商家档口</th><th>下单时间</th><th>停留时长</th><th>异常判定原因</th><th>异常触发类型</th><th style="text-align:right">紧急干预操作</th></tr></thead>
        <tbody>
          <tr v-for="order in filteredOrders" :key="order.orderId">
            <td><strong>{{ order.orderId }}</strong></td><td>{{ order.merchant }}</td><td>{{ order.time || '-' }}</td><td>{{ order.duration }}</td><td>{{ order.cause }}</td><td>{{ order.type || order.status }}</td>
            <td style="text-align:right">
              <button class="btn-table-action action-danger" @click="confirmAction = { type: 'cancel', order }">强制取消</button>
              <button class="btn-table-action action-success" @click="confirmAction = { type: 'retry', order }">重试释放库存</button>
            </td>
          </tr>
          <tr v-if="!filteredOrders.length"><td colspan="7" class="empty-cell">暂无异常订单</td></tr>
        </tbody>
      </table>
    </div>
    <div class="dialog-scrim" :class="{ show: confirmAction }" @click="confirmAction = null"></div>
    <div class="m3-dialog" :class="{ show: confirmAction }">
      <h3 class="m3-dialog-title">紧急干预确认</h3>
      <p class="m3-dialog-content">确定对订单 {{ confirmAction?.order.orderId }} 执行{{ confirmAction?.type === 'cancel' ? '强制取消' : '库存释放重试' }}吗？</p>
      <div class="m3-dialog-actions"><button class="m3-dialog-btn btn-text" @click="confirmAction = null">取消</button><button class="m3-dialog-btn btn-filled" @click="submitAction">确认执行</button></div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, inject, onMounted } from 'vue'
import request from '@common/api/request'

const toast = inject('toast')
const abnormalOrders = ref([])
const searchQuery = ref('')
const causeFilter = ref('')
const confirmAction = ref(null)
const filteredOrders = computed(() => abnormalOrders.value.filter(order => (!searchQuery.value || String(order.orderId).includes(searchQuery.value)) && (!causeFilter.value || order.type === causeFilter.value)))
async function fetchAbnormal() {
  try {
    const data = await request.get('/admin/orders/abnormal')
    abnormalOrders.value = data?.records || data || []
  } catch (e) { abnormalOrders.value = [] }
}
async function submitAction() {
  const { type, order } = confirmAction.value
  try {
    await request.post(`/admin/orders/${order.orderId}/${type === 'cancel' ? 'manual-cancel' : 'retry-release-stock'}`)
    toast.show(type === 'cancel' ? '订单已强制取消' : '库存释放已重试', 'check_circle')
    confirmAction.value = null
    fetchAbnormal()
  } catch (e) { toast.show(e.message || '处理失败', 'error') }
}
onMounted(fetchAbnormal)
</script>
