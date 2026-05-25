import '@material/web/textfield/outlined-text-field.js';
import '@material/web/progress/circular-progress.js';

// --- Mobile App State Management & Interactions ---

// Helper Selectors
const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => document.querySelectorAll(selector);

// DOM Elements
const loginView = $('#login-view');
const registerView = $('#register-view');
const menuView = $('#menu-view');

const tabPassword = $('#tab-password');
const tabCode = $('#tab-code');
const loginPwdWrapper = $('#login-password');
const loginCodeWrapper = $('#login-code-wrapper');

const loginPwdInput = $('#login-password');
const loginPwdToggle = $('#login-pwd-toggle');

const regPwdInput = $('#reg-password');
const regPwdToggle = $('#reg-pwd-toggle');
const regConfirmPwdInput = $('#reg-confirm-password');
const regConfirmPwdToggle = $('#reg-confirm-pwd-toggle');

// Initialize Event Listeners
document.addEventListener('DOMContentLoaded', () => {
  setupViewNavigation();
  setupLoginTabs();
  setupPasswordVisibilityToggles();
  setupVerificationCodeTimers();
  setupCodeInputsLengthRestrictions();
  setupFormSubmissions();
  setupCanteenMenu();
});

// --- Toast Alerts ---
function showToast(message, iconName = 'info') {
  const toast = $('#app-toast');
  const icon = $('#toast-icon');
  const msg = $('#toast-msg');
  
  icon.textContent = iconName;
  msg.textContent = message;
  
  toast.classList.add('show');
  
  setTimeout(() => {
    toast.classList.remove('show');
  }, 3500);
}

// --- View Swapping Navigation ---
function setupViewNavigation() {
  // Login to Register
  $('#go-to-register').addEventListener('click', (e) => {
    e.preventDefault();
    loginView.classList.remove('state-center');
    loginView.classList.add('state-left');
    registerView.classList.remove('state-right');
    registerView.classList.add('state-center');
  });

  // Register to Login
  $('#go-to-login').addEventListener('click', () => {
    registerView.classList.remove('state-center');
    registerView.classList.add('state-right');
    loginView.classList.remove('state-left');
    loginView.classList.add('state-center');
  });

  $('#reg-go-to-login').addEventListener('click', (e) => {
    e.preventDefault();
    registerView.classList.remove('state-center');
    registerView.classList.add('state-right');
    loginView.classList.remove('state-left');
    loginView.classList.add('state-center');
  });
}

// --- Password/Code Tab Selector (Login View) ---
function setupLoginTabs() {
  tabPassword.addEventListener('click', () => {
    tabPassword.classList.add('active');
    tabCode.classList.remove('active');
    
    loginPwdWrapper.classList.remove('hidden');
    loginCodeWrapper.classList.add('hidden');
    
    // Adjust input requirements
    $('#login-password').setAttribute('required', '');
    $('#login-code').removeAttribute('required');
  });

  tabCode.addEventListener('click', () => {
    tabCode.classList.add('active');
    tabPassword.classList.remove('active');
    
    loginCodeWrapper.classList.remove('hidden');
    loginPwdWrapper.classList.add('hidden');
    
    // Adjust input requirements
    $('#login-code').setAttribute('required', '');
    $('#login-password').removeAttribute('required');
  });
}

// --- Show/Hide Password Eye Toggle ---
function setupPasswordVisibilityToggles() {
  // Login Password Field
  loginPwdToggle.addEventListener('click', () => {
    const isPwd = loginPwdInput.type === 'password';
    loginPwdInput.type = isPwd ? 'text' : 'password';
    loginPwdToggle.textContent = isPwd ? 'visibility_off' : 'visibility';
  });

  // Registration Password Field
  regPwdToggle.addEventListener('click', () => {
    const isPwd = regPwdInput.type === 'password';
    regPwdInput.type = isPwd ? 'text' : 'password';
    regPwdToggle.textContent = isPwd ? 'visibility_off' : 'visibility';
  });

  // Registration Confirm Password Field
  regConfirmPwdToggle.addEventListener('click', () => {
    const isPwd = regConfirmPwdInput.type === 'password';
    regConfirmPwdInput.type = isPwd ? 'text' : 'password';
    regConfirmPwdToggle.textContent = isPwd ? 'visibility_off' : 'visibility';
  });
}

// --- Verification Code Timer Simulator ---
function setupVerificationCodeTimers() {
  const triggerTimer = (btnId) => {
    const btn = $(btnId);
    let secondsLeft = 60;
    
    btn.disabled = true;
    btn.textContent = `${secondsLeft}s`;
    
    // Generate a mock code for high-fidelity interactive testing
    const mockCode = Math.floor(100000 + Math.random() * 900000);
    setTimeout(() => {
      showToast(`Verification code sent! Code: ${mockCode}`, 'sms');
    }, 800);

    const countdown = setInterval(() => {
      secondsLeft--;
      if (secondsLeft <= 0) {
        clearInterval(countdown);
        btn.disabled = false;
        btn.textContent = 'Send Code';
      } else {
        btn.textContent = `${secondsLeft}s`;
      }
    }, 1000);
  };

  // Login send code
  $('#btn-login-send-code').addEventListener('click', () => {
    const phone = $('#login-account').value.trim();
    if (!phone) {
      showToast('Please enter your Account (Phone/ID) first!', 'warning');
      return;
    }
    triggerTimer('#btn-login-send-code');
  });

  // Register send code
  $('#btn-reg-send-code').addEventListener('click', () => {
    const phone = $('#reg-phone').value.trim();
    if (!phone) {
      showToast('Please enter your Phone Number first!', 'warning');
      return;
    }
    triggerTimer('#btn-reg-send-code');
  });
}

// --- Verification Code Inputs Sanitization & Length Restrictions ---
function setupCodeInputsLengthRestrictions() {
  const restrictToSixDigits = (id) => {
    const el = $(id);
    if (el) {
      el.addEventListener('input', (e) => {
        // Clean non-numeric characters and restrict length to 6 digits
        e.target.value = e.target.value.replace(/[^0-9]/g, '').slice(0, 6);
      });
    }
  };
  restrictToSixDigits('#login-code');
  restrictToSixDigits('#reg-code');
}

// --- Form Validation and Submission Handlers ---
function setupFormSubmissions() {
  
  // LOGIN FORM
  $('#btn-login-submit').addEventListener('click', (e) => {
    e.preventDefault();
    
    const account = $('#login-account').value.trim();
    const isPasswordTab = tabPassword.classList.contains('active');
    
    if (!account) {
      showToast('Please enter your Account details!', 'warning');
      return;
    }

    if (isPasswordTab) {
      const password = $('#login-password').value;
      if (!password) {
        showToast('Please enter your password!', 'warning');
        return;
      }
      
      // Mock validation
      showToast('Sign in successful! Welcome to Campus Eats.', 'check_circle');
      transitionToMenu();
    } else {
      const code = $('#login-code').value.trim();
      if (!code) {
        showToast('Please enter the verification code!', 'warning');
        return;
      }
      if (!/^\d{6}$/.test(code)) {
        showToast('Verification code must be 6 digits!', 'warning');
        return;
      }
      
      showToast('SMS Code Login successful!', 'check_circle');
      transitionToMenu();
    }
  });

  // REGISTER FORM
  $('#btn-register-submit').addEventListener('click', (e) => {
    e.preventDefault();
    
    const phone = $('#reg-phone').value.trim();
    const code = $('#reg-code').value.trim();
    const studentId = $('#reg-student-id').value.trim();
    const nickname = $('#reg-nickname').value.trim();
    const password = $('#reg-password').value;
    const confirmPassword = $('#reg-confirm-password').value;

    if (!phone || !code || !password || !confirmPassword) {
      showToast('Please fill out all required fields!', 'warning');
      return;
    }

    if (!/^\d{6}$/.test(code)) {
      showToast('Please enter a valid 6-digit verification code!', 'warning');
      return;
    }

    if (password.length < 8) {
      showToast('Password must be at least 8 characters long!', 'warning');
      return;
    }

    if (password !== confirmPassword) {
      showToast('Passwords do not match!', 'warning');
      return;
    }

    // Success Mock Registration
    showToast('Registration successful! Redirecting to login...', 'check_circle');
    
    setTimeout(() => {
      // Prefill login account
      $('#login-account').value = phone;
      
      // Clean register inputs
      $('#reg-phone').value = '';
      $('#reg-code').value = '';
      $('#reg-student-id').value = '';
      $('#reg-nickname').value = '';
      $('#reg-password').value = '';
      $('#reg-confirm-password').value = '';
      
      // Reset password visibility fields to 'password' type
      regPwdInput.type = 'password';
      regPwdToggle.textContent = 'visibility';
      regConfirmPwdInput.type = 'password';
      regConfirmPwdToggle.textContent = 'visibility';
      
      // Switch view
      registerView.classList.remove('state-center');
      registerView.classList.add('state-right');
      loginView.classList.remove('state-left');
      loginView.classList.add('state-center');
    }, 1500);
  });
}

// --- Menu Transition Helper ---
function transitionToMenu() {
  setTimeout(() => {
    loginView.classList.remove('state-center');
    loginView.classList.add('state-left');
    
    menuView.classList.remove('state-right');
    menuView.classList.add('state-center');
    
    // Clear password/code inputs
    $('#login-password').value = '';
    $('#login-code').value = '';
  }, 1000);
}

// --- Canteen Menu Setup and Logic ---
// --- Canteen Menu Setup and Logic ---
let cart = {};
let orders = [
  {
    id: 'A005',
    stallName: 'The Pizza Hub',
    items: [
      { name: 'Margherita Large', price: 16.50, quantity: 1, imgUrl: '/images/pizza_stall_icon.png', options: 'Regular thick crust' },
      { name: 'Garlic Bread', price: 4.00, quantity: 2, imgUrl: '/images/pork_gyoza.png', options: 'With cheese' }
    ],
    totalPrice: 24.50,
    remarks: '',
    status: 'ready',
    timestamps: {
      placed: 'Oct 24, 12:30 PM',
      preparing: 'Oct 24, 12:40 PM',
      ready: 'Oct 24, 12:45 PM',
      completed: ''
    }
  },
  {
    id: 'A006',
    stallName: 'Green Garden Bowls',
    items: [
      { name: 'Quinoa Power Bowl', price: 7.60, quantity: 2, imgUrl: '/images/salad_stall_icon.png', options: 'Olive oil dressing' },
      { name: 'Iced Lemon Tea', price: 3.00, quantity: 1, imgUrl: '/images/plum_soup.png', options: 'Less sweet' }
    ],
    totalPrice: 18.20,
    remarks: '',
    status: 'preparing',
    timestamps: {
      placed: 'Oct 24, 01:15 PM',
      preparing: 'Oct 24, 01:20 PM',
      ready: '',
      completed: ''
    }
  },
  {
    id: 'A003',
    stallName: 'Burger Palace',
    items: [
      { name: 'Double Beef Combo', price: 12.90, quantity: 1, imgUrl: '/images/burger_stall_icon.png', options: 'Regular fries, Diet Coke' }
    ],
    totalPrice: 12.90,
    remarks: '',
    status: 'completed',
    timestamps: {
      placed: 'Oct 22, 07:15 PM',
      preparing: 'Oct 22, 07:20 PM',
      ready: 'Oct 22, 07:28 PM',
      completed: 'Oct 22, 07:30 PM'
    }
  }
];

const STALL_METADATA = {
  'noodle-house': {
    heroImg: '/images/noodle_house_hero.png',
    cuisine: 'Asian Fusion',
    hours: 'Open until 9:00 PM',
    categories: [
      { id: 'main', label: 'Main Dishes' },
      { id: 'sides', label: 'Sides' },
      { id: 'drinks', label: 'Drinks' },
      { id: 'combos', label: 'Combos' }
    ]
  },
  'western-grill': {
    heroImg: '/images/western_grill_hero.png',
    cuisine: 'Gourmet Burgers',
    hours: 'Open until 10:00 PM',
    categories: [
      { id: 'main', label: 'Main Dishes' },
      { id: 'sides', label: 'Sides' },
      { id: 'drinks', label: 'Drinks' }
    ]
  },
  'salad-drinks': {
    heroImg: '/images/healthy_bowls_hero.png',
    cuisine: 'Salads & Juice',
    hours: 'Open until 8:30 PM',
    categories: [
      { id: 'main', label: 'Healthy Bowls' },
      { id: 'drinks', label: 'Drinks' }
    ]
  }
};

function setupCanteenMenu() {
  const dishesView = $('#dishes-view');
  const menuView = $('#menu-view');
  const stallSearch = $('#stall-search');
  const chips = $$('.category-chips-row .chip');
  const stallCards = $$('.stall-card');
  const foodCards = $$('.food-card');
  const navTabs = $$('.bottom-nav .nav-tab');
  const dishesBack = $('#dishes-back');

  // Confirm Order View elements
  const confirmOrderView = $('#confirm-order-view');
  const confirmOrderBack = $('#confirm-order-back');
  const confirmOrderPickupStall = $('#confirm-order-pickup-stall');
  const confirmOrderItemsList = $('#confirm-order-items-list');
  const confirmOrderTotalPrice = $('#confirm-order-total-price');
  const confirmOrderRemarksInput = $('#confirm-order-remarks-input');
  const confirmOrderSubmitBtn = $('#confirm-order-submit-btn');
  let checkoutPreviousView = null;

  // Payment View elements
  const paymentView = $('#payment-view');
  const paymentClose = $('#payment-close');
  const paymentTotalAmount = $('#payment-total-amount');
  const paymentBillItems = $('#payment-bill-items');
  const paymentDotsContainer = $('#payment-dots-container');
  const paymentConfirmBtn = $('#payment-confirm-btn');
  const paymentCancelBtn = $('#payment-cancel-btn');
  let paymentPassword = [];

  // Orders View & Order Details View elements
  const ordersView = $('#orders-view');
  const orderDetailView = $('#order-detail-view');
  const ordersListContainer = $('#orders-list-container');
  const orderDetailBack = $('#order-detail-back');

  // Profile View elements
  const profileView = $('#profile-view');
  const btnEditProfile = $('#btn-edit-profile');
  const btnSettingsOpt = $('#btn-settings-opt');
  const btnHelpOpt = $('#btn-help-opt');
  const btnLogoutOpt = $('#btn-logout-opt');
  const logoutDialog = $('#logout-dialog');
  const logoutDialogScrim = $('#logout-dialog-scrim');
  const btnLogoutCancel = $('#btn-logout-cancel');
  const btnLogoutConfirm = $('#btn-logout-confirm');

  // Edit Profile View elements
  const editProfileView = $('#edit-profile-view');
  const editProfileBack = $('#edit-profile-back');
  const btnChangeAvatar = $('#btn-change-avatar');
  const editAvatarPreview = $('#edit-avatar-preview');
  const editAvatarInput = $('#edit-avatar-input');
  const editNickname = $('#edit-nickname');
  const editStudentID = $('#edit-student-id');
  const editPhone = $('#edit-phone');
  const editVerificationRow = $('#edit-verification-row');
  const editVerificationCode = $('#edit-verification-code');
  const btnEditSendCode = $('#btn-edit-send-code');
  const btnSaveProfile = $('#btn-save-profile');

  // Avatar Picker elements
  const avatarPickerDialog = $('#avatar-picker-dialog');
  const avatarPickerScrim = $('#avatar-picker-scrim');
  const btnAvatarPickerCancel = $('#btn-avatar-picker-cancel');
  const avatarPresetItems = $$('.avatar-preset-item');

  let originalPhone = '';
  let selectedAvatarSrc = '';
  let mockEditVerificationCode = '';

  if (btnEditProfile) {
    btnEditProfile.addEventListener('click', () => {
      // Get current values
      const currentName = $('#profile-display-name') ? $('#profile-display-name').textContent.trim() : '';
      const currentPhone = $('#profile-display-phone') ? $('#profile-display-phone').textContent.trim() : '';
      
      // Parse ID: STU-882910 -> STU-882910
      const idText = $('#profile-display-student-id') ? $('#profile-display-student-id').textContent.trim() : '';
      const currentID = idText.replace(/^ID:\s*/i, '');
      
      const currentAvatar = $('#profile-avatar-img') ? $('#profile-avatar-img').src : '';

      // Populate input fields
      if (editNickname) editNickname.value = currentName;
      if (editStudentID) editStudentID.value = currentID;
      if (editPhone) editPhone.value = currentPhone;
      if (editAvatarPreview) editAvatarPreview.src = currentAvatar;

      originalPhone = currentPhone;
      selectedAvatarSrc = currentAvatar;
      mockEditVerificationCode = '';

      // Hide verification row by default
      if (editVerificationRow) {
        editVerificationRow.classList.add('hidden');
        editVerificationRow.style.display = 'none';
      }
      if (editVerificationCode) editVerificationCode.value = '';

      // Update avatar preset highlight
      avatarPresetItems.forEach(item => {
        const img = item.querySelector('img');
        if (img && img.src === currentAvatar) {
          item.classList.add('active');
          item.style.borderColor = 'var(--color-brand-rust)';
        } else {
          item.classList.remove('active');
          item.style.borderColor = 'transparent';
        }
      });

      // Slide views
      if (profileView) {
        profileView.classList.remove('state-center');
        profileView.classList.add('state-left');
      }
      if (editProfileView) {
        editProfileView.classList.remove('state-right');
        editProfileView.classList.add('state-center');
      }
    });
  }

  if (editPhone) {
    editPhone.addEventListener('input', () => {
      const inputVal = editPhone.value.trim();
      if (inputVal !== originalPhone) {
        if (editVerificationRow) {
          editVerificationRow.classList.remove('hidden');
          editVerificationRow.style.display = 'flex';
        }
      } else {
        if (editVerificationRow) {
          editVerificationRow.classList.add('hidden');
          editVerificationRow.style.display = 'none';
        }
      }
    });
  }

  // Restrict edit verification code to 6 digits
  if (editVerificationCode) {
    editVerificationCode.addEventListener('input', (e) => {
      e.target.value = e.target.value.replace(/[^0-9]/g, '').slice(0, 6);
    });
  }

  // Send Code Handler for Edit Profile
  if (btnEditSendCode) {
    btnEditSendCode.addEventListener('click', () => {
      const phone = editPhone ? editPhone.value.trim() : '';
      if (!phone) {
        showToast('Please enter your Phone Number first!', 'warning');
        return;
      }
      
      let secondsLeft = 60;
      btnEditSendCode.disabled = true;
      btnEditSendCode.textContent = `${secondsLeft}s`;
      
      // Generate code
      mockEditVerificationCode = String(Math.floor(100000 + Math.random() * 900000));
      setTimeout(() => {
        showToast(`Verification code sent! Code: ${mockEditVerificationCode}`, 'sms');
      }, 800);

      const countdown = setInterval(() => {
        secondsLeft--;
        if (secondsLeft <= 0) {
          clearInterval(countdown);
          btnEditSendCode.disabled = false;
          btnEditSendCode.textContent = 'Send Code';
        } else {
          btnEditSendCode.textContent = `${secondsLeft}s`;
        }
      }, 1000);
    });
  }

  // Avatar Selection Handlers (triggering native browser file upload)
  if (btnChangeAvatar && editAvatarInput) {
    btnChangeAvatar.addEventListener('click', () => {
      editAvatarInput.click();
    });
  }

  if (editAvatarInput) {
    editAvatarInput.addEventListener('change', (e) => {
      const file = e.target.files[0];
      if (file) {
        if (!file.type.startsWith('image/')) {
          showToast('Please select an image file!', 'warning');
          return;
        }
        
        const reader = new FileReader();
        reader.onload = (event) => {
          selectedAvatarSrc = event.target.result;
          if (editAvatarPreview) {
            editAvatarPreview.src = selectedAvatarSrc;
          }
        };
        reader.readAsDataURL(file);
      }
    });
  }

  // Back button handler
  if (editProfileBack) {
    editProfileBack.addEventListener('click', () => {
      if (editProfileView) {
        editProfileView.classList.remove('state-center');
        editProfileView.classList.add('state-right');
      }
      if (profileView) {
        profileView.classList.remove('state-left');
        profileView.classList.add('state-center');
      }
    });
  }

  // Save changes handler
  if (btnSaveProfile) {
    btnSaveProfile.addEventListener('click', () => {
      const nicknameVal = editNickname ? editNickname.value.trim() : '';
      const phoneVal = editPhone ? editPhone.value.trim() : '';
      const studentIDVal = editStudentID ? editStudentID.value.trim() : '';

      if (!nicknameVal) {
        showToast('Nickname cannot be empty!', 'warning');
        return;
      }

      if (!phoneVal) {
        showToast('Phone number cannot be empty!', 'warning');
        return;
      }

      // If phone changed, check verification code
      if (phoneVal !== originalPhone) {
        const enteredCode = editVerificationCode ? editVerificationCode.value.trim() : '';
        if (!enteredCode) {
          showToast('Please enter the verification code!', 'warning');
          return;
        }
        if (enteredCode !== mockEditVerificationCode) {
          showToast('Invalid verification code!', 'error');
          return;
        }
      }

      // Update values in display
      const displayNameEl = $('#profile-display-name');
      const displayPhoneEl = $('#profile-display-phone');
      const displayIDEl = $('#profile-display-student-id');
      const avatarImgEl = $('#profile-avatar-img');

      if (displayNameEl) displayNameEl.textContent = nicknameVal;
      if (displayPhoneEl) displayPhoneEl.textContent = phoneVal;
      if (displayIDEl) displayIDEl.textContent = studentIDVal ? `ID: ${studentIDVal}` : '';
      if (avatarImgEl && selectedAvatarSrc) avatarImgEl.src = selectedAvatarSrc;

      // Update Promo greeting if greeting exists (e.g. Hungry, Alex?)
      const promoGreetingEl = $('#promo-greeting');
      if (promoGreetingEl) {
        promoGreetingEl.textContent = `Hungry, ${nicknameVal}?`;
      }

      showToast('Profile updated successfully!', 'check_circle');

      // Go back
      if (editProfileView) {
        editProfileView.classList.remove('state-center');
        editProfileView.classList.add('state-right');
      }
      if (profileView) {
        profileView.classList.remove('state-left');
        profileView.classList.add('state-center');
      }
    });
  }
  if (btnSettingsOpt) {
    btnSettingsOpt.addEventListener('click', () => {
      showToast('Settings features coming soon!', 'settings');
    });
  }
  if (btnHelpOpt) {
    btnHelpOpt.addEventListener('click', () => {
      showToast('Help & Support features coming soon!', 'help');
    });
  }
  if (btnLogoutOpt) {
    btnLogoutOpt.addEventListener('click', () => {
      if (logoutDialog && logoutDialogScrim) {
        logoutDialog.classList.add('show');
        logoutDialogScrim.classList.add('show');
      }
    });
  }
  if (btnLogoutCancel) {
    btnLogoutCancel.addEventListener('click', () => {
      if (logoutDialog && logoutDialogScrim) {
        logoutDialog.classList.remove('show');
        logoutDialogScrim.classList.remove('show');
      }
    });
  }
  if (logoutDialogScrim) {
    logoutDialogScrim.addEventListener('click', () => {
      if (logoutDialog && logoutDialogScrim) {
        logoutDialog.classList.remove('show');
        logoutDialogScrim.classList.remove('show');
      }
    });
  }
  if (btnLogoutConfirm) {
    btnLogoutConfirm.addEventListener('click', () => {
      if (logoutDialog && logoutDialogScrim) {
        logoutDialog.classList.remove('show');
        logoutDialogScrim.classList.remove('show');
      }
      handleLogout();
    });
  }
  
  let activeOrderFilter = 'all';
  const orderFilterTabs = $$('.orders-filter-tab');
  orderFilterTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      orderFilterTabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      activeOrderFilter = tab.getAttribute('data-filter');
      renderOrdersList();
    });
  });
  
  // Stepper timeline
  const orderTimelineLineFill = $('#order-timeline-line-fill');
  const stepPlaced = $('#step-placed');
  const stepPreparing = $('#step-preparing');
  const stepReady = $('#step-ready');
  const stepCompleted = $('#step-completed');
  const timePlaced = $('#time-placed');
  const timePreparing = $('#time-preparing');
  const timeReady = $('#time-ready');
  const timeCompleted = $('#time-completed');
  
  // Detail card
  const orderStatusPillVal = $('#order-status-pill-val');
  const orderDetailStatusCard = $('#order-detail-status-card');
  const orderDetailItemsList = $('#order-detail-items-list');
  const orderDetailTotalPrice = $('#order-detail-total-price');
  const orderDetailRemarksVal = $('#order-detail-remarks-val');
  const orderDetailRemarksWrapper = $('#order-detail-remarks-wrapper');
  const btnOrderHelp = $('#btn-order-help');
  const btnOrderCancel = $('#btn-order-cancel');

  let activeOrderDetailOrder = null;
  let activeOrderDetailStatus = 'placed';
  let orderAdvanceTimeout1 = null;
  let orderAdvanceTimeout2 = null;

  // --- Category Chip Filter (Stalls page) ---
  chips.forEach(chip => {
    chip.addEventListener('click', () => {
      // Toggle active chip visually
      const activeChip = $('.category-chips-row .chip.active');
      if (activeChip) {
        activeChip.classList.remove('active');
      }
      chip.classList.add('active');

      const selectedCategory = chip.getAttribute('data-category');
      filterStalls(selectedCategory, stallSearch ? stallSearch.value : '');
    });
  });

  // --- Search Filter ---
  if (stallSearch) {
    stallSearch.addEventListener('input', () => {
      const activeChip = $('.category-chips-row .chip.active');
      const selectedCategory = activeChip ? activeChip.getAttribute('data-category') : 'all';
      filterStalls(selectedCategory, stallSearch.value);
    });
  }

  // Filter stalls logic helper
  function filterStalls(category, searchQuery) {
    const query = searchQuery.toLowerCase().trim();

    stallCards.forEach(card => {
      const cardCategory = card.getAttribute('data-category');
      const title = card.querySelector('.stall-title').textContent.toLowerCase();
      const cuisine = card.querySelector('.stall-cuisine').textContent.toLowerCase();

      // Check category match
      const matchesCategory = (category === 'all' || cardCategory === category);
      
      // Check search query match
      const matchesSearch = (!query || title.includes(query) || cuisine.includes(query));

      if (matchesCategory && matchesSearch) {
        card.classList.remove('hidden');
      } else {
        card.classList.add('hidden');
      }
    });
  }

  // --- Navigation to Dishes Detail View ---
  stallCards.forEach(card => {
    card.addEventListener('click', () => {
      const stallId = card.getAttribute('data-stall');
      const stallTitle = card.querySelector('.stall-title').textContent;

      // Update dishes view title
      const dishesTitleEl = $('#dishes-stall-title');
      if (dishesTitleEl) {
        dishesTitleEl.textContent = stallTitle;
      }

      // Load metadata
      const metadata = STALL_METADATA[stallId];
      if (metadata) {
        const heroImgEl = $('#dishes-hero-img');
        const cuisineTagEl = $('#dishes-cuisine-tag');
        const hoursLabelEl = $('#dishes-hours-label');
        
        if (heroImgEl) heroImgEl.src = metadata.heroImg;
        if (cuisineTagEl) cuisineTagEl.textContent = metadata.cuisine;
        if (hoursLabelEl) hoursLabelEl.textContent = metadata.hours;

        // Recreate subcategory chips dynamically
        const categoryRow = $('#dish-category-row');
        if (categoryRow) {
          categoryRow.innerHTML = '';
          metadata.categories.forEach((cat, index) => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = `dish-chip${index === 0 ? ' active' : ''}`;
            btn.setAttribute('data-category', cat.id);
            btn.textContent = cat.label;
            categoryRow.appendChild(btn);

            // Category chip click handler
            btn.addEventListener('click', () => {
              categoryRow.querySelectorAll('.dish-chip').forEach(c => c.classList.remove('active'));
              btn.classList.add('active');
              filterDishes(stallId, cat.id);
            });
          });
        }
      }

      // Filter to first category by default
      const defaultCategory = metadata && metadata.categories[0] ? metadata.categories[0].id : 'main';
      filterDishes(stallId, defaultCategory);

      // Sync all food cards
      foodCards.forEach(fc => {
        const addBtn = fc.querySelector('.btn-add-cart-circle');
        if (addBtn) {
          const id = addBtn.getAttribute('data-id');
          syncFoodCardUI(id);
        }
      });

      // Transition screens
      if (menuView) {
        menuView.classList.remove('state-center');
        menuView.classList.add('state-left');
      }
      if (dishesView) {
        dishesView.classList.remove('state-right');
        dishesView.classList.add('state-center');
      }
    });
  });

  // Filter dishes by stall ID and category
  function filterDishes(stallId, categoryId) {
    foodCards.forEach(card => {
      const foodStalls = (card.getAttribute('data-stall') || '').split(' ');
      const cardCategory = card.getAttribute('data-category');

      if (foodStalls.includes(stallId) && cardCategory === categoryId) {
        card.classList.remove('hidden');
      } else {
        card.classList.add('hidden');
      }
    });
  }

  // --- Navigation back to Stalls List ---
  if (dishesBack) {
    dishesBack.addEventListener('click', () => {
      if (dishesView) {
        dishesView.classList.remove('state-center');
        dishesView.classList.add('state-right');
      }
      if (menuView) {
        menuView.classList.remove('state-left');
        menuView.classList.add('state-center');
      }
    });
  }

  // --- Animation Helper for Quantity Bounce/Squeeze ---
  function triggerQtyAnimation(buttonEl, numBoxEl) {
    if (!buttonEl || !numBoxEl) return;
    
    buttonEl.classList.remove('qty-btn-anim');
    numBoxEl.classList.remove('qty-num-anim');
    
    void buttonEl.offsetWidth;
    void numBoxEl.offsetWidth;
    
    buttonEl.classList.add('qty-btn-anim');
    numBoxEl.classList.add('qty-num-anim');
    
    const onEnd = () => {
      buttonEl.classList.remove('qty-btn-anim');
      numBoxEl.classList.remove('qty-num-anim');
      buttonEl.removeEventListener('animationend', onEnd);
    };
    buttonEl.addEventListener('animationend', onEnd);
  }

  // --- UI Synchronizer for Food Quantity Controls ---
  function syncFoodCardUI(id) {
    const qty = cart[id] ? cart[id].quantity : 0;
    
    const addButtons = $$(`.btn-add-cart-circle[data-id="${id}"]`);
    const qtyControls = $$(`.quantity-control[data-id="${id}"]`);
    
    addButtons.forEach(btn => {
      if (qty > 0) {
        btn.classList.add('hidden');
      } else {
        btn.classList.remove('hidden');
      }
    });
    
    qtyControls.forEach(ctrl => {
      if (qty > 0) {
        ctrl.classList.remove('hidden');
        const numBox = ctrl.querySelector('.qty-number-box');
        if (numBox) {
          numBox.textContent = qty;
        }
      } else {
        ctrl.classList.add('hidden');
      }
    });
  }

  // --- Reset All Food Cards UI Helper ---
  function resetAllFoodCardUI() {
    const addButtons = $$(`.btn-add-cart-circle`);
    const qtyControls = $$(`.quantity-control`);
    
    addButtons.forEach(btn => {
      btn.classList.remove('hidden');
    });
    
    qtyControls.forEach(ctrl => {
      ctrl.classList.add('hidden');
      const numBox = ctrl.querySelector('.qty-number-box');
      if (numBox) {
        numBox.textContent = '0';
      }
    });
  }

  // --- Add To Cart & Quantity Selection logic via delegation ---
  document.addEventListener('click', (e) => {
    // 1. Initial "Add to cart" circular button
    const addBtn = e.target.closest('.btn-add-cart-circle');
    if (addBtn) {
      e.stopPropagation();
      const id = addBtn.getAttribute('data-id');
      const price = parseFloat(addBtn.getAttribute('data-price'));
      const name = addBtn.getAttribute('data-name');

      if (!cart[id]) {
        // Find image and other properties from the food card in DOM
        const cardEl = addBtn.closest('.food-card');
        const imgEl = cardEl ? cardEl.querySelector('.food-img') : null;
        const imgUrl = imgEl ? imgEl.getAttribute('src') : '';

        // Define default mock options for realism matching mockup
        const optionsMap = {
          'tonkotsu_ramen': 'Regular portion, Extra spice',
          'spicy_dan_dan': 'Regular portion, Medium spice',
          'beef_chow_mein': 'Regular portion, Less oil',
          'pork_gyoza': 'With soy sauce',
          'chicken_meal': 'Brown rice, Pepper sauce',
          'beef_burger': 'Medium rare, Cheddar cheese',
          'french_fries': 'With ketchup',
          'tofu_salad': 'Olive oil dressing',
          'avocado_salad': 'Ranch dressing',
          'plum_soup': 'Less ice, 50% sugar'
        };
        const options = optionsMap[id] || 'Regular portion';

        cart[id] = { price, name, imgUrl, options, quantity: 0 };
      }
      cart[id].quantity = 1;

      syncFoodCardUI(id);
      updateCartFloatingBar();
      showToast(`Added ${name} to cart!`, 'shopping_cart');
      return;
    }

    // 2. Quantity Plus button
    const plusBtn = e.target.closest('.btn-qty-plus');
    if (plusBtn) {
      e.stopPropagation();
      const id = plusBtn.getAttribute('data-id');
      if (!cart[id]) return;

      cart[id].quantity += 1;

      const qtyCtrl = plusBtn.closest('.quantity-control');
      const numBox = qtyCtrl ? qtyCtrl.querySelector('.qty-number-box') : null;
      triggerQtyAnimation(plusBtn, numBox);

      syncFoodCardUI(id);
      updateCartFloatingBar();
      return;
    }

    // 3. Quantity Minus button
    const minusBtn = e.target.closest('.btn-qty-minus');
    if (minusBtn) {
      e.stopPropagation();
      const id = minusBtn.getAttribute('data-id');
      if (!cart[id] || cart[id].quantity <= 0) return;

      cart[id].quantity -= 1;

      const qtyCtrl = minusBtn.closest('.quantity-control');
      const numBox = qtyCtrl ? qtyCtrl.querySelector('.qty-number-box') : null;

      if (cart[id].quantity === 0) {
        delete cart[id];
        syncFoodCardUI(id);
      } else {
        triggerQtyAnimation(minusBtn, numBox);
        syncFoodCardUI(id);
      }

      updateCartFloatingBar();
      return;
    }
  });

  // --- Cart Floating Bar & Bottom Sheet Event Bindings ---
  const cartBar = $('#cart-floating-bar');
  const cartSheet = $('#cart-sheet');
  const cartSheetScrim = $('#cart-sheet-scrim');
  const cartSheetCloseBtn = $('#cart-sheet-close-btn');
  const cartSheetSubtitle = $('#cart-sheet-subtitle');

  function openCartSheet() {
    if (!cartSheet || !cartSheetScrim) return;
    
    // Update subtitle to current active stall title
    const dishesTitleEl = $('#dishes-stall-title');
    if (cartSheetSubtitle && dishesTitleEl) {
      cartSheetSubtitle.textContent = dishesTitleEl.textContent || 'Noodle House';
    }

    renderCartSheet();
    
    cartSheet.classList.remove('hidden');
    cartSheetScrim.classList.remove('hidden');
    // Force DOM reflow to trigger transition
    void cartSheet.offsetWidth;
    cartSheet.classList.add('show');
    cartSheetScrim.classList.add('show');
  }

  function closeCartSheet() {
    if (!cartSheet || !cartSheetScrim) return;
    cartSheet.classList.remove('show');
    cartSheetScrim.classList.remove('show');
    
    // Hide after slide animation finishes
    setTimeout(() => {
      if (!cartSheet.classList.contains('show')) {
        cartSheet.classList.add('hidden');
        cartSheetScrim.classList.add('hidden');
      }
    }, 350);
  }

  if (cartBar) {
    cartBar.addEventListener('click', () => {
      openCartSheet();
    });
  }

  if (cartSheetScrim) {
    cartSheetScrim.addEventListener('click', () => {
      closeCartSheet();
    });
  }

  if (cartSheetCloseBtn) {
    cartSheetCloseBtn.addEventListener('click', () => {
      closeCartSheet();
    });
  }

  const btnCheckout = $('#btn-checkout');
  if (btnCheckout) {
    btnCheckout.addEventListener('click', () => {
      // 1. Determine previous view
      if (dishDetailView && dishDetailView.classList.contains('state-center')) {
        checkoutPreviousView = dishDetailView;
      } else if (dishesView && dishesView.classList.contains('state-center')) {
        checkoutPreviousView = dishesView;
      } else {
        checkoutPreviousView = menuView;
      }

      // 2. Populate checkout list dynamically
      if (confirmOrderItemsList) {
        confirmOrderItemsList.innerHTML = '';
        let totalVal = 0;
        for (const id in cart) {
          const item = cart[id];
          totalVal += item.price * item.quantity;
          
          const itemEl = document.createElement('div');
          itemEl.className = 'confirm-order-item';
          itemEl.innerHTML = `
            <img class="confirm-order-item-img" src="${item.imgUrl || '/images/default.png'}" alt="${item.name}" />
            <div class="confirm-order-item-info">
              <span class="confirm-order-item-name">${item.name}</span>
              <span class="confirm-order-item-qty">Qty: ${item.quantity}</span>
            </div>
            <span class="confirm-order-item-price">$${(item.price * item.quantity).toFixed(2)}</span>
          `;
          confirmOrderItemsList.appendChild(itemEl);
        }
        
        if (confirmOrderTotalPrice) {
          confirmOrderTotalPrice.textContent = `$${totalVal.toFixed(2)}`;
        }
      }

      // 3. Update active pickup stall name
      const activeStall = ($('#dishes-stall-title')?.textContent || 'Noodle House');
      if (confirmOrderPickupStall) {
        confirmOrderPickupStall.textContent = `Pickup Stall: ${activeStall}`;
      }

      // 4. Close cart sheet
      closeCartSheet();

      // 5. Slide out previous view
      if (checkoutPreviousView) {
        checkoutPreviousView.classList.remove('state-center');
        checkoutPreviousView.classList.add('state-left');
      }

      // 6. Slide in confirm-order-view
      if (confirmOrderView) {
        confirmOrderView.classList.remove('state-right');
        confirmOrderView.classList.add('state-center');
      }
    });
  }

  // --- Render bottom sheet cart content dynamically ---
  function renderCartSheet() {
    const listEl = $('#cart-sheet-items-list');
    const checkoutPriceEl = $('#checkout-total-price');
    if (!listEl) return;

    listEl.innerHTML = '';
    let totalPrice = 0;

    for (const id in cart) {
      const item = cart[id];
      totalPrice += item.price * item.quantity;

      const itemEl = document.createElement('div');
      itemEl.className = 'cart-sheet-item';
      itemEl.innerHTML = `
        <img class="cart-item-img" src="${item.imgUrl || '/images/default.png'}" alt="${item.name}" />
        <div class="cart-item-details">
          <span class="cart-item-name">${item.name}</span>
          <span class="cart-item-options">${item.options}</span>
        </div>
        <div class="cart-item-right">
          <span class="cart-item-price">$${(item.price * item.quantity).toFixed(2)}</span>
          <div class="cart-qty-control">
            <button type="button" class="cart-qty-btn cart-btn-minus" data-id="${id}">-</button>
            <span class="cart-qty-num">${item.quantity}</span>
            <button type="button" class="cart-qty-btn cart-btn-plus" data-id="${id}">+</button>
          </div>
        </div>
      `;
      listEl.appendChild(itemEl);
    }

    if (checkoutPriceEl) {
      checkoutPriceEl.textContent = `$${totalPrice.toFixed(2)}`;
    }

    // Bind quantity change events inside the sheet
    const plusButtons = listEl.querySelectorAll('.cart-btn-plus');
    const minusButtons = listEl.querySelectorAll('.cart-btn-minus');

    plusButtons.forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const id = btn.getAttribute('data-id');
        if (cart[id]) {
          cart[id].quantity += 1;
          syncFoodCardUI(id);
          updateCartFloatingBar();
          renderCartSheet();
        }
      });
    });

    minusButtons.forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const id = btn.getAttribute('data-id');
        if (cart[id]) {
          cart[id].quantity -= 1;
          if (cart[id].quantity === 0) {
            delete cart[id];
          }
          syncFoodCardUI(id);
          updateCartFloatingBar();
          if (Object.keys(cart).length === 0) {
            closeCartSheet();
          } else {
            renderCartSheet();
          }
        }
      });
    });
  }

  function updateCartFloatingBar() {
    const cartBarEl = $('#cart-floating-bar');
    const cartFabText = $('#cart-fab-text');
    const cartFabPrice = $('#cart-fab-price');

    let totalCount = 0;
    let totalPrice = 0;

    for (const id in cart) {
      totalCount += cart[id].quantity;
      totalPrice += cart[id].price * cart[id].quantity;
    }

    if (totalCount > 0) {
      if (cartBarEl) cartBarEl.classList.remove('hidden');
      if (cartFabText) cartFabText.textContent = `Cart (${totalCount})`;
      if (cartFabPrice) cartFabPrice.textContent = `$${totalPrice.toFixed(2)}`;
    } else {
      if (cartBarEl) cartBarEl.classList.add('hidden');
    }

    const checkoutPriceEl = $('#checkout-total-price');
    if (checkoutPriceEl) {
      checkoutPriceEl.textContent = `$${totalPrice.toFixed(2)}`;
    }
  }

  // --- Bottom Navigation Tab Switches ---
  function syncBottomNavActiveTab(tabName) {
    const allTabs = $$('.bottom-nav .nav-tab');
    allTabs.forEach(tab => {
      if (tab.getAttribute('data-tab') === tabName) {
        tab.classList.add('active');
      } else {
        tab.classList.remove('active');
      }
    });
  }

  navTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const tabName = tab.getAttribute('data-tab');

      if (tabName === 'profile') {
        syncBottomNavActiveTab('profile');
        
        // Hide other center views by sliding them to state-left
        [menuView, dishesView, dishDetailView, confirmOrderView, paymentView, ordersView, orderDetailView].forEach(v => {
          if (v) {
            v.classList.remove('state-center');
            v.classList.add('state-left');
          }
        });
        if (profileView) {
          profileView.classList.remove('state-right', 'state-left');
          profileView.classList.add('state-center');
        }
      } else if (tabName === 'orders') {
        syncBottomNavActiveTab('orders');
        renderOrdersList();
        
        // Hide menu-side views by sliding them to state-left
        [menuView, dishesView, dishDetailView, confirmOrderView, paymentView].forEach(v => {
          if (v) {
            v.classList.remove('state-center');
            v.classList.add('state-left');
          }
        });
        // Hide profile-side views by sliding them to state-right
        if (profileView) {
          profileView.classList.remove('state-center');
          profileView.classList.add('state-right');
        }
        // Sibling order details slides to left
        if (orderDetailView) {
          orderDetailView.classList.remove('state-center');
          orderDetailView.classList.add('state-left');
        }
        
        if (ordersView) {
          ordersView.classList.remove('state-right', 'state-left');
          ordersView.classList.add('state-center');
        }
      } else if (tabName === 'menu') {
        syncBottomNavActiveTab('menu');
        
        // Hide all other views by sliding them to state-right
        [dishesView, dishDetailView, confirmOrderView, paymentView, ordersView, orderDetailView, profileView].forEach(v => {
          if (v) {
            v.classList.remove('state-center');
            v.classList.add('state-right');
          }
        });
        if (menuView) {
          menuView.classList.remove('state-left', 'state-right');
          menuView.classList.add('state-center');
        }
      }
    });
  });

  function handleLogout() {
    showToast('Logged out successfully!', 'logout');
    closeCartSheet();
    
    // Transition back to login view
    [menuView, dishesView, dishDetailView, confirmOrderView, paymentView, ordersView, orderDetailView, profileView, editProfileView].forEach(v => {
      if (v) {
        v.classList.remove('state-center', 'state-left');
        v.classList.add('state-right');
      }
    });
    if (loginView) {
      loginView.classList.remove('state-left');
      loginView.classList.add('state-center');
    }

    // Reset states after animation completes
    setTimeout(() => {
      // Clear search
      if (stallSearch) {
        stallSearch.value = '';
      }
      // Reset chips on stalls page
      const activeChip = $('.category-chips-row .chip.active');
      if (activeChip) {
        activeChip.classList.remove('active');
      }
      if (chips[0]) {
        chips[0].classList.add('active');
      }
      // Reset all cards visibility
      stallCards.forEach(card => {
        card.classList.remove('hidden');
      });
      foodCards.forEach(card => {
        card.classList.remove('hidden');
      });
      
      // Reset cart
      cart = {};
      resetAllFoodCardUI();
      updateCartFloatingBar();
      
      // Reset bottom nav active tab across both views
      const activeTabs = $$('.bottom-nav .nav-tab');
      activeTabs.forEach(t => t.classList.remove('active'));
      const menuTabs = $$('.bottom-nav .nav-tab[data-tab="menu"]');
      menuTabs.forEach(t => t.classList.add('active'));
    }, 400);
  }

  // --- Dish Detail View Setup ---
  const dishDetailView = $('#dish-detail-view');
  const dishDetailBack = $('#dish-detail-back');
  const dishDetailHeaderStallName = $('#dish-detail-header-stall-name');
  const dishDetailImg = $('#dish-detail-img');
  const dishDetailCategory = $('#dish-detail-category');
  const dishDetailTitle = $('#dish-detail-title');
  const dishDetailStallName = $('#dish-detail-stall-name');
  const dishDetailPrice = $('#dish-detail-price');
  const dishDetailStock = $('#dish-detail-stock');
  const dishDetailProgressFill = $('#dish-detail-progress-fill');
  const dishDetailDesc = $('#dish-detail-desc');
  
  const dishDetailQtyControl = $('#dish-detail-qty-control');
  const dishDetailQtyMinus = $('#dish-detail-qty-minus');
  const dishDetailQtyNum = $('#dish-detail-qty-num');
  const dishDetailQtyPlus = $('#dish-detail-qty-plus');
  const dishDetailAddBtn = $('#dish-detail-add-btn');
  const dishDetailAddBtnText = $('#dish-detail-add-btn-text');

  let activeDetailDishId = null;
  let activeDetailPrice = 0;
  let activeDetailName = '';
  let activeDetailImgUrl = '';
  let activeDetailQty = 0;

  const DISH_STOCK_DATA = {
    'tonkotsu_ramen': { left: 15, total: 50 },
    'spicy_dan_dan': { left: 8, total: 30 },
    'beef_chow_mein': { left: 24, total: 40 },
    'pork_gyoza': { left: 45, total: 60 },
    'chicken_meal': { left: 12, total: 25 },
    'beef_burger': { left: 3, total: 15 },
    'french_fries': { left: 50, total: 50 },
    'tofu_salad': { left: 18, total: 20 },
    'avocado_salad': { left: 7, total: 15 },
    'plum_soup': { left: 85, total: 100 }
  };

  function openDishDetail(id, name, price, imgUrl, desc, categoryId, stallId) {
    activeDetailDishId = id;
    activeDetailPrice = price;
    activeDetailName = name;
    activeDetailImgUrl = imgUrl;
    
    // Get current quantity in cart
    const cartQty = cart[id] ? cart[id].quantity : 0;
    activeDetailQty = cartQty;
    
    // Update text and image
    if (dishDetailImg) dishDetailImg.src = imgUrl;
    if (dishDetailTitle) dishDetailTitle.textContent = name;
    if (dishDetailPrice) dishDetailPrice.textContent = `$${price.toFixed(2)}`;
    if (dishDetailDesc) dishDetailDesc.textContent = desc;
    
    // Category mapping
    const categoryLabels = {
      'main': 'Main Dish',
      'sides': 'Side Dish',
      'drinks': 'Drink',
      'combos': 'Combo Meal'
    };
    if (dishDetailCategory) {
      dishDetailCategory.textContent = categoryLabels[categoryId] || 'Special dish';
    }
    
    // Stall name
    const fullStallName = STALL_METADATA[stallId] ? $('#dishes-stall-title').textContent : 'Campus Eatery';
    if (dishDetailHeaderStallName) dishDetailHeaderStallName.textContent = fullStallName;
    if (dishDetailStallName) dishDetailStallName.textContent = fullStallName;
    
    // Stock and Progress Bar
    const stockInfo = DISH_STOCK_DATA[id] || { left: 10, total: 20 };
    if (dishDetailStock) dishDetailStock.textContent = `${stockInfo.left} left`;
    if (dishDetailProgressFill) {
      const percentage = (stockInfo.left / stockInfo.total) * 100;
      dishDetailProgressFill.style.width = `${percentage}%`;
    }
    
    // Bottom Bar
    updateDetailQtyUI();
    
    // Transition
    if (dishesView) {
      dishesView.classList.remove('state-center');
      dishesView.classList.add('state-left');
    }
    if (dishDetailView) {
      dishDetailView.classList.remove('state-right');
      dishDetailView.classList.add('state-center');
    }
  }

  function updateDetailQtyUI() {
    const id = activeDetailDishId;
    const qty = cart[id] ? cart[id].quantity : 0;
    activeDetailQty = qty;

    if (qty > 0) {
      if (dishDetailQtyNum) {
        dishDetailQtyNum.textContent = qty;
      }
      if (dishDetailQtyControl) {
        dishDetailQtyControl.classList.remove('hidden');
      }
      if (dishDetailAddBtn) {
        dishDetailAddBtn.classList.add('hidden');
      }
    } else {
      if (dishDetailQtyControl) {
        dishDetailQtyControl.classList.add('hidden');
      }
      if (dishDetailAddBtn) {
        dishDetailAddBtn.classList.remove('hidden');
      }
      if (dishDetailAddBtnText) {
        dishDetailAddBtnText.textContent = `Add to Order`;
      }
    }
  }

  function goBackFromDetail() {
    if (dishDetailView) {
      dishDetailView.classList.remove('state-center');
      dishDetailView.classList.add('state-right');
    }
    if (dishesView) {
      dishesView.classList.remove('state-left');
      dishesView.classList.add('state-center');
    }
  }

  if (dishDetailBack) {
    dishDetailBack.addEventListener('click', goBackFromDetail);
  }

  if (dishDetailQtyMinus) {
    dishDetailQtyMinus.addEventListener('click', () => {
      const id = activeDetailDishId;
      if (cart[id]) {
        cart[id].quantity--;
        if (cart[id].quantity === 0) {
          delete cart[id];
        } else {
          triggerQtyAnimation(dishDetailQtyMinus, dishDetailQtyNum);
        }
        syncFoodCardUI(id);
        updateCartFloatingBar();
        updateDetailQtyUI();
      }
    });
  }

  if (dishDetailQtyPlus) {
    dishDetailQtyPlus.addEventListener('click', () => {
      const id = activeDetailDishId;
      if (cart[id]) {
        cart[id].quantity++;
        triggerQtyAnimation(dishDetailQtyPlus, dishDetailQtyNum);
        syncFoodCardUI(id);
        updateCartFloatingBar();
        updateDetailQtyUI();
      }
    });
  }

  if (dishDetailAddBtn) {
    dishDetailAddBtn.addEventListener('click', () => {
      const id = activeDetailDishId;
      const price = activeDetailPrice;
      const name = activeDetailName;
      const imgUrl = activeDetailImgUrl;
      
      if (!cart[id]) {
        const optionsMap = {
          'tonkotsu_ramen': 'Regular portion, Extra spice',
          'spicy_dan_dan': 'Regular portion, Medium spice',
          'beef_chow_mein': 'Regular portion, Less oil',
          'pork_gyoza': 'With soy sauce',
          'chicken_meal': 'Brown rice, Pepper sauce',
          'beef_burger': 'Medium rare, Cheddar cheese',
          'french_fries': 'With ketchup',
          'tofu_salad': 'Olive oil dressing',
          'avocado_salad': 'Ranch dressing',
          'plum_soup': 'Less ice, 50% sugar'
        };
        const options = optionsMap[id] || 'Regular portion';
        cart[id] = { price, name, imgUrl, options, quantity: 1 };
      } else {
        cart[id].quantity = 1;
      }
      
      syncFoodCardUI(id);
      updateCartFloatingBar();
      showToast(`Added ${name} to cart!`, 'shopping_cart');
      
      updateDetailQtyUI();
    });
  }

  // --- Add Click Listeners to Food Cards (excl. buttons) ---
  foodCards.forEach(card => {
    card.addEventListener('click', (e) => {
      if (e.target.closest('.btn-add-cart-circle') || e.target.closest('.quantity-control')) {
        return;
      }
      
      const addBtn = card.querySelector('.btn-add-cart-circle');
      if (!addBtn) return;
      
      const id = addBtn.getAttribute('data-id');
      const name = card.querySelector('.food-title').textContent;
      const price = parseFloat(addBtn.getAttribute('data-price'));
      const imgEl = card.querySelector('.food-img');
      const imgUrl = imgEl ? imgEl.getAttribute('src') : '';
      const descEl = card.querySelector('.food-desc');
      const desc = descEl ? descEl.textContent : '';
      const category = card.getAttribute('data-category');
      const stallStalls = (card.getAttribute('data-stall') || '').split(' ');
      const stallId = stallStalls[0] || 'noodle-house';
      
      openDishDetail(id, name, price, imgUrl, desc, category, stallId);
    });
  });

  // --- Confirm Order Event Handlers ---
  if (confirmOrderBack) {
    confirmOrderBack.addEventListener('click', () => {
      if (confirmOrderView) {
        confirmOrderView.classList.remove('state-center');
        confirmOrderView.classList.add('state-right');
      }
      if (checkoutPreviousView) {
        checkoutPreviousView.classList.remove('state-left');
        checkoutPreviousView.classList.add('state-center');
      } else {
        if (menuView) {
          menuView.classList.remove('state-left');
          menuView.classList.add('state-center');
        }
      }
    });
  }

  if (confirmOrderSubmitBtn) {
    confirmOrderSubmitBtn.addEventListener('click', () => {
      // 1. Calculate and set Total Amount on Payment Screen
      let totalVal = 0;
      if (paymentBillItems) {
        paymentBillItems.innerHTML = '';
        for (const id in cart) {
          const item = cart[id];
          totalVal += item.price * item.quantity;
          
          const billItemEl = document.createElement('div');
          billItemEl.className = 'payment-bill-item';
          billItemEl.innerHTML = `
            <div class="payment-bill-item-left">
              <span class="payment-item-qty">${item.quantity}x</span>
              <span class="payment-item-name">${item.name}</span>
            </div>
            <span class="payment-item-price">$${(item.price * item.quantity).toFixed(2)}</span>
          `;
          paymentBillItems.appendChild(billItemEl);
        }
      }
      
      if (paymentTotalAmount) {
        paymentTotalAmount.textContent = `$${totalVal.toFixed(2)}`;
      }

      // 2. Reset Payment Password state
      paymentPassword = [];
      updatePasswordDots();
      if (paymentConfirmBtn) {
        paymentConfirmBtn.disabled = true;
      }

      // 3. Slide transition
      if (confirmOrderView) {
        confirmOrderView.classList.remove('state-center');
        confirmOrderView.classList.add('state-left');
      }
      if (paymentView) {
        paymentView.classList.remove('state-right');
        paymentView.classList.add('state-center');
      }
    });
  }

  const confirmOrderPaymentChange = $('.confirm-order-payment-change');
  if (confirmOrderPaymentChange) {
    confirmOrderPaymentChange.addEventListener('click', () => {
      showToast('Only Campus Card payment is supported currently!', 'info');
    });
  }

  // --- Payment Screen Event Handlers ---
  function goBackFromPayment() {
    if (paymentView) {
      paymentView.classList.remove('state-center');
      paymentView.classList.add('state-right');
    }
    if (confirmOrderView) {
      confirmOrderView.classList.remove('state-left');
      confirmOrderView.classList.add('state-center');
    }
  }

  if (paymentClose) {
    paymentClose.addEventListener('click', goBackFromPayment);
  }

  if (paymentCancelBtn) {
    paymentCancelBtn.addEventListener('click', goBackFromPayment);
  }

  const keypadButtons = $$('.keypad-btn');
  keypadButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const key = btn.getAttribute('data-key');
      
      if (key === 'backspace') {
        if (paymentPassword.length > 0) {
          paymentPassword.pop();
          updatePasswordDots();
        }
      } else {
        if (paymentPassword.length < 6) {
          paymentPassword.push(key);
          updatePasswordDots();
        }
      }
      
      // Enable/disable confirm button
      if (paymentConfirmBtn) {
        paymentConfirmBtn.disabled = (paymentPassword.length < 6);
      }
    });
  });

  function updatePasswordDots() {
    if (!paymentDotsContainer) return;
    const dots = paymentDotsContainer.querySelectorAll('.payment-dot');
    dots.forEach((dot, idx) => {
      if (idx < paymentPassword.length) {
        dot.classList.add('filled');
      } else {
        dot.classList.remove('filled');
      }
    });
  }

  if (paymentConfirmBtn) {
    paymentConfirmBtn.addEventListener('click', () => {
      // 1. Show success toast
      showToast('Payment successful! Order submitted!', 'check_circle');
      
      // 2. Compile a new order object
      const newOrderId = `A${Math.floor(100 + Math.random() * 900)}`;
      const activeStall = ($('#dishes-stall-title')?.textContent || 'The Noodle House');
      const totalAmountVal = parseFloat(paymentTotalAmount?.textContent.replace('$', '') || 0);
      const remarkText = confirmOrderRemarksInput?.value || '';
      
      const now = new Date();
      const newOrder = {
        id: newOrderId,
        stallName: activeStall,
        items: JSON.parse(JSON.stringify(Object.values(cart))),
        totalPrice: totalAmountVal,
        remarks: remarkText,
        status: 'placed',
        timestamps: {
          placed: formatTime(now),
          preparing: formatTime(new Date(now.getTime() + 15 * 60000)),
          ready: formatTime(new Date(now.getTime() + 30 * 60000)),
          completed: formatTime(new Date(now.getTime() + 45 * 60000))
        }
      };
      
      // Add to front of orders list
      orders.unshift(newOrder);
      renderOrdersList();

      // 3. Reset cart state
      cart = {};
      resetAllFoodCardUI();
      updateCartFloatingBar();
      
      // 4. Clear remarks input
      if (confirmOrderRemarksInput) {
        confirmOrderRemarksInput.value = '';
      }
      
      // 5. Slide out payment-view
      if (paymentView) {
        paymentView.classList.remove('state-center');
        paymentView.classList.add('state-right');
      }
      
      // 6. Open Order Details screen for the newly created order
      openOrderDetail(newOrder);

      // 7. Auto progress simulation:
      // Placed -> Preparing in 4 seconds
      if (orderAdvanceTimeout1) clearTimeout(orderAdvanceTimeout1);
      if (orderAdvanceTimeout2) clearTimeout(orderAdvanceTimeout2);
      
      orderAdvanceTimeout1 = setTimeout(() => {
        if (newOrder.status === 'placed') {
          newOrder.status = 'preparing';
          renderOrdersList();
          if (activeOrderDetailOrder && activeOrderDetailOrder.id === newOrder.id) {
            renderOrderDetail(newOrder, 'preparing');
          }
        }
      }, 4000);

      // Preparing -> Ready in 10 seconds (4 + 6)
      orderAdvanceTimeout2 = setTimeout(() => {
        if (newOrder.status === 'preparing') {
          newOrder.status = 'ready';
          renderOrdersList();
          if (activeOrderDetailOrder && activeOrderDetailOrder.id === newOrder.id) {
            renderOrderDetail(newOrder, 'ready');
          }
        }
      }, 10000);
    });
  }

  // --- Orders & Detail View Helpers & Handlers ---
  
  function formatTime(date) {
    let hours = date.getHours();
    let minutes = date.getMinutes();
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12;
    hours = hours ? hours : 12; // the hour '0' should be '12'
    minutes = minutes < 10 ? '0' + minutes : minutes;
    return hours + ':' + minutes + ' ' + ampm;
  }

  function renderOrdersList() {
    if (!ordersListContainer) return;
    ordersListContainer.innerHTML = '';

    // Filter orders
    const filteredOrders = orders.filter(order => {
      if (activeOrderFilter === 'all') return true;
      return order.status === activeOrderFilter;
    });

    if (filteredOrders.length === 0) {
      ordersListContainer.innerHTML = `
        <div style="text-align: center; padding: 40px 20px; color: var(--color-text-secondary);">
          <span class="material-symbols-outlined" style="font-size: 3rem; margin-bottom: 12px; color: var(--color-text-muted);">receipt</span>
          <p style="font-weight: 700;">No orders yet</p>
          <p style="font-size: 0.85rem; margin-top: 4px;">Items in this status will appear here.</p>
        </div>
      `;
      return;
    }

    filteredOrders.forEach(order => {
      const cardEl = document.createElement('div');
      cardEl.className = 'order-card';
      
      // Format items summary string (e.g. "1x Margherita Large, 2x Garlic Bread")
      let itemsSummary = '';
      if (order.items && order.items.length > 0) {
        itemsSummary = order.items.map(item => `${item.quantity}x ${item.name}`).join(', ');
      } else {
        itemsSummary = 'No items';
      }

      // Format status label and prefix icon
      let statusLabel = 'Placed';
      let statusIcon = '';
      if (order.status === 'placed') {
        statusLabel = 'Pending';
        statusIcon = '<span class="material-symbols-outlined" style="font-size: 0.95rem; margin-right: 4px; vertical-align: middle;">schedule</span>';
      } else if (order.status === 'preparing') {
        statusLabel = 'Preparing';
        statusIcon = '<span class="material-symbols-outlined animated-sync" style="font-size: 0.95rem; margin-right: 4px; vertical-align: middle; display: inline-block;">sync</span>';
      } else if (order.status === 'ready') {
        statusLabel = 'Ready';
        statusIcon = '<span class="material-symbols-outlined" style="font-size: 0.95rem; margin-right: 4px; vertical-align: middle;">check_circle</span>';
      } else if (order.status === 'completed') {
        statusLabel = 'Completed';
        statusIcon = ''; // Completed status has no prefix icon in the mockup
      }

      // Render Middle content box depending on status
      let middleHTML = '';
      if (order.status === 'completed') {
        middleHTML = `
          <div class="order-card-items-row">
            <span class="order-card-items-summary">${itemsSummary}</span>
            <span class="order-card-completed-price">$${order.totalPrice.toFixed(2)}</span>
          </div>
        `;
      } else {
        middleHTML = `
          <div class="order-card-details-box">
            <div class="order-card-items-summary">${itemsSummary}</div>
            <div class="order-card-divider"></div>
            <div class="order-card-total-row">
              <span class="order-card-total-label">TOTAL</span>
              <span class="order-card-total-price">$${order.totalPrice.toFixed(2)}</span>
            </div>
          </div>
        `;
      }

      // Render bottom action buttons depending on status
      let actionButtonsHTML = '';
      if (order.status === 'ready') {
        actionButtonsHTML = `
          <div class="order-card-actions">
            <button type="button" class="btn-card-primary btn-view-pickup">View Pickup Code</button>
          </div>
        `;
      } else if (order.status === 'preparing' || order.status === 'placed') {
        actionButtonsHTML = `
          <div class="order-card-actions">
            <button type="button" class="btn-card-outline btn-cancel-order">Cancel</button>
            <button type="button" class="btn-card-primary btn-track-order disabled" disabled>Track Order</button>
          </div>
        `;
      } else if (order.status === 'completed') {
        actionButtonsHTML = '';
      }

      cardEl.innerHTML = `
        <div class="order-card-top-row">
          <img src="${order.items[0]?.imgUrl || '/images/sour_beef.png'}" class="order-card-stall-img" alt="${order.stallName}" />
          <div class="order-card-stall-info">
            <div class="order-card-stall-name">${order.stallName}</div>
            <div class="order-card-time">${order.timestamps.placed}</div>
          </div>
          <div class="order-card-status-wrapper">
            <span class="order-card-status ${order.status}">
              ${statusIcon}${statusLabel}
            </span>
          </div>
        </div>
        ${middleHTML}
        ${actionButtonsHTML}
      `;

      // Event listeners for actions inside the card:
      
      // Card click opens detail
      cardEl.addEventListener('click', () => {
        openOrderDetail(order);
      });

      // View Pickup Code button click opens detail
      const viewPickupBtn = cardEl.querySelector('.btn-view-pickup');
      if (viewPickupBtn) {
        viewPickupBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          openOrderDetail(order);
        });
      }

      // Cancel button click cancels the order
      const cancelBtn = cardEl.querySelector('.btn-cancel-order');
      if (cancelBtn) {
        cancelBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          order.status = 'completed';
          order.timestamps.completed = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
          showToast(`Order ${order.id} has been cancelled.`, 'check_circle');
          renderOrdersList();
        });
      }

      // Reorder button click copies items to cart and opens cart
      const reorderBtn = cardEl.querySelector('.btn-reorder');
      if (reorderBtn) {
        reorderBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          cart = {}; // Clear active cart
          order.items.forEach(item => {
            const itemId = item.name.toLowerCase().replace(/ /g, '_');
            cart[itemId] = {
              name: item.name,
              price: item.price,
              quantity: item.quantity,
              imgUrl: item.imgUrl,
              options: item.options || 'Regular portion'
            };
          });
          updateCartFloatingBar();
          openCartSheet();
          showToast(`Reordered items from ${order.stallName}!`, 'shopping_cart');
        });
      }

      ordersListContainer.appendChild(cardEl);
    });

    // Add Show Older Orders button at the bottom of the list
    const footerEl = document.createElement('div');
    footerEl.className = 'orders-list-footer';
    footerEl.innerHTML = `
      <button type="button" class="btn-show-older">
        <span class="material-symbols-outlined">expand_more</span>
        Show Older Orders
      </button>
    `;
    
    // Bind Show Older Orders click
    const showOlderBtn = footerEl.querySelector('.btn-show-older');
    if (showOlderBtn) {
      showOlderBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        showToast('Older orders loaded successfully!', 'check_circle');
        // Let's add two older orders
        const olderOrders = [
          {
            id: 'A002',
            stallName: 'The Noodle House',
            items: [
              { name: 'Spicy Miso Ramen', price: 12.00, quantity: 2, imgUrl: '/images/spicy_dan_dan.png', options: 'Less spicy' }
            ],
            totalPrice: 24.00,
            remarks: '',
            status: 'completed',
            timestamps: { placed: 'Oct 20, 12:30 PM', completed: 'Oct 20, 12:45 PM' }
          },
          {
            id: 'A001',
            stallName: 'Healthy Bowls',
            items: [
              { name: 'Tofu Salad', price: 10.00, quantity: 1, imgUrl: '/images/tofu_broccoli.png', options: '' }
            ],
            totalPrice: 10.00,
            remarks: '',
            status: 'completed',
            timestamps: { placed: 'Oct 19, 06:15 PM', completed: 'Oct 19, 06:30 PM' }
          }
        ];
        
        olderOrders.forEach(o => {
          if (!orders.some(ex => ex.id === o.id)) {
            orders.push(o);
          }
        });
        
        renderOrdersList();
      });
    }

    ordersListContainer.appendChild(footerEl);
  }

  function openOrderDetail(order) {
    activeOrderDetailOrder = order;
    activeOrderDetailStatus = order.status;

    // Render details
    renderOrderDetail(order, order.status);

    // Transition view
    [menuView, dishesView, dishDetailView, confirmOrderView, paymentView, ordersView].forEach(v => {
      if (v) {
        v.classList.remove('state-center');
        v.classList.add(v === ordersView ? 'state-left' : 'state-left');
      }
    });
    if (orderDetailView) {
      orderDetailView.classList.remove('state-right', 'state-left');
      orderDetailView.classList.add('state-center');
    }

    // Set active bottom nav tab to Orders
    syncBottomNavActiveTab('orders');
  }

  function renderOrderDetail(order, status) {
    activeOrderDetailStatus = status;

    // Highlight simulator buttons active state
    const simBtns = $$('.simulation-row .sim-btn');
    simBtns.forEach(btn => {
      const btnSim = btn.getAttribute('data-sim');
      if (btnSim === status) {
        btn.classList.add('active');
      } else {
        btn.classList.remove('active');
      }
    });

    // 1. Stepper line fill
    let fillWidth = '0%';
    if (status === 'preparing') fillWidth = '33%';
    else if (status === 'ready') fillWidth = '66%';
    else if (status === 'completed') fillWidth = '100%';
    if (orderTimelineLineFill) {
      orderTimelineLineFill.style.width = fillWidth;
    }

    // Stepper dots class toggles
    // Step 1: Placed
    if (stepPlaced) {
      stepPlaced.className = 'timeline-step completed';
      if (timePlaced) timePlaced.textContent = order.timestamps.placed;
    }
    
    // Step 2: Preparing
    if (stepPreparing) {
      const stepCircle = stepPreparing.querySelector('.timeline-step-circle');
      if (status === 'placed') {
        stepPreparing.className = 'timeline-step pending';
        if (stepCircle) stepCircle.innerHTML = '<span class="material-symbols-outlined" style="font-size: 1.15rem;">soup_kitchen</span>';
        if (timePreparing) timePreparing.textContent = '--:--';
      } else if (status === 'preparing') {
        stepPreparing.className = 'timeline-step active';
        if (stepCircle) stepCircle.innerHTML = '<span class="material-symbols-outlined animated-skillet" style="font-size: 1.15rem;">soup_kitchen</span>';
        if (timePreparing) timePreparing.textContent = 'Now';
      } else {
        stepPreparing.className = 'timeline-step completed';
        if (stepCircle) stepCircle.innerHTML = '<span class="material-symbols-outlined" style="font-size: 1.15rem;">check</span>';
        if (timePreparing) timePreparing.textContent = order.timestamps.preparing;
      }
    }

    // Step 3: Ready
    if (stepReady) {
      const stepCircle = stepReady.querySelector('.timeline-step-circle');
      if (status === 'placed' || status === 'preparing') {
        stepReady.className = 'timeline-step pending';
        if (stepCircle) stepCircle.innerHTML = '<span class="material-symbols-outlined" style="font-size: 1.15rem;">restaurant</span>';
        if (timeReady) timeReady.textContent = '--:--';
      } else if (status === 'ready') {
        stepReady.className = 'timeline-step active';
        if (stepCircle) stepCircle.innerHTML = '<span class="material-symbols-outlined" style="font-size: 1.15rem;">restaurant</span>';
        if (timeReady) timeReady.textContent = 'Now';
      } else {
        stepReady.className = 'timeline-step completed';
        if (stepCircle) stepCircle.innerHTML = '<span class="material-symbols-outlined" style="font-size: 1.15rem;">check</span>';
        if (timeReady) timeReady.textContent = order.timestamps.ready;
      }
    }

    // Step 4: Collected
    if (stepCompleted) {
      const stepCircle = stepCompleted.querySelector('.timeline-step-circle');
      if (status !== 'completed') {
        stepCompleted.className = 'timeline-step pending';
        if (stepCircle) stepCircle.innerHTML = '<span class="material-symbols-outlined" style="font-size: 1.15rem;">inbox</span>';
        if (timeCompleted) timeCompleted.textContent = '--:--';
      } else {
        stepCompleted.className = 'timeline-step completed';
        if (stepCircle) stepCircle.innerHTML = '<span class="material-symbols-outlined" style="font-size: 1.15rem;">check</span>';
        if (timeCompleted) timeCompleted.textContent = order.timestamps.completed;
      }
    }

    // Stepper Status Badge Pill Text
    if (orderStatusPillVal) {
      if (status === 'placed') orderStatusPillVal.textContent = 'WAITING FOR ACCEPTANCE';
      else if (status === 'preparing') orderStatusPillVal.textContent = 'PREPARING MEAL';
      else if (status === 'ready') orderStatusPillVal.textContent = 'READY FOR PICKUP';
      else if (status === 'completed') orderStatusPillVal.textContent = 'ORDER COMPLETED';
    }

    // 2. Status Info Display Card
    if (orderDetailStatusCard) {
      orderDetailStatusCard.innerHTML = '';
      const innerEl = document.createElement('div');
      innerEl.className = 'order-status-card-inner';
      
      if (status === 'placed') {
        innerEl.innerHTML = `
          <div class="order-status-large-icon-circle waiting">
            <md-circular-progress indeterminate style="--md-circular-progress-active-indicator-color: var(--color-brand-rust-light); --md-circular-progress-size: 48px;"></md-circular-progress>
          </div>
          <h3 class="order-status-main-title">Waiting for Accept...</h3>
          <p class="order-status-main-subtitle">The merchant will accept your order shortly. Thank you for your patience.</p>
          
          <div class="order-pickup-box">
            <div class="order-pickup-row">
              <span class="order-pickup-label">Collection ID</span>
              <span class="order-pickup-value font-brand-rust">${order.id}</span>
            </div>
            <div class="order-pickup-row">
              <span class="order-pickup-label">Pickup Stall</span>
              <span class="order-pickup-value font-text-primary"><span class="material-symbols-outlined inline-icon" style="color: var(--color-brand-rust);">storefront</span>${order.stallName}</span>
            </div>
          </div>
        `;
      } else if (status === 'preparing') {
        innerEl.innerHTML = `
          <div class="order-status-large-icon-circle preparing">
            <md-circular-progress indeterminate style="--md-circular-progress-active-indicator-color: #f06209; --md-circular-progress-size: 48px;"></md-circular-progress>
          </div>
          <h3 class="order-status-main-title">Preparing Meal...</h3>
          <p class="order-status-main-subtitle">The chef is preparing your meal. It will be ready in 10-15 minutes.</p>
          
          <div class="order-pickup-box">
            <div class="order-pickup-row">
              <span class="order-pickup-label">Collection ID</span>
              <span class="order-pickup-value font-brand-rust">${order.id}</span>
            </div>
            <div class="order-pickup-row">
              <span class="order-pickup-label">Pickup Stall</span>
              <span class="order-pickup-value font-text-primary"><span class="material-symbols-outlined inline-icon" style="color: var(--color-brand-rust);">storefront</span>${order.stallName}</span>
            </div>
          </div>
        `;
      } else if (status === 'ready') {
        innerEl.innerHTML = `
          <div class="order-qrcode-wrapper">
            <svg width="160" height="160" viewBox="0 0 29 29" style="background-color: white; padding: 12px; border-radius: 16px; border: 1px solid var(--color-border);">
              <!-- corner finders -->
              <rect x="0" y="0" width="7" height="7" fill="black" />
              <rect x="1" y="1" width="5" height="5" fill="white" />
              <rect x="2" y="2" width="3" height="3" fill="black" />
              
              <rect x="22" y="0" width="7" height="7" fill="black" />
              <rect x="23" y="1" width="5" height="5" fill="white" />
              <rect x="24" y="2" width="3" height="3" fill="black" />
              
              <rect x="0" y="22" width="7" height="7" fill="black" />
              <rect x="1" y="23" width="5" height="5" fill="white" />
              <rect x="2" y="24" width="3" height="3" fill="black" />
              
              <!-- alignment pattern -->
              <rect x="20" y="20" width="5" height="5" fill="black" />
              <rect x="21" y="21" width="3" height="3" fill="white" />
              <rect x="22" y="22" width="1" height="1" fill="black" />
              
              <!-- timings -->
              <rect x="8" y="6" width="1" height="1" fill="black" /><rect x="10" y="6" width="1" height="1" fill="black" /><rect x="12" y="6" width="1" height="1" fill="black" /><rect x="14" y="6" width="1" height="1" fill="black" /><rect x="16" y="6" width="1" height="1" fill="black" /><rect x="18" y="6" width="1" height="1" fill="black" /><rect x="20" y="6" width="1" height="1" fill="black" />
              <rect x="6" y="8" width="1" height="1" fill="black" /><rect x="6" y="10" width="1" height="1" fill="black" /><rect x="6" y="12" width="1" height="1" fill="black" /><rect x="6" y="14" width="1" height="1" fill="black" /><rect x="6" y="16" width="1" height="1" fill="black" /><rect x="6" y="18" width="1" height="1" fill="black" /><rect x="6" y="20" width="1" height="1" fill="black" />
              
              <!-- data pixels -->
              <rect x="9" y="9" width="2" height="1" fill="black" />
              <rect x="14" y="10" width="3" height="1" fill="black" />
              <rect x="12" y="14" width="1" height="3" fill="black" />
              <rect x="10" y="19" width="4" height="2" fill="black" />
              <rect x="18" y="12" width="3" height="2" fill="black" />
              <rect x="16" y="18" width="2" height="3" fill="black" />
              <rect x="22" y="10" width="4" height="1" fill="black" />
              <rect x="26" y="14" width="2" height="4" fill="black" />
              <rect x="10" y="2" width="2" height="3" fill="black" />
              <rect x="15" y="1" width="3" height="2" fill="black" />
              <rect x="14" y="16" width="1" height="2" fill="black" />
              <rect x="18" y="24" width="2" height="2" fill="black" />
              <rect x="2" y="10" width="3" height="2" fill="black" />
              <rect x="1" y="15" width="2" height="3" fill="black" />
            </svg>
          </div>
          <h3 class="order-status-qr-code">${order.id}</h3>
          <div class="order-pickup-stall-line">
            <span class="material-symbols-outlined" style="color: var(--color-brand-rust); font-size: 1.25rem;">storefront</span>
            <span style="font-weight: 700; color: var(--color-text-primary);">${order.stallName}</span>
          </div>
          <p class="order-status-qr-subtitle">Present this code at the counter to collect your order.</p>

          <!-- Simulation Pickup Trigger Button -->
          <button type="button" class="btn-simulation-pickup" id="btn-simulation-pickup-action" style="margin-top: 16px; font-size: 0.85rem; padding: 10px 18px; background-color: var(--color-brand-rust); color: white; border: none; border-radius: 100px; font-weight: 700; cursor: pointer; box-shadow: 0 4px 12px rgba(162, 59, 0, 0.25);">
            Simulate Staff Scan (Verify Pickup)
          </button>
        `;
        
        // Bind Scan Action Button
        const scanBtn = innerEl.querySelector('#btn-simulation-pickup-action');
        if (scanBtn) {
          scanBtn.addEventListener('click', () => {
            order.status = 'completed';
            renderOrderDetail(order, 'completed');
            renderOrdersList();
            showToast('Order collected successfully!', 'check_circle');
          });
        }
      } else if (status === 'completed') {
        innerEl.innerHTML = `
          <div class="order-status-large-icon-circle completed">
            <span class="material-symbols-outlined" style="font-weight: bold;">check</span>
          </div>
          <h3 class="order-status-main-title">Thank You!</h3>
          <p class="order-status-main-subtitle" id="order-status-completed-desc">Your order from ${order.stallName} has been successfully collected.</p>
          
          <div class="order-pickup-box">
            <div class="order-pickup-row">
              <span class="order-pickup-label">Collection ID</span>
              <span class="order-pickup-value font-brand-rust">${order.id}</span>
            </div>
            <div class="order-pickup-row">
              <span class="order-pickup-label">Collected at</span>
              <span class="order-pickup-value font-text-primary">${order.timestamps.completed}</span>
            </div>
          </div>
        `;
      }
      
      orderDetailStatusCard.appendChild(innerEl);
    }

    // 3. Dynamic selection list
    if (orderDetailItemsList) {
      orderDetailItemsList.innerHTML = '';
      order.items.forEach(item => {
        const itemEl = document.createElement('div');
        itemEl.className = 'confirm-order-item';
        itemEl.innerHTML = `
          <img class="confirm-order-item-img" src="${item.imgUrl || '/images/default.png'}" alt="${item.name}" />
          <div class="confirm-order-item-info">
            <span class="confirm-order-item-name">${item.quantity}x ${item.name}</span>
            <span class="confirm-order-item-qty">${item.options || 'Regular portion'}</span>
          </div>
          <span class="confirm-order-item-price">$${(item.price * item.quantity).toFixed(2)}</span>
        `;
        orderDetailItemsList.appendChild(itemEl);
      });
    }

    // Total Price
    if (orderDetailTotalPrice) {
      orderDetailTotalPrice.textContent = `$${order.totalPrice.toFixed(2)}`;
    }

    // Remarks Card display toggle
    if (orderDetailRemarksWrapper) {
      if (order.remarks && order.remarks.trim() !== '') {
        orderDetailRemarksWrapper.style.display = 'flex';
        if (orderDetailRemarksVal) {
          orderDetailRemarksVal.textContent = `"${order.remarks}"`;
        }
      } else {
        orderDetailRemarksWrapper.style.display = 'none';
      }
    }

    // Cancel Button display toggle
    if (btnOrderCancel) {
      if (status === 'completed') {
        btnOrderCancel.style.display = 'none';
      } else {
        btnOrderCancel.style.display = 'flex';
      }
    }
  }

  // --- Back Arrow on Order Details ---
  if (orderDetailBack) {
    orderDetailBack.addEventListener('click', () => {
      if (orderDetailView) {
        orderDetailView.classList.remove('state-center');
        orderDetailView.classList.add('state-right');
      }
      if (ordersView) {
        ordersView.classList.remove('state-left');
        ordersView.classList.add('state-center');
      }
    });
  }

  // --- Dev Simulator Pills Clicks ---
  const simBtns = $$('.simulation-row .sim-btn');
  simBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetSim = btn.getAttribute('data-sim');
      if (activeOrderDetailOrder) {
        activeOrderDetailOrder.status = targetSim;
        renderOrderDetail(activeOrderDetailOrder, targetSim);
        renderOrdersList();
      }
    });
  });

  // --- Help Button clicks ---
  if (btnOrderHelp) {
    btnOrderHelp.addEventListener('click', () => {
      showToast('Support team has been notified!', 'help');
    });
  }

  // Initialize order lists at start
  renderOrdersList();
}
