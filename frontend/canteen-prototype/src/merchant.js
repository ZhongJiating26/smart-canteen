import '@material/web/textfield/outlined-text-field.js';
import '@material/web/progress/circular-progress.js';
import '@material/web/switch/switch.js';
import '@material/web/checkbox/checkbox.js';
import '@material/web/menu/menu.js';
import '@material/web/menu/menu-item.js';

// --- Mobile App State Management & Interactions ---

// Helper Selectors
const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => document.querySelectorAll(selector);

// DOM Elements
const loginView = $('#merchant-login-view');
const mainView = $('#merchant-main-view');

// Initialize Event Listeners
document.addEventListener('DOMContentLoaded', () => {
  setupLoginTabs();
  setupPasswordToggle();
  setupVerificationCodeTimer();
  setupLoginSubmit();
  setupNavigationRail();
  setupOrderManagement();
  setupRejectDialog();
  setupScanDialog();
  setupDishManagement();
  setupCreateDishDialog();
  setupDailyMenu();
  setupHistoryLog();
  setupStoreConfiguration();
});

// --- Password Visibility Toggle ---
function setupPasswordToggle() {
  const pwdInput = $('#login-password');
  const pwdToggle = $('#merchant-login-pwd-toggle');
  
  if (pwdInput && pwdToggle) {
    pwdToggle.addEventListener('click', () => {
      const isPwd = pwdInput.type === 'password';
      pwdInput.type = isPwd ? 'text' : 'password';
      pwdToggle.textContent = isPwd ? 'visibility_off' : 'visibility';
    });
  }
}

// --- Toast Alerts ---
function showToast(message, iconName = 'info') {
  const toast = $('#merchant-toast');
  const icon = $('#m-toast-icon');
  const msg = $('#m-toast-msg');
  
  if (icon) icon.textContent = iconName;
  if (msg) msg.textContent = message;
  
  if (toast) {
    toast.classList.add('show');
    setTimeout(() => {
      toast.classList.remove('show');
    }, 3000);
  }
}

// --- Login Switcher ---
function setupLoginTabs() {
  const tabPwd = $('#tab-pwd-btn');
  const tabCode = $('#tab-code-btn');
  const pwdWrapper = $('#pwd-input-wrapper');
  const codeWrapper = $('#code-input-wrapper');
  
  if (tabPwd && tabCode) {
    tabPwd.addEventListener('click', () => {
      tabPwd.classList.add('active');
      tabCode.classList.remove('active');
      pwdWrapper.classList.remove('hidden');
      codeWrapper.classList.add('hidden');
      $('#login-password').setAttribute('required', '');
      $('#login-code').removeAttribute('required');
    });

    tabCode.addEventListener('click', () => {
      tabCode.classList.add('active');
      tabPwd.classList.remove('active');
      codeWrapper.classList.remove('hidden');
      pwdWrapper.classList.add('hidden');
      $('#login-code').setAttribute('required', '');
      $('#login-password').removeAttribute('required');
    });
  }
}

// --- Verification Code Timer ---
let mockSmsCode = '';
function setupVerificationCodeTimer() {
  const btnSend = $('#btn-send-sms');
  if (btnSend) {
    btnSend.addEventListener('click', () => {
      const account = $('#login-account').value.trim();
      if (!account) {
        showToast('Please enter your Phone Number first!', 'warning');
        return;
      }
      
      let secondsLeft = 60;
      btnSend.disabled = true;
      btnSend.textContent = `${secondsLeft}s`;
      
      mockSmsCode = String(Math.floor(100000 + Math.random() * 900000));
      setTimeout(() => {
        showToast(`Verification code sent! Code: ${mockSmsCode}`, 'sms');
      }, 800);

      const countdown = setInterval(() => {
        secondsLeft--;
        if (secondsLeft <= 0) {
          clearInterval(countdown);
          btnSend.disabled = false;
          btnSend.textContent = 'Send Code';
        } else {
          btnSend.textContent = `${secondsLeft}s`;
        }
      }, 1000);
    });
  }
}

// --- Login Submission ---
function setupLoginSubmit() {
  const form = $('#merchant-login-form');
  if (form) {
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      
      const accountVal = $('#login-account').value.trim();
      const pwdTabActive = $('#tab-pwd-btn').classList.contains('active');
      
      if (!accountVal) {
        showToast('Please fill in Stall ID / Phone', 'warning');
        return;
      }
      
      if (!pwdTabActive) {
        const enterCode = $('#login-code').value.trim();
        if (!enterCode) {
          showToast('Please enter verification code!', 'warning');
          return;
        }
        if (enterCode !== mockSmsCode) {
          showToast('Invalid verification code!', 'error');
          return;
        }
      }

      // Successful Login transition
      showToast('Merchant portal signed in!', 'check_circle');
      if (loginView) loginView.classList.remove('active');
      if (mainView) mainView.classList.add('active');
      
      // Setup initial data load
      renderOrderCards();
      renderDishGrid();
      renderDailyMenu();
      renderHistoryList();
    });
  }
}

// --- Collapsible Navigation Rail ---
function setupNavigationRail() {
  const rail = $('#merchant-nav-rail');
  const toggleBtn = $('#btn-rail-toggle');
  const railItems = $$('.rail-item:not(.logout-item)');
  
  if (toggleBtn && rail) {
    toggleBtn.addEventListener('click', () => {
      if (rail.classList.contains('collapsed')) {
        rail.classList.remove('collapsed');
        rail.classList.add('expanded');
      } else {
        rail.classList.remove('expanded');
        rail.classList.add('collapsed');
      }
    });
  }

  // View Swapper
  railItems.forEach(item => {
    item.addEventListener('click', () => {
      railItems.forEach(i => i.classList.remove('active'));
      item.classList.add('active');
      
      const targetView = item.getAttribute('data-view');
      const labelText = item.querySelector('.rail-label').textContent;
      
      // Update header workspace title
      const workspaceTitle = $('#current-view-title');
      if (workspaceTitle) workspaceTitle.textContent = labelText;
      
      // Toggle workspace view content
      $$('.workspace-sub-view').forEach(view => {
        view.classList.remove('active');
      });
      const currentActiveView = $(`#${targetView}`);
      if (currentActiveView) currentActiveView.classList.add('active');
    });
  });

  // Logout Interaction
  const logoutBtn = $('#btn-merchant-logout');
  const logoutDialog = $('#merchant-logout-dialog');
  const logoutScrim = $('#merchant-logout-dialog-scrim');
  const cancelLogout = $('#btn-merchant-logout-cancel');
  const confirmLogout = $('#btn-merchant-logout-confirm');

  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      if (logoutDialog && logoutScrim) {
        logoutDialog.classList.add('show');
        logoutScrim.classList.add('show');
      }
    });
  }

  const closeLogoutDialog = () => {
    if (logoutDialog && logoutScrim) {
      logoutDialog.classList.remove('show');
      logoutScrim.classList.remove('show');
    }
  };

  if (cancelLogout) cancelLogout.addEventListener('click', closeLogoutDialog);
  if (logoutScrim) logoutScrim.addEventListener('click', closeLogoutDialog);

  if (confirmLogout) {
    confirmLogout.addEventListener('click', () => {
      closeLogoutDialog();
      showToast('Merchant portal logged out!', 'logout');
      
      // Switch screen back to login
      if (mainView) mainView.classList.remove('active');
      if (loginView) loginView.classList.add('active');
      
      // Clear forms
      $('#merchant-login-form').reset();
    });
  }
}

// --- 1. ORDER PROCESSING MODULE ---
let ordersDatabase = [
  { id: '#1003', customer: 'Emma Watson', items: 'Tonkotsu Ramen x1, Plum Soup x1', total: '$15.00', time: '3 mins ago', remarks: '不要香菜，多放辣', status: 'pending' },
  { id: '#1004', customer: 'John Doe', items: 'Spicy Dan Dan Noodles x2', total: '$19.00', time: '5 mins ago', status: 'pending' },
  { id: '#1005', customer: 'Jane Smith', items: 'Pork Gyoza x1, Beef Chow Mein x1', total: '$18.50', time: '8 mins ago', remarks: '饺子需要双份蘸酱', status: 'pending' },
  { id: '#1001', customer: 'Michael Chang', items: 'Tonkotsu Ramen x1', total: '$11.00', time: '12 mins ago', status: 'preparing' },
  { id: '#1002', customer: 'Sarah Connor', items: 'Beef Chow Mein x1, Plum Soup x2', total: '$21.00', time: '15 mins ago', remarks: '少放油，多点汤，谢谢！', status: 'preparing' },
  { id: '#1000', customer: 'Chris Evans', items: 'Pork Gyoza x2', total: '$12.00', time: 'Picked up in 10 mins', status: 'ready' }
];

let orderIdPendingReject = '';

function setupOrderManagement() {
  // Tabs are removed, columns are rendered simultaneously
}

function renderOrderCards() {
  const listPending = $('#list-pending');
  const listPreparing = $('#list-preparing');
  const listReady = $('#list-ready');
  
  if (!listPending || !listPreparing || !listReady) return;
  
  // Clear lists
  listPending.innerHTML = '';
  listPreparing.innerHTML = '';
  listReady.innerHTML = '';
  
  // Filter database counts
  const pendingOrders = ordersDatabase.filter(o => o.status === 'pending');
  const preparingOrders = ordersDatabase.filter(o => o.status === 'preparing');
  const readyOrders = ordersDatabase.filter(o => o.status === 'ready');
  
  // Update header badges
  const badgePending = $('#badge-pending');
  const badgePreparing = $('#badge-preparing');
  const badgeReady = $('#badge-ready');
  
  if (badgePending) badgePending.textContent = pendingOrders.length;
  if (badgePreparing) badgePreparing.textContent = preparingOrders.length;
  if (badgeReady) badgeReady.textContent = readyOrders.length;

  // Update nav rail badge dot for new orders
  const ordersBadge = $('#orders-badge-dot');
  if (ordersBadge) {
    if (pendingOrders.length > 0) {
      ordersBadge.classList.add('show');
    } else {
      ordersBadge.classList.remove('show');
    }
  }

  // Populate Pending column
  if (pendingOrders.length === 0) {
    listPending.innerHTML = `
      <div class="empty-state">
        <span class="material-symbols-outlined" style="font-size: 2rem;">inbox</span>
        <p style="font-size: 0.8rem;">No pending orders</p>
      </div>
    `;
  } else {
    pendingOrders.forEach(order => {
      listPending.appendChild(createOrderCardElement(order));
    });
  }

  // Populate Preparing column
  if (preparingOrders.length === 0) {
    listPreparing.innerHTML = `
      <div class="empty-state">
        <span class="material-symbols-outlined" style="font-size: 2rem;">soup_kitchen</span>
        <p style="font-size: 0.8rem;">No preparing orders</p>
      </div>
    `;
  } else {
    preparingOrders.forEach(order => {
      listPreparing.appendChild(createOrderCardElement(order));
    });
  }

  // Populate Ready column
  if (readyOrders.length === 0) {
    listReady.innerHTML = `
      <div class="empty-state">
        <span class="material-symbols-outlined" style="font-size: 2rem;">check_circle</span>
        <p style="font-size: 0.8rem;">No ready orders</p>
      </div>
    `;
  } else {
    readyOrders.forEach(order => {
      listReady.appendChild(createOrderCardElement(order));
    });
  }
}

function createOrderCardElement(order) {
  const card = document.createElement('div');
  card.className = 'merchant-order-card';
  
  // Parse order items to lists
  const itemsMarkup = order.items.split(', ').map(item => {
    const parts = item.split(' x');
    const name = parts[0];
    const qty = parts[1];
    return `
      <div class="order-card-item">
        <span>${name}</span>
        <span class="order-card-item-qty">x${qty}</span>
      </div>
    `;
  }).join('');

  let actionButtons = '';
  if (order.status === 'pending') {
    actionButtons = `
      <button class="order-card-btn btn-outline" data-id="${order.id}" data-action="reject">Reject</button>
      <button class="order-card-btn btn-filled" data-id="${order.id}" data-action="accept">Accept</button>
    `;
  } else if (order.status === 'preparing') {
    actionButtons = `
      <button class="order-card-btn btn-filled" style="width: 100%;" data-id="${order.id}" data-action="ready">Mark Ready</button>
    `;
  } else if (order.status === 'ready') {
    actionButtons = `
      <button class="order-card-btn btn-filled-light" style="width: 100%;" data-id="${order.id}" data-action="call">再次叫号</button>
    `;
  }

  card.innerHTML = `
    <div class="order-card-header">
      <span class="order-card-id">${order.id}</span>
      <span class="order-card-time">${order.time}</span>
    </div>
    <div class="order-card-items-list">
      ${itemsMarkup}
    </div>
    ${order.remarks ? `
    <div class="order-card-remarks">
      <span class="material-symbols-outlined remarks-icon">chat_bubble</span>
      <span class="remarks-text">备注：${order.remarks}</span>
    </div>
    ` : ''}
    <div class="order-card-total-row">
      <span class="order-card-total-label">Total Amount</span>
      <span class="order-card-total-price">${order.total}</span>
    </div>
    <div class="order-card-actions">
      ${actionButtons}
    </div>
  `;
  
  // Attach button click events inside card
  card.querySelectorAll('.order-card-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const orderId = btn.getAttribute('data-id');
      const action = btn.getAttribute('data-action');
      handleOrderAction(orderId, action);
    });
  });

  return card;
}

function handleOrderAction(orderId, action) {
  const index = ordersDatabase.findIndex(o => o.id === orderId);
  if (index === -1) return;
  
  const order = ordersDatabase[index];
  
  if (action === 'reject') {
    orderIdPendingReject = orderId;
    const rejectDialog = $('#merchant-reject-dialog');
    const rejectScrim = $('#merchant-reject-dialog-scrim');
    if (rejectDialog && rejectScrim) {
      rejectDialog.classList.add('show');
      rejectScrim.classList.add('show');
    }
  } else if (action === 'accept') {
    showToast(`Order ${orderId} accepted! Preparing food...`, 'soup_kitchen');
    order.status = 'preparing';
    order.time = 'Just now';
    renderOrderCards();
  } else if (action === 'ready') {
    showToast(`Order ${orderId} is ready! Notifying user...`, 'notifications_active');
    order.status = 'ready';
    order.time = 'Ready';
    renderOrderCards();
  } else if (action === 'call') {
    showToast(`叫号成功！已再次通知顾客取餐 (订单 ${orderId})`, 'notifications_active');
  } else if (action === 'complete') {
    showToast(`Order ${orderId} marked completed!`, 'check_circle');
    // Move to history log
    historyDatabase.unshift({
      id: order.id,
      customer: order.customer,
      items: order.items,
      total: order.total,
      time: 'Just now',
      status: 'completed'
    });
    // Remove from active orders
    ordersDatabase.splice(index, 1);
    renderHistoryList();
    renderOrderCards();
  }
}

function setupRejectDialog() {
  const rejectDialog = $('#merchant-reject-dialog');
  const rejectScrim = $('#merchant-reject-dialog-scrim');
  const cancelBtn = $('#btn-merchant-reject-cancel');
  const confirmBtn = $('#btn-merchant-reject-confirm');

  const closeRejectDialog = () => {
    if (rejectDialog && rejectScrim) {
      rejectDialog.classList.remove('show');
      rejectScrim.classList.remove('show');
    }
    orderIdPendingReject = '';
  };

  if (cancelBtn) cancelBtn.addEventListener('click', closeRejectDialog);
  if (rejectScrim) rejectScrim.addEventListener('click', closeRejectDialog);

  if (confirmBtn) {
    confirmBtn.addEventListener('click', () => {
      if (orderIdPendingReject) {
        const orderId = orderIdPendingReject;
        const index = ordersDatabase.findIndex(o => o.id === orderId);
        if (index !== -1) {
          showToast(`Order ${orderId} has been rejected`, 'cancel');
          ordersDatabase.splice(index, 1);
          renderOrderCards();
        }
      }
      closeRejectDialog();
    });
  }
}

// --- 2. DISH MANAGEMENT MODULE ---
let dishesDatabase = [
  { title: 'Tonkotsu Ramen', price: '$11.00', available: true, img: '/images/tonkotsu_ramen.png', category: '主食', dailySelected: true, stock: 45, desc: '经典招牌博多豚骨拉面，配料丰富，汤头浓郁香甜。', dailyStockType: 'reset', dailyStockValue: 45 },
  { title: 'Spicy Dan Dan Noodles', price: '$9.50', available: true, img: '/images/spicy_dan_dan.png', category: '主食', dailySelected: true, stock: 30, desc: '四川经典担担面，芝麻酱香与秘制辣油完美契合。', dailyStockType: 'reset', dailyStockValue: 30 },
  { title: 'Beef Chow Mein', price: '$12.50', available: true, img: '/images/beef_chow_mein.png', category: '主食', dailySelected: true, stock: 25, desc: '老火镬气牛肉炒面，牛肉鲜嫩，面条爽滑入味。', dailyStockType: 'reset', dailyStockValue: 25 },
  { title: 'Pork Gyoza', price: '$6.00', available: false, img: '/images/pork_gyoza.png', category: '小吃', dailySelected: true, stock: 0, desc: '香脆猪肉煎饺，外皮金黄酥脆，肉馅多汁鲜美。', dailyStockType: 'add', dailyStockValue: 10 },
  { title: 'Plum Soup', price: '$4.00', available: true, img: '/images/plum_soup.png', category: '小吃', dailySelected: true, stock: 80, desc: '清凉解暑酸梅汤，传统秘制配方，酸甜适口。', dailyStockType: 'add', dailyStockValue: 20 }
];

let currentDishCategory = 'all';
let selectedDishIndex = -1;
let isEditingDish = false;
let tempUploadedImage = '';

let currentDailyMenuCategory = 'all';
let selectedDailyDishIndex = -1;

function setupDishManagement() {
  renderCategoryFilters();
}

function renderCategoryFilters() {
  const filterContainer = $('#dish-category-filter');
  if (!filterContainer) return;
  
  const categories = Array.from(new Set(dishesDatabase.map(d => d.category)));
  
  let html = `
    <button type="button" class="segmented-button ${currentDishCategory === 'all' ? 'active' : ''}" data-category="all">
      <span>全部</span>
    </button>
  `;
  
  categories.forEach(cat => {
    html += `
      <button type="button" class="segmented-button ${currentDishCategory === cat ? 'active' : ''}" data-category="${cat}">
        <span>${cat}</span>
      </button>
    `;
  });
  
  filterContainer.innerHTML = html;
  
  const categoryBtns = $$('#dish-category-filter .segmented-button');
  categoryBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      categoryBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentDishCategory = btn.getAttribute('data-category');
      isEditingDish = false; // 切换分类时重置编辑状态
      renderDishGrid();
    });
  });
}

function renderDishGrid() {
  renderCategoryFilters();
  const listContainer = $('#merchant-dishes-list');
  const editorPanel = $('#dish-detail-editor-panel');
  if (!listContainer || !editorPanel) return;
  
  listContainer.innerHTML = '';
  
  // Get filtered dishes
  const filteredDishes = [];
  dishesDatabase.forEach((dish, idx) => {
    if (currentDishCategory === 'all' || dish.category === currentDishCategory) {
      filteredDishes.push({ dish, originalIndex: idx });
    }
  });

  // Handle selected index logic
  if (filteredDishes.length > 0) {
    const isStillInFiltered = filteredDishes.some(item => item.originalIndex === selectedDishIndex);
    if (selectedDishIndex === -1 || !isStillInFiltered) {
      selectedDishIndex = filteredDishes[0].originalIndex;
    }
  } else {
    selectedDishIndex = -1;
  }

  // Render Left List Items
  filteredDishes.forEach(item => {
    const dish = item.dish;
    const originalIndex = item.originalIndex;
    
    const listItem = document.createElement('div');
    listItem.className = `m3-list-item ${originalIndex === selectedDishIndex ? 'active' : ''}`;
    listItem.setAttribute('data-index', originalIndex);
    
    // Check stock styling label
    let stockText = `库存: ${dish.stock}`;
    if (dish.stock === 0) {
      stockText = `<span style="color: var(--color-error); font-weight: 600;">已售罄</span>`;
    } else if (dish.stock <= 10) {
      stockText = `<span style="color: var(--color-warning); font-weight: 600;">库存紧张 (${dish.stock})</span>`;
    }
    
    let offShelfBadge = '';
    if (!dish.available) {
      offShelfBadge = `<span class="list-item-status-tag">下架</span>`;
    }
    
    listItem.innerHTML = `
      <img class="list-item-avatar" src="${dish.img}" alt="${dish.title}" />
      <div class="list-item-text">
        <span class="list-item-title">${dish.title}</span>
        <span class="list-item-subtitle">${stockText} · ${dish.price}</span>
      </div>
      ${offShelfBadge}
    `;
    
    listItem.addEventListener('click', () => {
      selectedDishIndex = originalIndex;
      isEditingDish = false; // 选择其他菜品时重置编辑状态
      // Re-render list items to update active state class
      listContainer.querySelectorAll('.m3-list-item').forEach(el => {
        el.classList.remove('active');
        if (parseInt(el.getAttribute('data-index'), 10) === selectedDishIndex) {
          el.classList.add('active');
        }
      });
      renderDishDetails(selectedDishIndex);
    });
    
    listContainer.appendChild(listItem);
  });

  // Render Right Details Panel
  renderDishDetails(selectedDishIndex);
}

function renderDishDetails(idx) {
  const panel = $('#dish-detail-editor-panel');
  if (!panel) return;
  
  if (idx === -1 || !dishesDatabase[idx]) {
    panel.innerHTML = `
      <div class="detail-placeholder">
        <span class="material-symbols-outlined">restaurant</span>
        <p>请在左侧选择一个菜品以查看或修改详情</p>
      </div>
    `;
    return;
  }

  const dish = dishesDatabase[idx];
  tempUploadedImage = dish.img;

  // 菜品详情卡片只读态：直接显示文本内容，不使用输入框组件
  panel.innerHTML = `
    <div class="dish-detail-content">
      <!-- 主区块：左右两列 (只读态) -->
      <div class="dish-detail-main-block">
        <!-- 左列：名称、价格、库存、分类 -->
        <div class="dish-detail-left-col" style="gap: 12px;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span class="detail-display-category-badge">${dish.category}</span>
            ${dish.available ? 
              `<span class="detail-display-status-badge on-shelf">已上架</span>` : 
              `<span class="detail-display-status-badge off-shelf">已下架</span>`
            }
          </div>
          <h1 class="detail-display-title">${dish.title}</h1>
          <div class="detail-display-price">${dish.price}</div>
          <div class="detail-display-stock">剩余库存：<strong>${dish.stock}</strong> 件</div>
        </div>
        
        <!-- 右列：图片 -->
        <div class="dish-detail-right-col">
          <div class="detail-image-wrapper">
            <img src="${tempUploadedImage}" alt="${dish.title}" class="detail-image" />
          </div>
        </div>
      </div>
      
      <!-- 区块下方：菜品介绍 -->
      <div style="margin-top: 12px; padding-top: 16px; border-top: 1px solid var(--color-border);">
        <h3 style="font-size: 14px; font-weight: 700; color: var(--color-text-secondary); margin-bottom: 8px; letter-spacing: 0.5px;">菜品介绍</h3>
        <p class="detail-display-desc">${dish.desc || '暂无介绍。'}</p>
      </div>
      
      <div class="detail-form" style="margin-top: 16px;">
        <div class="detail-actions-row">
          <button type="button" class="btn-save-daily-menu" id="btn-start-dish-edit" style="height: 40px; border-radius: 20px; background-color: var(--color-brand-peach); color: var(--color-brand-rust); box-shadow: none !important; display: flex; align-items: center; gap: 8px;">
            <span class="material-symbols-outlined" style="font-size: 18px;">edit</span>
            <span>修改</span>
          </button>
        </div>
      </div>
    </div>
  `;

  // 绑定只读态下的“修改”按钮点击事件以开启编辑悬浮窗
  const startEditBtn = $('#btn-start-dish-edit');
  if (startEditBtn) {
    startEditBtn.addEventListener('click', () => {
      openEditDishDialog(idx);
    });
  }
}

function openEditDishDialog(idx) {
  const dialog = $('#merchant-edit-dish-dialog');
  const scrim = $('#merchant-edit-dish-dialog-scrim');
  if (!dialog || !scrim) return;

  const dish = dishesDatabase[idx];
  tempUploadedImage = dish.img; // Pull latest image for editing

  // Build the form inside the dialog content
  dialog.innerHTML = `
    <h3 class="m3-dialog-title" style="display: flex; align-items: center; gap: 8px; margin-bottom: 20px;">
      <span class="material-symbols-outlined" style="color: var(--color-brand-rust);">edit_note</span>
      <span>修改菜品详情</span>
    </h3>
    
    <div class="dish-detail-content" style="gap: 16px;">
      <!-- 主区块：左右两列 (编辑态) -->
      <div class="dish-detail-main-block">
        <!-- 左列：名称、价格、库存、分类 -->
        <div class="dish-detail-left-col" style="flex: 3; display: flex; flex-direction: column; gap: 12px;">
          <div class="detail-row">
            <md-outlined-text-field id="edit-dish-title" label="菜品名称" value="${dish.title}" style="width: 100%;"></md-outlined-text-field>
          </div>
          <div class="detail-row" style="display: flex; gap: 16px;">
            <md-outlined-text-field id="edit-dish-price" label="价格 (元)" value="${dish.price.replace('$', '')}" type="number" step="0.01" style="flex: 1;"></md-outlined-text-field>
            
            <div style="display: flex; flex-direction: column; gap: 4px; flex: 1;">
              <span style="font-size: 12px; color: var(--color-text-secondary); font-weight: 600; margin-bottom: 2px;">剩余库存</span>
              <div class="quantity-control" id="dish-detail-qty-control" style="pointer-events: auto; align-self: flex-start;">
                <button type="button" class="btn-qty-minus" id="btn-stock-minus">-</button>
                <span class="qty-number-box" id="edit-dish-stock-num">${dish.stock}</span>
                <button type="button" class="btn-qty-plus" id="btn-stock-plus">+</button>
              </div>
            </div>
          </div>
          <div class="detail-row" style="position: relative; width: 100%;">
            <md-outlined-text-field id="edit-dish-category" label="菜品分类" value="${dish.category}" readonly style="width: 100%; cursor: pointer;">
              <span slot="trailing-icon" class="material-symbols-outlined">arrow_drop_down</span>
            </md-outlined-text-field>
            <md-menu id="edit-dish-category-menu" anchor="edit-dish-category" style="--md-menu-container-color: #ffffff; min-width: 240px; z-index: 1002;">
              <!-- Populated dynamically -->
            </md-menu>
          </div>
        </div>
        
        <!-- 右列：图片 -->
        <div class="dish-detail-right-col" style="flex: 2; display: flex; flex-direction: column; align-items: center; justify-content: flex-start;">
          <div class="detail-image-wrapper editable" id="btn-trigger-image-upload" style="width: 100%; aspect-ratio: 4/3;">
            <img src="${tempUploadedImage}" alt="${dish.title}" class="detail-image" id="detail-dish-image-preview" />
            <div class="image-upload-overlay">
              <span class="material-symbols-outlined">photo_camera</span>
              <span>更换图片</span>
            </div>
          </div>
          <!-- 用于上传本地图片的隐藏 input -->
          <input type="file" id="edit-dish-file-input" accept="image/*" style="display: none;" />
        </div>
      </div>
      
      <!-- 区块下方：上架状态 -->
      <div class="detail-row" style="display: flex; justify-content: space-between; align-items: center; background-color: var(--color-bg-viewport); padding: 12px 16px; border-radius: 12px; border: 1px solid var(--color-border); margin-top: 4px;">
        <div style="display: flex; flex-direction: column; gap: 2px;">
          <span style="font-size: 14px; font-weight: 600; color: var(--color-text-primary);">上架状态</span>
          <span style="font-size: 12px; color: var(--color-text-secondary);">控制顾客是否可以下单购买</span>
        </div>
        <md-switch id="edit-dish-available" ${dish.available ? 'selected' : ''}></md-switch>
      </div>
      
      <!-- 区块下方：菜品介绍 -->
      <div class="detail-row" style="margin-top: 4px;">
        <md-outlined-text-field id="edit-dish-desc" label="菜品介绍" type="textarea" rows="2" value="${dish.desc || ''}" style="width: 100%;"></md-outlined-text-field>
      </div>
      
      <!-- Action Buttons -->
      <div class="m3-dialog-actions" style="margin-top: 16px;">
        <button type="button" class="m3-dialog-btn btn-text" id="btn-edit-dish-cancel" style="color: var(--color-text-secondary);">取消修改</button>
        <button type="button" class="m3-dialog-btn btn-filled" id="btn-save-dish-edit">保存修改</button>
      </div>
    </div>
  `;

  // Show dialog and scrim
  dialog.classList.add('show');
  scrim.classList.add('show');

  // Render categories menu
  renderEditCategoryMenu();

  // 绑定对话框内的所有事件处理器
  
  // 0. 库存加减微调逻辑
  const btnMinus = $('#btn-stock-minus');
  const btnPlus = $('#btn-stock-plus');
  const stockNum = $('#edit-dish-stock-num');
  if (btnMinus && btnPlus && stockNum) {
    btnMinus.addEventListener('click', () => {
      let val = parseInt(stockNum.textContent, 10) || 0;
      if (val > 0) {
        stockNum.textContent = val - 1;
      }
    });
    btnPlus.addEventListener('click', () => {
      let val = parseInt(stockNum.textContent, 10) || 0;
      stockNum.textContent = val + 1;
    });
  }

  // 0.1 菜品分类下拉菜单触发
  const categoryField = $('#edit-dish-category');
  const categoryMenu = $('#edit-dish-category-menu');
  if (categoryField && categoryMenu) {
    categoryField.addEventListener('click', () => {
      categoryMenu.open = !categoryMenu.open;
    });
  }

  // 1. 本地图片上传逻辑
  const uploadTrigger = $('#btn-trigger-image-upload');
  const fileInput = $('#edit-dish-file-input');
  if (uploadTrigger && fileInput) {
    uploadTrigger.addEventListener('click', () => {
      fileInput.click();
    });
    
    fileInput.addEventListener('change', (e) => {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (event) => {
          const dataUrl = event.target.result;
          const imgPreview = $('#detail-dish-image-preview');
          if (imgPreview) imgPreview.src = dataUrl;
          tempUploadedImage = dataUrl; // 保存 Base64 图片
          showToast('新图片已载入预览！', 'image');
        };
        reader.readAsDataURL(file);
      }
    });
  }

  // 2. 取消修改逻辑
  const cancelBtn = $('#btn-edit-dish-cancel');
  if (cancelBtn) {
    cancelBtn.addEventListener('click', () => {
      dialog.classList.remove('show');
      scrim.classList.remove('show');
    });
  }
  if (scrim) {
    scrim.onclick = () => {
      dialog.classList.remove('show');
      scrim.classList.remove('show');
    };
  }

  // 3. 确认保存逻辑
  const saveBtn = $('#btn-save-dish-edit');
  if (saveBtn) {
    saveBtn.addEventListener('click', () => {
      const newTitle = $('#edit-dish-title').value.trim();
      const newPrice = $('#edit-dish-price').value.trim();
      const stockNumEl = $('#edit-dish-stock-num');
      const newStockVal = stockNumEl ? stockNumEl.textContent.trim() : '';
      const newAvailable = $('#edit-dish-available').selected;
      const newCategory = $('#edit-dish-category').value.trim();
      const newDesc = $('#edit-dish-desc').value.trim();

      if (!newTitle) {
        showToast('菜品名称不能为空！', 'warning');
        return;
      }
      if (!newPrice || isNaN(parseFloat(newPrice))) {
        showToast('请输入有效的菜品价格！', 'warning');
        return;
      }
      if (newStockVal === '' || isNaN(parseInt(newStockVal, 10)) || parseInt(newStockVal, 10) < 0) {
        showToast('请输入有效的库存数量！', 'warning');
        return;
      }
      if (!newCategory) {
        showToast('请选择或输入一个菜品分类！', 'warning');
        return;
      }

      const stock = parseInt(newStockVal, 10);
      const isAvailable = stock > 0 ? newAvailable : false;

      // 更新数据库
      dishesDatabase[idx].title = newTitle;
      dishesDatabase[idx].price = '$' + parseFloat(newPrice).toFixed(2);
      dishesDatabase[idx].stock = stock;
      dishesDatabase[idx].available = isAvailable;
      dishesDatabase[idx].category = newCategory;
      dishesDatabase[idx].desc = newDesc;
      dishesDatabase[idx].img = tempUploadedImage; // 保存最新图片

      // 关闭对话框
      dialog.classList.remove('show');
      scrim.classList.remove('show');

      // 重新渲染视图
      renderDishGrid();
      renderDailyMenu();
      renderDishDetails(idx); // 重新渲染只读态卡片

      showToast(`成功修改菜品：${newTitle}`, 'check_circle');
    });
  }
}

function renderEditCategoryMenu() {
  const categoryMenu = $('#edit-dish-category-menu');
  if (!categoryMenu) return;
  
  const categories = Array.from(new Set(dishesDatabase.map(d => d.category)));
  let html = categories.map(cat => `
    <md-menu-item data-category="${cat}" style="display: flex; align-items: center; justify-content: space-between; min-width: 220px;">
      <div slot="headline">${cat}</div>
      <span slot="end" class="material-symbols-outlined delete-category-btn" data-category="${cat}" style="font-size: 18px; color: var(--color-error); cursor: pointer; padding: 4px; transition: color 0.2s;" title="删除分类">delete</span>
    </md-menu-item>
  `).join('') + `
    <md-divider></md-divider>
    <md-menu-item data-action="new-category" style="color: var(--color-brand-rust);">
      <div slot="headline">+ 新增分类...</div>
    </md-menu-item>
  `;
  
  categoryMenu.innerHTML = html;
  
  // Bind click handlers to menu-items
  const items = categoryMenu.querySelectorAll('md-menu-item');
  items.forEach(item => {
    item.addEventListener('click', (e) => {
      const categoryField = $('#edit-dish-category');
      
      // Check if delete icon was clicked
      const deleteBtn = e.target.closest('.delete-category-btn');
      if (deleteBtn) {
        e.stopPropagation();
        e.preventDefault();
        const catToDelete = deleteBtn.getAttribute('data-category');
        categoryMenu.open = false; // 关闭选择菜单
        
        // 使用自定义 Material 3 对话框确认删除
        const deleteDialog = $('#category-delete-dialog');
        const deleteScrim = $('#category-delete-dialog-scrim');
        const deleteText = $('#category-delete-dialog-text');
        
        if (deleteDialog && deleteScrim && deleteText) {
          deleteText.textContent = `确定要删除分类“${catToDelete}”吗？删除后该分类下的所有菜品将被归类为“未分类”。`;
          deleteDialog.classList.add('show');
          deleteScrim.classList.add('show');
          
          const confirmDeleteBtn = $('#btn-category-delete-confirm');
          const cancelDeleteBtn = $('#btn-category-delete-cancel');
          
          const onConfirm = () => {
            dishesDatabase.forEach(d => {
              if (d.category === catToDelete) {
                d.category = '未分类';
              }
            });
            renderEditCategoryMenu();
            renderDishGrid();
            
            if (categoryField && categoryField.value === catToDelete) {
              categoryField.value = '未分类';
            }
            
            deleteDialog.classList.remove('show');
            deleteScrim.classList.remove('show');
            showToast(`成功删除分类：${catToDelete}`, 'check_circle');
          };
          
          const onCancel = () => {
            deleteDialog.classList.remove('show');
            deleteScrim.classList.remove('show');
          };
          
          confirmDeleteBtn.replaceWith(confirmDeleteBtn.cloneNode(true));
          cancelDeleteBtn.replaceWith(cancelDeleteBtn.cloneNode(true));
          
          $('#btn-category-delete-confirm').addEventListener('click', onConfirm);
          $('#btn-category-delete-cancel').addEventListener('click', onCancel);
          deleteScrim.onclick = onCancel;
        }
        return;
      }
      
      const cat = item.getAttribute('data-category');
      const action = item.getAttribute('data-action');
      
      if (action === 'new-category') {
        categoryMenu.open = false;
        
        // 使用自定义 Material 3 对话框输入新分类名
        const createDialog = $('#category-create-dialog');
        const createScrim = $('#category-create-dialog-scrim');
        const inputField = $('#new-category-name-input');
        
        if (createDialog && createScrim && inputField) {
          inputField.value = '';
          createDialog.classList.add('show');
          createScrim.classList.add('show');
          
          const confirmCreateBtn = $('#btn-category-create-confirm');
          const cancelCreateBtn = $('#btn-category-create-cancel');
          
          const onConfirm = () => {
            const trimmed = inputField.value.trim();
            if (trimmed) {
              if (categoryField) categoryField.value = trimmed;
              createDialog.classList.remove('show');
              createScrim.classList.remove('show');
            } else {
              showToast('分类名称不能为空！', 'warning');
            }
          };
          
          const onCancel = () => {
            createDialog.classList.remove('show');
            createScrim.classList.remove('show');
          };
          
          confirmCreateBtn.replaceWith(confirmCreateBtn.cloneNode(true));
          cancelCreateBtn.replaceWith(cancelCreateBtn.cloneNode(true));
          
          $('#btn-category-create-confirm').addEventListener('click', onConfirm);
          $('#btn-category-create-cancel').addEventListener('click', onCancel);
          createScrim.onclick = onCancel;
        }
      } else if (cat) {
        if (categoryField) categoryField.value = cat;
        categoryMenu.open = false;
      }
    });
  });
}

// --- 2b. DAILY MENU MODULE ---
function setupDailyMenu() {
  const addBtn = $('#btn-add-daily-menu-dish');
  if (addBtn) {
    addBtn.addEventListener('click', () => {
      openAddDailyDishDialog();
    });
  }
  renderDailyMenu();
}

function renderDailyMenuCategoryFilters() {
  const filterContainer = $('#daily-menu-category-filter');
  if (!filterContainer) return;
  
  const categories = Array.from(new Set(dishesDatabase.map(d => d.category)));
  
  let html = `
    <button type="button" class="segmented-button ${currentDailyMenuCategory === 'all' ? 'active' : ''}" data-category="all">
      <span>全部</span>
    </button>
  `;
  
  categories.forEach(cat => {
    html += `
      <button type="button" class="segmented-button ${currentDailyMenuCategory === cat ? 'active' : ''}" data-category="${cat}">
        <span>${cat}</span>
      </button>
    `;
  });
  
  filterContainer.innerHTML = html;
  
  const categoryBtns = filterContainer.querySelectorAll('.segmented-button');
  categoryBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      categoryBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentDailyMenuCategory = btn.getAttribute('data-category');
      renderDailyMenu();
    });
  });
}

function renderDailyMenu() {
  renderDailyMenuCategoryFilters();
  
  const listContainer = $('#merchant-daily-dishes-list');
  const panel = $('#daily-dish-detail-panel');
  if (!listContainer || !panel) return;
  
  listContainer.innerHTML = '';
  
  // Get filtered daily menu dishes
  const filteredDaily = [];
  dishesDatabase.forEach((dish, idx) => {
    if (dish.dailySelected) {
      if (currentDailyMenuCategory === 'all' || dish.category === currentDailyMenuCategory) {
        filteredDaily.push({ dish, originalIndex: idx });
      }
    }
  });
  
  if (filteredDaily.length > 0) {
    const isStillInFiltered = filteredDaily.some(item => item.originalIndex === selectedDailyDishIndex);
    if (selectedDailyDishIndex === -1 || !isStillInFiltered) {
      selectedDailyDishIndex = filteredDaily[0].originalIndex;
    }
  } else {
    selectedDailyDishIndex = -1;
  }
  
  // Render Left List Items - No Price & No Stock
  filteredDaily.forEach(item => {
    const dish = item.dish;
    const originalIndex = item.originalIndex;
    
    const listItem = document.createElement('div');
    listItem.className = `m3-list-item ${originalIndex === selectedDailyDishIndex ? 'active' : ''}`;
    listItem.setAttribute('data-index', originalIndex);
    
    let offShelfBadge = '';
    if (!dish.available) {
      offShelfBadge = `<span class="list-item-status-tag">下架</span>`;
    }
    
    listItem.innerHTML = `
      <img class="list-item-avatar" src="${dish.img}" alt="${dish.title}" />
      <div class="list-item-text">
        <span class="list-item-title">${dish.title}</span>
      </div>
      ${offShelfBadge}
    `;
    
    listItem.addEventListener('click', () => {
      selectedDailyDishIndex = originalIndex;
      listContainer.querySelectorAll('.m3-list-item').forEach(el => {
        el.classList.remove('active');
        if (parseInt(el.getAttribute('data-index'), 10) === selectedDailyDishIndex) {
          el.classList.add('active');
        }
      });
      renderDailyDishDetails(selectedDailyDishIndex);
    });
    
    listContainer.appendChild(listItem);
  });
  
  // Render Right Details Panel
  renderDailyDishDetails(selectedDailyDishIndex);
}

function renderDailyDishDetails(idx) {
  const panel = $('#daily-dish-detail-panel');
  if (!panel) return;
  
  if (idx === -1 || !dishesDatabase[idx] || !dishesDatabase[idx].dailySelected) {
    panel.innerHTML = `
      <div class="detail-placeholder">
        <span class="material-symbols-outlined">restaurant</span>
        <p>请在左侧选择一个今日供应的菜品以查看详情</p>
      </div>
    `;
    return;
  }
  
  const dish = dishesDatabase[idx];
  
  const ruleText = dish.dailyStockType === 'add'
    ? `自动更新库存：每日增加 <strong>${dish.dailyStockValue || 0}</strong> 件`
    : `自动更新库存：每日重置为 <strong>${dish.dailyStockValue || 0}</strong> 件`;
  
  panel.innerHTML = `
    <div class="dish-detail-content">
      <div class="dish-detail-main-block">
        <div class="dish-detail-left-col" style="gap: 12px;">
          <h1 class="detail-display-title">${dish.title}</h1>
          <div class="detail-display-stock" style="margin-top: 8px; font-size: 15px;">当前剩余库存：<strong>${dish.stock}</strong> 件</div>
          
          <!-- Supply Config Rules display -->
          <div style="margin-top: 16px; background-color: var(--color-bg-viewport); padding: 12px; border-radius: 12px; border: 1px solid var(--color-border); display: flex; flex-direction: column; align-items: flex-start; gap: 8px; width: 100%;">
            <span style="font-size: 12px; font-weight: 700; color: var(--color-text-secondary); text-transform: uppercase; letter-spacing: 0.5px;">每日供应库存设置</span>
            <span style="font-size: 14px; color: var(--color-text-primary);">${ruleText}</span>
            <button type="button" class="btn-save-daily-menu" id="btn-edit-daily-stock" style="height: 32px; border-radius: 16px; font-size: 12px; font-weight: 700; background-color: var(--color-brand-peach); color: var(--color-brand-rust); border: none; cursor: pointer; display: flex; align-items: center; gap: 6px; padding: 0 12px; margin-top: 4px; box-shadow: none;">
              <span class="material-symbols-outlined" style="font-size: 16px;">settings_backup_restore</span>
              <span>修改规则</span>
            </button>
          </div>
        </div>
        <div class="dish-detail-right-col">
          <div class="detail-image-wrapper" style="width: 100%; aspect-ratio: 4/3;">
            <img src="${dish.img}" alt="${dish.title}" class="detail-image" />
          </div>
        </div>
      </div>
      
      <!-- Slide-to-Remove Widget -->
      <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--color-border); display: flex; flex-direction: column; gap: 8px;">
        <span style="font-size: 12px; font-weight: 700; color: var(--color-text-secondary);">移出供应操作</span>
        <div class="slide-to-remove-container" id="slide-remove-container">
          <div class="slide-text">右滑移出每日菜单</div>
          <div class="slide-handle" id="slide-remove-handle">
            <span class="material-symbols-outlined">delete</span>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Bind Modify Rules Button
  const editStockBtn = $('#btn-edit-daily-stock');
  if (editStockBtn) {
    editStockBtn.addEventListener('click', () => {
      openDailyStockConfigDialog(idx);
    });
  }
  
  // Bind Swipe-to-Remove Drag Handler
  const container = $('#slide-remove-container');
  const handle = $('#slide-remove-handle');
  if (container && handle) {
    let isDragging = false;
    let startX = 0;
    let currentX = 0;
    
    const onStart = (e) => {
      isDragging = true;
      startX = (e.type === 'touchstart') ? e.touches[0].clientX : e.clientX;
      handle.style.transition = 'none';
      const text = container.querySelector('.slide-text');
      if (text) text.style.transition = 'none';
      
      if (e.type === 'touchstart') {
        document.addEventListener('touchmove', onMove, { passive: true });
        document.addEventListener('touchend', onEnd);
      } else {
        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onEnd);
      }
    };
    
    const onMove = (e) => {
      if (!isDragging) return;
      currentX = (e.type === 'touchmove') ? e.touches[0].clientX : e.clientX;
      let diff = currentX - startX;
      const maxSlide = container.clientWidth - handle.clientWidth - 8;
      
      if (diff < 0) diff = 0;
      if (diff > maxSlide) diff = maxSlide;
      
      handle.style.transform = `translateX(${diff}px)`;
      
      const text = container.querySelector('.slide-text');
      if (text) {
        const opacity = Math.max(0, 1 - (diff / (maxSlide * 0.7)));
        text.style.opacity = opacity;
      }
    };
    
    const onEnd = () => {
      if (!isDragging) return;
      isDragging = false;
      
      document.removeEventListener('mousemove', onMove);
      document.removeEventListener('mouseup', onEnd);
      document.removeEventListener('touchmove', onMove);
      document.removeEventListener('touchend', onEnd);
      
      const maxSlide = container.clientWidth - handle.clientWidth - 8;
      let diff = currentX - startX;
      
      if (diff >= maxSlide * 0.9) {
        handle.style.transition = 'transform 0.1s ease-out';
        handle.style.transform = `translateX(${maxSlide}px)`;
        
        setTimeout(() => {
          dish.dailySelected = false;
          showToast(`成功将“${dish.title}”移出每日供应！`, 'check_circle');
          renderDailyMenu();
        }, 120);
      } else {
        handle.style.transition = 'transform 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
        handle.style.transform = 'translateX(0px)';
        const text = container.querySelector('.slide-text');
        if (text) {
          text.style.transition = 'opacity 0.3s';
          text.style.opacity = 1;
        }
      }
    };
    
    handle.addEventListener('mousedown', onStart);
    handle.addEventListener('touchstart', onStart);
  }
}

function openAddDailyDishDialog() {
  const dialog = $('#merchant-add-daily-dish-dialog');
  const scrim = $('#merchant-add-daily-dish-dialog-scrim');
  if (!dialog || !scrim) return;
  
  const checkedIndices = new Set();
  let currentDialogCategory = 'all';
  
  dialog.classList.add('show');
  scrim.classList.add('show');
  
  const renderDialogDishes = () => {
    const dishesGrid = $('#add-daily-dialog-dishes-grid');
    if (!dishesGrid) return;
    
    dishesGrid.innerHTML = '';
    dishesGrid.className = 'm3-list-card';
    
    const availableDishes = [];
    dishesDatabase.forEach((dish, idx) => {
      if (!dish.dailySelected) {
        if (currentDialogCategory === 'all' || dish.category === currentDialogCategory) {
          availableDishes.push({ dish, originalIndex: idx });
        }
      }
    });
    
    if (availableDishes.length === 0) {
      dishesGrid.innerHTML = `
        <div style="text-align: center; color: var(--color-text-muted); padding: 40px 0;">
          <span class="material-symbols-outlined" style="font-size: 40px; margin-bottom: 8px; display: block;">restaurant_menu</span>
          <span>没有可添加的菜品</span>
        </div>
      `;
      return;
    }
    
    availableDishes.forEach(item => {
      const dish = item.dish;
      const originalIndex = item.originalIndex;
      
      const row = document.createElement('div');
      row.className = `m3-list-item ${checkedIndices.has(originalIndex) ? 'active' : ''}`;
      row.setAttribute('data-index', originalIndex);
      row.style.cursor = 'pointer';
      
      row.innerHTML = `
        <img class="list-item-avatar" src="${dish.img}" alt="${dish.title}" />
        <div class="list-item-text">
          <span class="list-item-title">${dish.title}</span>
          <span class="list-item-subtitle">${dish.category} · ${dish.price}</span>
        </div>
        <md-checkbox class="dialog-dish-checkbox" data-index="${originalIndex}" ${checkedIndices.has(originalIndex) ? 'checked' : ''} style="pointer-events: none; margin-left: 12px;"></md-checkbox>
      `;
      
      row.addEventListener('click', () => {
        if (checkedIndices.has(originalIndex)) {
          checkedIndices.delete(originalIndex);
        } else {
          checkedIndices.add(originalIndex);
        }
        updateDialogUI();
      });
      
      dishesGrid.appendChild(row);
    });
  };
  
  const renderDialogCategories = () => {
    const filterContainer = $('#add-daily-dialog-category-filter');
    if (!filterContainer) return;
    
    const categories = Array.from(new Set(dishesDatabase.map(d => d.category)));
    
    let html = `
      <button type="button" class="segmented-button ${currentDialogCategory === 'all' ? 'active' : ''}" data-category="all">
        <span>全部</span>
      </button>
    `;
    
    categories.forEach(cat => {
      html += `
        <button type="button" class="segmented-button ${currentDialogCategory === cat ? 'active' : ''}" data-category="${cat}">
          <span>${cat}</span>
        </button>
      `;
    });
    
    filterContainer.innerHTML = html;
    
    const categoryBtns = filterContainer.querySelectorAll('.segmented-button');
    categoryBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        categoryBtns.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentDialogCategory = btn.getAttribute('data-category');
        renderDialogDishes();
      });
    });
  };
  
  const updateDialogUI = () => {
    const rows = $('#add-daily-dialog-dishes-grid').querySelectorAll('.m3-list-item');
    rows.forEach(row => {
      const checkbox = row.querySelector('md-checkbox');
      if (checkbox) {
        const originalIndex = parseInt(checkbox.getAttribute('data-index'), 10);
        const isChecked = checkedIndices.has(originalIndex);
        checkbox.checked = isChecked;
        if (isChecked) {
          row.classList.add('active');
        } else {
          row.classList.remove('active');
        }
      }
    });
    
    const countLabel = $('#add-daily-dialog-checked-count');
    if (countLabel) countLabel.textContent = checkedIndices.size;
    
    const previewList = $('#add-daily-dialog-preview-list');
    if (!previewList) return;
    previewList.innerHTML = '';
    
    if (checkedIndices.size === 0) {
      previewList.innerHTML = `
        <div style="text-align: center; color: var(--color-text-muted); padding: 40px 0; font-size: 13px;">
          <span>暂无勾选菜品</span>
        </div>
      `;
      return;
    }
    
    checkedIndices.forEach(originalIndex => {
      const dish = dishesDatabase[originalIndex];
      const previewRow = document.createElement('div');
      previewRow.className = 'dialog-preview-row';
      previewRow.style = 'display: flex; align-items: center; justify-content: space-between; padding: 8px; background-color: var(--color-bg-viewport); border-radius: 10px; border: 1px solid var(--color-border);';
      previewRow.innerHTML = `
        <div style="display: flex; align-items: center; gap: 8px; pointer-events: none;">
          <img src="${dish.img}" style="width: 32px; height: 32px; border-radius: 6px; object-fit: cover;" />
          <span style="font-size: 13px; font-weight: 500; color: var(--color-text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 120px;">${dish.title}</span>
        </div>
        <span class="material-symbols-outlined remove-checked-btn" data-index="${originalIndex}" style="font-size: 18px; color: var(--color-text-secondary); cursor: pointer; padding: 2px;">close</span>
      `;
      
      const removeBtn = previewRow.querySelector('.remove-checked-btn');
      removeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        checkedIndices.delete(originalIndex);
        updateDialogUI();
      });
      
      previewList.appendChild(previewRow);
    });
  };
  
  renderDialogCategories();
  renderDialogDishes();
  updateDialogUI();
  
  const confirmBtn = $('#btn-add-daily-dish-confirm');
  const cancelBtn = $('#btn-add-daily-dish-cancel');
  
  const onCancel = () => {
    dialog.classList.remove('show');
    scrim.classList.remove('show');
  };
  
  const onConfirm = () => {
    if (checkedIndices.size === 0) {
      showToast('未勾选任何菜品！', 'warning');
      return;
    }
    
    checkedIndices.forEach(idx => {
      dishesDatabase[idx].dailySelected = true;
    });
    
    showToast(`成功添加 ${checkedIndices.size} 个菜品到每日菜单！`, 'check_circle');
    renderDailyMenu();
    onCancel();
  };
  
  confirmBtn.replaceWith(confirmBtn.cloneNode(true));
  cancelBtn.replaceWith(cancelBtn.cloneNode(true));
  
  $('#btn-add-daily-dish-confirm').addEventListener('click', onConfirm);
  $('#btn-add-daily-dish-cancel').addEventListener('click', onCancel);
  scrim.onclick = onCancel;
}

function openDailyStockConfigDialog(idx) {
  const dialog = $('#daily-stock-config-dialog');
  const scrim = $('#daily-stock-config-dialog-scrim');
  if (!dialog || !scrim) return;
  
  const dish = dishesDatabase[idx];
  
  dialog.innerHTML = `
    <h3 class="m3-dialog-title" style="display: flex; align-items: center; gap: 8px; margin-bottom: 16px;">
      <span class="material-symbols-outlined" style="color: var(--color-brand-rust);">settings_backup_restore</span>
      <span>每日供应自动更新库存</span>
    </h3>
    
    <div style="display: flex; flex-direction: column; gap: 16px; align-items: center; margin-bottom: 20px;">
      <img src="${dish.img}" style="width: 100px; height: 100px; border-radius: 12px; object-fit: cover; box-shadow: 0 4px 10px rgba(0,0,0,0.1);" />
      <div style="text-align: center;">
        <h4 style="margin: 0; font-size: 15px; font-weight: 700; color: var(--color-text-primary);">${dish.title}</h4>
        <span style="font-size: 13px; color: var(--color-text-secondary); margin-top: 4px; display: inline-block;">当前剩余库存: <strong>${dish.stock}</strong> 件</span>
      </div>
      
      <div style="width: 100%; border-top: 1px solid var(--color-border); padding-top: 16px; display: flex; flex-direction: column; gap: 12px;">
        <label style="font-size: 13px; font-weight: 700; color: var(--color-text-secondary);">供应更新选项</label>
        
        <div style="display: flex; flex-direction: column; gap: 8px; align-self: flex-start; width: 100%;">
          <label style="display: flex; align-items: center; gap: 10px; cursor: pointer; font-size: 14px; color: var(--color-text-primary);">
            <input type="radio" name="daily-stock-type" value="add" ${dish.dailyStockType === 'add' ? 'checked' : ''} style="width: 18px; height: 18px; accent-color: var(--color-brand-rust);" />
            <span>每日增加库存数量</span>
          </label>
          <label style="display: flex; align-items: center; gap: 10px; cursor: pointer; font-size: 14px; color: var(--color-text-primary);">
            <input type="radio" name="daily-stock-type" value="reset" ${dish.dailyStockType !== 'add' ? 'checked' : ''} style="width: 18px; height: 18px; accent-color: var(--color-brand-rust);" />
            <span>每日重置为库存数量</span>
          </label>
        </div>
        
        <md-outlined-text-field id="daily-stock-value-input" label="库存数量 (件)" type="number" value="${dish.dailyStockValue || 50}" style="width: 100%; margin-top: 4px;"></md-outlined-text-field>
      </div>
    </div>
    
    <div class="m3-dialog-actions" style="border-top: 1px solid var(--color-border); padding-top: 16px;">
      <button type="button" class="m3-dialog-btn btn-text" id="btn-daily-stock-cancel" style="color: var(--color-text-secondary);">取消</button>
      <button type="button" class="m3-dialog-btn btn-filled" id="btn-daily-stock-confirm">确认修改</button>
    </div>
  `;
  
  dialog.classList.add('show');
  scrim.classList.add('show');
  
  const confirmBtn = $('#btn-daily-stock-confirm');
  const cancelBtn = $('#btn-daily-stock-cancel');
  
  const onCancel = () => {
    dialog.classList.remove('show');
    scrim.classList.remove('show');
  };
  
  const onConfirm = () => {
    const valInput = $('#daily-stock-value-input');
    const typeRadios = dialog.querySelectorAll('input[name="daily-stock-type"]');
    let selectedType = 'reset';
    typeRadios.forEach(r => {
      if (r.checked) selectedType = r.value;
    });
    
    const qtyVal = valInput ? parseInt(valInput.value.trim(), 10) : NaN;
    if (isNaN(qtyVal) || qtyVal < 0) {
      showToast('请输入有效的库存数量！', 'warning');
      return;
    }
    
    dish.dailyStockType = selectedType;
    dish.dailyStockValue = qtyVal;
    
    showToast(`“${dish.title}”的供应库存规则修改成功！`, 'check_circle');
    renderDailyMenu();
    onCancel();
  };
  
  confirmBtn.addEventListener('click', onConfirm);
  cancelBtn.addEventListener('click', onCancel);
  scrim.onclick = onCancel;
}

// --- 3. ORDER HISTORY LOG MODULE ---
let historyDatabase = [
  { id: '#0999', customer: 'Tony Stark', items: 'Tonkotsu Ramen x2, Plum Soup x2', total: '$30.00', time: 'Today 12:45 PM', status: 'completed' },
  { id: '#0998', customer: 'Bruce Banner', items: 'Spicy Dan Dan Noodles x3', total: '$28.50', time: 'Today 11:30 AM', status: 'completed' },
  { id: '#0997', customer: 'Steve Rogers', items: 'Pork Gyoza x4', total: '$24.00', time: 'Today 10:15 AM', status: 'cancelled' }
];

function setupHistoryLog() {}

function renderHistoryList() {
  const container = $('#merchant-history-container');
  if (!container) return;
  
  container.innerHTML = '';
  
  historyDatabase.forEach(item => {
    const row = document.createElement('div');
    row.className = 'history-row';
    
    row.innerHTML = `
      <span class="history-id">${item.id}</span>
      <span class="history-customer">${item.customer}</span>
      <span class="history-items">${item.items}</span>
      <span class="history-price">${item.total}</span>
      <span class="history-time">${item.time}</span>
      <div>
        <span class="history-status-badge ${item.status === 'completed' ? 'completed' : 'cancelled'}">
          ${item.status === 'completed' ? 'Completed' : 'Cancelled'}
        </span>
      </div>
    `;
    
    container.appendChild(row);
  });
}

// --- 4. STORE CONFIGURATION MODULE ---
function setupStoreConfiguration() {
  const saveBtn = $('#btn-save-store-config');
  const storeToggle = $('#config-store-toggle');
  
  if (storeToggle) {
    storeToggle.addEventListener('change', () => {
      const isOnline = storeToggle.selected;
      const badge = $('#store-status-badge');
      if (badge) {
        if (isOnline) {
          badge.className = 'store-status-pill online';
          badge.textContent = 'Stall Online';
          showToast('Store has been set to ONLINE', 'check_circle');
        } else {
          badge.className = 'store-status-pill offline';
          badge.textContent = 'Stall Offline';
          showToast('Store has been set to OFFLINE', 'error');
        }
      }
    });
  }

  if (saveBtn) {
    saveBtn.addEventListener('click', () => {
      const nameVal = $('#config-store-name').value.trim();
      const cuisineVal = $('#config-store-cuisine').value.trim();
      
      if (!nameVal) {
        showToast('Stall name cannot be empty!', 'warning');
        return;
      }
      
      // Update display details in header
      const displayName = $('#merchant-stall-name');
      if (displayName) displayName.textContent = nameVal;
      
      showToast('Store configurations saved successfully!', 'check_circle');
    });
  }
}

function setupScanDialog() {
  const scanBtn = $('#btn-header-scan');
  const scanDialog = $('#merchant-scan-dialog');
  const scanScrim = $('#merchant-scan-dialog-scrim');
  const cancelBtn = $('#btn-merchant-scan-cancel');
  const confirmBtn = $('#btn-merchant-scan-confirm');
  const inputField = $('#scan-order-id');

  const scanVideo = $('#scan-video');
  const cameraPlaceholder = $('#scan-camera-placeholder');
  const cameraOverlay = $('#scan-viewfinder-overlay');
  const cameraStatus = $('#scan-camera-status-text');

  let videoStream = null;
  let simulatedScanTimeout = null;

  function playSynthBeep() {
    try {
      const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      const oscillator = audioCtx.createOscillator();
      const gainNode = audioCtx.createGain();
      oscillator.type = 'sine';
      oscillator.frequency.setValueAtTime(1000, audioCtx.currentTime); // 1000Hz beep
      gainNode.gain.setValueAtTime(0.12, audioCtx.currentTime); // Gain volume
      oscillator.connect(gainNode);
      gainNode.connect(audioCtx.destination);
      oscillator.start();
      oscillator.stop(audioCtx.currentTime + 0.15); // Beep for 150ms
    } catch (e) {
      console.warn('Synth audio beep not supported or blocked:', e);
    }
  }

  function stopCameraStream() {
    if (videoStream) {
      videoStream.getTracks().forEach(track => track.stop());
      videoStream = null;
    }
    if (scanVideo) {
      scanVideo.srcObject = null;
      scanVideo.style.display = 'none';
    }
    if (cameraPlaceholder) {
      cameraPlaceholder.style.display = 'flex';
      if (cameraStatus) cameraStatus.textContent = '正在启动摄像头...';
    }
    if (cameraOverlay) {
      cameraOverlay.style.display = 'none';
    }
    if (simulatedScanTimeout) {
      clearTimeout(simulatedScanTimeout);
      simulatedScanTimeout = null;
    }
  }

  function startCameraStream() {
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      if (cameraStatus) cameraStatus.textContent = '正在获取摄像头权限...';
      navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
        .then(stream => {
          videoStream = stream;
          if (scanVideo) {
            scanVideo.srcObject = stream;
            scanVideo.style.display = 'block';
          }
          if (cameraPlaceholder) {
            cameraPlaceholder.style.display = 'none';
          }
          if (cameraOverlay) {
            cameraOverlay.style.display = 'block';
          }
          
          // Trigger the 2.5-second simulated scanning recognition
          startSimulatedScanRecognition();
        })
        .catch(err => {
          console.error('Camera streaming access error:', err);
          if (cameraStatus) cameraStatus.textContent = '无法启动摄像头：权限拒绝';
        });
    } else {
      if (cameraStatus) cameraStatus.textContent = '当前环境不支持摄像头输入';
    }
  }

  function startSimulatedScanRecognition() {
    simulatedScanTimeout = setTimeout(() => {
      // Find the first ready order (waiting for pickup)
      const readyOrder = ordersDatabase.find(o => o.status === 'ready');
      if (readyOrder) {
        playSynthBeep();
        if (inputField) {
          inputField.value = readyOrder.id;
        }
        showToast(`扫描成功！识别到订单：${readyOrder.id}`, 'qr_code_scanner');
        
        // Auto trigger validation after 0.8s for smooth styling feedback
        setTimeout(() => {
          if (confirmBtn) {
            confirmBtn.click();
          }
        }, 800);
      }
    }, 2800);
  }

  if (scanBtn) {
    scanBtn.addEventListener('click', () => {
      if (scanDialog && scanScrim) {
        scanDialog.classList.add('show');
        scanScrim.classList.add('show');
      }
      if (inputField) {
        inputField.value = '';
      }
      startCameraStream();
    });
  }

  const closeScanDialog = () => {
    if (scanDialog && scanScrim) {
      scanDialog.classList.remove('show');
      scanScrim.classList.remove('show');
    }
    stopCameraStream();
  };

  if (cancelBtn) cancelBtn.addEventListener('click', closeScanDialog);
  if (scanScrim) scanScrim.addEventListener('click', closeScanDialog);

  if (confirmBtn) {
    confirmBtn.addEventListener('click', () => {
      let codeVal = inputField ? inputField.value.trim() : '';
      if (!codeVal) {
        showToast('请输入订单号或取餐码！', 'warning');
        return;
      }
      
      if (!codeVal.startsWith('#')) {
        codeVal = '#' + codeVal;
      }
      
      const index = ordersDatabase.findIndex(o => o.id === codeVal);
      if (index !== -1) {
        const order = ordersDatabase[index];
        showToast(`核销成功！订单 ${codeVal} 已完成取餐`, 'check_circle');
        
        historyDatabase.unshift({
          id: order.id,
          customer: order.customer,
          items: order.items,
          total: order.total,
          time: 'Just now',
          status: 'completed'
        });
        
        ordersDatabase.splice(index, 1);
        renderHistoryList();
        renderOrderCards();
        closeScanDialog();
      } else {
        showToast(`未找到订单号或取餐码 ${codeVal}！`, 'error');
      }
    });
  }
}

// --- 2c. CREATE DISH MODULE ---
function setupCreateDishDialog() {
  const createBtn = $('#btn-create-new-dish');
  const dialog = $('#merchant-create-dish-dialog');
  const scrim = $('#merchant-create-dish-dialog-scrim');
  const cancelBtn = $('#btn-create-dish-cancel');
  const confirmBtn = $('#btn-create-dish-confirm');
  
  if (!dialog || !scrim) return;

  const categoryBtns = dialog.querySelectorAll('.merchant-tab');
  let selectedCategory = '主食';
  
  categoryBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      categoryBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      selectedCategory = btn.getAttribute('data-category');
    });
  });

  const openDialog = () => {
    dialog.classList.add('show');
    scrim.classList.add('show');
    $('#new-dish-name').value = '';
    $('#new-dish-price').value = '';
    $('#new-dish-stock').value = '';
    // Reset category selection
    categoryBtns.forEach(b => b.classList.remove('active'));
    const mainBtn = $('#btn-select-category-main');
    if (mainBtn) mainBtn.classList.add('active');
    selectedCategory = '主食';
  };

  const closeDialog = () => {
    dialog.classList.remove('show');
    scrim.classList.remove('show');
  };

  if (createBtn) createBtn.addEventListener('click', openDialog);
  if (cancelBtn) cancelBtn.addEventListener('click', closeDialog);
  if (scrim) scrim.addEventListener('click', closeDialog);

  if (confirmBtn) {
    confirmBtn.addEventListener('click', () => {
      const name = $('#new-dish-name').value.trim();
      const priceVal = $('#new-dish-price').value.trim();
      const stockVal = $('#new-dish-stock').value.trim();
      
      if (!name) {
        showToast('请输入菜品名称！', 'warning');
        return;
      }
      if (!priceVal || isNaN(parseFloat(priceVal))) {
        showToast('请输入有效的菜品价格！', 'warning');
        return;
      }
      if (!stockVal || isNaN(parseInt(stockVal, 10)) || parseInt(stockVal, 10) < 0) {
        showToast('请输入有效的初始库存！', 'warning');
        return;
      }

      const stock = parseInt(stockVal, 10);
      const available = stock > 0;

      // Add to database
      const formattedPrice = '$' + parseFloat(priceVal).toFixed(2);
      const newDish = {
        title: name,
        price: formattedPrice,
        available: available,
        img: '/images/beef_chow_mein.png', // Default placeholder image
        category: selectedCategory,
        dailySelected: true,
        stock: stock,
        dailyStockType: 'reset',
        dailyStockValue: stock
      };
      
      dishesDatabase.push(newDish);
      
      // Select the newly added dish
      selectedDishIndex = dishesDatabase.length - 1;
      
      // Re-render views
      renderDishGrid();
      renderDailyMenu();
      
      showToast(`成功创建新菜品：${name} (${selectedCategory})`, 'check_circle');
      closeDialog();
    });
  }
}

