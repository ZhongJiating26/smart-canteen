// Material Web Components import
import '@material/web/textfield/outlined-text-field.js';

// DOM Helper Selectors
const $ = selector => document.querySelector(selector);
const $$ = selector => document.querySelectorAll(selector);

// --- MOCK DATABASES ---
let usersDatabase = [
  { id: '#10001', phone: '13800138000', nickname: 'Alex Thompson', role: 'USER', status: 'ACTIVE', createTime: '2026-05-01 12:00:00' },
  { id: '#10002', phone: '13800138001', nickname: 'Emma Watson', role: 'USER', status: 'ACTIVE', createTime: '2026-05-02 14:22:00' },
  { id: '#10003', phone: '13912345678', nickname: 'Noodle House', role: 'MERCHANT', status: 'ACTIVE', createTime: '2026-05-03 10:15:00' },
  { id: '#10004', phone: '13987654321', nickname: 'Pizza Hub', role: 'MERCHANT', status: 'ACTIVE', createTime: '2026-05-03 11:30:00' },
  { id: '#10005', phone: '13000000000', nickname: 'Admin Root', role: 'ADMIN', status: 'ACTIVE', createTime: '2026-05-01 09:00:00' },
  { id: '#10006', phone: '13811112222', nickname: 'Sarah Connor', role: 'USER', status: 'DISABLED', createTime: '2026-05-04 16:45:00' },
  { id: '#10007', phone: '13500001111', nickname: 'Green Garden', role: 'MERCHANT', status: 'ACTIVE', createTime: '2026-05-05 08:30:00' }
];

let merchantsDatabase = [
  { id: 'M001', userId: '#10003', name: 'Noodle House', contactName: '张大叔', contactPhone: '13912345678', category: '面点主食', status: 'ACTIVE' },
  { id: 'M002', userId: '#10004', name: 'Pizza Hub', contactName: '李阿姨', contactPhone: '13987654321', category: '西式烧烤', status: 'ACTIVE' },
  { id: 'M003', userId: '#10007', name: 'Green Garden Bowls', contactName: '王经理', contactPhone: '13500001111', category: '沙拉轻食', status: 'SUSPENDED' }
];

let configDatabase = {
  pay_timeout_minutes: 10,
  accept_timeout_minutes: 10,
  cancel_window_minutes: 1,
  pickup_number_reset_rule: 'DAILY',
  stock_alert_scan_interval: 300
};

let configLogs = [
  { id: 'CL003', operator: 'Admin Root', item: 'pay_timeout_minutes', oldValue: '15', newValue: '10', time: '2026-05-24 10:15:00' },
  { id: 'CL002', operator: 'Admin Root', item: 'pickup_number_reset_rule', oldValue: 'NONE', newValue: 'DAILY', time: '2026-05-23 09:30:00' },
  { id: 'CL001', operator: 'Admin Root', item: 'stock_alert_scan_interval', oldValue: '600', newValue: '300', time: '2026-05-22 14:22:00' }
];

let logsDatabase = [
  { id: 'L20601', operator: 'Admin Root', type: 'CONFIG_UPDATE', target: 'order-service.config', result: 'SUCCESS', time: '2026-05-24 10:15:00', details: '{"pay_timeout_minutes": 10, "old": 15}' },
  { id: 'L20602', operator: 'Admin Root', type: 'USER_STATUS', target: 'User #10006', result: 'SUCCESS', time: '2026-05-24 11:22:00', details: '{"userId": "#10006", "status": "DISABLED", "reason": "异常高频提交订单"}' },
  { id: 'L20603', operator: 'Admin Root', type: 'USER_PWD', target: 'User #10001', result: 'SUCCESS', time: '2026-05-24 14:05:00', details: '{"userId": "#10001", "action": "RESET_PASSWORD"}' },
  { id: 'L20604', operator: 'Admin Root', type: 'MERCHANT_ADD', target: 'Stall Green Garden', result: 'SUCCESS', time: '2026-05-24 14:40:00', details: '{"name": "Green Garden Bowls", "userId": "#10007"}' }
];

let abnormalOrdersDatabase = [
  { id: 'AB001', orderId: '#1006', merchant: 'Noodle House', status: 'preparing', duration: '45 mins', cause: '支付超时锁定异常', type: 'PAY_TIMEOUT', time: '2026-05-24 22:15:00' },
  { id: 'AB002', orderId: '#1007', merchant: 'Pizza Hub', status: 'pending', duration: '28 mins', cause: '商家接单超时挂起', type: 'STALL_TIMEOUT', time: '2026-05-24 22:30:00' },
  { id: 'AB003', orderId: '#1008', merchant: 'Green Garden Bowls', status: 'ready', duration: '1.5 hours', cause: '扣减库存与释放回滚错误', type: 'STOCK_LOCKED', time: '2026-05-24 21:10:00' }
];

// --- APP INITIALIZATION ---
document.addEventListener('DOMContentLoaded', () => {
  setupAuthentication();
  setupNavigation();
  setupDashboard();
  setupUserManagement();
  setupMerchantManagement();
  setupSystemConfiguration();
  setupOperationLogs();
  setupAbnormalOrders();
  setupGlobalControls();
});

// --- GLOBAL TOAST ---
function showToast(msg, icon = 'info') {
  const toast = $('#admin-toast');
  const toastIcon = $('#a-toast-icon');
  const toastMsg = $('#a-toast-msg');
  
  if (toast && toastIcon && toastMsg) {
    toastIcon.textContent = icon;
    toastMsg.textContent = msg;
    toast.classList.add('show');
    
    setTimeout(() => {
      toast.classList.remove('show');
    }, 3000);
  }
}

// --- GLOBAL REFRESH ---
function setupGlobalControls() {
  const refreshBtn = $('#btn-header-refresh');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', () => {
      showToast('系统同步成功！数据已更新', 'sync');
      setupDashboard();
      renderUsersTable();
      renderMerchantsTable();
      renderConfigLogs();
      renderLogsTable();
      renderAbnormalTable();
    });
  }
}

// --- CONFIRMATION DIALOG ---
let confirmCallback = null;
function showConfirmDialog(title, content, callback) {
  const dialog = $('#dialog-confirm');
  const scrim = $('#dialog-confirm-scrim');
  const dTitle = $('#confirm-title');
  const dContent = $('#confirm-content');
  const cancelBtn = $('#btn-confirm-cancel');
  const okBtn = $('#btn-confirm-ok');
  
  if (dialog && scrim && dTitle && dContent) {
    dTitle.textContent = title;
    dContent.textContent = content;
    
    dialog.classList.add('show');
    scrim.classList.add('show');
    
    confirmCallback = callback;
    
    const closeDialog = () => {
      dialog.classList.remove('show');
      scrim.classList.remove('show');
    };
    
    cancelBtn.onclick = closeDialog;
    scrim.onclick = closeDialog;
    
    okBtn.onclick = () => {
      if (confirmCallback) confirmCallback();
      closeDialog();
    };
  }
}

// --- 0. AUTHENTICATION MODULE ---
function setupAuthentication() {
  const loginForm = $('#admin-login-form');
  const loginView = $('#admin-login-view');
  const mainView = $('#admin-main-view');
  const pwdToggle = $('#admin-login-pwd-toggle');
  
  // Set body class if loginView is active
  if (loginView && loginView.classList.contains('active')) {
    document.body.classList.add('login-page');
  }

  // Toggle pwd visibility
  if (pwdToggle) {
    pwdToggle.addEventListener('click', () => {
      const pwdField = $('#login-password');
      if (pwdField) {
        const type = pwdField.getAttribute('type') === 'password' ? 'text' : 'password';
        pwdField.setAttribute('type', type);
        pwdToggle.textContent = type === 'password' ? 'visibility' : 'visibility_off';
      }
    });
  }

  const loginSuccess = () => {
    showToast('Login successful! Welcome Admin.', 'check_circle');
    
    if (loginView) loginView.classList.remove('active');
    if (mainView) mainView.classList.add('active');
    document.body.classList.remove('login-page');
    
    setupDashboard();
  };

  if (loginForm) {
    loginForm.addEventListener('submit', (e) => {
      e.preventDefault();
      
      const usernameVal = $('#login-username').value.trim();
      const passwordVal = $('#login-password').value;
      
      if (usernameVal !== 'admin') {
        showToast('Invalid admin username! Use: admin', 'error');
        return;
      }
      
      if (passwordVal === 'admin123') {
        loginSuccess();
      } else {
        showToast('Invalid credentials! Hint: admin / admin123', 'error');
      }
    });
  }

  // Logout click
  const logoutBtn = $('#btn-admin-logout');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      showConfirmDialog('Confirm Sign Out', 'Are you sure you want to sign out of the admin console?', () => {
        if (mainView) mainView.classList.remove('active');
        if (loginView) loginView.classList.add('active');
        document.body.classList.add('login-page');
        
        // Reset login inputs
        if (loginForm) {
          loginForm.reset();
        }
        
        showToast('Console logged out', 'logout');
      });
    });
  }
}

// --- NAVIGATION MODULE ---
function setupNavigation() {
  const navItems = $$('.nav-item:not(.logout-item)');
  const subViews = $$('.workspace-sub-view');
  const drawerToggle = $('#btn-drawer-toggle');
  const sideDrawer = $('#admin-side-drawer');
  const viewTitle = $('#current-view-title');

  // View Routing
  navItems.forEach(item => {
    item.addEventListener('click', () => {
      const targetViewId = item.getAttribute('data-view');
      
      // Update sidebar nav items active states
      navItems.forEach(n => n.classList.remove('active'));
      item.classList.add('active');
      
      // Switch active view
      subViews.forEach(v => v.classList.remove('active'));
      const targetView = $('#' + targetViewId);
      if (targetView) targetView.classList.add('active');
      
      // Update header title
      if (viewTitle) {
        viewTitle.textContent = item.querySelector('.nav-label').textContent;
      }
      
      // Setup modules based on tab opened
      if (targetViewId === 'view-dashboard') setupDashboard();
      else if (targetViewId === 'view-users') renderUsersTable();
      else if (targetViewId === 'view-merchants') renderMerchantsTable();
      else if (targetViewId === 'view-config') renderConfigLogs();
      else if (targetViewId === 'view-logs') renderLogsTable();
      else if (targetViewId === 'view-abnormal') renderAbnormalTable();
    });
  });

  // Collapsible toggle
  if (drawerToggle && sideDrawer) {
    drawerToggle.addEventListener('click', () => {
      sideDrawer.classList.toggle('collapsed');
      sideDrawer.classList.toggle('expanded');
    });
  }
}

// --- 1. DASHBOARD MODULE ---
function setupDashboard() {
  // Update counts
  const totalUsersEl = $('#dashboard-total-users');
  const totalStallsEl = $('#dashboard-total-stalls');
  const todayOrdersEl = $('#dashboard-today-orders');
  const abnormalOrdersEl = $('#dashboard-abnormal-orders');
  
  if (totalUsersEl) totalUsersEl.textContent = usersDatabase.length;
  if (totalStallsEl) totalStallsEl.textContent = merchantsDatabase.length;
  
  // Static mock order stats
  if (todayOrdersEl) todayOrdersEl.textContent = 42;
  if (abnormalOrdersEl) {
    abnormalOrdersEl.textContent = abnormalOrdersDatabase.length;
    // Update red count badge in sidebar
    const badgeSidebar = $('#abnormal-badge-count');
    if (badgeSidebar) {
      badgeSidebar.textContent = abnormalOrdersDatabase.length;
      badgeSidebar.style.display = abnormalOrdersDatabase.length > 0 ? 'block' : 'none';
    }
  }

  // Update System health timestamps
  const systemTimeEl = $('#system-update-time');
  if (systemTimeEl) {
    const now = new Date();
    systemTimeEl.textContent = now.toLocaleTimeString();
  }

  // Bind Quick Actions shortcuts
  const quickLinks = $$('.quick-link-btn');
  quickLinks.forEach(link => {
    link.onclick = () => {
      const targetView = link.getAttribute('data-target');
      const sideNavItem = $(`.nav-item[data-view="${targetView}"]`);
      if (sideNavItem) sideNavItem.click();
    };
  });
}

// --- 2. USER MANAGEMENT MODULE ---
let userPageLimit = 5;
let userPageIndex = 0;

function setupUserManagement() {
  const searchInput = $('#user-search-input');
  const roleFilter = $('#user-role-filter');
  const statusFilter = $('#user-status-filter');
  const prevBtn = $('#btn-user-prev');
  const nextBtn = $('#btn-user-next');
  const closeDrawerBtn = $('#btn-user-drawer-close');
  const drawerScrim = $('#drawer-user-details-scrim');
  
  const handleFilterChange = () => {
    userPageIndex = 0;
    renderUsersTable();
  };
  
  if (searchInput) searchInput.addEventListener('input', handleFilterChange);
  if (roleFilter) roleFilter.addEventListener('change', handleFilterChange);
  if (statusFilter) statusFilter.addEventListener('change', handleFilterChange);
  
  if (prevBtn) {
    prevBtn.onclick = () => {
      if (userPageIndex > 0) {
        userPageIndex--;
        renderUsersTable();
      }
    };
  }
  
  if (nextBtn) {
    nextBtn.onclick = () => {
      const filtered = getFilteredUsers();
      if ((userPageIndex + 1) * userPageLimit < filtered.length) {
        userPageIndex++;
        renderUsersTable();
      }
    };
  }

  // Details drawer close
  const closeUserDrawer = () => {
    const drawer = $('#drawer-user-details');
    if (drawer) drawer.classList.remove('show');
    if (drawerScrim) drawerScrim.classList.remove('show');
  };
  if (closeDrawerBtn) closeDrawerBtn.onclick = closeUserDrawer;
  if (drawerScrim) drawerScrim.onclick = closeUserDrawer;

  // Reset password dialog handlers
  const cancelResetBtn = $('#btn-reset-pwd-cancel');
  const scrimReset = $('#dialog-reset-pwd-scrim');
  const closeReset = () => {
    $('#dialog-reset-pwd').classList.remove('show');
    scrimReset.classList.remove('show');
  };
  if (cancelResetBtn) cancelResetBtn.onclick = closeReset;
  if (scrimReset) scrimReset.onclick = closeReset;

  const confirmResetBtn = $('#btn-reset-pwd-confirm');
  if (confirmResetBtn) {
    confirmResetBtn.onclick = () => {
      const userId = $('#reset-pwd-user-id').value;
      const newPwd = $('#reset-pwd-new').value.trim();
      if (!newPwd) {
        showToast('Password cannot be empty!', 'warning');
        return;
      }
      
      showToast(`密码重置成功！ID: ${userId}`, 'check_circle');
      
      // Log operation
      logsDatabase.unshift({
        id: 'L' + (20600 + logsDatabase.length + 1),
        operator: 'Admin Root',
        type: 'USER_PWD',
        target: `User ${userId}`,
        result: 'SUCCESS',
        time: new Date().toISOString().replace('T', ' ').slice(0, 19),
        details: JSON.stringify({ userId, action: 'RESET_PASSWORD', passwordLength: newPwd.length })
      });
      
      closeReset();
    };
  }
}

function getFilteredUsers() {
  const query = $('#user-search-input').value.trim().toLowerCase();
  const role = $('#user-role-filter').value;
  const status = $('#user-status-filter').value;
  
  return usersDatabase.filter(user => {
    const matchesQuery = user.nickname.toLowerCase().includes(query) || user.phone.includes(query) || user.id.includes(query);
    const matchesRole = role === 'ALL' || user.role === role;
    const matchesStatus = status === 'ALL' || user.status === status;
    return matchesQuery && matchesRole && matchesStatus;
  });
}

function renderUsersTable() {
  const tbody = $('#users-table-tbody');
  if (!tbody) return;
  
  const filtered = getFilteredUsers();
  
  // Paging calculations
  const totalItems = filtered.length;
  const pageStart = totalItems === 0 ? 0 : userPageIndex * userPageLimit + 1;
  const pageEnd = Math.min((userPageIndex + 1) * userPageLimit, totalItems);
  
  const pStartEl = $('#user-page-start');
  const pEndEl = $('#user-page-end');
  const pTotalEl = $('#user-page-total');
  
  if (pStartEl) pStartEl.textContent = pageStart;
  if (pEndEl) pEndEl.textContent = pageEnd;
  if (pTotalEl) pTotalEl.textContent = totalItems;
  
  // Toggle buttons
  const prevBtn = $('#btn-user-prev');
  const nextBtn = $('#btn-user-next');
  if (prevBtn) prevBtn.disabled = userPageIndex === 0;
  if (nextBtn) nextBtn.disabled = (userPageIndex + 1) * userPageLimit >= totalItems;
  
  // Slice page items
  const pageItems = filtered.slice(userPageIndex * userPageLimit, (userPageIndex + 1) * userPageLimit);
  
  tbody.innerHTML = '';
  
  if (pageItems.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--color-text-muted); padding: 30px;">未找到匹配的用户数据</td></tr>`;
    return;
  }
  
  pageItems.forEach(user => {
    const tr = document.createElement('tr');
    
    const roleBadgeClass = user.role.toLowerCase();
    const statusBadgeClass = user.status.toLowerCase();
    const statusText = user.status === 'ACTIVE' ? '正常' : '禁用';
    
    tr.innerHTML = `
      <td><strong>${user.id}</strong></td>
      <td>${user.phone}</td>
      <td>${user.nickname}</td>
      <td><span class="role-badge ${roleBadgeClass}">${user.role}</span></td>
      <td><span class="status-badge ${statusBadgeClass}">${statusText}</span></td>
      <td>${user.createTime}</td>
      <td style="text-align: right;">
        <button type="button" class="btn-table-action action-view" data-id="${user.id}">查看详情</button>
        <button type="button" class="btn-table-action ${user.status === 'ACTIVE' ? 'action-danger' : 'action-success'}" data-id="${user.id}" data-action="toggle-status">
          ${user.status === 'ACTIVE' ? '禁用' : '启用'}
        </button>
        <button type="button" class="btn-table-action action-reset" data-id="${user.id}">重置密码</button>
      </td>
    `;
    
    // Bind actions
    tr.querySelector('.action-view').onclick = () => showUserDetailsDrawer(user.id);
    tr.querySelector('[data-action="toggle-status"]').onclick = () => toggleUserStatus(user.id);
    tr.querySelector('.action-reset').onclick = () => openResetPasswordDialog(user.id);
    
    tbody.appendChild(tr);
  });
}

function showUserDetailsDrawer(userId) {
  const user = usersDatabase.find(u => u.id === userId);
  if (!user) return;
  
  const content = $('#user-drawer-content');
  const drawer = $('#drawer-user-details');
  const scrim = $('#drawer-user-details-scrim');
  
  if (content && drawer && scrim) {
    content.innerHTML = `
      <div class="drawer-avatar-section">
        <div class="drawer-avatar">
          <span class="material-symbols-outlined">person</span>
        </div>
        <h4 class="drawer-nickname">${user.nickname}</h4>
        <span class="role-badge ${user.role.toLowerCase()}">${user.role}</span>
      </div>
      <div class="drawer-info-grid">
        <div class="info-item">
          <span class="info-label">用户 ID</span>
          <span class="info-value">${user.id}</span>
        </div>
        <div class="info-item">
          <span class="info-label">登录手机号 / 账号</span>
          <span class="info-value">${user.phone}</span>
        </div>
        <div class="info-item">
          <span class="info-label">账户状态</span>
          <span class="info-value">${user.status === 'ACTIVE' ? '正常 (Active)' : '已锁定禁用 (Disabled)'}</span>
        </div>
        <div class="info-item">
          <span class="info-label">创建/注册时间</span>
          <span class="info-value">${user.createTime}</span>
        </div>
        <div class="info-item" style="border-top: 1px dashed var(--color-border); padding-top: 14px;">
          <span class="info-label">历史下单统计</span>
          <span class="info-value">共下发订单 12 笔（总额 ¥240.50）</span>
        </div>
        <div class="info-item">
          <span class="info-label">最近访问活动</span>
          <span class="info-value">2026-05-24 14:02 (Web Browser Client)</span>
        </div>
      </div>
    `;
    
    drawer.classList.add('show');
    scrim.classList.add('show');
  }
}

function toggleUserStatus(userId) {
  const user = usersDatabase.find(u => u.id === userId);
  if (!user) return;
  
  const actionText = user.status === 'ACTIVE' ? '禁用' : '启用';
  const confirmMsg = `您确定要${actionText}用户 "${user.nickname}" (${user.phone}) 吗？${user.status === 'ACTIVE' ? '禁用后该账户将无法登录客户端及下单！' : ''}`;
  
  showConfirmDialog(`${actionText}用户确认`, confirmMsg, () => {
    user.status = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
    renderUsersTable();
    showToast(`用户已成功${actionText}！`, 'check_circle');
    
    // Log operation
    logsDatabase.unshift({
      id: 'L' + (20600 + logsDatabase.length + 1),
      operator: 'Admin Root',
      type: 'USER_STATUS',
      target: `User ${userId}`,
      result: 'SUCCESS',
      time: new Date().toISOString().replace('T', ' ').slice(0, 19),
      details: JSON.stringify({ userId, status: user.status, action: 'TOGGLE_STATUS' })
    });
  });
}

function openResetPasswordDialog(userId) {
  const user = usersDatabase.find(u => u.id === userId);
  if (!user) return;
  
  const dialog = $('#dialog-reset-pwd');
  const scrim = $('#dialog-reset-pwd-scrim');
  const prompt = $('#reset-pwd-prompt-text');
  const idField = $('#reset-pwd-user-id');
  const pwdField = $('#reset-pwd-new');
  
  if (dialog && scrim && prompt && idField && pwdField) {
    idField.value = userId;
    pwdField.value = '888888'; // default placeholder
    prompt.innerHTML = `正在为用户 <strong>${user.nickname}</strong> (${user.phone}) 重置登录密码，请输入新密码：`;
    
    dialog.classList.add('show');
    scrim.classList.add('show');
  }
}

// --- 3. MERCHANT WINDOW MANAGEMENT MODULE ---
function setupMerchantManagement() {
  const addBtn = $('#btn-add-merchant');
  const cancelAddBtn = $('#btn-add-merchant-cancel');
  const submitAddBtn = $('#btn-add-merchant-submit');
  const searchInput = $('#merchant-search-input');
  const statusFilter = $('#merchant-status-filter');
  
  const handleFilterChange = () => {
    renderMerchantsTable();
  };
  if (searchInput) searchInput.addEventListener('input', handleFilterChange);
  if (statusFilter) statusFilter.addEventListener('change', handleFilterChange);

  // Dialog show/hide
  const closeAddDialog = () => {
    $('#dialog-add-merchant').classList.remove('show');
    $('#dialog-add-merchant-scrim').classList.remove('show');
  };
  if (addBtn) {
    addBtn.onclick = () => {
      $('#form-add-merchant').reset();
      $('#dialog-add-merchant').classList.add('show');
      $('#dialog-add-merchant-scrim').classList.add('show');
    };
  }
  if (cancelAddBtn) cancelAddBtn.onclick = closeAddDialog;
  if ($('#dialog-add-merchant-scrim')) $('#dialog-add-merchant-scrim').onclick = closeAddDialog;

  // Submit new stall
  if (submitAddBtn) {
    submitAddBtn.onclick = () => {
      const name = $('#add-stall-name').value.trim();
      const manager = $('#add-manager').value.trim();
      const phone = $('#add-phone').value.trim();
      const category = $('#add-category').value;
      const username = $('#add-username').value.trim();
      const password = $('#add-password').value.trim();
      
      if (!name || !manager || !phone || !username || !password) {
        showToast('All fields are required!', 'warning');
        return;
      }
      
      // Step 1: Create merchant user account
      const newUserId = '#1000' + (usersDatabase.length + 1);
      usersDatabase.push({
        id: newUserId,
        phone: username,
        nickname: name,
        role: 'MERCHANT',
        status: 'ACTIVE',
        createTime: new Date().toISOString().replace('T', ' ').slice(0, 19)
      });
      
      // Step 2: Create stall record
      const newStallId = 'M00' + (merchantsDatabase.length + 1);
      merchantsDatabase.push({
        id: newStallId,
        userId: newUserId,
        name: name,
        contactName: manager,
        contactPhone: phone,
        category: category,
        status: 'ACTIVE'
      });
      
      showToast(`美食窗口 "${name}" 创建成功！`, 'check_circle');
      
      // Log operation
      logsDatabase.unshift({
        id: 'L' + (20600 + logsDatabase.length + 1),
        operator: 'Admin Root',
        type: 'MERCHANT_ADD',
        target: `Stall ${name} (${newStallId})`,
        result: 'SUCCESS',
        time: new Date().toISOString().replace('T', ' ').slice(0, 19),
        details: JSON.stringify({ name, manager, phone, newUserId, newStallId })
      });
      
      closeAddDialog();
      renderMerchantsTable();
    };
  }

  // Edit dialog cancel handlers
  const cancelEditBtn = $('#btn-edit-merchant-cancel');
  const closeEditDialog = () => {
    $('#dialog-edit-merchant').classList.remove('show');
    $('#dialog-edit-merchant-scrim').classList.remove('show');
  };
  if (cancelEditBtn) cancelEditBtn.onclick = closeEditDialog;
  if ($('#dialog-edit-merchant-scrim')) $('#dialog-edit-merchant-scrim').onclick = closeEditDialog;

  // Submit edit
  const submitEditBtn = $('#btn-edit-merchant-submit');
  if (submitEditBtn) {
    submitEditBtn.onclick = () => {
      const id = $('#edit-merchant-id').value;
      const name = $('#edit-stall-name').value.trim();
      const manager = $('#edit-manager').value.trim();
      const phone = $('#edit-phone').value.trim();
      const category = $('#edit-category').value;
      const status = $('#edit-status').value;
      
      if (!name || !manager || !phone) {
        showToast('All fields are required!', 'warning');
        return;
      }
      
      const stall = merchantsDatabase.find(m => m.id === id);
      if (stall) {
        stall.name = name;
        stall.contactName = manager;
        stall.contactPhone = phone;
        stall.category = category;
        stall.status = status;
        
        // Also update nickname in user account
        if (stall.userId) {
          const user = usersDatabase.find(u => u.id === stall.userId);
          if (user) user.nickname = name;
        }
        
        showToast('Stall configurations updated successfully!', 'check_circle');
        
        // Log operation
        logsDatabase.unshift({
          id: 'L' + (20600 + logsDatabase.length + 1),
          operator: 'Admin Root',
          type: 'MERCHANT_EDIT',
          target: `Stall ${id}`,
          result: 'SUCCESS',
          time: new Date().toISOString().replace('T', ' ').slice(0, 19),
          details: JSON.stringify({ id, name, manager, phone, category, status })
        });
        
        closeEditDialog();
        renderMerchantsTable();
      }
    };
  }
}

function getFilteredMerchants() {
  const query = $('#merchant-search-input').value.trim().toLowerCase();
  const status = $('#merchant-status-filter').value;
  
  return merchantsDatabase.filter(m => {
    const matchesQuery = m.name.toLowerCase().includes(query) || m.contactName.includes(query) || m.id.toLowerCase().includes(query);
    const matchesStatus = status === 'ALL' || m.status === status;
    return matchesQuery && matchesStatus;
  });
}

function renderMerchantsTable() {
  const tbody = $('#merchants-table-tbody');
  if (!tbody) return;
  
  const filtered = getFilteredMerchants();
  tbody.innerHTML = '';
  
  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--color-text-muted); padding: 30px;">未找到匹配的美食窗口</td></tr>`;
    return;
  }
  
  filtered.forEach(stall => {
    const tr = document.createElement('tr');
    
    // Bind username
    let userPhoneText = '无账号绑定';
    if (stall.userId) {
      const user = usersDatabase.find(u => u.id === stall.userId);
      if (user) userPhoneText = user.phone;
    }
    
    const statusText = stall.status === 'ACTIVE' ? '正常营运' : '停用中';
    const statusBadgeClass = stall.status === 'ACTIVE' ? 'active' : 'disabled';
    
    tr.innerHTML = `
      <td><strong>${stall.id}</strong></td>
      <td>${userPhoneText}</td>
      <td>${stall.name}</td>
      <td>${stall.contactName}</td>
      <td>${stall.contactPhone}</td>
      <td>${stall.category}</td>
      <td><span class="status-badge ${statusBadgeClass}">${statusText}</span></td>
      <td style="text-align: right;">
        <button type="button" class="btn-table-action action-edit" data-id="${stall.id}">编辑</button>
        <button type="button" class="btn-table-action ${stall.status === 'ACTIVE' ? 'action-danger' : 'action-success'}" data-id="${stall.id}" data-action="toggle-status">
          ${stall.status === 'ACTIVE' ? '停用' : '启用'}
        </button>
      </td>
    `;
    
    // Bind click events
    tr.querySelector('.action-edit').onclick = () => openEditStallDialog(stall.id);
    tr.querySelector('[data-action="toggle-status"]').onclick = () => toggleStallStatus(stall.id);
    
    tbody.appendChild(tr);
  });
}

function openEditStallDialog(stallId) {
  const stall = merchantsDatabase.find(m => m.id === stallId);
  if (!stall) return;
  
  const dialog = $('#dialog-edit-merchant');
  const scrim = $('#dialog-edit-merchant-scrim');
  
  if (dialog && scrim) {
    $('#edit-merchant-id').value = stall.id;
    $('#edit-stall-name').value = stall.name;
    $('#edit-manager').value = stall.contactName;
    $('#edit-phone').value = stall.contactPhone;
    $('#edit-category').value = stall.category;
    $('#edit-status').value = stall.status;
    
    dialog.classList.add('show');
    scrim.classList.add('show');
  }
}

function toggleStallStatus(stallId) {
  const stall = merchantsDatabase.find(m => m.id === stallId);
  if (!stall) return;
  
  const actionText = stall.status === 'ACTIVE' ? '停用' : '启用';
  const confirmMsg = `确认要${actionText}窗口 "${stall.name}" 吗？停用后该窗口菜品将无法被用户检索购买，且商家账号将被锁定。`;
  
  showConfirmDialog(`窗口${actionText}确认`, confirmMsg, () => {
    stall.status = stall.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    
    // Lock/Unlock associate login account
    if (stall.userId) {
      const user = usersDatabase.find(u => u.id === stall.userId);
      if (user) {
        user.status = stall.status === 'ACTIVE' ? 'ACTIVE' : 'DISABLED';
      }
    }
    
    renderMerchantsTable();
    showToast(`窗口已成功${actionText}！`, 'check_circle');
    
    // Log operation
    logsDatabase.unshift({
      id: 'L' + (20600 + logsDatabase.length + 1),
      operator: 'Admin Root',
      type: 'MERCHANT_EDIT',
      target: `Stall ${stallId} (Status: ${stall.status})`,
      result: 'SUCCESS',
      time: new Date().toISOString().replace('T', ' ').slice(0, 19),
      details: JSON.stringify({ id: stallId, status: stall.status, action: 'TOGGLE_STATUS' })
    });
  });
}

// --- 4. SYSTEM CONFIGURATION MODULE ---
function setupSystemConfiguration() {
  const tabs = $$('.config-tab');
  const groups = $$('.config-group-section');
  const configForm = $('#system-config-form');
  
  // Segmented Tabs switching
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      tabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      
      const targetGroup = tab.getAttribute('data-group');
      groups.forEach(g => g.classList.remove('active'));
      $('#config-group-' + targetGroup).classList.add('active');
    });
  });

  // Save Config Form
  if (configForm) {
    configForm.addEventListener('submit', (e) => {
      e.preventDefault();
      
      const payTimeout = parseInt($('#cfg-pay-timeout').value);
      const acceptTimeout = parseInt($('#cfg-accept-timeout').value);
      const cancelWindow = parseInt($('#cfg-cancel-window').value);
      const pickupReset = $('#cfg-pickup-reset').value;
      const stockScan = parseInt($('#cfg-stock-scan').value);
      
      // Update logs for changes
      const operator = 'Admin Root';
      const time = new Date().toISOString().replace('T', ' ').slice(0, 19);
      
      if (configDatabase.pay_timeout_minutes !== payTimeout) {
        configLogs.unshift({
          id: 'CL' + (configLogs.length + 1),
          operator,
          item: 'pay_timeout_minutes',
          oldValue: configDatabase.pay_timeout_minutes.toString(),
          newValue: payTimeout.toString(),
          time
        });
        configDatabase.pay_timeout_minutes = payTimeout;
      }
      
      if (configDatabase.accept_timeout_minutes !== acceptTimeout) {
        configLogs.unshift({
          id: 'CL' + (configLogs.length + 1),
          operator,
          item: 'accept_timeout_minutes',
          oldValue: configDatabase.accept_timeout_minutes.toString(),
          newValue: acceptTimeout.toString(),
          time
        });
        configDatabase.accept_timeout_minutes = acceptTimeout;
      }

      if (configDatabase.cancel_window_minutes !== cancelWindow) {
        configLogs.unshift({
          id: 'CL' + (configLogs.length + 1),
          operator,
          item: 'cancel_window_minutes',
          oldValue: configDatabase.cancel_window_minutes.toString(),
          newValue: cancelWindow.toString(),
          time
        });
        configDatabase.cancel_window_minutes = cancelWindow;
      }

      if (configDatabase.pickup_number_reset_rule !== pickupReset) {
        configLogs.unshift({
          id: 'CL' + (configLogs.length + 1),
          operator,
          item: 'pickup_number_reset_rule',
          oldValue: configDatabase.pickup_number_reset_rule,
          newValue: pickupReset,
          time
        });
        configDatabase.pickup_number_reset_rule = pickupReset;
      }

      if (configDatabase.stock_alert_scan_interval !== stockScan) {
        configLogs.unshift({
          id: 'CL' + (configLogs.length + 1),
          operator,
          item: 'stock_alert_scan_interval',
          oldValue: configDatabase.stock_alert_scan_interval.toString(),
          newValue: stockScan.toString(),
          time
        });
        configDatabase.stock_alert_scan_interval = stockScan;
      }
      
      showToast('配置成功保存！对应Redis缓存已刷新并分发。', 'check_circle');
      
      // Add operation log
      logsDatabase.unshift({
        id: 'L' + (20600 + logsDatabase.length + 1),
        operator: 'Admin Root',
        type: 'CONFIG_UPDATE',
        target: 'system.global.configurations',
        result: 'SUCCESS',
        time,
        details: JSON.stringify(configDatabase)
      });
      
      renderConfigLogs();
    });
  }
}

function renderConfigLogs() {
  const container = $('#config-logs-container');
  if (!container) return;
  
  container.innerHTML = '';
  
  if (configLogs.length === 0) {
    container.innerHTML = `<div style="text-align: center; color: var(--color-text-muted); font-size: 0.8rem; padding: 20px;">暂无修改记录</div>`;
    return;
  }
  
  configLogs.forEach(log => {
    const item = document.createElement('div');
    item.className = 'timeline-item';
    item.innerHTML = `
      <div class="timeline-header">
        <span>修改人: ${log.operator}</span>
        <span>${log.time}</span>
      </div>
      <div class="timeline-action">修改项: ${log.item}</div>
      <div class="timeline-val">变更: <strong>${log.oldValue}</strong> &rarr; <strong>${log.newValue}</strong></div>
    `;
    container.appendChild(item);
  });
}

// --- 5. OPERATION LOGS MODULE ---
function setupOperationLogs() {
  const search = $('#log-search-input');
  const typeFilter = $('#log-type-filter');
  
  const handleFilterChange = () => {
    renderLogsTable();
  };
  
  if (search) search.addEventListener('input', handleFilterChange);
  if (typeFilter) typeFilter.addEventListener('change', handleFilterChange);

  // Close log payload dialog
  const cancelBtn = $('#btn-log-details-close');
  const scrim = $('#dialog-log-details-scrim');
  const closeLogDialog = () => {
    $('#dialog-log-details').classList.remove('show');
    scrim.classList.remove('show');
  };
  if (cancelBtn) cancelBtn.onclick = closeLogDialog;
  if (scrim) scrim.onclick = closeLogDialog;
}

function getFilteredLogs() {
  const query = $('#log-search-input').value.trim().toLowerCase();
  const type = $('#log-type-filter').value;
  
  return logsDatabase.filter(log => {
    const matchesQuery = log.operator.toLowerCase().includes(query) || log.target.toLowerCase().includes(query) || log.id.toLowerCase().includes(query);
    const matchesType = type === 'ALL' || log.type === type;
    return matchesQuery && matchesType;
  });
}

function renderLogsTable() {
  const tbody = $('#logs-table-tbody');
  if (!tbody) return;
  
  const filtered = getFilteredLogs();
  tbody.innerHTML = '';
  
  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--color-text-muted); padding: 30px;">无匹配的操作日志记录</td></tr>`;
    return;
  }
  
  filtered.forEach(log => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${log.id}</strong></td>
      <td>${log.operator}</td>
      <td><strong>${log.type}</strong></td>
      <td>${log.target}</td>
      <td><span class="status-badge active">${log.result}</span></td>
      <td>${log.time}</td>
      <td style="text-align: right;">
        <button type="button" class="btn-table-action action-view-payload" data-id="${log.id}">查看Payload</button>
      </td>
    `;
    
    tr.querySelector('.action-view-payload').onclick = () => showLogPayloadDialog(log.id);
    tbody.appendChild(tr);
  });
}

function showLogPayloadDialog(logId) {
  const log = logsDatabase.find(l => l.id === logId);
  if (!log) return;
  
  const dialog = $('#dialog-log-details');
  const scrim = $('#dialog-log-details-scrim');
  const pre = $('#log-details-payload');
  
  if (dialog && scrim && pre) {
    try {
      const parsed = JSON.parse(log.details);
      pre.textContent = JSON.stringify(parsed, null, 2);
    } catch (e) {
      pre.textContent = log.details;
    }
    
    dialog.classList.add('show');
    scrim.classList.add('show');
  }
}

// --- 6. ABNORMAL ORDER PROCESSING MODULE ---
function setupAbnormalOrders() {
  const searchInput = $('#abnormal-search-input');
  const causeFilter = $('#abnormal-cause-filter');
  
  const handleFilterChange = () => {
    renderAbnormalTable();
  };
  
  if (searchInput) searchInput.addEventListener('input', handleFilterChange);
  if (causeFilter) causeFilter.addEventListener('change', handleFilterChange);
}

function getFilteredAbnormalOrders() {
  const query = $('#abnormal-search-input').value.trim().toLowerCase();
  const cause = $('#abnormal-cause-filter').value;
  
  return abnormalOrdersDatabase.filter(o => {
    const matchesQuery = o.orderId.toLowerCase().includes(query) || o.merchant.toLowerCase().includes(query);
    const matchesCause = cause === 'ALL' || o.type === cause;
    return matchesQuery && matchesCause;
  });
}

function renderAbnormalTable() {
  const tbody = $('#abnormal-table-tbody');
  if (!tbody) return;
  
  const filtered = getFilteredAbnormalOrders();
  tbody.innerHTML = '';
  
  // Refresh sidebar warning count
  const badgeSidebar = $('#abnormal-badge-count');
  if (badgeSidebar) {
    badgeSidebar.textContent = abnormalOrdersDatabase.length;
    badgeSidebar.style.display = abnormalOrdersDatabase.length > 0 ? 'block' : 'none';
  }

  // Update urgent card count on dashboard
  const urgentCardEl = $('#dashboard-abnormal-orders');
  if (urgentCardEl) urgentCardEl.textContent = abnormalOrdersDatabase.length;
  
  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--color-text-muted); padding: 30px;">目前没有需要紧急干预的异常阻断订单</td></tr>`;
    return;
  }
  
  filtered.forEach(o => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong style="color: var(--color-error);">${o.orderId}</strong></td>
      <td>${o.merchant}</td>
      <td>${o.time}</td>
      <td><span style="color: var(--color-error); font-weight: 700;">${o.duration}</span></td>
      <td>${o.cause}</td>
      <td><span class="role-badge merchant">${o.type}</span></td>
      <td style="text-align: right;">
        <button type="button" class="btn-table-action action-danger btn-force-cancel" data-id="${o.id}">手动取消</button>
        <button type="button" class="btn-table-action action-success btn-retry-stock" data-id="${o.id}">释放重试</button>
      </td>
    `;
    
    tr.querySelector('.btn-force-cancel').onclick = () => forceCancelAbnormalOrder(o.id);
    tr.querySelector('.btn-retry-stock').onclick = () => retryStockAbnormalOrder(o.id);
    tbody.appendChild(tr);
  });
}

function forceCancelAbnormalOrder(abnormalId) {
  const record = abnormalOrdersDatabase.find(r => r.id === abnormalId);
  if (!record) return;
  
  const confirmMsg = `警告：确认要强制取消异常订单 "${record.orderId}" 吗？该操作将强制写入撤单结果，触发支付服务全额退款，并清退事务。此操作不可逆。`;
  
  showConfirmDialog('强制取消订单确认', confirmMsg, () => {
    // Delete from abnormal orders
    const index = abnormalOrdersDatabase.findIndex(r => r.id === abnormalId);
    if (index !== -1) {
      abnormalOrdersDatabase.splice(index, 1);
    }
    
    renderAbnormalTable();
    showToast(`订单 ${record.orderId} 已被管理员强制取消，退款已到账。`, 'check_circle');
    
    // Log operation
    logsDatabase.unshift({
      id: 'L' + (20600 + logsDatabase.length + 1),
      operator: 'Admin Root',
      type: 'ABNORMAL_ORDER',
      target: `Order ${record.orderId}`,
      result: 'SUCCESS',
      time: new Date().toISOString().replace('T', ' ').slice(0, 19),
      details: JSON.stringify({ record, action: 'FORCE_CANCEL' })
    });
  });
}

function retryStockAbnormalOrder(abnormalId) {
  const record = abnormalOrdersDatabase.find(r => r.id === abnormalId);
  if (!record) return;
  
  const confirmMsg = `确认要重新尝试释放订单 "${record.orderId}" 的事务锁和食材库存吗？系统将向 RocketMQ 消息队列重新发送一条库存释回广播。`;
  
  showConfirmDialog('重新释放锁定库存确认', confirmMsg, () => {
    // Delete from abnormal orders
    const index = abnormalOrdersDatabase.findIndex(r => r.id === abnormalId);
    if (index !== -1) {
      abnormalOrdersDatabase.splice(index, 1);
    }
    
    renderAbnormalTable();
    showToast(`消息已投递！订单 ${record.orderId} 锁定库存已成功释放。`, 'check_circle');
    
    // Log operation
    logsDatabase.unshift({
      id: 'L' + (20600 + logsDatabase.length + 1),
      operator: 'Admin Root',
      type: 'ABNORMAL_ORDER',
      target: `Order ${record.orderId} (Stock Retry)`,
      result: 'SUCCESS',
      time: new Date().toISOString().replace('T', ' ').slice(0, 19),
      details: JSON.stringify({ record, action: 'RETRY_STOCK_RELEASE' })
    });
  });
}
