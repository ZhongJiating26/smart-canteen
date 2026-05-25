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
        <h2 class="column-title">Preparing</h2>
        <span class="count-badge badge-muted">{{ preparingList.length }} Orders</span>
      </div>
      <div class="grid-container preparing-grid">
        <div v-for="order in preparingList" :key="order.orderId || order.pickupNo" class="preparing-card">
          <span class="ticket-number">{{ order.pickupNo }}</span>
          <span class="wait-time">{{ order.waitMinutes || 0 }} mins ago</span>
          <span class="stall-name">{{ order.stallName || stallName }}</span>
        </div>
        <div v-if="preparingList.length === 0" class="empty-state">
          <span class="material-symbols-outlined">restaurant</span>
          <p>No preparing orders</p>
        </div>
      </div>
    </section>

    <section class="queue-column column-ready">
      <div class="column-header">
        <span class="material-symbols-outlined header-icon">check_circle</span>
        <h2 class="column-title">Ready for Pickup</h2>
        <span class="count-badge badge-highlight">{{ waitingList.length }} Orders Ready</span>
      </div>
      <div class="grid-container ready-grid">
        <div
          v-for="order in waitingList"
          :key="order.orderId || order.pickupNo"
          class="ready-card"
          :class="{ 'called-highlight': order.pickupNo === lastCalledTicket }"
        >
          <span class="ticket-number-large">{{ order.pickupNo }}</span>
          <span class="stall-name-small">{{ order.stallName || stallName }}</span>
        </div>
        <div v-if="waitingList.length === 0" class="empty-state">
          <span class="material-symbols-outlined">inventory</span>
          <p>No ready orders</p>
        </div>
      </div>
    </section>
  </main>

  <div v-if="showAnnouncement" class="announcement-scrim" id="announcement-overlay">
    <div class="announcement-card">
      <span class="material-symbols-outlined bell-icon">notifications</span>
      <h2 class="announcement-header">Please Collect</h2>
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

const merchantId = ref(new URLSearchParams(location.search).get('merchantId') || '1')
const stallName = ref('Noodle House')
const preparingList = ref([])
const waitingList = ref([])

const { isConnected, connectionState, lastMessage, preparingList: wsPreparing, waitingList: wsWaiting } = useWebSocket(merchantId.value)

const showAnnouncement = ref(false)
const announcedNumber = ref('')
const announcedStall = ref('')
const lastCalledTicket = ref(null)
let announceTimer = null

const dateString = ref('')
const timeString = ref('')

const connectionClass = computed(() => {
  const map = { CONNECTED: 'state-connected', CONNECTING: 'state-connecting', DISCONNECTED: 'state-disconnected', POLLING: 'state-connecting' }
  return map[connectionState.value] || 'state-disconnected'
})

const connectionLabel = computed(() => {
  const map = { CONNECTED: 'Live', CONNECTING: 'Connecting', DISCONNECTED: 'Offline', POLLING: 'Polling' }
  return map[connectionState.value] || 'Offline'
})

function updateClock() {
  const now = new Date()
  timeString.value = now.toLocaleTimeString()
  dateString.value = now.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
}

let clockTimer = null

onMounted(async () => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)

  try {
    const data = await getSnapshot(merchantId.value)
    if (data) {
      preparingList.value = data.preparingQueue || data.preparing || []
      waitingList.value = data.waitingQueue || data.waiting || []
    }
  } catch (e) { /* silent */ }

  // Sync with WebSocket data
  watch(wsPreparing, (val) => { if (val.length > 0) preparingList.value = val })
  watch(wsWaiting, (val) => { if (val.length > 0) waitingList.value = val })
})

onUnmounted(() => {
  clearInterval(clockTimer)
  clearTimeout(announceTimer)
})
</script>
