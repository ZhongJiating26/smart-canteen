# Walkthrough - Stall Selection Page, Dishes Detail View, and Slide Transitions

We have successfully completed the implementation of the **Campus Eats** mobile prototype, matching the Stall Selection mockup (`pictures/菜单-档口选择页.png`) and establishing a smooth slide navigation flow between views.

---

## 🛠️ Completed Implementation Details

### 1. View Shell & Slide Transition Layout ([index.html](file:///Users/zhongjiating/Projects/smart-canteen/prototype/canteen-prototype/index.html))
- **Phone Shell Container**: Houses all absolute-positioned views using the `state-left`, `state-center`, and `state-right` classes to orchestrate slide animations.
- **Sign In & Register Screen**: Fully styled with centered cards, custom Material 3 text fields, required vs optional inputs, and verification code mock triggers.
- **Stall Selection Landing (`#menu-view`)**:
  - Pinned Header: Clean "Campus Eats" title.
  - Promo Banner: "Hungry, Alex?" with background image `banner_bg.png` and inline tags.
  - Search Bar: Rounded text field with tune icon.
  - Category Chips: Horizontal scroll row for "All Stalls", "Noodles", "Western".
  - Stall Cards: Rendered Noodle House, Western Grill, and Healthy Bowls with sales, cuisine, and live queue status tags.
- **Dishes Detail Screen (`#dishes-view`)**:
  - Secondary sliding view containing a back button, dynamic stall header title, and a shopping cart button with a red badge counter.
  - Houses the dish card list representing the individual food items.

### 2. Interaction Logic ([src/main.js](file:///Users/zhongjiating/Projects/smart-canteen/prototype/canteen-prototype/src/main.js))
- **Stall List Filtering**:
  - Tapping category chips (e.g., "Noodles" or "Western") filters the visible stall cards immediately.
  - Typing in the search input dynamically filters stalls by title or cuisine tags.
- **Stall Menu Detail Navigation**:
  - Tapping a stall card triggers a slide transition: `#menu-view` slides to `state-left`, and `#dishes-view` slides in to `state-center`.
  - The header title (`#dishes-stall-title`) is dynamically updated to match the clicked stall.
  - Dish cards are filtered to display only items mapped to the clicked stall ID (handles multi-stall mapping such as drinks).
- **Dish Back Transition**:
  - Tapping the back arrow (`#dishes-back`) slides `#dishes-view` back to `state-right` and `#menu-view` back to `state-center`.
- **Add to Cart & Badge**:
  - Tapping the `+` button on a dish increments the cart badge count in the header.
  - The badge scales up and down dynamically using CSS pop animations.
- **Bottom Navigation & Logout**:
  - Highlights tabs correctly. Clicking "Profile" triggers the `handleLogout()` routine: displays a logout toast, slides `#menu-view` to the right, and restores the `#login-view` at the center. State variables and search fields are reset.

### 3. Styling Enhancements ([src/style.css](file:///Users/zhongjiating/Projects/smart-canteen/prototype/canteen-prototype/src/style.css))
- Integrated custom styles for the promo banner, category chips, outline stall cards, food grid, and active Material 3 bottom-tab navigation pills.
- Configured cubic-bezier transitions for smooth, high-fidelity slide transitions at native speed.

---

## 🚀 Verification & Testing

### 1. Build Verification
We ran the build command to ensure clean compiling with Vite:
```bash
npm run dev
npm run build
```
**Result**: Build completed successfully, generating the production assets (`dist/index.html`, `dist/assets/index-*.css`, `dist/assets/index-*.js`) with zero warnings or errors.

### 2. Interactive Flow Test Steps
1. **Login & Register**:
   - Access prototype -> go to registration (slide animation).
   - Sign up -> redirect back to login (slide animation).
   - Enter credentials -> log in.
2. **Stall Selection Landing**:
   - Verify the design matches `pictures/菜单-档口选择页.png`.
   - Click "Noodles" to filter and see only "Noodle House".
   - Click "All Stalls" to restore the list.
   - Enter "Burgers" in the search box to filter to "Western Grill".
3. **Dishes Detail Screen**:
   - Click "Noodle House" -> screen slides to the left, showing Noodle House dishes (Sour Soup Beef, Sweet Sour Plum Soup).
   - Add items to the cart -> cart badge increments and flashes.
   - Click the back arrow -> screen slides back to the stall list.
4. **Logout**:
   - Click the "Profile" bottom nav tab -> logs out and slides back to the Sign In screen.
