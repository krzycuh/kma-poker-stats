# Poker Stats Web App - UI Wireframes & Component Structure

## Screen-by-Screen Wireframes (Mobile-First)

---

## 1. Dashboard / Home Screen

### Layout (Mobile)
```
┌─────────────────────────────────┐
│  ☰  Poker Stats      [Avatar]  │ ← Header
├─────────────────────────────────┤
│                                 │
│  👋 Welcome back, John!         │
│                                 │
│  ┌─────────────────────────┐  │
│  │  YOUR STATS             │  │ ← Stats Card
│  │  Net Profit: +$1,250    │  │
│  │  ROI: 25.5% ↑           │  │
│  │  Win Rate: 45%          │  │
│  │  Sessions: 24           │  │
│  └─────────────────────────┘  │
│                                 │
│  Leaderboard Position           │
│  🥈 2nd Place (↑1)              │
│                                 │
│  ┌─────────────────────────┐  │
│  │  RECENT SESSIONS        │  │
│  │  ┌─────────────────┐    │  │
│  │  │ May 10 • Home   │    │  │ ← Session Card
│  │  │ +$150           │    │  │
│  │  │ 6 players       │    │  │
│  │  └─────────────────┘    │  │
│  │                         │  │
│  │  ┌─────────────────┐    │  │
│  │  │ May 3 • Club    │    │  │
│  │  │ -$75            │    │  │
│  │  │ 8 players       │    │  │
│  │  └─────────────────┘    │  │
│  │                         │  │
│  │  [View All History]     │  │
│  └─────────────────────────┘  │
│                                 │
└─────────────────────────────────┘
│  [Home] [Stats] [Board] [•••]  │ ← Bottom Nav
└─────────────────────────────────┘

[Admin View Additions]
┌─────────────────────────────────┐
│                                 │
│         [ + Log Game ]          │ ← Floating Action Button
│                                 │
│  ADMIN QUICK STATS              │
│  • 156 Total Sessions           │
│  • 42 Active Players            │
│  • Last game: 2 days ago        │
│                                 │
└─────────────────────────────────┘
```

### Components
- **Header**: App name, hamburger menu, user avatar
- **Welcome Banner**: Personalized greeting
- **Stats Summary Card**: Key metrics with trend indicators
- **Position Badge**: Current leaderboard rank with change indicator
- **Recent Sessions List**: Last 3-5 sessions with key info
- **FAB (Admin)**: Quick access to log game
- **Bottom Navigation**: Primary navigation tabs

---

## 2. My Stats Screen

### Layout (Mobile)
```
┌─────────────────────────────────┐
│  ←  My Stats                    │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐ │
│  │  PERFORMANCE OVERVIEW     │ │
│  │  ┌────┐  ┌────┐  ┌────┐  │ │
│  │  │Net │  │ROI │  │Win │  │ │ ← Metric Cards
│  │  │$1.2K│  │25% │  │45% │  │ │
│  │  └────┘  └────┘  └────┘  │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │  PROFIT OVER TIME         │ │
│  │      /\    /\             │ │ ← Line Chart
│  │     /  \  /  \    /\      │ │
│  │ ___/    \/    \__/  \___  │ │
│  │  Jan  Feb  Mar  Apr  May  │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │  ACHIEVEMENTS          3/8│ │
│  │  🏆 First Win             │ │
│  │  🔥 5 Game Streak         │ │
│  │  🎯 Biggest Winner        │ │
│  │  [View All]               │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │  BEST/WORST SESSIONS      │ │
│  │  Best:  +$450 (Apr 15)    │ │
│  │  Worst: -$200 (Mar 3)     │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
│  [Home] [Stats] [Board] [•••]  │
└─────────────────────────────────┘
```

### Components
- **Metric Cards**: Large numbers with icons
- **Chart Component**: Interactive line/bar chart
- **Achievement Tiles**: Visual badges with progress
- **Stats Tables**: Scrollable, sortable data
- **Expandable Sections**: Progressive disclosure

---

## 3. Leaderboard Screen

### Layout (Mobile)
```
┌─────────────────────────────────┐
│  ←  Leaderboard                 │
├─────────────────────────────────┤
│  [Net Profit ▾]                 │ ← Metric selector
│                                 │
│  ┌───────────────────────────┐ │
│  │  🥇 1. Mike Smith         │ │ ← Top 3 (prominent)
│  │     $2,450 • ROI 34.2%    │ │
│  │     ──────────────────    │ │
│  │  🥈 2. YOU               │ │
│  │     $1,250 • ROI 25.5%    │ │ (highlighted)
│  │     ──────────────────    │ │
│  │  🥉 3. Sarah Jones        │ │
│  │     $980 • ROI 22.1%      │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │  4. Tom Wilson            │ │ ← Rest of list
│  │     $720 • ROI 18.3%      │ │
│  ├───────────────────────────┤ │
│  │  5. Lisa Chen             │ │
│  │     $640 • ROI 16.8%      │ │
│  ├───────────────────────────┤ │
│  │  6. Dave Brown            │ │
│  │     $520 • ROI 14.2%      │ │
│  └───────────────────────────┘ │
│                                 │
│  [Load More...]                 │
│                                 │
└─────────────────────────────────┘
│  [Home] [Stats] [Board] [•••]  │
└─────────────────────────────────┘
```

### Components
- **Metric Selector**: Toggle between profit, ROI, win rate
- **Podium Section**: Top 3 with special styling
- **Player Card**: Avatar, name, key stat
- **User Highlight**: Visual distinction for current user
- **Rank Indicators**: Position numbers/badges
- **Infinite Scroll**: Load more on scroll

---

## 4. Session History Screen

### Layout (Mobile)
```
┌─────────────────────────────────┐
│  ←  Session History             │
├─────────────────────────────────┤
│  [Search] [Filter ⚙]  [Sort ↕] │
│                                 │
│  MAY 2025                       │ ← Month separator
│  ┌───────────────────────────┐ │
│  │ May 10, 2025              │ │
│  │ 📍 John's House           │ │
│  │ 6 players • 4h duration   │ │
│  │ Your result: +$150        │ │ (green)
│  │ ─────────────────         │ │
│  │ [View Details]            │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │ May 3, 2025               │ │
│  │ 📍 Poker Club             │ │
│  │ 8 players • 5h duration   │ │
│  │ Your result: -$75         │ │ (red)
│  │ ─────────────────         │ │
│  │ [View Details]            │ │
│  └───────────────────────────┘ │
│                                 │
│  APRIL 2025                     │
│  ┌───────────────────────────┐ │
│  │ Apr 28, 2025              │ │
│  │ 📍 Mike's Place           │ │
│  │ 7 players • 3.5h          │ │
│  │ Your result: +$220        │ │
│  │ ─────────────────         │ │
│  │ [View Details]            │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

### Components
- **Search Bar**: Search by location, players
- **Filter Panel**: Multi-select filters (drawer)
- **Sort Options**: Date, profit, duration
- **Month Dividers**: Chronological organization
- **Session Card**: Compact session summary
- **Color Coding**: Green profit, red loss
- **Expandable Details**: Tap to see full session

---

## 5. Session Detail Screen

### Layout (Mobile)
```
┌─────────────────────────────────┐
│  ←  Session Details      [Edit]│ ← Admin only
├─────────────────────────────────┤
│  May 10, 2025                   │
│  📍 John's House                │
│  🎲 Texas Hold'em               │
│  ⏱ 4 hours • Started 7:00 PM   │
│  💰 Min buy-in: $50             │
│                                 │
│  ┌───────────────────────────┐ │
│  │  RESULTS                  │ │
│  │  ─────────────────────    │ │
│  │  [Avatar] Mike Smith      │ │
│  │  Buy-in: $150             │ │
│  │  Cash-out: $320           │ │
│  │  Profit: +$170 ✓          │ │ (green)
│  │  ─────────────────────    │ │
│  │  [Avatar] YOU             │ │ (highlighted)
│  │  Buy-in: $100             │ │
│  │  Cash-out: $250           │ │
│  │  Profit: +$150 ✓          │ │
│  │  ─────────────────────    │ │
│  │  [Avatar] Sarah Jones     │ │
│  │  Buy-in: $50              │ │
│  │  Cash-out: $30            │ │
│  │  Profit: -$20 ✗           │ │ (red)
│  │  ─────────────────────    │ │
│  │  ... 3 more players       │ │
│  │  [Expand All]             │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │  SESSION NOTES            │ │
│  │  Great game! Close finish │ │
│  │  between Mike and John.   │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │  SUMMARY                  │ │
│  │  Total buy-ins: $600      │ │
│  │  Total cash-outs: $600    │ │
│  │  Status: ✓ Balanced       │ │
│  └───────────────────────────┘ │
│                                 │
│  [Share Session] [Delete]      │ ← Admin only
│                                 │
└─────────────────────────────────┘
```

### Components
- **Header with Edit**: Context menu for admin
- **Session Metadata**: Icon-labeled info
- **Results Table**: Sortable player results
- **Player Row**: Avatar, name, financial details
- **Profit/Loss Indicator**: Visual distinction
- **Expandable List**: Show top players, expand for all
- **Notes Section**: Freeform text
- **Summary Panel**: Verification of totals
- **Action Buttons**: Share, delete (with confirmation)

---

## 6. Log New Session Flow (Admin)

### Step 1: Session Details
```
┌─────────────────────────────────┐
│  ✕  Log New Session       [1/3]│
├─────────────────────────────────┤
│                                 │
│  When did you play?             │
│  ┌──────────────────────────┐  │
│  │ 📅 May 12, 2025          │  │ ← Date picker
│  └──────────────────────────┘  │
│                                 │
│  ┌──────────────────────────┐  │
│  │ 🕐 7:00 PM               │  │ ← Time picker
│  └──────────────────────────┘  │
│                                 │
│  Where?                         │
│  ┌──────────────────────────┐  │
│  │ John's House         ▾   │  │ ← Dropdown
│  └──────────────────────────┘  │
│    [+ Add new location]         │
│                                 │
│  What game?                     │
│  ┌──────────────────────────┐  │
│  │ Texas Hold'em        ▾   │  │
│  └──────────────────────────┘  │
│                                 │
│  Minimum buy-in?                │
│  ┌──────────────────────────┐  │
│  │ $ 50                     │  │ ← Numeric input
│  └──────────────────────────┘  │
│                                 │
│  [ Save Draft ]    [ Next → ]  │
│                                 │
└─────────────────────────────────┘
```

### Step 2: Select Players
```
┌─────────────────────────────────┐
│  ←  Select Players        [2/3] │
├─────────────────────────────────┤
│  [Search players...]            │
│                                 │
│  Selected (4)                   │
│  ┌──────────────────────────┐  │
│  │ ☑ [Avatar] Mike Smith    │  │ (checked)
│  │ ☑ [Avatar] Sarah Jones   │  │
│  │ ☑ [Avatar] Tom Wilson    │  │
│  │ ☑ [Avatar] Lisa Chen     │  │
│  └──────────────────────────┘  │
│                                 │
│  Available Players              │
│  ┌──────────────────────────┐  │
│  │ ☐ [Avatar] Dave Brown    │  │ (unchecked)
│  │ ☐ [Avatar] Amy Lee       │  │
│  │ ☐ [Avatar] Jake Martin   │  │
│  └──────────────────────────┘  │
│                                 │
│  [+ Add new player]             │
│                                 │
│  [← Back]         [ Next → ]   │
│                                 │
└─────────────────────────────────┘
```

### Step 3: Enter Results
```
┌─────────────────────────────────┐
│  ←  Enter Results         [3/3] │
├─────────────────────────────────┤
│  Player 1 of 4                  │ ← Progress indicator
│                                 │
│  [Avatar]                       │
│  Mike Smith                     │
│                                 │
│  Buy-in                         │
│  ┌──────────────────────────┐  │
│  │ $ 150                    │  │ ← Large input
│  └──────────────────────────┘  │
│                                 │
│  Cash-out                       │
│  ┌──────────────────────────┐  │
│  │ $ 320                    │  │
│  └──────────────────────────┘  │
│                                 │
│  Profit/Loss:  +$170 ✓          │ ← Auto-calculated
│                                 │
│  Notes (optional)               │
│  ┌──────────────────────────┐  │
│  │ Won big pot at midnight  │  │
│  └──────────────────────────┘  │
│                                 │
│  ┌──────────────────────────┐  │
│  │ ⚠ Discrepancy Alert      │  │ ← Warning banner
│  │ Total in ≠ Total out     │  │   (if applicable)
│  │ Difference: $50          │  │
│  └──────────────────────────┘  │
│                                 │
│  [← Previous]    [ Next → ]    │
│                                 │
│  ••○○  (pagination dots)        │
│                                 │
└─────────────────────────────────┘
```

### Step 4: Review & Confirm
```
┌─────────────────────────────────┐
│  ←  Review Session              │
├─────────────────────────────────┤
│  May 12, 2025 • 7:00 PM         │
│  📍 John's House                │
│  🎲 Texas Hold'em ($50 min)     │
│                                 │
│  ┌───────────────────────────┐ │
│  │  Mike Smith               │ │
│  │  $150 → $320 (+$170) ✓    │ │
│  │  ─────────────────────    │ │
│  │  Sarah Jones              │ │
│  │  $50 → $30 (-$20) ✗       │ │
│  │  ─────────────────────    │ │
│  │  Tom Wilson               │ │
│  │  $100 → $100 (±$0)        │ │
│  │  ─────────────────────    │ │
│  │  Lisa Chen                │ │
│  │  $100 → $150 (+$50) ✓     │ │
│  └───────────────────────────┘ │
│                                 │
│  Summary:                       │
│  Buy-ins: $400                  │
│  Cash-outs: $400                │
│  Status: ✓ Balanced             │
│                                 │
│  Session notes (optional)       │
│  ┌──────────────────────────┐  │
│  │ Great night, close game  │  │
│  └──────────────────────────┘  │
│                                 │
│  [← Edit]      [ Submit ✓ ]    │
│                                 │
└─────────────────────────────────┘
```

### Step 5: Success Confirmation
```
┌─────────────────────────────────┐
│  ┌─────────────────────────┐   │
│  │ ✓ Success!              │   │ ← Green background
│  │ Session logged          │   │
│  └─────────────────────────┘   │
│                                 │
│   May 12, 2025                  │
│   4 players • $400 pot          │
│                                 │
│   Winner: Mike Smith (+$170)    │
│                                 │
│                                 │
│   [ View Session ]              │
│                                 │
│   [ Log Another Game ]          │
│                                 │
│   [ Back to Dashboard ]         │
│                                 │
└─────────────────────────────────┘
```

### Components
- **Multi-step Form**: Progress indicator
- **Form Fields**: Large touch targets
- **Smart Inputs**: Keyboard type optimized (numeric for currency)
- **Validation**: Real-time feedback
- **Discrepancy Warning**: Prominent alert
- **Swipe Navigation**: Gesture support between players
- **Quick Actions**: Duplicate previous, copy amounts
- **Draft Save**: Auto-save progress
- **Success Screen**: Simple confirmation with green banner

---

## 7. Profile Screen

### Layout (Mobile)
```
┌─────────────────────────────────┐
│  ←  Profile                     │
├─────────────────────────────────┤
│                                 │
│       [Avatar Image]            │ ← Large avatar
│         John Doe                │
│     Member since May 2024       │
│                                 │
│  ┌───────────────────────────┐ │
│  │  ✏ Edit Profile           │ │
│  ├───────────────────────────┤ │
│  │  🔔 Notifications         │ │
│  ├───────────────────────────┤ │
│  │  🔒 Privacy Settings      │ │
│  ├───────────────────────────┤ │
│  │  ⚙ Preferences            │ │
│  │     • Currency: USD       │ │
│  │     • Theme: Auto         │ │
│  ├───────────────────────────┤ │
│  │  ❓ Help & FAQ            │ │
│  ├───────────────────────────┤ │
│  │  📊 Export My Data        │ │
│  ├───────────────────────────┤ │
│  │  📱 About                 │ │
│  │     Version 1.0.0         │ │
│  └───────────────────────────┘ │
│                                 │
│  [Admin Panel]                  │ ← Admin only
│                                 │
│  [ Log Out ]                    │
│                                 │
└─────────────────────────────────┘
│  [Home] [Stats] [Board] [•••]  │
└─────────────────────────────────┘
```

### Edit Profile Screen
```
┌─────────────────────────────────┐
│  ←  Edit Profile         [Save] │
├─────────────────────────────────┤
│                                 │
│       [Avatar Image]            │
│       [ Change Photo ]          │
│                                 │
│  Display Name                   │
│  ┌──────────────────────────┐  │
│  │ John Doe                 │  │
│  └──────────────────────────┘  │
│                                 │
│  Email                          │
│  ┌──────────────────────────┐  │
│  │ john@example.com         │  │
│  └──────────────────────────┘  │
│                                 │
│  [ Change Password ]            │
│                                 │
│  [ Delete Account ]             │
│                                 │
└─────────────────────────────────┘
```

### Components
- **Avatar Upload**: Camera/gallery picker
- **Settings List**: Grouped menu items
- **Toggle Switches**: For binary preferences
- **Nested Navigation**: Sub-screens for detailed settings
- **Destructive Actions**: Confirmation modals

---

## 8. Empty States

### No Games Yet
```
┌─────────────────────────────────┐
│                                 │
│         🃏                      │
│                                 │
│    No Games Yet!                │
│                                 │
│    You'll see your stats here   │
│    once an admin logs a game    │
│    with you as a player.        │
│                                 │
│    Ready to play? Talk to       │
│    your game organizer!         │
│                                 │
└─────────────────────────────────┘
```

### No Results Found
```
┌─────────────────────────────────┐
│                                 │
│         🔍                      │
│                                 │
│    No Sessions Found            │
│                                 │
│    Try adjusting your filters   │
│    or search terms              │
│                                 │
│    [ Clear Filters ]            │
│                                 │
└─────────────────────────────────┘
```

### Loading State
```
┌─────────────────────────────────┐
│                                 │
│    ┌─────────────────────┐     │ ← Skeleton screens
│    │ ████████            │     │
│    │ ████    ████        │     │
│    └─────────────────────┘     │
│                                 │
│    ┌─────────────────────┐     │
│    │ ████████            │     │
│    │ ████    ████        │     │
│    └─────────────────────┘     │
│                                 │
└─────────────────────────────────┘
```

---

## 9. Component Library

### Core Components

**Buttons:**
- Primary: Filled, high emphasis
- Secondary: Outlined, medium emphasis
- Text: No border, low emphasis
- FAB: Floating action button (circular)
- Icon: Icon-only button

**Cards:**
- Elevated: Shadow, white background
- Outlined: Border, no shadow
- Filled: Colored background
- Interactive: Pressable with ripple

**Inputs:**
- Text field: Single line
- Text area: Multi-line
- Select/Dropdown: Single/multi-select
- Date picker: Calendar interface
- Time picker: Clock interface
- Currency input: Formatted with currency symbol

**Navigation:**
- Bottom nav: Primary navigation (4-5 items)
- Tab bar: Secondary navigation
- Drawer: Hamburger menu
- Breadcrumb: Hierarchical navigation

**Feedback:**
- Toast: Temporary message
- Snackbar: Actionable notification
- Alert: Modal dialog
- Loading spinner: Activity indicator
- Progress bar: Linear progress
- Skeleton: Content placeholder

**Data Display:**
- Table: Responsive, sortable
- List: Vertical scrolling
- Grid: Multi-column layout
- Chart: Line, bar, pie
- Badge: Notification count
- Chip: Compact element
- Avatar: User image/initials

**Overlays:**
- Modal: Full overlay
- Drawer: Slide-in panel
- Popover: Contextual overlay
- Tooltip: Hover information

---

## 10. Responsive Breakpoints

### Mobile (320px - 767px)
- Single column layout
- Bottom navigation
- Full-width cards
- Stacked forms
- Hamburger menu

### Tablet (768px - 1024px)
- Two-column layouts (where appropriate)
- Side navigation (optional)
- Grid views (2-3 columns)
- Larger touch targets maintained

### Desktop (1025px+)
- Multi-column layouts (3-4 columns)
- Sidebar navigation
- Hover states
- Keyboard shortcuts
- Data tables with more columns visible
- Larger charts with more detail

---

## 11. Design Tokens

### Colors
```
Primary: #2563EB (Blue)
Secondary: #10B981 (Green - for profits)
Error: #EF4444 (Red - for losses)
Warning: #F59E0B (Amber)
Success: #10B981 (Green)
Background: #FFFFFF
Surface: #F9FAFB
Text Primary: #111827
Text Secondary: #6B7280
Border: #E5E7EB
```

### Typography
```
Heading 1: 32px/40px, Bold
Heading 2: 24px/32px, Bold
Heading 3: 20px/28px, Semibold
Body Large: 18px/28px, Regular
Body: 16px/24px, Regular
Body Small: 14px/20px, Regular
Caption: 12px/16px, Regular

Font Family: System (SF Pro, Roboto, Helvetica)
```

### Spacing
```
xs: 4px
sm: 8px
md: 16px
lg: 24px
xl: 32px
2xl: 48px
3xl: 64px
```

### Borders
```
Radius: 8px (cards), 4px (buttons)
Width: 1px (standard), 2px (focus)
```

### Shadows
```
sm: 0 1px 2px rgba(0,0,0,0.05)
md: 0 4px 6px rgba(0,0,0,0.1)
lg: 0 10px 15px rgba(0,0,0,0.1)
```

---

## 12. Interaction Patterns

### Gestures (Mobile)
- **Swipe left/right**: Navigate between players in entry form
- **Pull to refresh**: Update data on list screens
- **Long press**: Show context menu (admin actions)
- **Swipe down**: Dismiss modal/drawer

### Micro-interactions
- **Button press**: Scale down slightly + ripple
- **Success**: Green banner with checkmark
- **Error**: Red banner with shake animation
- **Loading**: Skeleton -> Fade in content
- **Profit/Loss**: Color-coded with icons (↑↓)

### Animations
- **Page transitions**: Slide (300ms ease-out)
- **Modal**: Fade + scale (250ms)
- **Drawer**: Slide from side (300ms)
- **List items**: Stagger fade-in (50ms delay each)
- **Charts**: Draw animation (500ms)

---

## Next Steps

1. **Create High-Fidelity Mockups**: Design detailed UI in Figma/Sketch
2. **Build Interactive Prototype**: Test user flows with clickable prototype
3. **User Testing**: Validate with target users (poker players)
4. **Iterate Based on Feedback**: Refine UI/UX
5. **Create Component Library**: Design system for development
6. **Accessibility Audit**: Ensure WCAG compliance
7. **Technical Specifications**: Define API contracts and data models
