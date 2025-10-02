# Banner Click Debug Guide

## Issue Description
Clicking on faction banners in the browser UI shows "Error: Faction application not found" even though the faction application exists in the database.

## Database Record Analysis
From your database record:
```
ID: 21
faction_id: bingus
faction_name: bingus
leader_uuid: dd449973-10fc-4749-905a-0ecfb28352bf
...
```

**Key Point**: The `faction_id` field contains "bingus", not "21".

## Debug Steps

### Step 1: Enable Debug Logging
Add this to your `config.yml`:
```yaml
testing:
  log-gui-interactions: true
  log-title-matching: true
```

### Step 2: Test Banner Creation
1. Run `/recruit` command
2. Toggle to faction mode
3. Check console logs for messages like:
   ```
   [DEBUG] Adding faction ID to banner lore: 'bingus' for faction: bingus
   ```

**Expected**: Should show the faction ID being stored as "bingus"

### Step 3: Test Banner Clicking
1. Click on the faction banner
2. Check console logs for messages like:
   ```
   [DEBUG] Banner clicked - Extracted faction ID: 'bingus'
   [DEBUG] Banner lore size: 7
   [DEBUG] Lore[0]: 'Leader: [playername]'
   [DEBUG] Lore[1]: 'Members: [x/y]'
   [DEBUG] Lore[2]: 'Timezones: EU_WEST'
   [DEBUG] Lore[3]: 'Looking for: DEFENSE'
   [DEBUG] Lore[4]: 'bingus'
   [DEBUG] Lore[5]: ''
   [DEBUG] Lore[6]: 'Click to apply'
   ```

**Expected**: Should show faction ID being extracted as "bingus" from lore[4]

### Step 4: Test Database Lookup
Check for messages like:
```
[DEBUG] Querying for faction application with ID: 'bingus'
[DEBUG] Found faction application: bingus (ID: bingus)
```

**OR if failing**:
```
[DEBUG] No faction application found for ID: 'bingus'
```

## Potential Issues and Solutions

### Issue 1: Wrong Lore Index
**Symptoms**: Lore extraction gets wrong data
**Debug**: Check what lore[4] actually contains
**Solution**: Verify lore structure matches expected format

### Issue 2: Color Code Interference
**Symptoms**: Faction ID has color codes attached
**Debug**: Check if extracted faction ID has extra characters
**Solution**: Ensure `ChatColor.stripColor()` removes all formatting

### Issue 3: Database Inconsistency
**Symptoms**: Database query fails despite correct faction ID
**Debug**: Check database connection and table structure
**Solution**: Verify faction_applications table has correct data

### Issue 4: Character Encoding
**Symptoms**: Faction ID appears correct but database lookup fails
**Debug**: Check for invisible characters or encoding issues
**Solution**: Trim the faction ID: `factionId.trim()`

## Testing Commands

Run these in sequence and share the console output:

1. **Enable Debug Logging**:
   ```yaml
   # In config.yml
   testing:
     log-gui-interactions: true
   ```

2. **Test Sequence**:
   - `/recruit`
   - Toggle to faction mode
   - Click faction banner
   - Check console for debug messages

3. **Manual Database Check**:
   ```sql
   SELECT faction_id, faction_name FROM faction_applications WHERE faction_id = 'bingus';
   ```

## Expected Debug Output

If working correctly, you should see:
```
[DEBUG] Adding faction ID to banner lore: 'bingus' for faction: bingus
[DEBUG] Banner clicked - Extracted faction ID: 'bingus'
[DEBUG] Querying for faction application with ID: 'bingus'
[DEBUG] Found faction application: bingus (ID: bingus)
```

If failing, you might see:
```
[DEBUG] Adding faction ID to banner lore: 'bingus' for faction: bingus
[DEBUG] Banner clicked - Extracted faction ID: '[some other value]'
[DEBUG] Querying for faction application with ID: '[some other value]'
[DEBUG] No faction application found for ID: '[some other value]'
```

## Next Steps

Share the debug output and I'll help identify exactly where the issue is occurring:

1. **Banner Creation Phase**: Is the correct faction ID being stored?
2. **Banner Click Phase**: Is the correct faction ID being extracted?
3. **Database Query Phase**: Is the database lookup working correctly?

The debug logging will pinpoint exactly where the mismatch occurs.