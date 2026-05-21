import '@material/web/textfield/outlined-text-field.js';

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
      showToast('Proceeding to checkout!', 'shopping_cart');
      closeCartSheet();
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
            syncFoodCardUI(id);
          }
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
  navTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const tabName = tab.getAttribute('data-tab');

      if (tabName === 'profile') {
        handleLogout();
      } else if (tabName === 'orders') {
        showToast('Orders feature coming soon!', 'explore');
      } else if (tabName === 'menu') {
        // Go back to stalls list if currently on dishes view
        if (dishesView && dishesView.classList.contains('state-center')) {
          dishesView.classList.remove('state-center');
          dishesView.classList.add('state-right');
          if (menuView) {
            menuView.classList.remove('state-left');
            menuView.classList.add('state-center');
          }
        } else {
          showToast('Already on Menu view', 'explore');
        }
      }
    });
  });

  function handleLogout() {
    showToast('Logged out successfully!', 'logout');
    closeCartSheet();
    
    // Transition back to login view
    if (menuView) {
      menuView.classList.remove('state-center');
      menuView.classList.add('state-right');
    }
    if (dishesView) {
      dishesView.classList.remove('state-center');
      dishesView.classList.add('state-right');
    }
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
}
