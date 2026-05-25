import '@material/web/textfield/outlined-text-field.js';

// Helper Selectors
const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => document.querySelectorAll(selector);

// Merchant Stall Database (Matches dynamic URL binds)
const merchantDatabase = {
  '1': { name: 'Noodle House', title: 'Main Dining Hall' },
  '2': { name: 'Sigkladas Grill', title: 'Main Dining Hall' },
  '3': { name: 'Sizzling Grill', title: 'Main Dining Hall' },
  '4': { name: 'Salad House', title: 'Main Dining Hall' }
};

// Initial queue dataset structured to match the mockup exactly
const initialQueues = {
  preparing: [
    { ticketNumber: 'A006', stall: 'Noodle House', waitMinutes: 5 },
    { ticketNumber: 'A007', stall: 'Sigkladas Grill', waitMinutes: 5 },
    { ticketNumber: 'A008', stall: 'Sigkladas Grill', waitMinutes: 5 },
    { ticketNumber: 'A009', stall: 'Sigkladas Grill', waitMinutes: 5 },
    { ticketNumber: 'A010', stall: 'Sigkladas Grill', waitMinutes: 5 },
    { ticketNumber: 'A011', stall: 'Sigkladas Grill', waitMinutes: 2 }
  ],
  ready: [
    { ticketNumber: 'A003', stall: 'Sizzling Grill' },
    { ticketNumber: 'A004', stall: 'Sizzling Grill' },
    { ticketNumber: 'A005', stall: 'Sizzling Grill' },
    { ticketNumber: 'A006', stall: 'Sizzling Grill' },
    { ticketNumber: 'A007', stall: 'Sizzling Grill' },
    { ticketNumber: 'A008', stall: 'Sizzling Grill' }
  ]
};

// Application States
let currentMerchantId = '1';
let currentStallName = 'Noodle House';
let preparingOrders = [];
let readyOrders = [];
let connectionState = 'DISCONNECTED'; // CONNECTING, CONNECTED, DISCONNECTED, POLLING
let wsReconnectTimer = null;
let wsReconnectAttempts = 0;
const maxWsReconnectAttempts = 5;
let pollingIntervalTimer = null;
let timeElapsedIntervalTimer = null;
let lastCalledTicket = null; // Highlighting called card

// Initialize
document.addEventListener('DOMContentLoaded', () => {
  initMerchantAndData();
  setupDigitalClock();
  setupWebSocketSimulation();
  setupDebugPanel();
  setupTimerIncrements();
});

// --- Extract params & load static data ---
function initMerchantAndData() {
  const urlParams = new URLSearchParams(window.location.search);
  let mId = urlParams.get('merchantId') || urlParams.get('m') || '1';
  
  if (!merchantDatabase[mId]) {
    mId = '1'; // Default fallback
  }
  
  currentMerchantId = mId;
  currentStallName = merchantDatabase[currentMerchantId].name;
  
  // Set window title in code
  document.title = `Campus Eats - ${currentStallName} TV Display`;

  // Clone initial queues
  preparingOrders = JSON.parse(JSON.stringify(initialQueues.preparing));
  readyOrders = JSON.parse(JSON.stringify(initialQueues.ready));

  // Customize first card stall to match the current active merchant
  if (preparingOrders.length > 0) {
    preparingOrders[0].stall = currentStallName;
  }
  
  renderQueues();
}

// --- Digital Clock ---
function setupDigitalClock() {
  const timeEl = $('#clock-time');
  const dateEl = $('#clock-date');

  function updateClock() {
    const now = new Date();
    
    // Time format: HH:MM:SS
    const hrs = String(now.getHours()).padStart(2, '0');
    const mins = String(now.getMinutes()).padStart(2, '0');
    const secs = String(now.getSeconds()).padStart(2, '0');
    timeEl.textContent = `${hrs}:${secs}:${mins}`; // Displays hh:ss:mm or similar, let's do standard hh:mm:ss
    timeEl.textContent = `${hrs}:${mins}:${secs}`;
    
    // Date format: Monday, 25 May 2026
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    
    const dayName = days[now.getDay()];
    const mName = months[now.getMonth()];
    const dy = now.getDate();
    const yr = now.getFullYear();
    
    dateEl.textContent = `${dayName}, ${dy} ${mName} ${yr}`;
  }

  updateClock();
  setInterval(updateClock, 1000);
}

// --- Render Queues ---
function renderQueues() {
  renderPreparingList();
  renderReadyList();
}

function renderPreparingList() {
  const listContainer = $('#preparing-list');
  const countBadge = $('#prep-count-badge');
  
  listContainer.innerHTML = '';
  countBadge.textContent = `${preparingOrders.length} Orders`;

  if (preparingOrders.length === 0) {
    listContainer.innerHTML = `
      <div style="grid-column: span 2; display: flex; flex-direction: column; align-items: center; justify-content: center; color: var(--color-text-secondary); padding: 4vw 0;">
        <span class="material-symbols-outlined" style="font-size: 4vw; opacity: 0.3; margin-bottom: 1vw;">restaurant</span>
        <p style="font-size: 1.2vw; font-weight: 500;">No preparing orders</p>
      </div>
    `;
    return;
  }

  preparingOrders.forEach(order => {
    const card = document.createElement('div');
    card.className = 'preparing-card';
    
    card.innerHTML = `
      <div class="preparing-card-header">
        <span class="ticket-num">${order.ticketNumber}</span>
        <span class="wait-time">${order.waitMinutes} mins ago</span>
      </div>
      <span class="stall-name">${order.stall}</span>
    `;
    listContainer.appendChild(card);
  });
}

function renderReadyList() {
  const listContainer = $('#ready-list');
  const countBadge = $('#ready-count-badge');
  
  listContainer.innerHTML = '';
  countBadge.textContent = `${readyOrders.length} Orders Ready`;

  if (readyOrders.length === 0) {
    listContainer.innerHTML = `
      <div style="grid-column: span 4; display: flex; flex-direction: column; align-items: center; justify-content: center; color: var(--color-text-secondary); padding: 4vw 0;">
        <span class="material-symbols-outlined" style="font-size: 4vw; opacity: 0.3; margin-bottom: 1vw;">check_circle</span>
        <p style="font-size: 1.2vw; font-weight: 500;">No ready orders</p>
      </div>
    `;
    return;
  }

  readyOrders.forEach(order => {
    const card = document.createElement('div');
    const isLatest = order.ticketNumber === lastCalledTicket;
    
    card.className = `ready-card ${isLatest ? 'called-highlight' : ''}`;
    
    card.innerHTML = `
      <span class="ticket-num">${order.ticketNumber}</span>
      <span class="stall-name">${order.stall}</span>
    `;
    listContainer.appendChild(card);
  });
}

// Increment waiting times every minute
function setupTimerIncrements() {
  timeElapsedIntervalTimer = setInterval(() => {
    preparingOrders.forEach(order => {
      order.waitMinutes++;
    });
    renderPreparingList();
  }, 60000);
}

// ================= SILENT BUSINESS EVENTS & WEBSOCKET SIMULATORS =================

function setConnectionState(state) {
  connectionState = state;
  const pill = $('#connection-pill');
  const label = $('#connection-status-label');

  pill.className = 'live-pill';

  switch (state) {
    case 'CONNECTED':
      pill.classList.add('state-connected');
      label.textContent = 'Live';
      wsReconnectAttempts = 0;
      break;
    case 'CONNECTING':
      pill.classList.add('state-connecting');
      label.textContent = 'Connecting';
      break;
    case 'POLLING':
      pill.classList.add('state-connecting');
      label.textContent = 'Polling';
      break;
    case 'DISCONNECTED':
    default:
      pill.classList.add('state-disconnected');
      label.textContent = 'Offline';
      break;
  }
}

function setupWebSocketSimulation() {
  setConnectionState('CONNECTING');
  setTimeout(() => {
    connectWS();
  }, 1200);
}

function connectWS() {
  if (wsReconnectTimer) clearTimeout(wsReconnectTimer);
  if (pollingIntervalTimer) clearInterval(pollingIntervalTimer);

  setConnectionState('CONNECTED');
  startPeriodicEventSimulations();
}

function disconnectWSAndRetry() {
  if (wsReconnectTimer) clearTimeout(wsReconnectTimer);
  if (pollingIntervalTimer) clearInterval(pollingIntervalTimer);
  stopPeriodicEventSimulations();

  wsReconnectAttempts = 1;
  setConnectionState('CONNECTING');
  runReconnectSequence();
}

function runReconnectSequence() {
  if (wsReconnectAttempts > maxWsReconnectAttempts) {
    setConnectionState('POLLING');
    startHTTPPollingBackup();
    return;
  }

  wsReconnectTimer = setTimeout(() => {
    const success = Math.random() < 0.4;
    if (success) {
      connectWS();
    } else {
      wsReconnectAttempts++;
      setConnectionState('CONNECTING');
      runReconnectSequence();
    }
  }, 1800);
}

function startHTTPPollingBackup() {
  pollingIntervalTimer = setInterval(() => {
    if (Math.random() < 0.3) {
      triggerPreparingUpdate();
    }
  }, 10000);
}

let periodicEventsTimer = null;
function startPeriodicEventSimulations() {
  if (periodicEventsTimer) clearInterval(periodicEventsTimer);
  
  periodicEventsTimer = setInterval(() => {
    const dice = Math.random();
    if (dice < 0.35) {
      triggerPreparingUpdate();
    } else if (dice < 0.70) {
      triggerReadyNotification();
    }
  }, 25000);
}

function stopPeriodicEventSimulations() {
  if (periodicEventsTimer) clearInterval(periodicEventsTimer);
}

// --- Event 1: Preparing Order ---
function handlePreparingUpdate(ticketNum) {
  if (!preparingOrders.some(o => o.ticketNumber === ticketNum)) {
    preparingOrders.push({
      ticketNumber: ticketNum,
      stall: currentStallName,
      waitMinutes: 0
    });
    renderPreparingList();
  }
}

// --- Event 2: Ready Order ---
function handleWaitingUpdate(ticketNum) {
  preparingOrders = preparingOrders.filter(o => o.ticketNumber !== ticketNum);
  renderPreparingList();

  if (!readyOrders.some(o => o.ticketNumber === ticketNum)) {
    readyOrders.unshift({
      ticketNumber: ticketNum,
      stall: currentStallName
    });
    renderReadyList();
  }
}

// --- Event 3: Calling Overlay (CALL_NOTIFY) - NO SOUNDS ---
function handleCallNotify(ticketNum) {
  // Ensure it is moved to the ready list first
  handleWaitingUpdate(ticketNum);

  // Focus glow
  lastCalledTicket = ticketNum;
  renderReadyList();

  // Show center visual popup card
  const overlay = $('#announcement-overlay');
  const numEl = $('#announcement-number');
  const stallEl = $('#announcement-stall');
  
  numEl.textContent = ticketNum;
  stallEl.textContent = currentStallName;
  
  overlay.classList.add('show');

  // NO Speech Synthesis, NO synthesized beeps. (Silence mode as requested)

  // Disappears after 2.0s
  setTimeout(() => {
    overlay.classList.remove('show');
  }, 2000);
}

// --- Random Mock Triggers ---
function triggerPreparingUpdate() {
  const prefix = currentMerchantId === '1' ? 'A' : currentMerchantId === '2' ? 'B' : currentMerchantId === '3' ? 'C' : 'D';
  const num = String(Math.floor(10 + Math.random() * 89));
  const ticket = `${prefix}0${num}`;
  handlePreparingUpdate(ticket);
}

function triggerReadyNotification() {
  if (preparingOrders.length > 0) {
    const targetIdx = Math.floor(Math.random() * preparingOrders.length);
    const targetTicket = preparingOrders[targetIdx].ticketNumber;
    handleCallNotify(targetTicket);
  } else {
    const prefix = currentMerchantId === '1' ? 'A' : currentMerchantId === '2' ? 'B' : currentMerchantId === '3' ? 'C' : 'D';
    const num = String(Math.floor(10 + Math.random() * 89));
    const ticket = `${prefix}0${num}`;
    handleCallNotify(ticket);
  }
}

// ================= FLOATING DEVELOPER DEBUG PANEL =================

function setupDebugPanel() {
  const panel = $('#debug-panel');
  const closeBtn = $('#btn-debug-close');

  document.addEventListener('keydown', (e) => {
    if (e.key === 'd' || e.key === 'D') {
      if (document.activeElement.tagName !== 'INPUT' && document.activeElement.tagName !== 'TEXTAREA') {
        panel.classList.toggle('active');
      }
    }
  });

  document.body.addEventListener('dblclick', (e) => {
    if (!e.target.closest('.debug-panel') && !e.target.closest('.announcement-card')) {
      panel.classList.toggle('active');
    }
  });

  if (closeBtn) {
    closeBtn.addEventListener('click', () => {
      panel.classList.remove('active');
    });
  }

  // Bindings
  $('#btn-dbg-connect').addEventListener('click', () => connectWS());
  $('#btn-dbg-disconnect').addEventListener('click', () => disconnectWSAndRetry());
  $('#btn-dbg-fail').addEventListener('click', () => {
    if (wsReconnectTimer) clearTimeout(wsReconnectTimer);
    if (pollingIntervalTimer) clearInterval(pollingIntervalTimer);
    stopPeriodicEventSimulations();
    wsReconnectAttempts = maxWsReconnectAttempts + 1;
    setConnectionState('POLLING');
    startHTTPPollingBackup();
  });

  $('#btn-msg-prep').addEventListener('click', () => {
    const num = $('#dbg-ticket-num').value.trim().toUpperCase() || 'A008';
    handlePreparingUpdate(num);
  });

  $('#btn-msg-ready').addEventListener('click', () => {
    const num = $('#dbg-ticket-num').value.trim().toUpperCase() || 'A008';
    handleWaitingUpdate(num);
  });

  $('#btn-msg-call').addEventListener('click', () => {
    const num = $('#dbg-ticket-num').value.trim().toUpperCase() || 'A008';
    handleCallNotify(num);
  });

  $('#btn-dbg-reset').addEventListener('click', () => {
    initMerchantAndData();
  });

  $('#btn-dbg-clear').addEventListener('click', () => {
    preparingOrders = [];
    readyOrders = [];
    lastCalledTicket = null;
    renderQueues();
  });
}
