# Poker Stats - Frequently Asked Questions (FAQ)

---

## General Questions

### What is Poker Stats?

Poker Stats is a web application designed for casual poker groups to track game sessions, monitor individual performance, and view leaderboards. It's perfect for home games, weekly tournaments, or regular poker nights with friends.

### Who can use Poker Stats?

Anyone with an account! There are two types of users:
- **Casual Players**: Can view their own stats, game history, and leaderboards
- **Admins**: Can log sessions, manage players, and see all game data

### Is Poker Stats free?

The application is self-hosted, so there are no subscription fees. Your group runs it on your own server (like a Raspberry Pi).

### Do I need to download an app?

No! Poker Stats is a web application that runs in your browser. Just visit the website on any device - phone, tablet, or computer.

### Can I use Poker Stats on my phone?

Absolutely! The application is fully responsive and optimized for mobile devices. You can even add it to your home screen for an app-like experience.

---

## Account & Login

### How do I create an account?

1. Navigate to the Poker Stats website
2. Click "Sign Up"
3. Enter your email, name, and password
4. Click "Create Account"

### I forgot my password. How do I reset it?

1. Click "Forgot Password?" on the login page
2. Enter your email address
3. Check your email for reset instructions
4. Follow the link to create a new password

**Note**: If you don't receive an email, check your spam folder or contact your admin.

### Can I change my email address?

Yes! Go to Profile → Settings → Email and update it there.

### How do I delete my account?

Contact your admin. Account deletion requires admin intervention to preserve data integrity.

### Can I have multiple accounts?

No. One user account per person. If you accidentally created multiple accounts, contact your admin to merge them.

---

## Game Sessions

### How are sessions added?

Only admins can log game sessions. After each game, an admin enters:
- Session details (date, location, game type)
- All players who participated
- Buy-in and cash-out amounts for each player

### Why can't I add my own sessions?

To maintain data accuracy and prevent disputes. Having a single trusted admin log all sessions ensures consistency and fairness.

### When will I see a session I just played?

Immediately after the admin logs it! Stats update in real-time.

### Can I view sessions I didn't participate in?

No. For privacy, you can only see sessions you were part of.

### What if there's an error in a session?

Contact your admin. They can edit sessions to correct mistakes.

### How far back does session history go?

All sessions ever logged are preserved. There's no time limit on historical data.

---

## Statistics

### How is net profit calculated?

Simple addition across all your sessions:
```
Net Profit = Total Cash-Outs - Total Buy-Ins
```

Example:
- Session 1: +$50
- Session 2: -$20
- Session 3: +$30
- **Net Profit: $60**

### What is ROI and how is it calculated?

ROI (Return on Investment) measures efficiency:
```
ROI = (Net Profit / Total Buy-Ins) × 100%
```

Example:
- Total Buy-Ins: $1,000
- Net Profit: $200
- **ROI: 20%**

This means you make $20 profit for every $100 you invest.

### What is win rate?

The percentage of sessions where you had a profit:
```
Win Rate = (Winning Sessions / Total Sessions) × 100%
```

Example:
- Total Sessions: 10
- Winning Sessions: 6
- **Win Rate: 60%**

### Why is my ROI negative?

You've lost more money than you've invested. A negative ROI means you're currently down overall. This is normal in poker due to variance - keep playing and focus on improving!

### How many sessions do I need for accurate stats?

**Minimum**: 10 sessions for basic trends  
**Recommended**: 20+ sessions for meaningful statistics  
**Ideal**: 50+ sessions for reliable long-term indicators

Early statistics will be volatile due to small sample size.

### What's a good win rate?

It depends on your group, but generally:
- **30-40%**: Below average (but not bad for tough games)
- **40-50%**: Average
- **50-60%**: Above average
- **60%+**: Excellent

Remember: In poker, the goal is net profit, not just high win rate!

### What's a good ROI?

Again, it varies, but typical ranges:
- **0-10%**: Breakeven to slight profit
- **10-20%**: Good performance
- **20-30%**: Very good
- **30%+**: Excellent (or small sample size!)

### Why don't buy-ins match cash-outs in a session?

Common reasons:
- Dealer tips or host fees
- Food/drink expenses taken from pot
- Rounding differences
- Players leaving early with chips
- Data entry errors (rare)

Small discrepancies ($1-$5) are normal and acceptable.

---

## Leaderboards

### How are leaderboards calculated?

Leaderboards rank players by different metrics:
- **Total Profit**: Sum of all profit/loss
- **ROI %**: Return on investment percentage
- **Win Rate**: Percentage of winning sessions
- **Current Streak**: Consecutive wins/losses

### Why am I not on the leaderboard?

You need at least **5 sessions** to appear on leaderboards. This prevents skewed statistics from limited data.

### Can I see all-time leaderboards?

Yes! The default view shows all-time statistics. You can filter by date range if the feature is available.

### Why did my leaderboard position change?

Your position updates after every session logged. If others played and you didn't, they might have moved up. Keep playing to climb!

### Who can see the leaderboard?

All users can see the leaderboard, but it only includes players from sessions they've participated in. You can't see stats from games you weren't part of.

---

## Privacy & Security

### Who can see my stats?

**Everyone can see:**
- Your leaderboard position
- Your results in sessions they participated in
- Your profile information (name, picture)

**Only you can see:**
- Your complete session history
- Your detailed statistics
- Your profit/loss trends

**Admins can see:**
- All sessions (even ones you weren't in)
- All players' detailed stats

### Is my data secure?

Yes! Security measures include:
- 🔒 Encrypted HTTPS connections
- 🔑 Hashed passwords (not stored in plain text)
- 🛡️ Protection against common web attacks
- 💾 Regular automated backups

### Can I make my stats private?

No. Transparency is core to Poker Stats. All players can see leaderboards and shared session results. This promotes fair play and accountability.

### Can I download my data?

Yes! You can export your statistics as CSV from the Stats page.

---

## Technical Questions

### What browsers are supported?

Poker Stats works on all modern browsers:
- ✅ Chrome/Chromium
- ✅ Firefox
- ✅ Safari
- ✅ Edge
- ✅ Mobile browsers (iOS Safari, Chrome Mobile)

**Recommended**: Latest versions for best performance.

### Why is the app slow?

Possible causes:
- Slow internet connection
- Server under heavy load
- Many sessions being processed
- Browser cache needs clearing

Try:
1. Refresh the page (Ctrl/Cmd + R)
2. Clear browser cache
3. Wait a moment and try again
4. Contact admin if persistent

### Can I use Poker Stats offline?

Limited offline functionality:
- ✅ View cached data
- ❌ Cannot log new sessions
- ❌ Cannot update profile
- ❌ Stats won't update

You need an internet connection for full functionality.

### How do I report a bug?

1. Note what you were doing when the bug occurred
2. Take a screenshot if applicable
3. Contact your admin or email support@pokerstats.local
4. Or file a GitHub issue if you're technical

---

## Features

### Can I add notes to sessions?

Admins can add notes when logging sessions. These appear in session details.

### Can I filter my session history?

Yes! Use filters for:
- Date range
- Location
- Specific players
- Min/max profit

### Can I compare my stats with a specific player?

Not directly in the current version, but you can:
- View shared session results
- Compare leaderboard positions
- Track head-to-head records manually

### Are there achievements or badges?

This feature is planned for a future update! Stay tuned.

### Can I set profit/loss goals?

Currently, you track goals manually. Goal-setting features may be added in future updates.

---

## Troubleshooting

### I can't log in

Possible solutions:
1. Verify your email and password are correct
2. Check Caps Lock isn't on
3. Try password reset if you forgot it
4. Clear browser cache and cookies
5. Contact your admin

### Sessions aren't showing up

Possible causes:
- Filters are active (check filter settings)
- Session was saved as draft (admins only)
- Cache needs refreshing (reload page)
- Session deletion (contact admin)

### My stats seem wrong

First, verify:
1. Are you looking at the right date range?
2. Are filters applied?
3. Do the individual session results look correct?

If still incorrect, contact your admin with specific examples.

### Charts won't load

Try:
1. Refresh the page
2. Clear browser cache
3. Try a different browser
4. Check internet connection
5. Contact support if persistent

### Profile picture won't upload

Requirements:
- **File format**: JPG, PNG, or GIF
- **Max size**: 5 MB
- **Recommended**: Square image, at least 200x200 pixels

If it still fails:
- Try a smaller file
- Try a different image
- Check your internet connection

---

## Miscellaneous

### What currency does Poker Stats use?

**Default**: PLN (Polish Zloty)

The currency is configured by the system administrator. All amounts in the system use the same currency.

### Can I track multiple poker groups?

Currently, Poker Stats is designed for one group per installation. For multiple groups, you'd need separate instances.

### Can we use this for tournaments?

Yes! Log tournament results as:
- **Buy-in**: Tournament entry fee
- **Cash-out**: Prize won (or 0 if eliminated)
- Add a note specifying tournament format

### What about cash games vs tournaments?

The app treats everything as cash game equivalents (buy-in and cash-out). You can note the format in session notes.

### Can we split the pot (multiple winners)?

Each player records their individual result. If multiple players profit, that's fine - just record accurate buy-in and cash-out for each.

### How do rake/tips work?

Record the actual amounts players bought in and cashed out. If there's a rake or tips, this will create a natural discrepancy (total cash-outs slightly less than total buy-ins). This is normal!

---

## Getting More Help

### Still have questions?

**For users:**
- Contact your admin
- Check the [User Onboarding Guide](USER_ONBOARDING.md)
- Email: support@pokerstats.local

**For admins:**
- Check the [Admin Guide](ADMIN_GUIDE.md)
- Check the [Troubleshooting Guide](TROUBLESHOOTING.md)
- Email: admin-support@pokerstats.local

**Report bugs:**
- GitHub Issues: [Report here](https://github.com/yourorg/pokerstats/issues)

---

## Feature Requests

Have an idea for a new feature? We'd love to hear it!

1. Check if it's already suggested in GitHub Issues
2. Open a new "Feature Request" issue
3. Describe your idea and why it would be useful
4. Be patient - features are prioritized based on demand and feasibility

---

**Version**: 1.0.0  
**Last Updated**: October 2025  
**Contributors**: Poker Stats Team

---

*This FAQ is continuously updated. If you have a question that's not answered here, please let us know!*
