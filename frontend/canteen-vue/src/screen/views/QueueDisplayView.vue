<template>
  <header class="screen-header">
    <div class="header-left">
      <h1 class="brand-title">Campus Eats</h1>
      <span class="header-date">{{ dateString }}</span>
    </div>
    <div class="header-right">
      <div class="time-pill">
        <span class="clock-time">{{ timeString }}</span>
      </div>
      <div class="live-pill" :class="connectionClass">
        <span class="live-dot"></span>
        <span class="live-text">{{ connectionLabel }}</span>
      </div>
    </div>
  </header>

  <main class="columns-layout">
    <section class="queue-column column-preparing">
      <div class="column-header">
        <span class="material-symbols-outlined header-icon">restaurant</span>
        <h2 class="column-title">制作中</h2>
        <span class="count-badge badge-muted">{{ preparingList.length }} 个订单</span>
      </div>
      <div class="grid-container preparing-grid">
        <div v-for="order in preparingList" :key="order.orderId || order.pickupNo" class="preparing-card">
          <div class="preparing-card-header">
            <span class="ticket-num">{{ order.pickupNo }}</span>
            <span class="wait-time">已等候 {{ order.waitMinutes || 0 }} 分钟</span>
          </div>
          <span class="stall-name">{{ order.stallName || stallName }}</span>
        </div>
        <div v-if="preparingList.length === 0" class="empty-state">
          <span class="material-symbols-outlined">restaurant</span>
          <p>暂无制作中订单</p>
        </div>
      </div>
    </section>

    <section class="queue-column column-ready">
      <div class="column-header">
        <span class="material-symbols-outlined header-icon">check_circle</span>
        <h2 class="column-title">请取餐</h2>
        <span class="count-badge badge-highlight">{{ waitingList.length }} 个订单可取</span>
      </div>
      <div class="grid-container ready-grid">
        <div
          v-for="order in waitingList"
          :key="order.orderId || order.pickupNo"
          class="ready-card"
          :class="{ 'called-highlight': order.pickupNo === lastCalledTicket }"
        >
          <span class="ticket-num">{{ order.pickupNo }}</span>
          <span class="stall-name">{{ order.stallName || stallName }}</span>
        </div>
        <div v-if="waitingList.length === 0" class="empty-state">
          <span class="material-symbols-outlined">inventory</span>
          <p>暂无待取餐订单</p>
        </div>
      </div>
    </section>
  </main>

  <div class="announcement-scrim" :class="{ show: showAnnouncement }" id="announcement-overlay">
    <div class="announcement-card">
      <span class="material-symbols-outlined bell-icon">notifications</span>
      <h2 class="announcement-header">请取餐</h2>
      <div class="number-block-container">
        <span class="announcement-number">{{ announcedNumber }}</span>
      </div>
      <div class="announcement-stall">{{ announcedStall }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useWebSocket } from '@common/composables/useWebSocket'
import { getSnapshot } from '@common/api/pickupApi'
import { getAvailableMenu } from '@common/api/menuApi'

const merchantId = ref(new URLSearchParams(location.search).get('merchantId') || '2000001')
const stallName = ref('特色档口')
const preparingList = ref([])
const waitingList = ref([])

const { isConnected, connectionState, lastMessage, preparingList: wsPreparing, waitingList: wsWaiting, connect, disconnect } = useWebSocket(merchantId.value)

const showAnnouncement = ref(false)
const announcedNumber = ref('')
const announcedStall = ref('')
const lastCalledTicket = ref(null)
let announceTimer = null

const dateString = ref('')
const timeString = ref('')
let snapshotTimer = null
let latestCallKey = null

const connectionClass = computed(() => {
  const map = { CONNECTED: 'state-connected', CONNECTING: 'state-connecting', DISCONNECTED: 'state-disconnected', POLLING: 'state-connecting' }
  return map[connectionState.value] || 'state-disconnected'
})

const connectionLabel = computed(() => {
  const map = { CONNECTED: '实时连接', CONNECTING: '连接中', DISCONNECTED: '已断开', POLLING: '轮询中' }
  return map[connectionState.value] || '已断开'
})

function updateClock() {
  const now = new Date()
  timeString.value = now.toLocaleTimeString('zh-CN')
  dateString.value = now.toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
}

let clockTimer = null

function showCallAnnouncement(message) {
  if (message?.type !== 'CALL_NOTIFY' || !message.data?.pickupNo) {
    return
  }
  announcedNumber.value = message.data.pickupNo
  announcedStall.value = stallName.value
  lastCalledTicket.value = message.data.pickupNo
  showAnnouncement.value = true
  clearTimeout(announceTimer)
  announceTimer = setTimeout(() => {
    showAnnouncement.value = false
    lastCalledTicket.value = null
  }, 5000)
}

function callKey(call) {
  return call ? `${call.orderId}:${call.callTime}:${call.callType}` : null
}

async function refreshSnapshot(announceNewCall = false) {
  try {
    const data = await getSnapshot(merchantId.value)
    if (!data) {
      return
    }
    preparingList.value = data.preparingQueue || data.preparing || []
    waitingList.value = data.waitingQueue || data.waiting || []

    const latestCall = data.recentCalls?.[0]
    const nextCallKey = callKey(latestCall)
    if (announceNewCall && latestCall && latestCallKey && nextCallKey !== latestCallKey) {
      showCallAnnouncement({ type: 'CALL_NOTIFY', data: latestCall })
    }
    latestCallKey = nextCallKey
  } catch (e) { /* silent */ }
}

onMounted(async () => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)

  // 启动 WebSocket 连接
  connect()

  // 从可售菜单/商户接口中查询真实的餐厅窗口名称
  try {
    const menuData = await getAvailableMenu()
    if (menuData?.merchants) {
      const currentMerchant = menuData.merchants.find(
        m => String(m.merchantId) === String(merchantId.value)
      )
      if (currentMerchant) {
        stallName.value = currentMerchant.merchantName
      }
    }
  } catch (e) {
    // 失败则保留默认 fallback
  }

  await refreshSnapshot()
  snapshotTimer = setInterval(() => refreshSnapshot(true), 2000)

  // Sync with WebSocket data
  watch(wsPreparing, (val) => { preparingList.value = val })
  watch(wsWaiting, (val) => { if (val) waitingList.value = val })
  watch(lastMessage, showCallAnnouncement)
})

onUnmounted(() => {
  clearInterval(clockTimer)
  clearInterval(snapshotTimer)
  clearTimeout(announceTimer)
  // 断开 WebSocket 连接并清除定时器
  disconnect()
})
</script>
