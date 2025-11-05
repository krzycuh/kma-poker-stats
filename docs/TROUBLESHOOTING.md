# Poker Stats - Troubleshooting Guide

This guide helps you diagnose and resolve common issues with Poker Stats.

---

## Quick Diagnosis

**Before diving deep, try these quick fixes:**

1. **Refresh the page** (Ctrl/Cmd + R or F5)
2. **Clear browser cache** (Ctrl/Cmd + Shift + Delete)
3. **Try incognito/private mode**
4. **Check internet connection**
5. **Try a different browser**
6. **Wait 5 minutes and try again** (server might be restarting)

If quick fixes don't work, proceed with specific troubleshooting below.

---

## Table of Contents

- [Login Issues](#login-issues)
- [Display Issues](#display-issues)
- [Performance Issues](#performance-issues)
- [Data Issues](#data-issues)
- [Admin-Specific Issues](#admin-specific-issues)
- [Server/Infrastructure Issues](#serverinfrastructure-issues)
- [Getting Help](#getting-help)

---

## Login Issues

### Cannot Log In - "Invalid credentials"

**Symptoms**: Login fails with error message

**Possible causes:**
1. Incorrect email or password
2. Caps Lock is on
3. Account doesn't exist
4. Account was deactivated

**Solutions:**
```
1. Verify email address is correct
   - Check for typos
   - Confirm it's the email you registered with

2. Check password
   - Verify Caps Lock is OFF
   - Ensure you're using the correct password
   - Try password reset if unsure

3. Reset password
   - Click "Forgot Password?"
   - Enter your email
   - Check inbox (and spam!) for reset email
   - Follow instructions

4. Contact admin
   - If none of the above work
   - Provide: Your name, email, approximate registration date
```

### Password Reset Email Not Received

**Symptoms**: No email after requesting password reset

**Solutions:**
```
1. Check spam/junk folder
   - Email might be filtered

2. Wait 5-10 minutes
   - Email delivery can be delayed

3. Verify email address
   - Ensure you entered the correct email
   - Try again with correct email

4. Check email provider status
   - Your email service might be down

5. Contact admin
   - They can manually reset your password
```

### Session Expires Immediately

**Symptoms**: Logged out right after logging in

**Possible causes:**
- Browser not accepting cookies
- System time incorrect
- Session storage issue

**Solutions:**
```
1. Enable cookies
   - Check browser settings
   - Allow cookies from your Poker Stats domain

2. Check system time
   - Ensure date/time is correct on your device
   - JWT tokens are time-sensitive

3. Clear browser data
   - Clear cookies and cache
   - Try logging in again

4. Try different browser
   - Test if issue is browser-specific
```

---

## Display Issues

### Page Won't Load / Blank Screen

**Symptoms**: White/blank screen or infinite loading

**Solutions:**
```
1. Check internet connection
   - Try accessing another website
   - Restart router if needed

2. Clear browser cache
   - Ctrl/Cmd + Shift + Delete
   - Clear cached images and files
   - Reload page

3. Disable browser extensions
   - Ad blockers can interfere
   - Try incognito/private mode
   - Disable extensions one by one

4. Check browser console (Advanced)
   - F12 to open Developer Tools
   - Look for errors in Console tab
   - Share errors with support
```

### Charts Not Displaying

**Symptoms**: Graphs/charts show as empty or don't load

**Solutions:**
```
1. Wait for data to load
   - Charts load after data is fetched
   - Wait 5-10 seconds

2. Check if you have data
   - Need at least 2 sessions for most charts
   - Some charts require 5+ sessions

3. Clear cache and reload
   - Cached data might be corrupted

4. Try different date range
   - Ensure selected range has data

5. Browser compatibility
   - Update to latest browser version
   - Try Chrome or Firefox
```

### Images Not Loading

**Symptoms**: Profile pictures or icons missing

**Solutions:**
```
1. Check internet connection
   - Images might not download on slow connection

2. Disable VPN/Proxy
   - Can interfere with image loading

3. Clear image cache
   - Browser settings → Clear cached images

4. Check browser extensions
   - Some extensions block images
```

### Layout Broken on Mobile

**Symptoms**: Text overlapping, buttons cut off

**Solutions:**
```
1. Rotate device
   - Try portrait and landscape mode

2. Zoom reset
   - Pinch to reset zoom to 100%

3. Clear mobile browser data
   - Settings → Clear browsing data

4. Update mobile browser
   - App Store / Play Store

5. Try different mobile browser
   - Chrome, Safari, Firefox
```

---

## Performance Issues

### App is Slow

**Symptoms**: Pages take long to load, actions are delayed

**Diagnosis:**
```
Check what's slow:
- [ ] Initial page load
- [ ] Navigating between pages
- [ ] Loading stats/charts
- [ ] Submitting forms
```

**Solutions:**
```
1. Check internet speed
   - Run speed test (speedtest.net)
   - Required: 2+ Mbps download

2. Close other tabs/apps
   - Free up system resources

3. Clear browser cache
   - Old cached data can slow things down

4. Check server load (Admin)
   - Query `/actuator/metrics` for resource usage snapshots
   - Check if server is under heavy load

5. Optimize session history
   - Limit date range when viewing sessions
   - Use filters to reduce data loaded
```

### Slow After Many Sessions

**Symptoms**: App was fast initially, now slow

**For Admins:**
```
1. Database optimization
   bash scripts/verify-data-integrity.sh
   - Runs VACUUM ANALYZE

2. Check disk space
   df -h
   - Ensure sufficient space

3. Restart services
   docker-compose -f docker-compose.prod.yml restart

4. Check logs
   docker-compose -f docker-compose.prod.yml logs backend
   - Look for slow query warnings
```

---

## Data Issues

### Sessions Not Appearing

**Symptoms**: Recent session doesn't show in history

**Diagnosis checklist:**
```
- [ ] Was the session actually submitted? (Check with admin)
- [ ] Are you looking at the right player?
- [ ] Are filters hiding it?
- [ ] Is date range correct?
```

**Solutions:**
```
1. Clear filters
   - Remove all filters from session list
   - Check "All Time" date range

2. Refresh page
   - Force refresh: Ctrl/Cmd + Shift + R

3. Check with admin
   - Verify session was successfully logged
   - Not just saved as draft

4. Clear browser cache
   - Might be showing old cached version
```

### Stats Don't Match Expectations

**Symptoms**: Numbers seem incorrect

**Diagnosis:**
```
1. Verify individual sessions
   - Click each session
   - Check buy-in and cash-out
   - Verify profit calculation

2. Check date range
   - Are you looking at "Last Month" vs "All Time"?

3. Check filters
   - Are any location/player filters active?

4. Manual calculation
   - Add up a few sessions manually
   - Compare with displayed stats
```

**If Still Wrong:**
```
Contact admin with specifics:
- Which stat is wrong (ROI, Win Rate, Net Profit)?
- Expected value vs displayed value
- Screenshot of stats page
- Links to relevant sessions
```

### Leaderboard Position Incorrect

**Symptoms**: Your rank seems wrong

**Common Causes:**
```
1. Stats just updated
   - Leaderboard recalculates after each session
   - Recent session might have changed rankings

2. Tied with other players
   - Ties broken alphabetically by name

3. Minimum sessions not met
   - Need 5+ sessions to appear on leaderboard

4. Looking at wrong metric
   - Ensure you're viewing correct leaderboard
   - (Profit vs ROI vs Win Rate)
```

---

## Admin-Specific Issues

### Cannot Submit Session

**Symptoms**: Submit button disabled or errors on submission

**Checklist:**
```
- [ ] At least 2 players selected
- [ ] All required fields filled
- [ ] Buy-in and cash-out entered for all players
- [ ] Date is valid (not in future)
- [ ] Internet connection active
```

**Solutions:**
```
1. Check validation errors
   - Red text shows what's wrong
   - Fix highlighted fields

2. Check player count
   - Minimum 2 players required

3. Save draft and retry
   - Click "Save Draft"
   - Close browser
   - Reopen and resume from Drafts

4. Check browser console (F12)
   - Look for error messages
   - Share with support
```

### Player Not in List

**Symptoms**: Can't find player when logging session

**Causes:**
- Player is deactivated
- Player was deleted
- Wrong search term

**Solutions:**
```
1. Check if player is active
   - Go to Players page
   - Look for player in "Inactive" list
   - Reactivate if needed

2. Create new player
   - Click "+ Add New Player"
   - Enter player details

3. Check spelling
   - Try different search terms
   - Search by partial name
```

### Session Balance Won't Validate

**Symptoms**: Large discrepancy warning, can't submit

**Solutions:**
```
1. Check decimal points
   - $220 vs $22.0
   - Common typo error

2. Verify each player
   - Go through each player one by one
   - Recalculate buy-in and cash-out

3. Account for tips/rake
   - Small discrepancies are OK
   - Add note explaining large ones

4. Skip validation (if truly intentional)
   - Warning can be ignored
   - Add explanation in notes
```

### Draft Not Saving

**Symptoms**: Draft disappears when navigating away

**Solutions:**
```
1. Explicit save
   - Click "Save Draft" button
   - Wait for confirmation message

2. Check local storage
   - Browser might block local storage
   - Enable in browser settings

3. Don't use private mode
   - Incognito/private mode clears data
   - Use normal browser window
```

---

## Server/Infrastructure Issues

*For system administrators*

### All Users Can't Access App

**Symptoms**: Website not loading for anyone

**Diagnosis:**
```bash
# Check if server is running
ping your-server-ip

# Check if Docker containers are up
docker-compose -f docker-compose.prod.yml ps

# Check nginx is responding
curl http://localhost
```

**Solutions:**
```bash
1. Check service status
   docker-compose -f docker-compose.prod.yml ps
   - All services should show "healthy"

2. Restart services
   docker-compose -f docker-compose.prod.yml restart

3. Check logs
   docker-compose -f docker-compose.prod.yml logs --tail=100

4. Check disk space
   df -h
   - Need at least 1GB free

5. Reboot server (last resort)
   sudo reboot
```

### Database Connection Errors

**Symptoms**: "Could not connect to database" errors

**Diagnosis:**
```bash
# Check PostgreSQL container
docker ps | grep postgres

# Test database connection
docker exec pokerstats-postgres-prod pg_isready -U pokerstats

# Check logs
docker logs pokerstats-postgres-prod --tail=50
```

**Solutions:**
```bash
1. Restart PostgreSQL
   docker-compose -f docker-compose.prod.yml restart postgres

2. Check connection settings
   - Verify DATABASE_URL in .env.production
   - Check password is correct

3. Restore from backup (if corrupted)
   bash scripts/restore-database.sh
```

### Redis Connection Issues

**Symptoms**: Slow performance, caching not working

**Diagnosis:**
```bash
# Check Redis container
docker ps | grep redis

# Test Redis
docker exec pokerstats-redis-prod redis-cli --pass $REDIS_PASSWORD ping
# Should return: PONG
```

**Solutions:**
```bash
1. Restart Redis
   docker-compose -f docker-compose.prod.yml restart redis

2. Clear Redis cache
   docker exec pokerstats-redis-prod redis-cli --pass $REDIS_PASSWORD FLUSHALL

3. Check logs
   docker logs pokerstats-redis-prod --tail=50
```

### SSL Certificate Errors

**Symptoms**: "Not secure" warning, HTTPS not working

**Solutions:**
```bash
1. Check certificate expiry
   openssl x509 -in nginx/ssl/fullchain.pem -noout -dates

2. Renew Let's Encrypt certificate
   sudo certbot renew
   sudo cp /etc/letsencrypt/live/your-domain/*.pem nginx/ssl/
   docker-compose -f docker-compose.prod.yml restart nginx

3. Check certificate permissions
   sudo chmod 644 nginx/ssl/*.pem
```

### High Memory Usage

**Symptoms**: Server running out of memory

**Diagnosis:**
```bash
# Check memory
free -h

# Check container usage
docker stats --no-stream

# Check Java heap
docker exec pokerstats-backend-prod jmap -heap 1
```

**Solutions:**
```bash
1. Restart containers
   docker-compose -f docker-compose.prod.yml restart

2. Adjust JVM memory (in docker-compose.prod.yml)
   JAVA_OPTS: "-Xms256m -Xmx512m"  # Reduce if needed

3. Add swap space
   sudo fallocate -l 2G /swapfile
   sudo chmod 600 /swapfile
   sudo mkswap /swapfile
   sudo swapon /swapfile

4. Check for memory leaks
   - Review backend logs
   - Monitor over time
```

---

## Common Error Messages

### "403 Forbidden"

**Meaning**: You don't have permission for this action

**Solutions:**
- Ensure you're logged in
- Check if you have admin privileges (for admin actions)
- Contact admin to verify your permissions

### "404 Not Found"

**Meaning**: The page or resource doesn't exist

**Solutions:**
- Check URL is correct
- Resource might have been deleted
- Try navigating from homepage

### "500 Internal Server Error"

**Meaning**: Something went wrong on the server

**Solutions:**
- Wait a moment and try again
- If persistent, contact admin
- Admin should check server logs

### "Network Error" / "ERR_CONNECTION_REFUSED"

**Meaning**: Can't connect to server

**Solutions:**
- Check internet connection
- Verify server is running (admin)
- Check if URL is correct

---

## Getting Help

### Before Contacting Support

Gather this information:
1. **What were you trying to do?**
2. **What actually happened?**
3. **Error message** (exact text or screenshot)
4. **Browser and version** (Chrome 120, Safari 17, etc.)
5. **Device** (iPhone 12, Windows PC, etc.)
6. **When did it start?** (Today, been happening for a week, etc.)
7. **Can you reproduce it?** (Happens every time vs happened once)

### For Users

**Contact your admin first:**
- They can resolve most issues
- They have access to server logs
- They can check your account status

**Email support:**
- support@pokerstats.local
- Include information from "Before Contacting Support"
- Screenshots are very helpful!

### For Admins

**Check logs first:**
```bash
# Backend logs
docker-compose -f docker-compose.prod.yml logs backend --tail=100

# All services
docker-compose -f docker-compose.prod.yml logs --tail=100 --follow

# Specific error
docker-compose -f docker-compose.prod.yml logs | grep ERROR
```

**Email admin support:**
- admin-support@pokerstats.local
- Include logs if relevant
- Describe what you've already tried

**GitHub Issues:**
- [Report bug](https://github.com/yourorg/pokerstats/issues)
- Check existing issues first
- Include steps to reproduce

---

## Advanced Diagnostics

### Browser Console (Users)

Press `F12` to open Developer Tools:

1. **Console tab**: Shows JavaScript errors
2. **Network tab**: Shows failed requests
3. **Application tab**: Shows storage and cookies

Look for red error messages. Share screenshots with support.

### Server Health Check (Admins)

```bash
# Check all services
docker-compose -f docker-compose.prod.yml ps

# Check health endpoints
curl http://localhost:8080/actuator/health

# Check database size
docker exec pokerstats-postgres-prod psql -U pokerstats -d pokerstats \
  -c "SELECT pg_size_pretty(pg_database_size('pokerstats'));"

# Check disk space
df -h

# Check memory
free -h
```

---

## Preventive Maintenance

**To avoid issues:**

### For Users
- Keep browser updated
- Clear cache monthly
- Use supported browsers
- Stable internet connection

### For Admins
- Daily backups (automated)
- Weekly data integrity checks
- Monthly system updates
- Monitor disk space
- Review logs regularly

---

## Still Need Help?

If this guide didn't resolve your issue:

1. **Search this guide** for keywords related to your problem
2. **Check the FAQ** for common questions
3. **Contact your admin** (users)
4. **Check server logs** (admins)
5. **Email support** with detailed information
6. **File GitHub issue** for bugs

**Remember**: No question is too small. We're here to help!

---

**Version**: 1.0.0  
**Last Updated**: October 2025  
**Maintained by**: Poker Stats Team
