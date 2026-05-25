import { ref, onUnmounted } from 'vue'

export function usePolling(fn, intervalMs = 5000) {
  const isPolling = ref(false)
  let timer = null

  function start() {
    if (isPolling.value) return
    isPolling.value = true
    fn()
    timer = setInterval(fn, intervalMs)
  }

  function stop() {
    clearInterval(timer)
    timer = null
    isPolling.value = false
  }

  onUnmounted(() => stop())

  return { isPolling, start, stop }
}
