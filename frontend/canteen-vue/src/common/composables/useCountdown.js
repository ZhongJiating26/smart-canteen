import { ref } from 'vue'

export function useCountdown(initialSeconds = 60) {
  const countdown = ref(0)
  const isRunning = ref(false)
  let timer = null

  function start() {
    if (isRunning.value) return
    isRunning.value = true
    countdown.value = initialSeconds
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
        isRunning.value = false
      }
    }, 1000)
  }

  function reset() {
    clearInterval(timer)
    timer = null
    isRunning.value = false
    countdown.value = 0
  }

  return { countdown, isRunning, start, reset }
}
