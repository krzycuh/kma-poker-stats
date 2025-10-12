# Poker Stats Web App - Functionality Design

## Overview
A home game poker statistics tracking and sharing platform designed for casual poker groups to log sessions, track performance, and view leaderboards.

---

## 1. User Roles & Permissions

### Casual Player
**Capabilities:**
- View own statistics and game history
- View sessions they participated in
- View leaderboards (filtered to games they played in)
- Update own profile (name, avatar)
- View other players' public stats (from shared sessions)

**Restrictions:**
- Cannot add new sessions
- Cannot edit session data
- Cannot see sessions they didn't participate in

### Admin
**Capabilities:**
- All Casual Player capabilities
- Create new game sessions
- Edit existing sessions (correct errors, add late entries)
- Add/remove players from sessions
- Manage player accounts
- View all sessions (global access)
- Export data
- Generate reports

**Notes:**
- A player can be both a casual player and admin
- Multiple admins can exist
- Admin status is global (all admins have full access to all sessions)

---

## 2. Core Data Entities

### Game Session
```
- Session ID (unique)
- Date/Time (start)
- Duration (optional, or calculate from start/end)
- Location (text: "John's House", "Club XYZ")
- Min Buy-in (currency amount)
- Game Type (Texas Hold'em, Omaha, Mixed, etc.)
- Created by (admin user ID)
- Created at (timestamp)
- Updated at (timestamp)
- Notes (optional text field)
```

### Player
```
- Player ID (unique)
- Name
- User ID (foreign key, nullable - links to app user account if player has one)
- Avatar URL (optional)
- Created Date
- Notes (optional)
```

### User (App Account)
```
- User ID (unique)
- Email (for login)
- Password (hashed)
- Role (casual_player, admin)
- Join Date
- Preferences (notifications, settings)
```

**Note:** A Player entity represents someone who plays in games. A User entity represents someone with an app account. Players can exist without Users (guest players tracked by name only). Users are linked to Players via the User ID field.

### Session Result (Player Performance in Session)
```
- Result ID (unique)
- Session ID (foreign key)
- Player ID (foreign key)
- Buy-in Amount (total money brought)
- Cash-out Amount (total money taken out)
- Profit/Loss (calculated: cash-out - buy-in)
- Rebuys Count (optional)
- Notes (optional: "Won big pot at 2am")
```

### Derived Stats (Calculated, not stored)
```
For each player:
- Total Sessions Played
- Total Buy-in (sum)
- Total Cash-out (sum)
- Net Profit/Loss (sum of all P/L)
- ROI % (net profit / total buy-in * 100)
- Win Rate (sessions won / total sessions)
- Biggest Win
- Biggest Loss
- Average Session Profit
- Current Streak (wins/losses)
```

---

## 3. Key Features

### 3.1 Dashboard / Home View

**For Casual Players:**
- Quick stats card (personal overall stats)
- Recent sessions list (last 5-10 games)
- Current position on leaderboard
- Trend indicator (up/down arrow with percentage)
- Quick action: "View All My Games"

**For Admins:**
- All of the above, plus:
- Quick action button: "Log New Session"
- Recent sessions across all games
- System stats (total sessions, active players)

### 3.2 Stats View

**Personal Stats Page:**
- Overall Performance Card:
  - Total sessions
  - Net profit/loss (prominent)
  - ROI %
  - Win rate
- Performance chart (profit over time)
- Best/Worst sessions
- Session breakdown table

**Group/Leaderboard Stats:**
- Filterable leaderboards:
  - All-time profit
  - ROI %
  - Win rate
  - Most sessions played
  - Current streak
- Only shows players from sessions user participated in
- Visual indicators (medals, badges)

### 3.3 Game Logging (Admin Only)

**Create New Session Form:**
- Date picker (default: today)
- Location (dropdown of recent + add new)
- Min buy-in amount
- Game type (dropdown)
- Player selection (multi-select from active players)
- For each player:
  - Buy-in amount
  - Cash-out amount
  - Optional notes
- Session notes
- Save draft feature (for incomplete entries)

**Validation:**
- At least 2 players required
- Buy-in/cash-out must be non-negative
- Date cannot be in future
- Alert if buy-ins don't match cash-outs (common error check)

### 3.4 Session History / Timeline

**Features:**
- Chronological list of all sessions (user has access to)
- Filters:
  - Date range
  - Location
  - Players (multi-select)
  - Min profit/loss threshold
- Sort options:
  - Date (newest/oldest)
  - Personal profit (high/low)
  - Session size (number of players)
- Each session card shows:
  - Date, location
  - Players count
  - User's result (profit/loss highlighted)
  - Quick actions: View details

**Detail View:**
- Full session information
- All players' results (sortable table)
- Session notes
- Edit button (admin only)

### 3.5 Charts & Visualizations

**Personal Charts:**
- Profit/Loss over time (line chart)
- Win/Loss distribution (pie chart)
- Performance by location (bar chart)
- Performance by day of week/month
- Session size vs. profitability
- Cumulative profit curve

**Group Charts:**
- Leaderboard progression (animated line chart)
- Group activity heatmap (when games are played)

### 3.6 Additional Features

**Achievements/Badges:**
- "First Win"
- "5 Game Streak"
- "Biggest Winner"
- "Most Consistent" (lowest variance)
- "Comeback Kid" (biggest turnaround)

**Social Features:**
- Share individual session results (screenshot/link)
- Add comments to sessions
- React to results (emoji reactions)

**Profile Management:**
- Update avatar
- Edit name
- Set privacy preferences
- Notification settings

---

## 4. User Flows

### 4.1 New User Onboarding

**Flow:**
1. **Landing Page** (if not logged in)
   - Brief explanation of app
   - "Sign Up" or "Log In" buttons

2. **Sign Up**
   - Enter email
   - Choose password
   - Enter name
   - Upload avatar (optional)
   - Accept terms

3. **Welcome Screen**
   - Explain what they can do
   - "Your stats are empty - join a game to start!"
   - If invited by admin: show pending game invitation

4. **Empty State Dashboard**
   - Friendly message: "Welcome! You'll see your stats here once an admin logs a game with you."
   - Show example/demo of what dashboard will look like

5. **First Game Logged (Admin adds them)**
   - Notification: "You've been added to a game!"
   - Dashboard populates with first data point
   - Gentle prompt to explore features

### 4.2 Adding a New Game Session (Admin)

**Flow:**
1. **Trigger:**
   - Click "Log New Session" button from dashboard
   - Or navigate to "Sessions" → "Add New"

2. **Session Details Screen:**
   - Form with session metadata:
     - Date/time (default: now)
     - Location (dropdown + add new)
     - Min buy-in
     - Game type
   - "Next" button

3. **Player Selection Screen:**
   - List of all active players with checkboxes
   - Search/filter players
   - "Add new player" quick link
   - Shows selected count
   - "Next" button

4. **Results Entry Screen:**
   - For each selected player:
     - Player name/avatar
     - Buy-in input field
     - Cash-out input field
     - Auto-calculated profit/loss (shown immediately)
     - Optional notes field
   - Summary panel (sticky):
     - Total buy-ins
     - Total cash-outs
     - Discrepancy warning (if mismatch)
   - Session notes (global)
   - "Save Draft" button
   - "Submit" button

5. **Review & Confirm:**
   - Summary of session
   - List of results
   - "Looks good? Submit"
   - Or "Go back to edit"

6. **Success Confirmation:**
   - "Session logged successfully!"
   - Quick actions:
     - View session
     - Log another session
     - Back to dashboard

7. **Optional: Post-Submit Edit:**
   - Admin can edit within reasonable timeframe
   - Edit history tracked

### 4.3 Viewing Stats

**Personal Stats Flow:**
1. **Entry Points:**
   - "My Stats" from navigation
   - Click on stat card from dashboard
   - Profile dropdown → "My Performance"

2. **Stats Overview Page:**
   - Hero section: Key metrics at top
   - Scrollable sections:
     - Performance chart
     - Session history table
     - Achievements

3. **Deep Dive:**
   - Click on any session in table → detail view
   - Click on chart data point → show sessions from that period

**Group Stats Flow:**
1. **Entry Point:**
   - "Leaderboard" from navigation

2. **Leaderboard Page:**
   - Metric selector (toggle between ROI, Profit, Win Rate)
   - Top 10 prominent, rest in expandable list
   - User's position highlighted
   - Click on any player → see their public profile/stats (filtered to shared sessions)

---

## 5. UI/UX Design Priorities

### Mobile-First Approach

**Key Principles:**
- Touch-friendly targets (min 44px)
- Single column layouts
- Thumb-zone navigation
- Progressive disclosure (show essentials, hide advanced)
- Offline-capable (view cached data)

**Screen Sizes:**
- Mobile: 320px - 767px (primary)
- Tablet: 768px - 1024px
- Desktop: 1025px+ (enhanced)

### Quick Data Entry

**For Admin Game Logging:**
- Smart defaults (pre-fill common values)
- Save common locations/settings
- Duplicate last session feature
- Voice input consideration (future)
- Bulk entry mode (for multiple sessions)
- Clear visual feedback (autosave indicators)
- Keyboard shortcuts (desktop)
- Persistent drafts (resume later)

**Form Optimizations:**
- Numeric keypad for currency
- Autocomplete player names
- Swipe gestures to quickly navigate between players
- Copy previous session template

### Post-Game Entry Focus

**Immediate Post-Game:**
- Admin can log session right after game ends
- Players gather around to verify results
- Quick validation ("Does everyone agree?")
- Instant gratification: see updated stats immediately

**Design Considerations:**
- Large, easy-to-read fonts (tired eyes after long game)
- Clear contrast (might be late at night)
- Error prevention (confirmation for large amounts)
- Print/share summary option

### General UX Patterns

**Navigation:**
- Bottom tab bar (mobile): Home | Stats | Leaderboard | Profile
- Admin has additional "+" FAB for quick session logging
- Hamburger menu for secondary options

**Visual Design:**
- Card-based layouts
- Green for profit, red for loss
- Skeleton screens while loading
- Empty states with helpful messaging
- Consistent iconography

**Feedback & Confirmations:**
- Success messages with visual feedback
- Loading states for all async operations

---

## 6. Information Architecture

### Navigation Structure

```
├── Home (Dashboard)
│
├── My Stats
│   ├── Overview
│   ├── Session History
│   ├── Charts
│   └── Achievements
│
├── Leaderboard
│   ├── By Profit
│   ├── By ROI
│   ├── By Win Rate
│   └── By Streak
│
├── Sessions (Admin)
│   ├── All Sessions
│   ├── Add New Session
│   └── Manage Players
│
├── Profile
│   ├── Edit Profile
│   ├── Settings
│   ├── Notifications
│   └── Logout
│
└── Help/About
    ├── How to Use
    ├── FAQ
    └── Contact Support
```

---

## 7. Future Considerations (Phase 2+)

- Expense splitting (food, drinks)

---

## 8. Open Questions for Refinement

1. **Authentication:**
   - Phase 1: Email/password only
   - Future: Social login (Google, Apple)
   - No guest access

2. **Player Management:**
   - Players cannot delete their own accounts
   - Admins cannot deactivate players
   - All historical data is permanent

3. **Data Privacy:**
   - Players cannot hide their stats
   - All session results are visible to all participants

4. **Currency:**
   - Single currency only: PLN (Polish Zloty)
   - No display formatting preferences

5. **Session Editing:**
   - No time limit for edits
   - No audit log for changes
   - No player notifications when session is edited

6. **Invitations:**
   - Only admins can invite new users to the app
   - Players in sessions can be associated with app users OR be non-user players (tracked by name only)
   - Not all players need app accounts to have their stats tracked

7. **Performance:**
   - No pagination (load all sessions)
   - No archiving of old sessions

---

## Next Steps

1. Review and validate this functionality design
2. Create low-fidelity wireframes for key screens
3. Design UI mockups for mobile (primary) and desktop
4. Validate with potential users
5. Move to technical architecture design
