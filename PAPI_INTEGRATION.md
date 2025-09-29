# PlaceholderAPI Integration Guide

## Overview

The FactionsRecruit plugin now features comprehensive PlaceholderAPI integration to display real-time faction information in all GUIs. The system supports both **Kore** and **FactionsUUID** placeholder expansions with intelligent fallback.

---

## Architecture

### Integration Components

1. **PlaceholderAPIIntegration.java** - Core PAPI wrapper
   - Location: `src/main/java/com/gnut/factionsrecruit/integration/PlaceholderAPIIntegration.java`
   - Detects available expansions (Kore, FactionsUUID)
   - Provides unified API for faction data retrieval
   - Handles fallback logic (Kore → FactionsUUID)

2. **FactionInfo.java** - Immutable faction data model
   - Location: `src/main/java/com/gnut/factionsrecruit/integration/FactionInfo.java`
   - Stores all faction information retrieved from PAPI
   - Builder pattern for flexible construction

3. **FactionItemBuilder.java** - GUI item creation utility
   - Location: `src/main/java/com/gnut/factionsrecruit/util/FactionItemBuilder.java`
   - Creates faction banner items with PAPI data
   - Formats values for display (K/M/B suffixes)
   - Handles fallback when PAPI unavailable

---

## Placeholder Priority

### Primary Data Source: **Kore** (if available)
Used for:
- Faction worth/value: `%kore_faction_worth%`
- FTop ranking: `%kore_faction_rank%`
- Faction age: `%kore_faction_age%`

### Fallback Data Source: **FactionsUUID**
Used when Kore unavailable, or for:
- Faction name: `%factionsuuid_faction_name%`
- Leader name: `%factionsuuid_faction_leader%`
- Member counts: `%factionsuuid_faction_online%`, `%factionsuuid_faction_size%`
- Description: `%factionsuuid_faction_description%`
- Founded date: `%factionsuuid_faction_founded%`
- Power: `%factionsuuid_faction_power%` / `%factionsuuid_faction_powermax%`
- Land value: `%factionsuuid_faction_land_value%` (fallback for worth)
- Claims: `%factionsuuid_faction_claims%`
- Bank balance: `%factionsuuid_faction_bank_balance%`

---

## Faction Detection

### Is Player in Faction?
```java
boolean inFaction = PlaceholderAPIIntegration.isInFaction(player);
```

Checks if `%factionsuuid_faction_name%` returns a valid faction (not empty, not "Wilderness").

### Is Player Faction Leader?
```java
boolean isLeader = PlaceholderAPIIntegration.isFactionLeader(player);
```

Compares `%factionsuuid_faction_leader%` with player's name. If they match, player has permission to:
- Accept applications to their faction
- Send invitations to players
- Manage faction recruitment settings

---

## Faction Information Display

### Full Faction Banner Item
Shows comprehensive faction details:
- **Leader name** - From `%factionsuuid_faction_leader%`
- **Online members** - From `%factionsuuid_faction_online%` (green if >0, gray if 0)
- **Total members** - From `%factionsuuid_faction_size%`
- **Faction age** - From `%kore_faction_age%` or `%factionsuuid_faction_founded%`
- **Faction value** - From `%kore_faction_worth%` or `%factionsuuid_faction_land_value%`
  - Formatted with K/M/B suffixes ($1.5M, $250K, etc.)
- **FTop position** - From `%kore_faction_rank%` (only if Kore available)
  - Displayed as "#3", "#1", etc.
- **Power/DTR** - From `%factionsuuid_faction_power%` / `%factionsuuid_faction_powermax%`
- **Description** - From `%factionsuuid_faction_description%`

### Compact Faction Item
Minimal display for lists:
- Leader name
- Online/total members
- FTop position (if available)

---

## Usage Examples

### Creating Faction Banner Items

#### Full Banner with PAPI Data
```java
// Get a member of the faction to query
Player factionMember = /* ... */;

// Create full faction banner item
ItemStack banner = FactionItemBuilder.createFactionBannerItem(factionMember);
inventory.setItem(slot, banner);
```

#### Compact Faction Item
```java
ItemStack compactBanner = FactionItemBuilder.createCompactFactionItem(factionMember);
inventory.setItem(slot, compactBanner);
```

### Checking Faction Status

#### Check if Player Can Accept Applications
```java
if (FactionItemBuilder.isFactionLeader(player)) {
    // Player can accept applications
    // Show "Accept/Reject" options
} else {
    // Player cannot accept applications
    // Show read-only view
}
```

#### Check if Player Is in Faction
```java
if (FactionItemBuilder.isInFaction(player)) {
    // Player has a faction
    // Show faction-specific options
} else {
    // Player is factionless
    // Show "Join a Faction" options
}
```

### Getting Faction Data Programmatically

#### Get Complete Faction Info
```java
FactionInfo info = PlaceholderAPIIntegration.getFactionInfo(player);
if (info != null) {
    String factionName = info.getFactionName();
    String leaderName = info.getLeaderName();
    int onlineMembers = info.getOnlineMembers();
    String ftopPos = info.getFtopPosition(); // "N/A" if not available
    String value = info.getFactionValue();
}
```

#### Get Individual Fields
```java
// Quick access methods
String factionName = PlaceholderAPIIntegration.getFactionName(player);
String leaderName = PlaceholderAPIIntegration.getFactionLeader(player);
int onlineCount = PlaceholderAPIIntegration.getOnlineMembers(player);
int totalCount = PlaceholderAPIIntegration.getTotalMembers(player);
String factionValue = PlaceholderAPIIntegration.getFactionValue(player);
String ftopRank = PlaceholderAPIIntegration.getFtopPosition(player);
String factionAge = PlaceholderAPIIntegration.getFactionAge(player);
```

#### Format Values for Display
```java
// Format faction worth with K/M/B suffixes
String formatted = PlaceholderAPIIntegration.formatFactionValue("1500000");
// Returns: "1.50M"

// Get pre-formatted faction value
String displayValue = FactionItemBuilder.getFormattedFactionValue(player);
// Returns: "$1.50M"

// Get FTop position with # prefix
String ftopDisplay = FactionItemBuilder.getFtopPosition(player);
// Returns: "#3" or "N/A"
```

---

## GUI Integration Points

### 1. LandingUI
- Check if player is faction leader to show/hide "Accept Applications" button
- Use `FactionItemBuilder.isFactionLeader(player)` vs `FactionItemBuilder.isInFaction(player)`

### 2. FactionsRecruitingGUI
- Create faction banner items using `FactionItemBuilder.createFactionBannerItem()`
- Display leader, members, age, value, FTop position
- Only show factions where members are online for recruiting

### 3. LookingForFactionsGUI
- Show player heads with skill information
- Faction leaders see "Send Invitation" option
- Use `FactionItemBuilder.isFactionLeader(viewer)` to determine actions

### 4. PlayerApplicationsGUI
- Show incoming applications with faction information
- Use PAPI to display faction details for each application
- Different views for faction leaders vs regular players

---

## Required Dependencies

### pom.xml
```xml
<repositories>
    <repository>
        <id>placeholderapi</id>
        <url>https://repo.extendedclip.com/content/repositories/placeholderapi/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.clip</groupId>
        <artifactId>placeholderapi</artifactId>
        <version>2.11.6</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### plugin.yml
```yaml
softdepend:
  - Factions
  - FactionsUUID
  - PlaceholderAPI
  - Kore
```

---

## Server Setup Requirements

### Required Plugins
1. **PlaceholderAPI** (required)
   - Download: https://www.spigotmc.org/resources/placeholderapi.6245/

2. **FactionsUUID** (required)
   - Download: https://www.spigotmc.org/resources/factionsuuid.1035/

3. **FactionsUUID Expansion** for PlaceholderAPI (required)
   - Install via: `/papi ecloud download FactionsUUID`
   - Enable via: `/papi reload`

4. **Kore** (optional, recommended for FTop)
   - Provides FTop rankings and enhanced faction data
   - If not installed, plugin will fallback to FactionsUUID land value

### Installation Steps
```bash
# 1. Install PlaceholderAPI
# Place PlaceholderAPI.jar in plugins/

# 2. Install FactionsUUID
# Place FactionsUUID.jar in plugins/

# 3. Start server and install FactionsUUID expansion
/papi ecloud download FactionsUUID
/papi reload

# 4. (Optional) Install Kore for FTop data
# Place Kore.jar in plugins/

# 5. Install FactionsRecruit
# Place FactionsRecruit.jar in plugins/

# 6. Restart server
/stop
```

---

## Startup Logs

When properly configured, you'll see:
```
[FactionsRecruit] Initializing PlaceholderAPI integration...
[FactionsRecruit] PlaceholderAPI found! Checking for faction expansions...
[FactionsRecruit] Kore expansion detected - will use for FTop data
[FactionsRecruit] FactionsUUID expansion detected
```

If missing expansions:
```
[FactionsRecruit] PlaceholderAPI found! Checking for faction expansions...
[FactionsRecruit] No faction expansions found! Faction integration will be limited.
```

---

## Fallback Behavior

### When PlaceholderAPI Not Installed
- Faction items display "PlaceholderAPI not available" message
- Barrier icon shown instead of banner
- Basic plugin functionality still works (player profiles, manual data entry)

### When FactionsUUID Expansion Missing
- Same as above - faction detection won't work
- Player must install expansion via `/papi ecloud download FactionsUUID`

### When Kore Not Installed
- FTop position shows "N/A"
- Faction value uses FactionsUUID land value instead
- Faction age shows founded date instead of calculated age
- All other features work normally

---

## Data Formatting

### Faction Value Formatting
```java
// Raw value: "1500000"
// Formatted: "$1.50M"

// Raw value: "250000"
// Formatted: "$250.00K"

// Raw value: "5000000000"
// Formatted: "$5.00B"
```

### Member Display
- Online members shown in **green** if > 0
- Online members shown in **gray** if = 0
- Format: "Online/Total" (e.g., "3/25")

### FTop Position
- Displayed with # prefix: "#1", "#3", "#10"
- Shows "N/A" if Kore not available or faction not ranked

---

## Performance Considerations

1. **Placeholder parsing is done per-player**
   - Each faction member in GUI requires separate PAPI queries
   - Results are not cached (PAPI handles caching internally)

2. **Async operations recommended**
   - Fetch faction data async when loading GUIs with many factions
   - Avoid blocking main thread with heavy PAPI queries

3. **Expansion availability checked once**
   - On plugin initialization
   - Restart required if expansions added/removed

---

## Troubleshooting

### "PlaceholderAPI not available" in GUIs
**Cause**: PlaceholderAPI plugin not installed
**Solution**: Install PlaceholderAPI from Spigot

### FTop Position Always Shows "N/A"
**Cause**: Kore plugin not installed
**Solution**: Install Kore plugin, or accept that FTop data unavailable

### Faction Name Shows as Placeholder Text (%faction...%)
**Cause**: FactionsUUID expansion not installed
**Solution**: `/papi ecloud download FactionsUUID` then `/papi reload`

### Leader Detection Not Working
**Cause**: Player name doesn't match leader name exactly
**Solution**: Check for nickname conflicts, ensure FactionsUUID expansion updated

---

## Future Enhancements

Potential improvements for PAPI integration:

1. **Caching Layer**
   - Cache faction data for X seconds to reduce PAPI queries
   - Configurable cache duration

2. **Custom Placeholders**
   - Register FactionsRecruit placeholders for use in other plugins
   - Example: `%factionsrecruit_player_looking%`, `%factionsrecruit_faction_applications%`

3. **Additional Expansions**
   - Support for other faction plugins (Factions 3.x, SaberFactions)
   - Fallback chain: Kore → FactionsUUID → Factions3 → Manual

4. **Async Data Loading**
   - Pre-fetch faction data when opening GUIs
   - Display loading indicators while fetching

---

## Summary

✅ **Complete PAPI Integration** - Kore and FactionsUUID support
✅ **Intelligent Fallback** - Kore → FactionsUUID → Default
✅ **Leader Detection** - Automatic permission checking
✅ **Rich Faction Display** - Leader, members, age, value, FTop
✅ **Value Formatting** - K/M/B suffixes for readability
✅ **Build Success** - All code compiles and integrates properly

**Files Created**: 3 new classes (~450 lines of integration code)
**Dependencies Added**: PlaceholderAPI 2.11.6
**Placeholders Supported**: 15+ from FactionsUUID, 3+ from Kore

The integration is production-ready and will automatically detect available expansions on startup!