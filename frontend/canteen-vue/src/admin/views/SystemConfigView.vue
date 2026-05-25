<template>
  <div class="workspace-sub-view active" id="view-config">
    <div class="config-container">
      <div class="config-main-card">
        <div class="config-tabs-bar">
          <button v-for="tab in tabs" :key="tab.key" class="config-tab" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">{{ tab.label }}</button>
        </div>
        <form class="config-form" @submit.prevent="saveConfig">
          <div class="config-group-section" :class="{ active: activeTab === 'order' }">
            <div v-for="field in orderFields" :key="field.key" class="config-form-item">
              <div class="config-meta"><label class="config-form-label">{{ field.label }}</label><p class="config-form-desc">{{ field.description }}</p></div>
              <md-outlined-text-field :value="String(config[field.key])" @input="config[field.key] = Number($event.target.value)" type="number" style="width:200px" required></md-outlined-text-field>
            </div>
          </div>
          <div class="config-group-section" :class="{ active: activeTab === 'pickup' }">
            <div class="config-form-item">
              <div class="config-meta"><label class="config-form-label">取餐排队流水号重置规则</label><p class="config-form-desc">用于规范小票取餐码在什么时候归零重新计算流水。</p></div>
              <div class="select-wrapper" style="width:220px"><select v-model="config.pickup_number_reset_rule"><option value="NONE">不自动重置 (NONE)</option><option value="DAILY">每天重置归零 (DAILY)</option><option value="WEEKLY">每周重置归零 (WEEKLY)</option></select></div>
            </div>
          </div>
          <div class="config-group-section" :class="{ active: activeTab === 'stock' }">
            <div class="config-form-item">
              <div class="config-meta"><label class="config-form-label">极速低库存预警检测扫描间隔 (秒)</label><p class="config-form-desc">定时巡检各档口库存并触发低库存警报的时间间隔。</p></div>
              <md-outlined-text-field :value="String(config.stock_alert_scan_interval)" @input="config.stock_alert_scan_interval = Number($event.target.value)" type="number" style="width:200px" required></md-outlined-text-field>
            </div>
          </div>
          <div class="config-submit-row"><button type="submit" class="btn-primary">保存配置更改</button></div>
        </form>
      </div>
      <div class="config-side-card">
        <h3 class="widget-title">近期修改日志</h3>
        <div class="log-timeline">
          <div v-for="entry in configLogs" :key="entry.time + entry.item" class="timeline-item">
            <div class="timeline-header"><span>{{ entry.operator }}</span><span>{{ entry.time }}</span></div>
            <div class="timeline-action">{{ entry.item }}</div>
            <div class="timeline-val">{{ entry.oldValue }} -> {{ entry.newValue }}</div>
          </div>
          <p v-if="!configLogs.length" class="empty-state">暂无本次会话的配置修改记录</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted } from 'vue'
import request from '@common/api/request'

const toast = inject('toast')
const activeTab = ref('order')
const tabs = [{ key: 'order', label: '订单时限配置' }, { key: 'pickup', label: '排队取餐配置' }, { key: 'stock', label: '菜单库存配置' }]
const orderFields = [
  { key: 'pay_timeout_minutes', label: '待支付订单超时时限 (分钟)', description: '用户提交订单后未支付，系统自动释放库存的等待时限（默认 10 分钟）。' },
  { key: 'accept_timeout_minutes', label: '商家接单超时时限 (分钟)', description: '支付完成后商家未在时限内接单，订单自动取消退款。' },
  { key: 'cancel_window_minutes', label: '支付后自主取消时间窗口 (分钟)', description: '商家接单前，用户允许自主发起退单的时间窗口。' }
]
const config = reactive({ pay_timeout_minutes: 10, accept_timeout_minutes: 10, cancel_window_minutes: 1, pickup_number_reset_rule: 'DAILY', stock_alert_scan_interval: 300 })
const savedConfig = reactive({ ...config })
const configLogs = ref([])

function mapConfigItems(items) {
  ;(items || []).forEach(item => {
    if (Object.prototype.hasOwnProperty.call(config, item.configKey)) {
      config[item.configKey] = item.configType === 'INT' ? Number(item.configValue) : item.configValue
      savedConfig[item.configKey] = config[item.configKey]
    }
  })
}
onMounted(async () => {
  const results = await Promise.all(['order', 'pickup', 'menu'].map(service => request.get(`/admin/${service}/config`).catch(() => [])))
  results.forEach(mapConfigItems)
})
async function saveConfig() {
  const keys = activeTab.value === 'order' ? orderFields.map(field => field.key) : activeTab.value === 'pickup' ? ['pickup_number_reset_rule'] : ['stock_alert_scan_interval']
  const service = activeTab.value === 'stock' ? 'menu' : activeTab.value
  const changed = keys.filter(key => config[key] !== savedConfig[key])
  if (!changed.length) {
    toast.show('配置没有变化', 'info')
    return
  }
  try {
    await Promise.all(changed.map(key => request.put(`/admin/${service}/config`, { configKey: key, configValue: String(config[key]) })))
    const time = new Intl.DateTimeFormat('zh-CN', { dateStyle: 'short', timeStyle: 'medium' }).format(new Date())
    changed.forEach(key => {
      configLogs.value.unshift({ operator: 'Admin Root', item: key, oldValue: String(savedConfig[key]), newValue: String(config[key]), time })
      savedConfig[key] = config[key]
    })
    toast.show('配置成功保存，缓存已刷新', 'check_circle')
  } catch (e) { toast.show(e.message || '保存失败', 'error') }
}
</script>
