# Faction Recruitment Plugin - Complete Specification

## Overview
A comprehensive recruitment system that streamlines the faction recruitment process through an intuitive browser interface. Players can create resumes, browse factions, submit applications, and manage recruitment efficiently.

## Configuration
```yaml
# config.yml
faction-member-limit: 30
application-slots-per-player: 3
application-expiry-days: 3
invitation-expiry-days: 3
resume-display-days: 3
application-cooldown-hours: 24
resume-edit-cooldown-hours: 6

notifications:
  login-check: true
  sounds:
    success: "ENTITY_EXPERIENCE_ORB_PICKUP"
    error: "BLOCK_NOTE_BLOCK_BASS"
    click: "BLOCK_NOTE_BLOCK_PLING"
    navigation: "UI_BUTTON_CLICK"

messages:
  application-sent: "§aApplication sent to %faction%!"
  application-expired: "§cYour application to %faction% has expired"
  application-cancelled: "§eApplication to %faction% cancelled"
  invitation-received: "§6You have received an invitation from %faction%!"
  invitation-expired: "§cInvitation from %faction% has expired"
  resume-expired: "§cYour resume is no longer visible to faction owners"
  slots-available: "§aYou have %slots% application slots available"
```

---

## Database Schema

```sql
CREATE TABLE player_resumes (
    uuid VARCHAR(36) PRIMARY KEY,
    timezone ENUM('NA_WEST','NA_EAST','EU_WEST','EU_CENTRAL','ASIA','OCEANIA'),
    experience ENUM('UNDER_6MO','1_YEAR','1_2_YEARS','2_3_YEARS','3_4_YEARS','4_5_YEARS','5_PLUS_YEARS'),
    available_days SET('MON','TUE','WED','THU','FRI','SAT','SUN'),
    skills SET('CANNON','PVP','DEFENSE','DESIGN','REDSTONE','FARM','FISH'),
    is_looking BOOLEAN DEFAULT FALSE,
    display_until TIMESTAMP NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE faction_applications (
    faction_id VARCHAR(50) PRIMARY KEY,
    faction_name VARCHAR(100),
    leader_uuid VARCHAR(36),
    desired_timezones SET('NA_WEST','NA_EAST','EU_WEST','EU_CENTRAL','ASIA','OCEANIA'),
    experience_levels SET('UNDER_6MO','1_YEAR','1_2_YEARS','2_3_YEARS','3_4_YEARS','4_5_YEARS','5_PLUS_YEARS'),
    required_days SET('MON','TUE','WED','THU','FRI','SAT','SUN'),
    desired_skills SET('CANNON','PVP','DEFENSE','DESIGN','REDSTONE','FARM','FISH'),
    is_accepting BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE recruitment_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    player_uuid VARCHAR(36),
    faction_id VARCHAR(50),
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    slot_available_at TIMESTAMP,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'CANCELLED') DEFAULT 'PENDING',
    INDEX idx_player_uuid (player_uuid),
    INDEX idx_faction_id (faction_id),
    INDEX idx_expires_at (expires_at)
);

CREATE TABLE faction_invitations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    faction_id VARCHAR(50),
    player_uuid VARCHAR(36),
    invited_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED') DEFAULT 'PENDING',
    INDEX idx_player_uuid (player_uuid),
    INDEX idx_expires_at (expires_at)
);

CREATE TABLE edit_cooldowns (
    uuid VARCHAR(36) PRIMARY KEY,
    last_resume_edit TIMESTAMP,
    last_application_edit TIMESTAMP,
    INDEX idx_resume_edit (last_resume_edit),
    INDEX idx_application_edit (last_application_edit)
);

CREATE TABLE login_notifications (
    uuid VARCHAR(36) PRIMARY KEY,
    has_expired_applications BOOLEAN DEFAULT FALSE,
    has_new_invitations BOOLEAN DEFAULT FALSE,
    has_available_slots BOOLEAN DEFAULT FALSE,
    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Main Browser UI Layout
**Command**: `/recruit`  
**Inventory Size**: 54 slots (6 rows × 9 columns)  
**Title**: "§6Faction Recruitment Browser"

```
Slot Layout (0-53):
Row 1: [0:PINK] [1:WHITE] [2:PINK] [3:WHITE] [4:PLAYER_HEAD] [5:WHITE] [6:PINK] [7:WHITE] [8:PINK]
Row 2: [9:WHITE] [10:EMPTY] [11:EMPTY] [12:EMPTY] [13:EMPTY] [14:EMPTY] [15:EMPTY] [16:EMPTY] [17:WHITE]
Row 3: [18:PINK] [19:EMPTY] [20:EMPTY] [21:EMPTY] [22:EMPTY] [23:EMPTY] [24:EMPTY] [25:EMPTY] [26:WHITE]
Row 4: [27:WHITE] [28:EMPTY] [29:EMPTY] [30:EMPTY] [31:EMPTY] [32:EMPTY] [33:EMPTY] [34:EMPTY] [35:WHITE]
Row 5: [36:PINK] [37:EMPTY] [38:EMPTY] [39:EMPTY] [40:EMPTY] [41:EMPTY] [42:EMPTY] [43:EMPTY] [44:WHITE]
Row 6: [45:ARROW/WHITE] [46:LECTERN] [47:JUKEBOX/PINK] [48:BOOK] [49:BARRIER] [50:COMPASS] [51:SWORD/BANNER] [52:HOPPER] [53:ARROW/WHITE]
```

### Interactive Elements:

**PLAYER_HEAD (Slot 4)**:
- **Item**: Player's head
- **Display Name**: "§6{player_name}'s Profile"
- **Lore**: Shows player's current resume data or "§7Click to set up your resume"
- **Click Action**: Toggle "looking for faction" status
- **States**:
  - Looking: Green enchantment glow + "§aCurrently visible to faction owners"
  - Not Looking: No glow + "§7Not visible to faction owners"

**ARROW NAVIGATION (Slots 45, 53)**:
- **Previous (45)**: Red Arrow or White Glass Pane if no previous page
- **Next (53)**: Green Arrow or White Glass Pane if no next page
- **Click**: Navigate between pages of results

**LECTERN (Slot 46)**:
- **Display Name**: "§6Manage Resume"
- **Lore**: ["§7Edit your recruitment profile", "§7and skills information"]
- **Click**: Open Resume Editor UI

**JUKEBOX (Slot 47)**:
- **Visibility**: Only shown if player is faction owner
- **Display Name**: "§6Manage Applications"
- **Lore**: ["§7Set faction requirements", "§7and view applications"]
- **Item**: Jukebox if owner, Pink Glass Pane if not
- **Click**: Open Faction Application Manager

**BOOK (Slot 48)**:
- **Display Name**: "§6Help & Commands"
- **Lore**:
  ```
  - "§7/recruit - Open browser"
  - "§7/recruit <player> - View player resume"
  - "§7"
  - "§eClick items to interact!"
  ```

**BARRIER (Slot 49)**:
- **Display Name**: "§cClose"
- **Click**: Close the menu

**COMPASS (Slot 50)**:
- **Display Name**: "§6Search"
- **Lore**:
  - Player mode: ["§7Search for player names"]
  - Faction mode: ["§7Search for faction names"]
- **Click**: Open sign editor for search input

**SWORD/BANNER (Slot 51)**:
- **Toggle Function**: Switch between player and faction view
- **States**:
  - Player Mode: Netherite Sword, "§6Browsing Players", shows player heads
  - Faction Mode: Banner, "§6Browsing Factions", shows faction banners
- **Default**: 
  - No faction: Faction Mode (Banner)
  - Has faction: Player Mode (Sword)

**HOPPER (Slot 52)**:
- **Display Name**: "§6Filter Results"
- **Lore**: ["§7Filter by timezone, experience,", "§7skills, and availability"]
- **Click**: Open Filter UI

### Content Display (Slots 10-43):
**Player Mode**: Shows player heads of users looking for factions
**Faction Mode**: Shows faction banners of recruiting factions

---

## Player Resume Editor UI

**Inventory Size**: 54 slots (6 rows × 9 columns)  
**Title**: "§6Resume Editor"

```
Slot Layout (0-53):
Row 1: [0:PINK] [1:WHITE] [2:PINK] [3:WHITE] [4:PLAYER_HEAD] [5:WHITE] [6:PINK] [7:WHITE] [8:PINK]
Row 2: [9:TZ_HEADER] [10:BLACK] [11:NA_WEST] [12:NA_EAST] [13:EU_WEST] [14:EU_CENT] [15:ASIA] [16:OCEANIA] [17:PINK]
Row 3: [18:EXP_HEADER] [19:BLACK] [20:EXP_6MO] [21:EXP_1YR] [22:EXP_2YR] [23:EXP_3YR] [24:EXP_4YR] [25:EXP_5YR] [26:EXP_5PLUS]
Row 4: [27:DAY_HEADER] [28:BLACK] [29:MON] [30:TUE] [31:WED] [32:THU] [33:FRI] [34:SAT] [35:SUN]
Row 5: [36:SKILL_HEADER] [37:BLACK] [38:CANNON] [39:PVP] [40:DEFENSE] [41:DESIGN] [42:REDSTONE] [43:FARM] [44:FISH]
Row 6: [45:PINK] [46:WHITE] [47:PINK] [48:WHITE] [49:SAVE_BTN] [50:WHITE] [51:PINK] [52:WHITE] [53:PINK]
```

### Section Headers:

- **Slot 9 - Timezone**: Paper, "§6Primary Timezone", Lore: ["§7Select your main timezone"]
- **Slot 18 - Experience**: Paper, "§6Factions Experience", Lore: ["§7How long have you played factions?"]
- **Slot 27 - Days**: Daylight Sensor, "§6Available Days", Lore: ["§7When can you be online?"]
- **Slot 36 - Skills**: TNT Minecart, "§6Your Skills", Lore: ["§7What are you good at?"]

### Timezone Selection (Radio - Single Choice):
**Items**: Green Candle With enchantment glint (selected) or Gray Candle (unselected)
- NA West, NA East, EU West, EU Central, Asia, Oceania
- **Selection**: Clicking deselects others, selects clicked item

### Experience Selection (Radio - Single Choice):
**Items**: Green Candle With enchantment glint (selected) or Gray Candle (unselected)
- <6 months, 1 year, 1-2 years, 2-3 years, 3-4 years, 4-5 years, 5+ years

### Available Days (Checkboxes - Multiple Choice):
**Items**: Green Candle With enchantment glint (checked) or Red Candle (unchecked)
- Monday through Sunday
- **Selection**: Toggle individual days on/off

### Skills (Checkboxes - Multiple Choice):
**Items**: Specific items with/without enchantment glow
**Limitation**: Only 3 can be selected.
- Cannoning: TNT
- PVP: Netherite Sword
- Base Defense: Water Bucket
- Base Design: Calibrated Sculk Sensor
- Redstone Tech: Redstone Dust
- Farming: Sugar Cane
- Fishing: Fishing Rod

### Action Buttons:
- **Slot 49 - Save**: Emerald, "§aSave & Continue"

### Validation Rules:
- Must select exactly 1 timezone
- Must select exactly 1 experience level
- Must select at least 1 day
- Must select 1-3 skills
- If validation fails: Play error sound, highlight missing sections with red borders

---

## Resume Confirmation Screen

**Inventory Size**: 27 slots (3 rows × 9 columns)  
**Title**: "§6Confirm Resume Changes"

```
Slot Layout (0-26):
Row 1: [0:WHITE] [1:PINK] [2:WHITE] [3:PINK] [4:WHITE] [5:PINK] [6:WHITE] [7:PINK] [8:WHITE]
Row 2: [9:PINK] [10:BLACK] [11:BLACK] [12:RED_GLASS] [13:PLAYER_HEAD] [14:LIME_GLASS] [15:BLACK] [16:BLACK] [17:PINK]
Row 3: [18:WHITE] [19:PINK] [20:WHITE] [21:PINK] [22:WHITE] [23:PINK] [24:WHITE] [25:PINK] [26:WHITE]
```

### Interactive Elements:
- **Slot 12 - Cancel**: Red Stained Glass Pane, "§cCancel Changes", return to editor
- **Slot 13 - Preview**: Player head showing new resume data in lore
- **Slot 14 - Confirm**: Lime Stained Glass Pane, "§aConfirm & Save", save to database

---

## Faction Application Editor UI

**Inventory Size**: 54 slots (6 rows × 9 columns)  
**Title**: "§6Faction Requirements Editor"

```
Slot Layout (0-53):
Row 1: [0:PINK] [1:WHITE] [2:PINK] [3:WHITE] [4:FACTION_HEAD] [5:WHITE] [6:PINK] [7:WHITE] [8:PINK]
Row 2: [9:TZ_HEADER] [10:BLACK] [11:NA_WEST] [12:NA_EAST] [13:EU_WEST] [14:EU_CENT] [15:ASIA] [16:OCEANIA] [17:PINK]
Row 3: [18:EXP_HEADER] [19:BLACK] [20:EXP_6MO] [21:EXP_1YR] [22:EXP_2YR] [23:EXP_3YR] [24:EXP_4YR] [25:EXP_5YR] [26:EXP_5PLUS]
Row 4: [27:DAY_HEADER] [28:BLACK] [29:MON] [30:TUE] [31:WED] [32:THU] [33:FRI] [34:SAT] [35:SUN]
Row 5: [36:SKILL_HEADER] [37:BLACK] [38:CANNON] [39:PVP] [40:DEFENSE] [41:DESIGN] [42:REDSTONE] [43:FARM] [44:FISH]
Row 6: [45:PINK] [46:WHITE] [47:PINK] [48:WHITE] [49:SAVE_BTN] [50:CONTINUE_BTN] [51:PINK] [52:WHITE] [53:PINK]
```

### Key Difference: ALL FIELDS ARE CHECKBOXES (Multiple Selection)

**Faction Head Display** (Slot 4):
- **Item**: Faction banner or leader's head
- **Display Name**: "§6{faction_name} Requirements"
- **Lore**:
  ```
  - "§7Faction: §f{faction_name}"
  - "§7Leader: §f{leader_name}"
  - "§7Members: §f{member_count}/{max_members}"
  - ""
  - "§eSet your recruitment requirements below"
  ```

### All Categories Use Checkboxes:
- **Desired Timezones**: Can select multiple regions
- **Experience Levels**: Can accept multiple experience ranges
- **Required Days**: Can require multiple days
- **Desired Skills**: Can seek multiple skills

### Validation:
- Must select at least 1 option in each category
- Save requirements to database
- Auto-hide faction from browser if member limit reached

---

## Player Info Display UI
**Command**: `/recruit <player_name>`  
**Inventory Size**: 27 slots (3 rows × 9 columns)  
**Title**: "§6{player_name}'s Profile"

```
Slot Layout (0-26):
Row 1: [0:WHITE] [1:PINK] [2:WHITE] [3:PINK] [4:WHITE] [5:PINK] [6:WHITE] [7:PINK] [8:WHITE]
Row 2: [9:PINK] [10:BLACK] [11:BLACK] [12:BLACK] [13:PLAYER_HEAD] [14:BLACK] [15:BLACK] [16:BLACK] [17:PINK]
Row 3: [18:WHITE] [19:PINK] [20:WHITE] [21:PINK] [22:LECTERN/WHITE] [23:PINK] [24:WHITE] [25:PINK] [26:WHITE]
```

### Elements:
**Player Head (Slot 13)**: Shows complete resume in lore
**Lectern (Slot 22)**: Only visible if viewer is faction owner
- **Click**: Open invitation confirmation dialog

---

## Invitation Confirmation Dialog

**Inventory Size**: 27 slots (3 rows × 9 columns)  
**Title**: "§6Send Invitation to {player_name}"

```
Slot Layout (0-26):
Row 1: [0:RED] [1:RED] [2:RED] [3:PINK] [4:WHITE] [5:PINK] [6:GREEN] [7:GREEN] [8:GREEN]
Row 2: [9:RED] [10:RED] [11:RED] [12:WHITE] [13:PLAYER_HEAD] [14:WHITE] [15:GREEN] [16:GREEN] [17:GREEN]
Row 3: [18:RED] [19:RED] [20:RED] [21:PINK] [22:WHITE] [23:PINK] [24:GREEN] [25:GREEN] [26:GREEN]
```

### Actions:
- **Red Side**: Cancel invitation
- **Green Side**: Send invitation (3-day expiry)
- **Center**: Player head with resume preview

---

## Application Confirmation Dialog

**Inventory Size**: 27 slots (3 rows × 9 columns)  
**Title**: "§6Apply to {faction_name}"

```
Slot Layout (0-26):
Row 1: [0:RED] [1:RED] [2:RED] [3:PINK] [4:WHITE] [5:PINK] [6:GREEN] [7:GREEN] [8:GREEN]
Row 2: [9:RED] [10:RED] [11:RED] [12:WHITE] [13:FACTION_BANNER] [14:WHITE] [15:GREEN] [16:GREEN] [17:GREEN]
Row 3: [18:RED] [19:RED] [20:RED] [21:PINK] [22:WHITE] [23:PINK] [24:GREEN] [25:GREEN] [26:GREEN]
```

### Elements:
**Faction Banner (Slot 13)**:
- **Display Name**: "§6{faction_name}"
- **Lore**:
  ```
  - "§7Leader: §f{leader_name}"
  - "§7Members: §f{member_count}/{max_members}"
  - "§7Recruiting: §f{timezone_list}"
  - "§7Looking for: §f{skill_list}"
  - ""
  - "§eApplications remaining: §f{remaining}/3"
  - "§7Application expires in 3 days"
  ```

### Actions:
- **Red Side**: Cancel application
- **Green Side**: Submit application (uses 1 of 3 slots)

---

## Manage Applications UI (Faction Owner)

**Inventory Size**: 27 slots (3 rows × 9 columns)  
**Title**: "§6{faction_name} Management"

```
Slot Layout (0-26):
Row 1: [0:WHITE] [1:PINK] [2:WHITE] [3:PINK] [4:WHITE] [5:PINK] [6:WHITE] [7:PINK] [8:WHITE]
Row 2: [9:PINK] [10:WHITE] [11:LECTERN] [12:WHITE] [13:BANNER] [14:WHITE] [15:EMERALD/REDSTONE] [16:WHITE] [17:PINK]
Row 3: [18:WHITE] [19:PINK] [20:WHITE] [21:PINK] [22:BOOKSHELF] [23:PINK] [24:WHITE] [25:PINK] [26:WHITE]
```

### Elements:
- **Lectern (11)**: Edit faction requirements
- **Banner (13)**: Faction display with current status
- **Emerald/Redstone (15)**: Toggle accepting applications
  - Emerald: "§aAccepting Applications"
  - Redstone: "§cNot Accepting Applications"
- **Bookshelf (22)**: View pending applications

---

## Manage Resume UI (Player)

**Inventory Size**: 27 slots (3 rows × 9 columns)  
**Title**: "§6Your Profile Management"

```
Slot Layout (0-26):
Row 1: [0:WHITE] [1:PINK] [2:WHITE] [3:PINK] [4:WHITE] [5:PINK] [6:WHITE] [7:PINK] [8:WHITE]
Row 2: [9:PINK] [10:WHITE] [11:LECTERN] [12:WHITE] [13:PLAYER_HEAD] [14:WHITE] [15:EMERALD/REDSTONE] [16:WHITE] [17:PINK]
Row 3: [18:WHITE] [19:PINK] [20:WHITE] [21:PINK] [22:BOOKSHELF] [23:PINK] [24:WHITE] [25:PINK] [26:WHITE]
```

### Elements:
- **Lectern (11)**: Edit resume
- **Player Head (13)**: Current resume display
- **Emerald/Redstone (15)**: Toggle "looking for faction" status
  - Emerald: "§aVisible to faction owners"
  - Redstone: "§7Hidden from faction owners"
- **Bookshelf (22)**: View pending applications/invitations

---

## Pending Applications UI

### Faction Owner Mode

**Inventory Size**: 54 slots (6 rows × 9 columns)  
**Title**: "§6Applications to {faction_name}"

```
Slot Layout (0-53):
Row 1: [0:PINK] [1:WHITE] [2:PINK] [3:WHITE] [4:FACTION_BANNER] [5:WHITE] [6:PINK] [7:WHITE] [8:PINK]
Row 2: [9:WHITE] [10:EMPTY] [11:EMPTY] [12:EMPTY] [13:EMPTY] [14:EMPTY] [15:EMPTY] [16:EMPTY] [17:WHITE]
Row 3: [18:PINK] [19:EMPTY] [20:EMPTY] [21:EMPTY] [22:EMPTY] [23:EMPTY] [24:EMPTY] [25:EMPTY] [26:WHITE]
Row 4: [27:WHITE] [28:EMPTY] [29:EMPTY] [30:EMPTY] [31:EMPTY] [32:EMPTY] [33:EMPTY] [34:EMPTY] [35:WHITE]
Row 5: [36:PINK] [37:EMPTY] [38:EMPTY] [39:EMPTY] [40:EMPTY] [41:EMPTY] [42:EMPTY] [43:EMPTY] [44:WHITE]
Row 6: [45:ARROW/WHITE] [46:WHITE] [47:PINK] [48:BOOK] [49:BARRIER] [50:COMPASS] [51:PINK] [52:HOPPER] [53:ARROW/WHITE]
```

**Content**: Player heads of applicants in slots 10-43
**Click Action**: Open PlayerInfoUI with invitation option

### Player Mode

**Inventory Size**: 27 slots (3 rows × 9 columns)  
**Title**: "§6Your Applications"

```
Slot Layout (0-26):
Row 1: [0:WHITE] [1:PINK] [2:WHITE] [3:PINK] [4:WHITE] [5:PINK] [6:WHITE] [7:PINK] [8:WHITE]
Row 2: [9:PINK] [10:WHITE] [11:PINK] [12:APP_SLOT_1] [13:APP_SLOT_2] [14:APP_SLOT_3] [15:PINK] [16:WHITE] [17:PINK]
Row 3: [18:WHITE] [19:PINK] [20:WHITE] [21:PINK] [22:BARRIER] [23:PINK] [24:WHITE] [25:PINK] [26:WHITE]
```

**Application Slots (12-14)**:
- **Item**: Faction banner if active application, Black Glass if empty
- **Display Name**: "§6{faction_name}" or "§7Empty Slot"
- **Lore**:
  ```
  - "§7Status: §ePending"
  - "§7Expires: §f{time_remaining}"
  - "§7Applied: §f{date}"
  - ""
  - "§cRight-click to cancel"
  ```
- **Right-Click**: Cancel application (only after 24 hours)

---

## Filter UI

Uses the same layout as the Editor UI but functions as a filter system. All selections create dynamic queries for the browser display.

**Key Features**:
- Multiple selections create OR conditions within categories
- Different categories create AND conditions between categories
- Reset button clears all filters
- Filters persist until menu closed or reset

**Search Logic**:
- Partial name matching
- Must match at least 50% of specified criteria
- Case-insensitive search

---

## Notification System

### Login Notifications
Players receive chat messages on login for:

```yaml
Messages:
  expired-applications: "§cYou have expired applications. Use /recruit to check status."
  new-invitations: "§6You have pending faction invitations! Use /recruit to view them."
  available-slots: "§aYou have %count% application slots available."
  resume-expired: "§cYour resume is no longer visible. Use /recruit to refresh it."
```

### Real-time Notifications
```yaml
Messages:
  application-sent: "§aApplication sent to %faction%! Expires in 3 days."
  application-received: "§6%player% has applied to join your faction!"
  invitation-sent: "§aInvitation sent to %player%! Expires in 3 days."
  invitation-received: "§6You have been invited to join %faction%!"
  application-accepted: "§aYour application to %faction% has been accepted!"
  application-rejected: "§cYour application to %faction% has been rejected."
  invitation-accepted: "§a%player% has accepted your faction invitation!"
  invitation-rejected: "§c%player% has declined your faction invitation."
```

---

## Data Management & Automation

### Automatic Cleanup Tasks (Run every hour):

1. **Expire Applications**: Mark applications past 3 days as EXPIRED
2. **Expire Invitations**: Mark invitations past 3 days as EXPIRED
3. **Free Application Slots**: Make slots available after 3 days regardless of status
4. **Hide Expired Resumes**: Remove resumes from display after 3 days of being active
5. **Update Notifications**: Set login notification flags for affected players
6. **Clean Old Data**: Delete expired records older than 30 days

### PlaceholderAPI Integration:

```yaml
Required Placeholders:
  - %factions_faction% - Player's faction name
  - %factions_leader% - Player's faction leader
  - %factions_role% - Player's role in faction
  - %factions_faction_size% - Current faction member count
  - %factions_has_faction% - Boolean if player has faction
  - %factions_is_leader% - Boolean if player is faction leader
```

### Edge Case Handling:

1. **Faction Disbanded**: All applications/invitations marked as REJECTED
2. **Player Joins Faction**: All other invitations REJECTED, all applications CANCELLED
3. **Leader Change**: Application manager transfers to new leader automatically
4. **Player Banned**: Resume hidden, applications cancelled after 7 days
5. **Faction Full**: Automatically hidden from browser, applications marked REJECTED
6. **Server Restart**: All UI sessions closed gracefully

---

## Permission Nodes

```yaml
Permissions:
  factionrecruitment.use: true # Basic access to /recruit
  factionrecruitment.bypass.cooldown: false # Skip edit cooldowns
  factionrecruitment.admin: false # Administrative commands
  factionrecruitment.reload: false # Reload configuration

Default Permissions:
  - factionrecruitment.use: true (all players)
```

---

## Administrative Commands

```yaml
Commands:
  /recruitadmin reload: Reload configuration
  /recruitadmin cleanup: Force cleanup of expired data
  /recruitadmin stats: Show system statistics
  /recruitadmin reset <player>: Reset player's recruitment data
  /recruitadmin faction <faction> status: Show faction recruitment status
```

---

## Success Metrics & Monitoring

### Key Performance Indicators:
- Applications submitted per day
- Successful recruitment rate (applications → joins)
- Resume completion rate
- Average time from application to acceptance
- Most sought-after skills/timezones

### Database Indexes for Performance:
```sql
CREATE INDEX idx_player_resumes_looking ON player_resumes(is_looking, display_until);
CREATE INDEX idx_faction_applications_active ON faction_applications(is_accepting);
CREATE INDEX idx_applications_status_expiry ON recruitment_requests(status, expires_at);
CREATE INDEX idx_invitations_status_expiry ON faction_invitations(status, expires_at);
```

This specification provides a complete implementation guide with detailed UI layouts, database schemas, automation logic, and edge case handling for a comprehensive faction recruitment system.