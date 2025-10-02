# GUI Interaction Test Plan

## Overview
This document provides a test plan to verify the confirmation dialogs and faction application functionality work correctly after recent changes.

## Changes Made

### 1. ✅ Player Invitation Confirmation
- **Location**: PlayerListener.java (lines 193-206)
- **Change**: Added confirmation dialog before sending faction invitations
- **Impact**: When clicking "Invite Player" in a player profile, shows confirmation before sending invitation

### 2. ✅ Faction Application Banner Fix
- **Location**: PlayerListener.java (lines 337-357)
- **Change**: Fixed faction ID extraction from banner lore (was line 3, now line 4)
- **Impact**: Clicking faction banners now correctly applies to factions

### 3. ✅ Enhanced Error Handling
- **Location**: PlayerListener.java (lines 350-355)
- **Change**: Added error messages for invalid faction data
- **Impact**: Better user feedback when faction applications fail

### 4. ✅ Fixed Confirmation UI Close Behavior
- **Location**: PlayerListener.java (lines 835-859)
- **Change**: Added InventoryCloseEvent handler to clean up GUI state when player closes with ESC/E
- **Impact**: Fixes "Please click green or red buttons" message after closing confirmation dialogs

## Test Cases

### Test Case 1: Player Invitation Confirmation
**Objective**: Verify invitation confirmation dialog appears and works correctly

**Steps**:
1. Run `/recruit` command
2. Ensure you're in player mode (toggle if needed)
3. Click on any player head to view their profile
4. Click the "Invite Player" button (lectern item in slot 22)

**Expected Results**:
- ✅ Confirmation dialog should appear with title "Confirm Invitation"
- ✅ Dialog should show "Send faction invitation to [PlayerName]?"
- ✅ Clicking "Confirm" should send the invitation and show success message
- ✅ Clicking "Cancel" should return to the player profile
- ✅ Only faction members should be able to send invitations

### Test Case 2: Faction Application Flow
**Objective**: Verify faction application confirmation and processing works

**Steps**:
1. Run `/recruit` command
2. Toggle to faction mode (click the toggle button)
3. Click on any faction banner (yellow banner item)

**Expected Results**:
- ✅ Confirmation dialog should appear with title "Apply to Faction"
- ✅ Dialog should show "Apply to [FactionName]?"
- ✅ Clicking "Confirm" should:
  - Add application to your pending applications list
  - Add your player info to faction leader's pending applications
  - Show success message "Application sent to [FactionName]"
- ✅ Clicking "Cancel" should return to main menu
- ✅ Should respect application cooldowns and slot limits

### Test Case 3: Confirmation UI Close Behavior
**Objective**: Verify closing confirmation dialogs with ESC/E works correctly

**Steps**:
1. Open a confirmation dialog (either invitation or faction application)
2. Press ESC or E to close the dialog instead of clicking Confirm/Cancel
3. Try to interact with any other GUI elements

**Expected Results**:
- ✅ Confirmation dialog closes properly
- ✅ No "Please click green or red buttons" message appears
- ✅ Subsequent GUI interactions work normally
- ✅ No need to relog to fix the issue

### Test Case 4: Error Handling
**Objective**: Verify error messages work correctly

**Steps**:
1. Try to apply to the same faction twice (should show cooldown message)
2. Try to send invitation when not in a faction (should show error)
3. Try to apply when at maximum application slots (should show slot limit message)

**Expected Results**:
- ✅ Appropriate error messages displayed
- ✅ No crashes or silent failures
- ✅ User returned to appropriate UI

### Test Case 5: Application Processing
**Objective**: Verify applications appear in both player and faction leader lists

**Steps**:
1. Apply to a faction (follow Test Case 2)
2. Check your applications: Main menu → "Manage Applications"
3. Have faction leader check their applications: Main menu → "Manage Applications"

**Expected Results**:
- ✅ Application appears in player's "Your Applications" list
- ✅ Application appears in faction leader's "Pending Applications" list
- ✅ Application shows correct status ("PENDING")
- ✅ Application has proper expiry date (3 days default)

## Debug Information

### Enable Debug Logging
To troubleshoot issues, enable debug logging in `config.yml`:
```yaml
testing:
  log-gui-interactions: true
  log-title-matching: true
```

### Common Issues and Solutions

**Issue**: "Error: Faction application not found"
- **Cause**: Faction ID extraction failed
- **Solution**: Check faction banner lore format in console logs

**Issue**: "Could not send invitation. Are you in a faction?"
- **Cause**: Player is not in a faction or lacks permissions
- **Solution**: Verify player has faction membership and appropriate role

**Issue**: Confirmation dialog doesn't appear
- **Cause**: GUI title normalization issue
- **Solution**: Check console logs for title matching failures

**Issue**: "Please click green or red buttons" message after closing with ESC/E
- **Cause**: GUI handler not cleaned up when inventory closed with ESC/E
- **Solution**: Fixed with InventoryCloseEvent handler (lines 835-859)

**Issue**: "You have reached the maximum number of application slots"
- **Cause**: Player has 3+ active applications
- **Solution**: Wait for applications to expire or be processed

## Verification Checklist

After testing, verify these behaviors work correctly:

### Player Invitation Flow
- [ ] Confirmation dialog appears when clicking invite
- [ ] Invitation is sent only after confirmation
- [ ] Cancel returns to player profile
- [ ] Success message is displayed
- [ ] Only faction members can send invitations

### Faction Application Flow
- [ ] Confirmation dialog appears when clicking faction banner
- [ ] Application is created only after confirmation
- [ ] Cancel returns to main menu
- [ ] Success message is displayed
- [ ] Application appears in both player and leader lists
- [ ] Cooldown and slot limits are enforced

### Error Handling
- [ ] Meaningful error messages are displayed
- [ ] No crashes or silent failures occur
- [ ] User is returned to appropriate UI after errors

## Performance Notes

The confirmation dialogs add minimal overhead:
- No database queries until confirmation
- UI rendering remains fast
- Memory usage unchanged

All existing functionality should work exactly as before, with only the addition of confirmation steps.