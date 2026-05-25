import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref({})
  const stallId = ref(null)
  const stallName = ref('')
  const isOpen = ref(false)

  const totalCount = computed(() => {
    return Object.values(items.value).reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalPrice = computed(() => {
    return Object.values(items.value).reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  const itemList = computed(() => {
    return Object.entries(items.value).map(([id, item]) => ({ id, ...item }))
  })

  const isEmpty = computed(() => totalCount.value === 0)

  function setStall(id, name) {
    if (stallId.value && stallId.value !== id && !isEmpty.value) {
      // Switching stalls clears cart
      clearCart()
    }
    stallId.value = id
    stallName.value = name
  }

  function addItem(id, price, name, imgUrl, options = '') {
    if (!items.value[id]) {
      items.value[id] = { price, name, imgUrl, options, quantity: 0 }
    }
    items.value[id].quantity++
  }

  function removeItem(id) {
    if (!items.value[id]) return
    items.value[id].quantity--
    if (items.value[id].quantity <= 0) {
      delete items.value[id]
    }
  }

  function setQuantity(id, qty) {
    if (qty <= 0) {
      delete items.value[id]
      return
    }
    if (items.value[id]) {
      items.value[id].quantity = qty
    }
  }

  function getQuantity(id) {
    return items.value[id]?.quantity || 0
  }

  function clearCart() {
    items.value = {}
    stallId.value = null
    stallName.value = ''
  }

  function openCart() {
    isOpen.value = true
  }

  function closeCart() {
    isOpen.value = false
  }

  function toggleCart() {
    isOpen.value = !isOpen.value
  }

  return {
    items,
    stallId,
    stallName,
    isOpen,
    totalCount,
    totalPrice,
    itemList,
    isEmpty,
    setStall,
    addItem,
    removeItem,
    setQuantity,
    getQuantity,
    clearCart,
    openCart,
    closeCart,
    toggleCart
  }
})
