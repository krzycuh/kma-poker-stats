# Poker Stats - Admin Guide 📋

This guide is for administrators who manage players and log game sessions in Poker Stats.

---

## Table of Contents

- [Admin Responsibilities](#admin-responsibilities)
- [Logging a New Session](#logging-a-new-session)
- [Managing Players](#managing-players)
- [Editing Sessions](#editing-sessions)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)
- [FAQs](#faqs)

---

## Admin Responsibilities

As an admin, you are responsible for:

1. **Logging game sessions** accurately and promptly
2. **Managing player accounts** (adding new players, updating information)
3. **Correcting errors** in session data when mistakes happen
4. **Ensuring data integrity** (balancing buy-ins and cash-outs)
5. **Helping users** with questions about their stats

**Important**: With great power comes great responsibility. Players trust you to record accurate data!

---

## Logging a New Session

### When to Log

**Best Practice**: Log sessions immediately after they end, while everyone is present.

**Why?**
- Fresh memory = accurate data
- Players can verify their results
- Prevents disputes later
- Real-time stats updates

### Step-by-Step Guide

#### Step 1: Access Session Logging

From the dashboard:
- Click the **"+ Log Session"** floating button (bottom right)

OR from the navigation:
- Click **"Sessions"** → **"Add New Session"**

#### Step 2: Enter Session Details

Fill in the following information:

**Date & Time:**
- Default: Current date/time
- Adjust if logging a past session
- Format: YYYY-MM-DD HH:MM

**Location:**
- Select from dropdown of recent locations
- Or click **"+ Add New"** to create a new location
- Examples: "John's House", "Club Downtown", "Mike's Garage"

**Min Buy-In:**
- The minimum buy-in for the session
- Used for reference only
- Currency: PLN (Polish Zloty)

**Game Type:**
- Select from dropdown:
  - Texas Hold'em
  - Omaha
  - Mixed Games
  - Other

**Session Notes** (Optional):
- Add any relevant notes
- Examples: "Tournament format", "Crazy night!", "Bad beat jackpot"

Click **"Next"** to continue.

#### Step 3: Select Players

**Select at least 2 players** (required minimum):

- Use checkboxes to select players who participated
- Use the search box to quickly find players
- Selected count shows at the top

**Adding a New Player:**
- Click **"+ Add New Player"**
- Enter name
- Optionally link to a user account
- Click "Save"

Click **"Next"** to continue.

#### Step 4: Enter Results for Each Player

For each selected player, enter:

**Buy-In Amount:**
- Total money the player bought in for
- Include all re-buys
- Example: Initial 100 + Re-buy 50 = **150**

**Cash-Out Amount:**
- Total money the player cashed out
- What they left with
- Example: **220**

**Profit/Loss:**
- Automatically calculated
- Shown in real-time as you type
- Green = Profit, Red = Loss

**Notes** (Optional):
- Player-specific notes
- Examples: "Won big pot at midnight", "Left early"

**Navigation:**
- Use **"Previous"** / **"Next"** buttons to navigate between players
- Or swipe left/right on mobile

#### Important: Balance Check

At the bottom, you'll see a **summary panel**:
```
Total Buy-Ins:   $600
Total Cash-Outs: $595
Discrepancy:     -$5  ⚠️
```

**What discrepancies mean:**
- **0**: Perfect balance (rare but ideal)
- **Small (<5)**: Acceptable (tips, rounding, dealer fee)
- **Large (>10)**: Likely data entry error - double-check!

**Common causes:**
- Dealer tips
- Host fee
- Leftover chips
- Data entry typo (most common!)

Click **"Next"** to continue.

#### Step 5: Review and Submit

**Review all information:**
- Session details
- All player results
- Total buy-ins and cash-outs
- Any discrepancies

**If everything looks correct:**
- Click **"Submit"**

**If you need to make changes:**
- Click **"Edit"** next to the section to modify
- Or click **"Back"** to navigate to previous steps

#### Step 6: Success!

After submission:
- ✅ Session is saved
- ✅ All players' stats are updated immediately
- ✅ Leaderboard is recalculated

**Quick Actions:**
- **View Session**: See the session you just logged
- **Log Another**: Start logging a new session
- **Back to Dashboard**: Return to main page

---

## Managing Players

### Adding a New Player

1. Navigate to **"Players"** → **"Add Player"**
2. Enter player information:
   - **Name**: Full name (required)
   - **User Link**: Optional - link to existing user account
   - **Avatar**: Optional - upload profile picture
   - **Notes**: Optional - any relevant notes
3. Click **"Save"**

### Editing Player Information

1. Navigate to **"Players"**
2. Find the player (use search if needed)
3. Click **"Edit"** (pencil icon)
4. Update information
5. Click **"Save Changes"**

### Linking Players to Users

**Why link?**
- Players can see their own stats
- They can log in and track performance
- Keeps data organized

**How to link:**
1. Edit the player
2. In **"User Account"** dropdown, select the user
3. Click **"Save"**

**Note**: A player can only be linked to ONE user account.

### Managing Inactive Players

**Deactivating a player:**
1. Edit the player
2. Toggle **"Active"** to OFF
3. Click **"Save"**

**Effect:**
- Player won't appear in player selection when logging sessions
- Historical data is preserved
- Can be reactivated anytime

---

## Editing Sessions

### When to Edit

Edit sessions to:
- Correct data entry errors
- Update results after discovering a mistake
- Add missing player notes
- Fix typos

**Important**: Edit responsibly. Changes affect all players' stats!

### How to Edit

1. Navigate to **"Sessions"**
2. Find the session to edit
3. Click on the session to view details
4. Click **"Edit Session"** button (admin only)
5. Make your changes
6. Click **"Save Changes"**

### What You Can Edit

- ✅ Session details (date, location, game type)
- ✅ Player results (buy-in, cash-out)
- ✅ Session notes
- ✅ Add/remove players (with caution!)
- ❌ Cannot change session ID

### Edit History

- All edits are tracked
- "Last edited" timestamp shown
- Consider adding a note explaining significant changes

---

## Best Practices

### 1. Accuracy is Key

✅ **DO:**
- Double-check all amounts before submitting
- Ask players to verify their results
- Log immediately after the game ends
- Keep good notes during the game

❌ **DON'T:**
- Rush through data entry
- Estimate buy-ins/cash-outs
- Log sessions days later without records
- Ignore large discrepancies

### 2. Communication

✅ **DO:**
- Announce when you're logging the session
- Share final results with all players
- Respond to questions about stats
- Explain any corrections you make

❌ **DON'T:**
- Log sessions without player knowledge
- Ignore player concerns about accuracy
- Make edits without explanation

### 3. Consistency

✅ **DO:**
- Use consistent location names
- Log all sessions, not just big wins
- Include all players who participated
- Use the same naming conventions

❌ **DON'T:**
- Skip logging sessions
- Use different names for same location
  - ❌ "John's Place", "John's House", "Johns"
  - ✅ "John's House" (pick one and stick with it)

### 4. Data Integrity

✅ **DO:**
- Verify total buy-ins match total cash-outs (within reason)
- Investigate discrepancies > $10
- Keep notes on unusual situations
- Run periodic data checks

❌ **DON'T:**
- Ignore large balance discrepancies
- Fudge numbers to make them balance
- Delete sessions to "fix" stats

### 5. Timely Logging

**Log sessions within 24 hours (preferably immediately)**

Benefits:
- ✅ Fresh memory
- ✅ Players can verify
- ✅ Real-time stats
- ✅ Fewer disputes

### 6. Save Drafts

If you need to leave mid-logging:
- Click **"Save Draft"**
- Resume later from "Sessions" → "Drafts"

---

## Common Scenarios

### Scenario 1: Player Leaves Early

**Solution:**
- Record their final buy-in and cash-out at time of leaving
- Add a note: "Left at 11 PM"
- This is accurate data - don't estimate what they "would have" won/lost

### Scenario 2: Late Arrival

**Solution:**
- Only record the buy-in/cash-out for the time they actually played
- Note their arrival time if relevant
- Don't include them if they didn't play

### Scenario 3: Missing Cash-Out Information

**Solution:**
- Ask the player before they leave!
- If impossible, make best estimate based on chip count
- Add note: "Approximate - left before count"
- Correct later if you get accurate information

### Scenario 4: Shared Buy-In

**Example**: Two players split a buy-in

**Solution:**
- Record each player individually with their actual stake
- Example:
  - Player A: Buy-in $50, Cash-out $100 → Profit $50
  - Player B: Buy-in $50, Cash-out $100 → Profit $50
- Not: Single entry for $100

### Scenario 5: Tournament Format

**Solution:**
- Still log as cash game equivalents
- Buy-in = Tournament entry
- Cash-out = Prize money
- Add note: "Tournament - 10 players, top 3 paid"

### Scenario 6: Discovered Error Days Later

**Solution:**
1. Edit the session
2. Correct the error
3. Add a note explaining the correction
4. Notify affected players
5. Stats will recalculate automatically

---

## Troubleshooting

### Problem: Can't Submit Session

**Possible causes:**
- Less than 2 players selected → Add more players
- Missing required fields → Fill in all required information
- Network error → Check internet connection, try again
- Server error → Wait a moment and retry

### Problem: Player Not Appearing in List

**Possible causes:**
- Player is marked inactive → Reactivate in player management
- Player was deleted → Re-create the player
- Cache issue → Refresh the page

### Problem: Discrepancy Too Large

**Solution:**
1. Double-check all buy-in amounts
2. Verify all cash-out amounts
3. Account for tips, dealer fees, etc.
4. If still large, investigate each player individually
5. Most common: Decimal point error (220 instead of 22.0)

### Problem: Session Not Appearing

**Possible causes:**
- Session was saved as draft → Check "Drafts" section
- Filter is hiding it → Clear all filters
- Date is in the future → Check date field
- Submission actually failed → Check for error message, resubmit

---

## FAQs

### How long should logging a session take?

**Typical time: 3-5 minutes** for a session with 6 players if you're prepared.

**Tips to speed up:**
- Have notes ready (write down results during game)
- Use "Duplicate Last Session" for regular games
- Pre-select common location

### Can I bulk import sessions?

Not currently. Each session must be logged individually to ensure accuracy.

### What if two admins log the same session?

**Prevention:**
- Designate one admin to log each session
- Check recent sessions before logging
- If duplicate created, delete one (soft delete)

### Can I delete a session?

Sessions are **soft-deleted**:
- They won't appear in lists
- Stats are recalculated without them
- Can be restored if needed
- Historical data is preserved

### How do I handle cash games vs tournaments?

**Cash Games:**
- Log normally (buy-in and cash-out)

**Tournaments:**
- Buy-in = Entry fee
- Cash-out = Prize won (or 0 if eliminated)
- Add note specifying tournament format

### What's the maximum session size?

**Technical limit**: 100 players (unlikely to reach!)
**Practical limit**: 20-30 players

For very large tournaments, consider:
- Only tracking final table
- Creating multiple sessions
- Using dedicated tournament software

### Can players see edit history?

Currently no. Only admins see "Last edited" timestamp.

### What if a player disputes their results?

1. Listen to their concern
2. Check your records/notes
3. Review the session details
4. Make corrections if they're right
5. Explain if data is accurate
6. Add notes for transparency

---

## Keyboard Shortcuts (Desktop)

Speed up logging with these shortcuts:

- `Ctrl/Cmd + N`: New session
- `Ctrl/Cmd + S`: Save draft
- `Tab`: Navigate between fields
- `Enter`: Submit form / Next step
- `Esc`: Cancel / Go back

---

## Tips from Experienced Admins

### "Keep a notepad during games"

Write down final buy-in/cash-out for each player as the game ends. Takes 30 seconds, saves 10 minutes of trying to remember later.

### "Use consistent location names"

Create a standard list of location names and stick to them. Makes filtering much easier.

### "Log immediately"

Don't wait until the next day. Memory fades, disputes arise, and you'll forget small details.

### "Screenshots are your friend"

Take a photo of final chip stacks before breaking them down. Evidence if there's a dispute.

### "Communicate changes"

If you edit a session, tell affected players. Transparency builds trust.

---

## Getting Help

**Need assistance?**
- Email: admin-support@pokerstats.local
- Check: [Troubleshooting Guide](TROUBLESHOOTING.md)
- Contact: System administrator

**Found a bug?**
- Report it: GitHub Issues
- Include: Steps to reproduce, expected vs actual behavior
- Screenshots help!

---

## Conclusion

Thank you for being an admin! Your accurate and timely logging makes Poker Stats valuable for everyone.

**Remember:**
- Accuracy > Speed
- Communication is key
- When in doubt, ask players to verify
- Document unusual situations

**Happy logging!** 🎰📊

---

**Version**: 1.0.0  
**Last Updated**: October 2025
