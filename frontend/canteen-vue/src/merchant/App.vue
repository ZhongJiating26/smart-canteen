<template>
  <div class="tablet-shell">
    <div v-if="authStore.isLoggedIn" class="merchant-layout">
      <NavRail />
      <div class="workspace">
        <header class="workspace-header">
          <div class="header-left">
            <h2 class="workspace-title">{{ currentTitle }}</h2>
            <span class="store-status-pill" :class="storeOnline ? 'online' : 'offline'">
              {{ storeOnline ? 'Stall Online' : 'Stall Offline' }}
            </span>
          </div>
          <div class="header-right">
            <button type="button" class="btn-scan-verify" @click="openScan()">
              <span class="material-symbols-outlined">qr_code_scanner</span>
              <span>扫码核销</span>
            </button>
          </div>
        </header>
        <div class="workspace-content">
          <router-view />
        </div>
      </div>
    </div>
    <router-view v-else v-slot="{ Component }">
      <component :is="Component" />
    </router-view>

    <div class="dialog-scrim" :class="{ show: scanOpen }" @click="closeScan"></div>
    <div class="m3-dialog" :class="{ show: scanOpen }" style="max-width:440px;">
      <h3 class="m3-dialog-title" style="display:flex;align-items:center;gap:8px;">
        <span class="material-symbols-outlined" style="color:var(--color-brand-rust);">qr_code_scanner</span>
        <span>扫码核销</span>
      </h3>
      <p class="m3-dialog-content" style="margin-bottom:12px;">请将用户手机取餐码放入框内，或在下方手动输入取餐码：</p>
      <div id="scan-camera-container">
        <video ref="scanVideo" id="scan-video" autoplay playsinline :style="{ display: cameraReady ? 'block' : 'none' }"></video>
        <div id="scan-viewfinder-overlay" :style="{ display: cameraReady ? 'block' : 'none' }">
          <div class="viewfinder-corner top-left"></div>
          <div class="viewfinder-corner top-right"></div>
          <div class="viewfinder-corner bottom-left"></div>
          <div class="viewfinder-corner bottom-right"></div>
          <div class="scan-laser"></div>
        </div>
        <div v-if="!cameraReady" id="scan-camera-placeholder">
          <span class="material-symbols-outlined">photo_camera</span>
          <span id="scan-camera-status-text">{{ cameraStatus }}</span>
        </div>
      </div>
      <md-outlined-text-field :value="pickupCode" @input="pickupCode = $event.target.value" label="订单号 / 取餐码" placeholder="例如: A1000" style="margin-bottom:20px;"></md-outlined-text-field>
      <div class="m3-dialog-actions">
        <button type="button" class="m3-dialog-btn btn-text" @click="closeScan">取消</button>
        <button type="button" class="m3-dialog-btn btn-filled" :disabled="verifying" @click="verifyCode">确认核销</button>
      </div>
    </div>

    <div v-if="toast.visible.value" class="toast show" id="merchant-toast">
      <span class="material-symbols-outlined">{{ toast.iconName.value }}</span>
      <span>{{ toast.message.value }}</span>
    </div>
  </div>
</template>

<script setup>
import { provide, ref, computed, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@common/stores/authStore'
import { useToast } from '@common/composables/useToast'
import { getCurrentMerchant } from '@common/api/menuApi'
import { verifyPickup } from '@common/api/pickupApi'
import NavRail from './components/NavRail.vue'

const authStore = useAuthStore()
const route = useRoute()
const toast = useToast()
const storeOnline = ref(true)
const pendingBadge = ref(0)
const scanOpen = ref(false)
const pickupCode = ref('')
const verifying = ref(false)
const scanVideo = ref(null)
const cameraReady = ref(false)
const cameraStatus = ref('正在启动摄像头...')
const refreshOrders = ref(0)
let videoStream = null

const titleMap = {
  OrderKanban: '订单处理',
  DishManagement: '菜品管理',
  DailyMenu: '每日菜单',
  OrderHistory: '历史订单',
  StoreConfig: '店铺信息'
}

const currentTitle = computed(() => {
  return titleMap[route.name] || 'Merchant Portal'
})

provide('toast', toast)
provide('storeOnline', storeOnline)
provide('refreshOrders', refreshOrders)
provide('openScan', openScan)
provide('pendingBadge', pendingBadge)

async function openScan(order = null) {
  pickupCode.value = order?.pickupNo || ''
  scanOpen.value = true
  cameraStatus.value = '正在获取摄像头权限...'
  if (!navigator.mediaDevices?.getUserMedia) {
    cameraStatus.value = '当前环境不支持摄像头输入'
    return
  }
  try {
    videoStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
    if (scanVideo.value) scanVideo.value.srcObject = videoStream
    cameraReady.value = true
  } catch (e) {
    cameraStatus.value = '无法启动摄像头，请手动输入取餐码'
  }
}

function closeScan() {
  scanOpen.value = false
  cameraReady.value = false
  if (videoStream) videoStream.getTracks().forEach(track => track.stop())
  videoStream = null
}

async function verifyCode() {
  const code = pickupCode.value.trim()
  if (!code) {
    toast.show('请输入订单号或取餐码！', 'warning')
    return
  }
  verifying.value = true
  try {
    const merchant = await getCurrentMerchant()
    await verifyPickup({ merchantId: merchant.merchantId, pickupCode: code, verifyType: 'MANUAL' })
    toast.show(`核销成功！取餐码 ${code} 已完成取餐`, 'check_circle')
    refreshOrders.value++
    closeScan()
  } catch (e) {
    toast.show(e.message || '核销失败，请检查取餐码', 'error')
  } finally {
    verifying.value = false
  }
}

onBeforeUnmount(closeScan)
</script>
