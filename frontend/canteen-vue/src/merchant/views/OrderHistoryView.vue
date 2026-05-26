<template>
  <div class="workspace-sub-view" id="view-history">
    <div class="history-list-wrapper">
      <div class="history-table-header">
        <span>订单号</span>
        <span>顾客</span>
        <span>商品</span>
        <span>总金额</span>
        <span>完成时间</span>
        <span>状态</span>
        <span>操作</span>
      </div>
      <div class="history-items-container">
        <div v-for="order in historyOrders" :key="order.orderId" class="history-row">
          <span class="history-id">{{ order.orderNo || '#' + order.orderId }}</span>
          <span class="history-customer">{{ customerName(order) }}</span>
          <span class="history-items">{{ itemText(order) }}</span>
          <span class="history-price">￥{{ Number(order.totalAmount).toFixed(2) }}</span>
          <span class="history-time">{{ formatTime(order.completeTime || order.cancelTime) }}</span>
          <span class="history-status-badge" :class="order.status === 'COMPLETED' ? 'completed' : 'cancelled'">
            {{ order.status === 'COMPLETED' ? '已完成' : '已取消' }}
          </span>
          <span class="history-actions">
            <button type="button" class="btn-detail-view" @click.stop="selectedOrder = order">详情</button>
          </span>
        </div>
        <div v-if="!historyOrders.length" class="empty-state">
          <span class="material-symbols-outlined">history</span>
          <p>暂无历史订单</p>
        </div>
      </div>
    </div>

    <!-- 订单详细信息对话框 -->
    <div class="dialog-scrim" :class="{ show: selectedOrder }" @click="selectedOrder = null"></div>
    <div class="m3-dialog" :class="{ show: selectedOrder }" style="max-width: 500px;">
      <h3 class="m3-dialog-title" style="display:flex;align-items:center;gap:8px;">
        <span class="material-symbols-outlined" style="color:var(--color-brand-rust);">receipt_long</span>
        <span>订单详细信息</span>
      </h3>
      <div v-if="selectedOrder" class="m3-dialog-content" style="max-height: 400px; overflow-y: auto; text-align: left;">
        <div class="detail-section" style="display:flex; flex-direction:column; gap:10px; font-size: 0.9rem;">
          <div style="display:flex; justify-content:space-between; border-bottom:1px solid #f0f0f0; padding-bottom:6px;">
            <strong style="color:var(--color-text-secondary);">订单号:</strong>
            <span>{{ selectedOrder.orderNo || selectedOrder.orderId }}</span>
          </div>
          <div style="display:flex; justify-content:space-between; border-bottom:1px solid #f0f0f0; padding-bottom:6px;">
            <strong style="color:var(--color-text-secondary);">客户信息:</strong>
            <span>{{ customerName(selectedOrder) }}</span>
          </div>
          <div style="display:flex; justify-content:space-between; border-bottom:1px solid #f0f0f0; padding-bottom:6px;">
            <strong style="color:var(--color-text-secondary);">总额:</strong>
            <span style="font-weight:bold; color:var(--color-brand-rust);">￥{{ Number(selectedOrder.totalAmount).toFixed(2) }}</span>
          </div>
          <div style="display:flex; justify-content:space-between; border-bottom:1px solid #f0f0f0; padding-bottom:6px;">
            <strong style="color:var(--color-text-secondary);">订单状态:</strong>
            <span class="history-status-badge" :class="selectedOrder.status === 'COMPLETED' ? 'completed' : 'cancelled'" style="display:inline-block; padding:2px 8px;">
              {{ selectedOrder.status === 'COMPLETED' ? '已完成' : '已取消' }}
            </span>
          </div>
          <div style="display:flex; justify-content:space-between; border-bottom:1px solid #f0f0f0; padding-bottom:6px;">
            <strong style="color:var(--color-text-secondary);">下单时间:</strong>
            <span>{{ formatTime(selectedOrder.createTime) }}</span>
          </div>
          <div style="display:flex; justify-content:space-between; border-bottom:1px solid #f0f0f0; padding-bottom:6px;">
            <strong style="color:var(--color-text-secondary);">完成/取消时间:</strong>
            <span>{{ formatTime(selectedOrder.completeTime || selectedOrder.cancelTime) }}</span>
          </div>
          <div v-if="selectedOrder.remark" style="display:flex; flex-direction:column; border-bottom:1px solid #f0f0f0; padding-bottom:6px; gap:4px;">
            <strong style="color:var(--color-text-secondary);">备注信息:</strong>
            <span style="background-color:#fafafa; padding:6px 10px; border-radius:8px; font-size:0.85rem; color:#555;">{{ selectedOrder.remark }}</span>
          </div>
          <div v-if="selectedOrder.cancelReason" style="display:flex; flex-direction:column; border-bottom:1px solid #f0f0f0; padding-bottom:6px; gap:4px;">
            <strong style="color:var(--color-text-secondary);">取消原因:</strong>
            <span style="background-color:#fff3f3; padding:6px 10px; border-radius:8px; font-size:0.85rem; color:#c62828;">{{ selectedOrder.cancelReason }}</span>
          </div>
          
          <div style="margin-top:10px;">
            <strong style="color:var(--color-text-secondary); display:block; margin-bottom:8px;">选购商品:</strong>
            <div style="display:flex; flex-direction:column; gap:6px; background-color:#fafafa; padding:10px; border-radius:12px;">
              <div v-for="item in (selectedOrder.items || [])" :key="item.id" style="display:flex; justify-content:space-between; font-size:0.85rem;">
                <span>{{ item.dishName }} <strong style="color:#666;">x{{ item.quantity }}</strong></span>
                <span style="font-weight:bold;">￥{{ (Number(item.unitPrice || 0) * item.quantity).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="m3-dialog-actions">
        <button type="button" class="m3-dialog-btn btn-filled" @click="selectedOrder = null">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted, watch } from 'vue'
import { getMerchantOrders, getOrderDetail } from '@common/api/orderApi'
import { formatTime as fmt } from '@common/utils/format'

const refreshOrders = inject('refreshOrders')
const historyOrders = ref([])
const selectedOrder = ref(null)

function formatTime(dateStr) {
  return dateStr ? fmt(dateStr) : '-'
}

function customerName(order) {
  return order.customer || (order.userId ? `用户 #${order.userId}` : '-')
}

function itemText(order) {
  return (order.items || []).map(item => `${item.dishName} x${item.quantity}`).join(', ') || '-'
}

async function fetchHistory() {
  const lists = await Promise.all(['COMPLETED', 'CANCELLED'].map(status => getMerchantOrders({ status })))
  const summaries = lists.flat()
  historyOrders.value = await Promise.all(summaries.map(async order => {
    try {
      return { ...order, ...(await getOrderDetail(order.orderId)) }
    } catch (e) {
      return order
    }
  }))
}

watch(refreshOrders, fetchHistory)
onMounted(fetchHistory)
</script>

<style scoped>
.btn-detail-view {
  background-color: var(--color-bg-card);
  color: var(--color-brand-rust);
  border: 1px solid var(--color-brand-rust);
  border-radius: 100px;
  padding: 4px 14px;
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-detail-view:hover {
  background-color: var(--color-brand-peach);
  color: var(--color-brand-rust);
}

.btn-detail-view:active {
  transform: scale(0.96);
}
</style>
