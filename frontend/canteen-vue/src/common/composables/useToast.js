import { ref } from 'vue'

export function useToast() {
  const visible = ref(false)
  const message = ref('')
  const iconName = ref('info')
  let timer = null

  function show(msg, icon = 'info') {
    clearTimeout(timer)
    message.value = msg
    iconName.value = icon
    visible.value = true
    timer = setTimeout(() => {
      visible.value = false
    }, 3000)
  }

  function hide() {
    clearTimeout(timer)
    visible.value = false
  }

  return { visible, message, iconName, show, hide }
}
