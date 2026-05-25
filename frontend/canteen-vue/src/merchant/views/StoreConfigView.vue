<template>
  <div class="workspace-sub-view" id="view-store">
    <div class="store-config-card">
      <div class="config-row">
        <div class="config-info">
          <span class="config-label">Operating Status</span>
          <span class="config-desc">Toggle to open or close the kitchen for online ordering</span>
        </div>
        <md-switch :selected="storeOnline" @change="updateOnline($event.target.selected)"></md-switch>
      </div>
      <div class="config-divider"></div>
      <div class="config-fields-grid">
        <md-outlined-text-field :value="form.storeName" @input="form.storeName = $event.target.value" label="Stall Name">
          <span class="material-symbols-outlined" slot="leading-icon">store</span>
        </md-outlined-text-field>
        <md-outlined-text-field :value="form.cuisineType" @input="form.cuisineType = $event.target.value" label="Cuisine Type">
          <span class="material-symbols-outlined" slot="leading-icon">restaurant</span>
        </md-outlined-text-field>
        <md-outlined-text-field :value="form.operatingHours" @input="form.operatingHours = $event.target.value" label="Operating Hours">
          <span class="material-symbols-outlined" slot="leading-icon">schedule</span>
        </md-outlined-text-field>
        <md-outlined-text-field :value="form.contactPhone" @input="form.contactPhone = $event.target.value" label="Contact Hotline">
          <span class="material-symbols-outlined" slot="leading-icon">phone</span>
        </md-outlined-text-field>
      </div>
      <div class="config-button-row">
        <button type="button" class="btn-save-config" @click="saveConfig">Save Store Config</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject, onMounted, reactive } from 'vue'
import { getCurrentMerchant } from '@common/api/menuApi'

const toast = inject('toast')
const storeOnline = inject('storeOnline')
const form = reactive({
  storeName: 'Noodle House',
  cuisineType: 'Ramen & Dumplings',
  operatingHours: '11:00 AM - 9:30 PM',
  contactPhone: '+1 (555) 309-8812'
})

function updateOnline(value) {
  storeOnline.value = value
  toast.show(value ? 'Store has been set to ONLINE' : 'Store has been set to OFFLINE', value ? 'check_circle' : 'error')
}

function saveConfig() {
  if (!form.storeName.trim()) {
    toast.show('Stall name cannot be empty!', 'warning')
    return
  }
  toast.show('Store configurations saved successfully!', 'check_circle')
}

onMounted(async () => {
  try {
    const merchant = await getCurrentMerchant()
    form.storeName = merchant.name || form.storeName
    form.cuisineType = merchant.category || form.cuisineType
    form.contactPhone = merchant.contactPhone || form.contactPhone
    storeOnline.value = merchant.status === 1
  } catch (e) {
    toast.show(e.message || 'Store information failed to load', 'error')
  }
})
</script>
