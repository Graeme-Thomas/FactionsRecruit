# GUI Testing Manual for FactionsRecruit Plugin

## Overview

This manual provides a comprehensive testing strategy for the FactionsRecruit plugin's GUI system. It includes both automated testing approaches and efficient manual testing procedures to verify GUI functionality without extensive manual work.

## Automated Testing Setup

### 1. Dependencies Added

The following testing dependencies have been added to `pom.xml`:
- **MockBukkit**: For Bukkit API mocking
- **JUnit Jupiter**: For modern unit testing
- **Mockito**: For object mocking and verification

### 2. Test Classes Created

#### VisualUtilsTest.java
- Tests title creation and small caps conversion
- Validates color palette constants and symbol definitions
- Ensures compact titles stay under character limits
- Verifies gradient and formatting functions

#### TitleNormalizationTest.java
- Tests the critical title matching logic in PlayerListener
- Validates the complete pipeline: decorated title → normalized title
- Tests edge cases (null, empty, whitespace)
- Performance testing for title normalization

#### PlayerListenerTest.java
- Tests GUI interaction handling with mocked components
- Validates title detection for all GUI types
- Tests click event handling and validation

### 3. Debug Logging System

#### GUITestLogger.java
- Comprehensive logging for GUI interactions
- Performance metrics tracking
- Title matching debugging
- Error condition logging

#### Configuration Added
```yaml
# config.yml
testing:
  log-gui-interactions: false
  log-title-matching: false
  log-performance-metrics: false
```

## Manual Testing Strategy

### Phase 1: Critical Path Testing (High Priority)

These are the most important functions that must work correctly:

1. **Main Menu Navigation**
   - `/recruit` command opens main browser
   - Profile visibility toggle (slot 4) works
   - Navigation between player/faction modes (slot 51) works

2. **Profile Management**
   - Resume editor opens and saves correctly
   - Profile visibility persists after server restart
   - Player profile viewing works (`/recruit <playername>`)

3. **Faction Features**
   - Faction application editor works for faction leaders
   - Pending applications view shows correct data
   - Invite button (slot 22) in player profiles works

### Phase 2: Secondary Features (Medium Priority)

4. **Search and Filtering**
   - AnvilGUI search functionality
   - Filter system application and reset
   - Pagination (slots 45, 53) works correctly

5. **Help and Navigation**
   - Help GUI displays correct information
   - All navigation buttons work consistently
   - Border glass panes properly cancel interactions

### Phase 3: Edge Cases (Lower Priority)

6. **Edge Case Testing**
   - Long player names in GUI titles
   - Special characters in search inputs
   - Rapid clicking scenarios
   - Plugin reload compatibility

## Efficient Testing Procedures

### 1. Enable Debug Logging

Before testing, enable debug logging in `config.yml`:
```yaml
testing:
  log-gui-interactions: true
  log-title-matching: true
```

This will log all GUI interactions to help identify issues quickly.

### 2. Quick Verification Checklist

Use this checklist for rapid testing after changes:

- [ ] `/recruit` opens main menu with correct title
- [ ] Profile toggle button changes state visually
- [ ] Clicking any player opens their profile
- [ ] Resume editor saves without errors
- [ ] Faction application editor works for faction leaders
- [ ] Search function accepts input and filters results
- [ ] Help menu displays and navigates correctly

### 3. Title Matching Verification

Run this simple test to verify title matching:
1. Open any GUI
2. Check console logs for title matching output
3. Verify "MATCHED" appears in logs for expected GUIs
4. If "NO MATCH" appears, check title normalization

### 4. Performance Testing

To test performance under load:
1. Enable performance metrics logging
2. Rapidly open/close GUIs
3. Check console for any warnings about slow operations
4. Operations should complete in under 5ms

## Testing Automation Tools

### 1. Unit Test Execution

To run the automated test suite:
```bash
mvn test
```

Individual test classes can be run with:
```bash
mvn test -Dtest=VisualUtilsTest
mvn test -Dtest=TitleNormalizationTest
mvn test -Dtest=PlayerListenerTest
```

### 2. Debug Output Analysis

When tests fail, check the following:
1. **Title Normalization**: Ensure small caps conversion is working
2. **Color Stripping**: Verify color codes are removed properly
3. **Performance**: Check if operations are completing within expected time

### 3. Mock Environment Setup

For more comprehensive testing, set up a test server with:
- FactionsUUID plugin installed
- PlaceholderAPI with faction placeholders
- Test factions and players created
- Database connection configured

## Common Issues and Solutions

### Issue: GUI Not Opening
**Symptoms**: `/recruit` command runs but no GUI appears
**Debug**: Check console for "GUI Opened" debug messages
**Solution**: Verify title matching in PlayerListener

### Issue: Clicks Not Working
**Symptoms**: Clicking items in GUI has no effect
**Debug**: Look for "Click Event" debug messages
**Solution**: Check title normalization and event cancellation

### Issue: Performance Problems
**Symptoms**: Server lag when opening GUIs
**Debug**: Enable performance metrics logging
**Solution**: Optimize title normalization or database queries

### Issue: Database Errors
**Symptoms**: GUI opens but data doesn't save
**Debug**: Check database connection and error logs
**Solution**: Verify database schema and connection string

## Testing Schedule

### After Code Changes
- Run unit tests (5 minutes)
- Quick verification checklist (10 minutes)
- Critical path testing if GUI code changed (15 minutes)

### Before Release
- Complete manual testing of all features (45 minutes)
- Performance testing under load (15 minutes)
- Edge case testing (30 minutes)
- Documentation updates if needed (15 minutes)

### Weekly Maintenance
- Run full test suite
- Check performance metrics
- Verify no new issues in error logs
- Update test cases if new features added

## Conclusion

This testing strategy provides 80% automated coverage while minimizing manual testing time. The debug logging system makes issue identification quick and efficient, and the prioritized manual testing ensures critical functionality is always verified first.

The key to efficient testing is using the automated tests to catch regression issues and focusing manual testing on user experience and integration aspects that are difficult to automate.