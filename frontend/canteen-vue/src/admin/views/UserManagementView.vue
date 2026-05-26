<template>
  <div class="workspace-sub-view active" id="view-users">
    <div class="filter-action-bar">
      <div class="filter-left">
        <div class="search-box">
          <span class="material-symbols-outlined search-icon">search</span>
          <input v-model="searchQuery" type="text" placeholder="输入手机号/昵称进行搜索..." />
        </div>
        <div class="select-wrapper">
          <select v-model="roleFilter">
            <option value="">所有角色</option>
            <option value="USER">学生/教职工 (USER)</option>
            <option value="MERCHANT">商家账户 (MERCHANT)</option>
            <option value="ADMIN">管理员 (ADMIN)</option>
          </select>
        </div>
        <div class="select-wrapper">
          <select v-model="statusFilter">
            <option value="">所有状态</option>
            <option value="1">正常</option>
            <option value="0">禁用</option>
          </select>
        </div>
      </div>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>用户 ID</th><th>登录账号 (手机号)</th><th>昵称</th><th>系统角色</th><th>账户状态</th><th>注册时间</th><th style="text-align:right">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in pageUsers" :key="user.userId">
            <td><strong>#{{ user.userId }}</strong></td>
            <td>{{ user.username || user.phone }}</td>
            <td>{{ user.nickname || '-' }}</td>
            <td><span class="role-badge" :class="user.role?.toLowerCase()">{{ user.role === 'ADMIN' ? '管理员' : user.role === 'MERCHANT' ? '商家' : '用户' }}</span></td>
            <td><span class="status-badge" :class="user.status === 1 ? 'active' : 'disabled'">{{ user.status === 1 ? '正常' : '禁用' }}</span></td>
            <td>{{ user.createTime || '-' }}</td>
            <td style="text-align:right">
              <button type="button" class="btn-table-action" @click="selectedUser = user">详情</button>
              <button type="button" class="btn-table-action" @click="openPasswordDialog(user)">重置密码</button>
              <button type="button" class="btn-table-action" :class="user.status === 1 ? 'action-danger' : 'action-success'" @click="toggleStatus(user)">{{ user.status === 1 ? '禁用' : '启用' }}</button>
            </td>
          </tr>
          <tr v-if="!pageUsers.length"><td colspan="7" class="empty-cell">暂无匹配用户</td></tr>
        </tbody>
      </table>
    </div>
    <div class="table-pagination">
      <span class="pagination-info">显示第 {{ start }} 至 {{ end }} 项，共 {{ filteredUsers.length }} 名用户</span>
      <div class="pagination-controls">
        <button class="page-btn" :disabled="page === 0" @click="page -= 1"><span class="material-symbols-outlined">chevron_left</span></button>
        <button class="page-btn" :disabled="end >= filteredUsers.length" @click="page += 1"><span class="material-symbols-outlined">chevron_right</span></button>
      </div>
    </div>

    <div class="drawer-scrim" :class="{ show: selectedUser }" @click="selectedUser = null"></div>
    <div class="detail-drawer" :class="{ show: selectedUser }">
      <div class="drawer-inner-header">
        <h3 class="drawer-header-title">用户详细账户档案</h3>
        <button class="drawer-close-btn" @click="selectedUser = null"><span class="material-symbols-outlined">close</span></button>
      </div>
      <div v-if="selectedUser" class="drawer-inner-body">
        <div class="drawer-avatar-section">
          <div class="drawer-avatar"><span class="material-symbols-outlined">person</span></div>
          <span class="drawer-nickname">{{ selectedUser.nickname || '未设置昵称' }}</span>
        </div>
        <div class="drawer-info-grid">
          <div class="info-item"><span class="info-label">用户 ID</span><span class="info-value">#{{ selectedUser.userId }}</span></div>
          <div class="info-item"><span class="info-label">登录账号</span><span class="info-value">{{ selectedUser.username || selectedUser.phone }}</span></div>
          <div class="info-item"><span class="info-label">系统角色</span><span class="info-value">{{ selectedUser.role === 'ADMIN' ? '管理员' : selectedUser.role === 'MERCHANT' ? '商家' : '用户' }}</span></div>
          <div class="info-item"><span class="info-label">注册时间</span><span class="info-value">{{ selectedUser.createTime || '-' }}</span></div>
        </div>
      </div>
    </div>
    <div class="dialog-scrim" :class="{ show: resetUser }" @click="resetUser = null"></div>
    <div class="m3-dialog" :class="{ show: resetUser }">
      <h3 class="m3-dialog-title">重置用户登录密码</h3>
      <p class="m3-dialog-content">正在为用户 {{ resetUser?.nickname || '' }} 重置账户密码，请输入新密码：</p>
      <div class="m3-dialog-content">
        <md-outlined-text-field :value="newPassword" @input="newPassword = $event.target.value" label="输入新密码" type="password" required></md-outlined-text-field>
      </div>
      <div class="m3-dialog-actions">
        <button class="m3-dialog-btn btn-text" @click="resetUser = null">取消</button>
        <button class="m3-dialog-btn btn-filled" @click="resetPassword">确认重置</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject, watch } from 'vue'
import request from '@common/api/request'

const toast = inject('toast')
const users = ref([])
const searchQuery = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const page = ref(0)
const pageSize = 5
const selectedUser = ref(null)
const resetUser = ref(null)
const newPassword = ref('')
const filteredUsers = computed(() => users.value.filter(user => {
  const query = searchQuery.value.toLowerCase()
  return (!query || (user.nickname || '').toLowerCase().includes(query) || (user.username || user.phone || '').includes(query) || String(user.userId).includes(query))
    && (!roleFilter.value || user.role === roleFilter.value)
    && (!statusFilter.value || String(user.status) === statusFilter.value)
}))
const pageUsers = computed(() => filteredUsers.value.slice(page.value * pageSize, (page.value + 1) * pageSize))
const start = computed(() => filteredUsers.value.length ? page.value * pageSize + 1 : 0)
const end = computed(() => Math.min((page.value + 1) * pageSize, filteredUsers.value.length))
watch([searchQuery, roleFilter, statusFilter], () => { page.value = 0 })

async function fetchUsers() {
  try {
    const data = await request.get('/admin/users', { params: { page: 1, size: 100 } })
    users.value = data?.records || []
  } catch (e) { users.value = [] }
}
async function toggleStatus(user) {
  try {
    await request.put(`/admin/users/${user.userId}/status`, { status: user.status === 1 ? 0 : 1 })
    toast.show('用户状态已更新', 'check_circle')
    fetchUsers()
  } catch (e) { toast.show(e.message || '更新失败', 'error') }
}
function openPasswordDialog(user) {
  resetUser.value = user
  newPassword.value = ''
}
async function resetPassword() {
  if (newPassword.value.length < 6) {
    toast.show('密码长度至少为 6 位', 'warning')
    return
  }
  try {
    await request.put(`/admin/users/${resetUser.value.userId}/password`, { password: newPassword.value })
    toast.show('用户密码已重置', 'check_circle')
    resetUser.value = null
  } catch (e) { toast.show(e.message || '重置失败', 'error') }
}
onMounted(fetchUsers)
</script>
