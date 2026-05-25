import { ref } from 'vue'

export function useWebSocket(merchantId) {
  const isConnected = ref(false)
  const connectionState = ref('DISCONNECTED')
  const lastMessage = ref(null)
  const preparingList = ref([])
  const waitingList = ref([])

  let ws = null
  let reconnectTimer = null
  let reconnectAttempts = 0
  let pollingTimer = null

  const MAX_RECONNECT_ATTEMPTS = 10
  const POLLING_INTERVAL = 10000

  function updateState(state) {
    connectionState.value = state
    isConnected.value = state === 'CONNECTED'
  }

  function connect() {
    if (ws && ws.readyState === WebSocket.OPEN) return

    updateState('CONNECTING')
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    const url = `${protocol}//${location.host}/ws/screen/${merchantId}`

    try {
      ws = new WebSocket(url)

      ws.onopen = () => {
        updateState('CONNECTED')
        reconnectAttempts = 0
        stopPolling()
      }

      ws.onmessage = (event) => {
        try {
          const msg = JSON.parse(event.data)
          lastMessage.value = msg
          handleMessage(msg)
        } catch (e) {
          console.warn('WebSocket message parse error:', e)
        }
      }

      ws.onclose = () => {
        updateState('DISCONNECTED')
        attemptReconnect()
      }

      ws.onerror = () => {
        ws?.close()
      }
    } catch (e) {
      updateState('DISCONNECTED')
      attemptReconnect()
    }
  }

  function handleMessage(msg) {
    switch (msg.type) {
      case 'PREPARING_UPDATE':
        if (msg.data?.preparingList) {
          preparingList.value = msg.data.preparingList
        }
        break
      case 'WAITING_UPDATE':
        if (msg.data?.waitingList) {
          waitingList.value = msg.data.waitingList
        }
        break
      case 'CALL_NOTIFY':
        break
    }
  }

  function attemptReconnect() {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      updateState('POLLING')
      startPolling()
      return
    }

    reconnectAttempts++
    const delay = Math.min(Math.pow(2, reconnectAttempts) * 1000, 30000)
    updateState('CONNECTING')

    reconnectTimer = setTimeout(() => {
      connect()
    }, delay)
  }

  function startPolling() {
    stopPolling()
    pollingTimer = setInterval(async () => {
      try {
        const { getSnapshot } = await import('@common/api/pickupApi')
        const data = await getSnapshot(merchantId)
        if (data) {
          preparingList.value = data.preparingQueue || []
          waitingList.value = data.waitingQueue || []
        }
      } catch (e) {
        console.warn('Polling fetch failed:', e)
      }
    }, POLLING_INTERVAL)
  }

  function stopPolling() {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
  }

  function disconnect() {
    clearTimeout(reconnectTimer)
    stopPolling()
    if (ws) {
      ws.onclose = null
      ws.close()
      ws = null
    }
    updateState('DISCONNECTED')
  }

  return {
    isConnected,
    connectionState,
    lastMessage,
    preparingList,
    waitingList,
    connect,
    disconnect
  }
}
