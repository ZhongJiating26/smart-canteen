<template>
  <div class="workspace-sub-view active" id="view-logs">
    <div class="filter-action-bar">
      <div class="filter-left">
        <div class="search-box"><span class="material-symbols-outlined search-icon">search</span><input v-model="searchQuery" type="text" placeholder="按操作者 / 操作对象搜索..." /></div>
        <div class="select-wrapper">
          <select v-model="typeFilter">
            <option value="">所有操作类型</option>
            <option v-for="type in logTypes" :key="type" :value="type">{{ type }}</option>
          </select>
        </div>
      </div>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead><tr><th>日志 ID</th><th>操作管理员</th><th>操作类型</th><th>操作靶向对象</th><th>执行结果</th><th>操作时戳</th><th style="text-align:right">细节</th></tr></thead>
        <tbody>
          <tr v-for="log in filteredLogs" :key="log.id">
            <td><strong>{{ log.id }}</strong></td><td>{{ log.operator }}</td><td>{{ log.type }}</td><td>{{ log.target }}</td>
            <td><span class="status-badge" :class="log.result === 'SUCCESS' ? 'active' : 'disabled'">{{ log.result }}</span></td><td>{{ log.time }}</td>
            <td style="text-align:right"><button class="btn-table-action" @click="selectedLog = log">查看详情</button></td>
          </tr>
          <tr v-if="!filteredLogs.length"><td colspan="7" class="empty-cell">暂无操作日志数据</td></tr>
        </tbody>
      </table>
    </div>
    <div class="dialog-scrim" :class="{ show: selectedLog }" @click="selectedLog = null"></div>
    <div class="m3-dialog log-details-modal" :class="{ show: selectedLog }">
      <h3 class="m3-dialog-title dialog-title-with-icon"><span class="material-symbols-outlined">info</span>日志操作数据详情</h3>
      <div class="m3-dialog-content"><pre class="json-code-box">{{ selectedLog?.details || '{}' }}</pre></div>
      <div class="m3-dialog-actions"><button class="m3-dialog-btn btn-filled" @click="selectedLog = null">关闭</button></div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

// The backend currently exposes no operation-log endpoint; keep the full prototype surface ready for live records.
const logs = ref([])
const searchQuery = ref('')
const typeFilter = ref('')
const selectedLog = ref(null)
const logTypes = ['USER_STATUS', 'USER_PWD', 'MERCHANT_ADD', 'MERCHANT_EDIT', 'CONFIG_UPDATE', 'ABNORMAL_ORDER']
const filteredLogs = computed(() => logs.value.filter(log => {
  const query = searchQuery.value.toLowerCase()
  return (!query || `${log.operator} ${log.target}`.toLowerCase().includes(query)) && (!typeFilter.value || log.type === typeFilter.value)
}))
</script>
