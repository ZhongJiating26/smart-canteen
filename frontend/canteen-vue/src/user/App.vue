<template>
  <div class="phone-shell">
    <router-view v-slot="{ Component, route }">
      <transition :name="transitionName" mode="out-in">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>

    <CartSheet />

    <BottomNav />

    <div v-if="toast.visible.value" class="toast show">
      <span class="material-symbols-outlined" id="toast-icon">{{ toast.iconName.value }}</span>
      <span id="toast-msg">{{ toast.message.value }}</span>
    </div>
  </div>
</template>

<script setup>
import { provide, ref } from 'vue'
import { useToast } from '@common/composables/useToast'
import BottomNav from './components/BottomNav.vue'
import CartSheet from './components/CartSheet.vue'

const toast = useToast()
const transitionName = ref('slide-left')

provide('toast', toast)
provide('transitionName', transitionName)
</script>
