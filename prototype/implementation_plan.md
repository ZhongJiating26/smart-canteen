# Implementation Plan - User Profile Page (Personal Center)

We will build the User Profile page (`#profile-view`) based on `pictures/用户端 - 个人中心页.png`, using Material 3 style cards, lists, and layout design tokens.

---

## Proposed Changes

### canteen-prototype

#### [MODIFY] [index.html](file:///Users/zhongjiating/Projects/smart-canteen/prototype/canteen-prototype/index.html)
- **Profile Screen (`#profile-view`)**: Add a new screen container `.mobile-screen.state-right` (as a sibling to `#orders-view`).
  - **Header**: Left-aligned brand title "Campus Eats" and a top-right clickable notification bell icon (`notifications`).
  - **Scroll Area (`.profile-scroll-container`)**:
    - **Profile Card**: Contains a circular avatar of Alex Thompson (`/images/profile_avatar.png`), a verified checkmark badge (`check`), nickname, phone, and student ID. Below the info, include an "Edit Profile" filled button with an edit icon (`edit`).
    - **Stats Flex Row**: Includes two cards side-by-side:
      - Left card: "12 Orders" (with the number "12" styled in rust/orange).
      - Right card: "$142 Spent" (with "$142" styled in rust/orange).
    - **Account Settings Card**: Styled block with section title "Account Settings", containing list items:
      - *Settings* (Icon: `settings`, Title: "Settings", Description: "App preferences and privacy", Trailing: `chevron_right`).
      - *Help & Support* (Icon: `help`, Title: "Help & Support", Description: "FAQs and contact support", Trailing: `chevron_right`).
      - *Logout* (Icon: `logout`, Title: "Logout" (reddish), Description: "Sign out of your account", no trailing chevron).

#### [MODIFY] [src/style.css](file:///Users/zhongjiating/Projects/smart-canteen/prototype/canteen-prototype/src/style.css)
- **Scroll Container**: Add `.profile-scroll-container` with vertical scrolling and `padding-bottom: 96px;` to clear the persistent bottom navigation bar.
- **Profile Card Layout**: Style the top card with a beautiful linear gradient from `--color-brand-peach` to a soft transparent hue, rounded corners (`28px`), and centered items.
- **Verified Badge**: Style the verified checkmark bubble overlay on the bottom right of the circular avatar.
- **Edit Button**: Style the filled brown/orange pill button (`var(--color-brand-rust)`) with white text and icon.
- **Stats Cards**: Style the side-by-side cards with `flex: 1`, background `var(--color-bg-card)`, and custom typography.
- **Account Settings Block**: Style the card wrapper, the section heading, and list items using standard Material 3 styles (left circle icon backdrops, title/subtitle flex vertical stacks, right chevrons, hover effects, and a special pink/red backdrop for the logout icon).

#### [MODIFY] [src/main.js](file:///Users/zhongjiating/Projects/smart-canteen/prototype/canteen-prototype/src/main.js)
- **DOM Selection**: Select `#profile-view` and its interactive elements.
- **Navigation Controls**:
  - Update `setupViewNavigation()` to handle bottom nav tab clicks:
    - Click "Profile" -> Slide `#profile-view` to `state-center`. Slide other views (`menuView`, `ordersView`, etc.) to `state-left`.
    - Click "Menu" -> Slide `#menu-view` to `state-center`. Slide `ordersView` and `profileView` to `state-right`.
    - Click "Orders" -> Slide `#orders-view` to `state-center`. Slide `menuView` to `state-left`, and `profileView` to `state-right`.
    - Correctly call `syncBottomNavActiveTab` for the profile page.
- **Click Triggers**:
  - Bind a click listener on the "Logout" list item inside the settings card to trigger `handleLogout()`.
  - Bind toast notifications to "Edit Profile", "Settings", "Help & Support", and the notification bell.

---

## Verification Plan

### Automated Tests
- Run `npm run build` to verify production compilation.

### Manual Verification
1. **Interactive Navigation**:
   - Log in, click the bottom "Profile" tab: verify the view transitions in from the right.
   - Click "Menu" or "Orders" tabs: verify the profile view slides out smoothly.
2. **Profile Layout & Aesthetics**:
   - Verify that the card matches the image, featuring a circular avatar, student details, and an Edit Profile button.
   - Verify the stats block shows "12 Orders" and "$142 Spent".
   - Verify list items for Settings, Help, and Logout are styled with custom icon containers.
3. **Actions & Triggers**:
   - Click "Logout": verify the application logs out successfully and returns to the login screen.
   - Click "Settings", "Help & Support", and "Edit Profile": verify descriptive toast notifications appear.
