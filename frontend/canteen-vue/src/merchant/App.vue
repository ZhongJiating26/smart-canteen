<template>
  <div class="tablet-shell">
    <div v-if="authStore.isLoggedIn" class="merchant-layout">
      <NavRail />
      <div class="workspace">
        <header class="workspace-header">
          <div class="header-left">
            <h2 class="workspace-title">{{ currentTitle }}</h2>
            <span class="store-status-pill" :class="storeOnline ? 'online' : 'offline'">
              {{ storeOnline ? '档口营业中' : '档口休息中' }}
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
      
      <div v-if="!showManualInput">
        <p class="m3-dialog-content" style="margin-bottom:12px;">请将用户手机取餐码/二维码放入框内：</p>
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
        <div style="text-align: center; margin: 16px 0 8px;">
          <a href="javascript:void(0)" class="switch-manual-btn" @click="switchToManual" style="color: var(--color-brand-rust); font-weight: bold; text-decoration: underline; font-size: 0.95rem;">
            取餐码核销
          </a>
        </div>
        <div class="m3-dialog-actions" style="margin-top: 12px;">
          <button type="button" class="m3-dialog-btn btn-text" @click="closeScan">关闭</button>
        </div>
      </div>

      <div v-else>
        <p class="m3-dialog-content" style="margin-bottom:12px;">请输入取餐编号（数字）或取餐码进行手动核销：</p>
        <md-outlined-text-field 
          :value="pickupCode" 
          @input="pickupCode = $event.target.value" 
          label="取餐编号 / 取餐码" 
          placeholder="例如: 123456789012345 或 A001" 
          style="margin-bottom:20px; width: 100%;"
        ></md-outlined-text-field>
        <div class="m3-dialog-actions">
          <button type="button" class="m3-dialog-btn btn-text" @click="cancelManual">返回扫码</button>
          <button type="button" class="m3-dialog-btn btn-filled" :disabled="verifying" @click="verifyCode">确认核销</button>
        </div>
      </div>
    </div>

    <div v-if="toast.visible.value" class="toast show" id="merchant-toast">
      <span class="material-symbols-outlined">{{ toast.iconName.value }}</span>
      <span>{{ toast.message.value }}</span>
    </div>
  </div>
</template>

<script setup>
import { provide, ref, computed, onMounted, onBeforeUnmount } from 'vue'
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
const showManualInput = ref(false)
const scanVideo = ref(null)
const cameraReady = ref(false)
const cameraStatus = ref('正在启动摄像头...')
const refreshOrders = ref(0)
let videoStream = null

const titleMap = {
  OrderKanban: '订单处理',
  DishManagement: '菜品管理',
  DailyMenu: '每日菜单',
  OrderHistory: '历史订单'
}

const currentTitle = computed(() => {
  return titleMap[route.name] || '商家端'
})

onMounted(async () => {
  try {
    const merchant = await getCurrentMerchant()
    storeOnline.value = merchant.status === 1
  } catch (e) {
    // silent
  }
})

provide('toast', toast)
provide('storeOnline', storeOnline)
provide('refreshOrders', refreshOrders)
provide('openScan', openScan)
provide('pendingBadge', pendingBadge)

let scanTimer = null

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
    if (scanVideo.value) {
      scanVideo.value.srcObject = videoStream
      scanVideo.value.setAttribute('playsinline', true)
      scanVideo.value.play()
    }
    cameraReady.value = true
    // 启动摄像头帧扫描分析
    startScanning()
  } catch (e) {
    cameraStatus.value = '无法启动摄像头，请手动输入取餐码'
  }
}

function closeScan() {
  scanOpen.value = false
  cameraReady.value = false
  showManualInput.value = false
  clearTimeout(scanTimer)
  if (videoStream) videoStream.getTracks().forEach(track => track.stop())
  videoStream = null
}

function switchToManual() {
  showManualInput.value = true
  cameraReady.value = false
  clearTimeout(scanTimer)
  if (videoStream) {
    videoStream.getTracks().forEach(track => track.stop())
  }
  videoStream = null
}

function cancelManual() {
  showManualInput.value = false
  openScan()
}

function startScanning() {
  const video = scanVideo.value
  if (!video) return

  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')

  async function scanFrame() {
    if (!scanOpen.value || !cameraReady.value) return
    if (video.readyState === video.HAVE_ENOUGH_DATA) {
      canvas.width = video.videoWidth
      canvas.height = video.videoHeight
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      
      try {
        const jsQRModule = (await import('https://esm.run/jsqr')).default
        const code = jsQRModule(imageData.data, imageData.width, imageData.height, {
          inversionAttempts: 'dontInvert'
        })
        if (code) {
          // 扫描成功，进行后端核销
          await handleScannedCode(code.data)
          return
        }
      } catch (e) {
        console.error('QR scanner error:', e)
      }
    }
    scanTimer = setTimeout(scanFrame, 200)
  }
  
  scanFrame()
}

async function handleScannedCode(content) {
  closeScan()
  verifying.value = true
  try {
    const merchant = await getCurrentMerchant()
    // 扫码核销
    await verifyPickup({ 
      merchantId: merchant.merchantId, 
      qrcodeContent: content, 
      verifyType: 'SCAN' 
    })
    toast.show('扫码核销成功！订单已取餐', 'check_circle')
    refreshOrders.value++
  } catch (e) {
    toast.show(e.message || '扫码核销失败，该订单可能不属于本店铺', 'error')
  } finally {
    verifying.value = false
  }
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
    toast.show(`手动核销成功！取餐码 ${code} 已完成取餐`, 'check_circle')
    refreshOrders.value++
    closeScan()
  } catch (e) {
    toast.show(e.message || '核销失败，请检查取餐码是否正确且属于本档口', 'error')
  } finally {
    verifying.value = false
  }
}

onBeforeUnmount(closeScan)
</script>
