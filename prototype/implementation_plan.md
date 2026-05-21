# Implementation Plan - Stall Selection Page & Dishes Detail View

We will redesign the post-login canteen landing screen to match the official **Stall Selection Page (菜单-档口选择页)** screenshot, utilizing the warm orange/rust color scheme and Material 3 design specs. We will also add a secondary dishes view that slides in when a stall is tapped.

---

## Proposed Changes

### [canteen-prototype Component](file:///Users/zhongjiating/Projects/smart-canteen/prototype/canteen-prototype)

#### [MODIFY] [index.html](file:///Users/zhongjiating/Projects/smart-canteen/prototype/canteen-prototype/index.html)
- **Modify `#menu-view` to represent Stall Selection**:
  - **Header**: Left-aligned "Campus Eats" brand title (Georgia brand font, rust color).
  - **Promo Banner**: Card showing "Hungry, Alex? Find your favorite campus stall and skip the queue." with a background of our high-fidelity generated `/images/banner_bg.png` and two pill chips: "Popular Now" (flame icon) and "Fast Pickup" (timer icon).
  - **Search Bar**: Search bar input field with leading search icon and trailing `tune` filter icon.
  - **Category Chips**: Scrollable row of outlined filter chips: "All Stalls" (grid icon), "Noodles" (soup kitchen icon), "Western" (lunch dining icon).
  - **Stall Cards List**: Outlined cards for:
    - **Noodle House** (Asian Cuisine, 920+ Sales, 20 orders in progress)
    - **Western Grill** (Gourmet Burgers, 450+ Sales, 8 orders in progress)
    - **Healthy Bowls** (Salads & Drinks, 680+ Sales, 5 orders in progress)
  - **Bottom Navigation Bar**: Fixed at the bottom containing 3 tabs to match the screenshot:
    - **Menu** (Active, fork/knife icon `restaurant` in a peach pill indicator)
    - **Orders** (Receipt icon `receipt`)
    - **Profile** (Person icon `person`)
- **Add `#dishes-view` for Stall Menu Details**:
  - Hidden by default (`state-right`). Slides in from the right when a stall card is tapped.
  - **Header**: Back arrow (`arrow_back`), dynamic stall name title, and a Shopping Cart icon with a red notification count badge.
  - **Dish Cards List**: Displays the specific food items corresponding to the selected stall. Clicking the "+" add button increments the header cart badge and triggers a toast notification.

#### [MODIFY] [src/style.css](file:///Users/zhongjiating/Projects/smart-canteen/prototype/canteen-prototype/src/style.css)
- **Promo Banner Styling**: Set background image, background overlay blend, text coloring, and align pill chips.
- **Search Bar**: Ensure trailing filter icon alignment and spacing inside `<md-outlined-text-field>`.
- **Stall Cards (`.stall-card`)**: Flex layout matching the mockup card with rounded corners, left stall photo, middle text details, and right-aligned tags (pink/beige sales badge and solid beige queue indicator).
- **Bottom Navigation Tabs**: Update spacing, colors, and layout to match the three-tab design in the screenshot.
- **Dishes View layout**: Support horizontal slide transition class states (`.state-left`, `.state-center`, `.state-right`).

#### [MODIFY] [src/main.js](file:///Users/zhongjiating/Projects/smart-canteen/prototype/canteen-prototype/src/main.js)
- Bind events for:
  - **Stall Category Filtering**: Clicking category chips filters the stall list nodes.
  - **Stall Search**: Typing filters the stall list by name or cuisine type.
  - **Stall Card Tapping**: Tapping a stall card triggers the sliding transition of `#dishes-view` to `.state-center` and updates its header title and filters dishes matching that stall.
  - **Dishes Back Arrow**: Slides the dishes view out to `.state-right` to return to the stall selection page.
  - **Dishes Cart Icon / Add-to-Cart**: Increases cart badge count and displays toasts.
  - **Bottom Navigation / Logout**: Swapping bottom tabs; clicking the Profile tab triggers the logout flow back to `#login-view`.

---

## Verification Plan

### Automated Tests
- Run `npm run build` to confirm Vite compilation works cleanly.

### Manual Verification
- Log in and verify that the Landing page is the Stall Selection page matching the screenshot.
- Test category filtering (e.g. clicking "Noodles" filters out Western Grill).
- Click "Noodle House" to slide in the Dishes list. Confirm title updates to "Noodle House".
- Add items to cart and check that the cart count badge on the header increases.
- Click the back arrow to slide back to the stalls list.
- Click "Profile" on the bottom navigation and verify the logout flow.
