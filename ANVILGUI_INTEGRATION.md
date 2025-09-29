# AnvilGUI Integration Guide

## Overview

The FactionsRecruit plugin uses **AnvilGUI** by WesJD for text input dialogs. This provides a clean, native Minecraft interface for collecting text input from players without relying on chat-based input systems.

---

## Architecture

### Integration Components

1. **AnvilInputUtil.java** - Comprehensive AnvilGUI wrapper
   - Location: `src/main/java/com/gnut/factionsrecruit/util/AnvilInputUtil.java`
   - Provides themed, validated text input dialogs
   - Handles all text input needs across the plugin
   - Integrates with server theme (white/pink/red gradients)

2. **AnvilGUI Library** (External Dependency)
   - Version: 1.10.8-SNAPSHOT
   - Provides core anvil GUI functionality
   - Supports all Minecraft versions (Paper/Spigot)

---

## Dependency Configuration

### pom.xml
```xml
<repositories>
    <repository>
        <id>codemc-snapshots</id>
        <url>https://repo.codemc.io/repository/maven-snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>net.wesjd</groupId>
        <artifactId>anvilgui</artifactId>
        <version>1.10.8-SNAPSHOT</version>
    </dependency>
</dependencies>
```

---

## Available Input Methods

### 1. **Simple Text Input**
Basic text input with optional validation.

```java
AnvilInputUtil.openTextInput(
    plugin,
    player,
    "Enter Name",           // Title
    "Default Text",         // Initial text
    text -> {
        // Callback with entered text
        player.sendMessage("You entered: " + text);
    },
    () -> {
        // Callback when player closes without submitting
        player.sendMessage("Cancelled");
    }
);
```

### 2. **Text Input with Validation**
Validates input before accepting.

```java
AnvilInputUtil.openTextInput(
    plugin,
    player,
    "Enter Name",
    "",
    text -> {
        // Validator - return error message or null if valid
        if (text.length() < 3) {
            return "Name must be at least 3 characters!";
        }
        if (text.length() > 16) {
            return "Name must be 16 characters or less!";
        }
        return null; // Valid
    },
    text -> {
        // Callback with validated text
        player.sendMessage("Valid name: " + text);
    },
    () -> player.sendMessage("Cancelled")
);
```

### 3. **Discord Username Input**
Pre-configured Discord username validation.

```java
AnvilInputUtil.openDiscordInput(
    plugin,
    player,
    "CurrentUser#1234",     // Current Discord (or null)
    newDiscord -> {
        // Callback with new Discord username
        player.sendMessage("Discord set to: " + newDiscord);
    },
    () -> player.sendMessage("Cancelled")
);
```

**Validation Rules:**
- 2-32 characters
- Alphanumeric + underscore + period only
- Non-empty

### 4. **Faction Name Input**
Pre-configured faction name validation.

```java
AnvilInputUtil.openFactionNameInput(
    plugin,
    player,
    factionName -> {
        // Callback with faction name
        player.sendMessage("Faction name: " + factionName);
    },
    () -> player.sendMessage("Cancelled")
);
```

**Validation Rules:**
- 1-20 characters
- Alphanumeric + underscore only
- Non-empty

### 5. **Numeric Input**
Validates integer input with min/max bounds.

```java
AnvilInputUtil.openNumericInput(
    plugin,
    player,
    "Enter Age",
    1,                      // Minimum value
    100,                    // Maximum value
    age -> {
        // Callback with parsed integer
        player.sendMessage("Age: " + age);
    },
    () -> player.sendMessage("Cancelled")
);
```

**Validation:**
- Must be a valid integer
- Must be between min and max (inclusive)

### 6. **Confirmation Dialog**
Yes/no confirmation prompt.

```java
AnvilInputUtil.openConfirmation(
    plugin,
    player,
    "Confirm Action",
    "Are you sure you want to delete this faction?",
    () -> {
        // Confirmed - player typed "yes"
        player.sendMessage("Faction deleted!");
    },
    () -> {
        // Cancelled
        player.sendMessage("Action cancelled");
    }
);
```

**Behavior:**
- Player must type "yes", "y", or "confirm"
- Any other text shows error
- Closing dialog = cancel

### 7. **List Input**
Collect multiple items sequentially.

```java
AnvilInputUtil.openListInput(
    plugin,
    player,
    "Enter Faction Names",
    10,                     // Maximum items
    factionList -> {
        // Callback with List<String>
        player.sendMessage("Factions: " + String.join(", ", factionList));
    },
    () -> player.sendMessage("Cancelled")
);
```

**Behavior:**
- Player enters items one at a time
- Type "done" to finish
- Automatically stops at max items
- Shows progress: "Enter Faction Names (3/10)"

### 8. **Multiline Input**
Collect multiple lines of text.

```java
AnvilInputUtil.openMultilineInput(
    plugin,
    player,
    "Enter Description",
    3,                      // Number of lines
    lines -> {
        // Callback with String[] array
        String fullText = String.join(" ", lines);
        player.sendMessage("Description: " + fullText);
    },
    () -> player.sendMessage("Cancelled")
);
```

**Behavior:**
- Sequentially collects N lines
- Shows progress: "Enter Description (Line 2/3)"
- Returns array of strings

---

## Usage in GUIs

### ApplicationSettingsGUI - Discord Input

When player clicks Discord input button (slot 13):

```java
case 13: // Discord input
    AnvilInputUtil.openDiscordInput(
        plugin,
        player,
        currentProfile.getDiscordTag(),
        newDiscord -> {
            // Update profile with new Discord
            currentProfile.setDiscordTag(newDiscord);

            // Save to database (async)
            playerResumeDAO.save(currentProfile).thenAccept(success -> {
                if (success) {
                    MessageUtil.sendSuccess(player, "Discord updated!");

                    // Reopen settings GUI
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        new ApplicationSettingsGUI(plugin, player, /* ... */).open();
                    });
                } else {
                    MessageUtil.sendError(player, "Failed to save profile");
                }
            });
        },
        () -> {
            // Cancelled - reopen settings GUI
            new ApplicationSettingsGUI(plugin, player, /* ... */).open();
        }
    );
    break;
```

### ApplicationSettingsGUI - Previous Factions Input

When player clicks Previous Factions button (slot 33):

```java
case 33: // Previous factions
    // Left-click: Add faction
    if (event.getClick().isLeftClick()) {
        AnvilInputUtil.openListInput(
            plugin,
            player,
            "Previous Factions",
            10,                     // Max 10 factions
            factionList -> {
                // Update profile
                currentProfile.setPreviousFactions(factionList);

                // Save to database
                playerResumeDAO.save(currentProfile).thenAccept(success -> {
                    if (success) {
                        MessageUtil.sendSuccess(player,
                            "Added " + factionList.size() + " factions!");

                        // Reopen GUI
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            new ApplicationSettingsGUI(plugin, player, /* ... */).open();
                        });
                    }
                });
            },
            () -> {
                // Cancelled - reopen GUI
                new ApplicationSettingsGUI(plugin, player, /* ... */).open();
            }
        );
    }
    // Right-click: Clear list
    else if (event.getClick().isRightClick()) {
        AnvilInputUtil.openConfirmation(
            plugin,
            player,
            "Clear Factions",
            "Clear all previous factions?",
            () -> {
                currentProfile.setPreviousFactions(new ArrayList<>());
                playerResumeDAO.save(currentProfile);
                MessageUtil.sendSuccess(player, "Previous factions cleared!");
                new ApplicationSettingsGUI(plugin, player, /* ... */).open();
            },
            () -> {
                new ApplicationSettingsGUI(plugin, player, /* ... */).open();
            }
        );
    }
    break;
```

---

## Visual Theming

All AnvilGUI dialogs are automatically themed with the server's visual style:

### Title Styling
- Converted to small caps: "Enter Text" → "ᴇɴᴛᴇʀ ᴛᴇxᴛ"
- Gradient applied: White → Pink
- Consistent with other GUI elements

### Left Slot Icon
- Discord input: NAME_TAG icon
- Confirmation: LIME_WOOL icon
- Themed with server colors

### Error Messages
- Red color for errors
- Pink color for info
- Green color for success
- Uses VisualUtils color palette

---

## Best Practices

### 1. **Always Provide Callbacks**
Both success and cancel callbacks should be provided:

```java
AnvilInputUtil.openTextInput(
    plugin,
    player,
    "Title",
    "",
    text -> {
        // Success callback
    },
    () -> {
        // IMPORTANT: Cancel callback - reopen parent GUI
        parentGUI.open();
    }
);
```

### 2. **Reopen Parent GUI on Cancel**
When player closes dialog without submitting:

```java
onCancel: () -> {
    // Always return to parent GUI
    Bukkit.getScheduler().runTask(plugin, () -> {
        parentGUI.open();
    });
}
```

### 3. **Async Database Operations**
Save data asynchronously after input:

```java
onComplete: text -> {
    profile.setDiscord(text);

    // Async save
    dao.save(profile).thenAccept(success -> {
        if (success) {
            // Success - reopen GUI on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                gui.open();
            });
        }
    });
}
```

### 4. **Validate Early**
Use built-in validators for common patterns:

```java
// Discord username
AnvilInputUtil.openDiscordInput(/* ... */);

// Faction name
AnvilInputUtil.openFactionNameInput(/* ... */);

// Numeric
AnvilInputUtil.openNumericInput(/* ... */);
```

### 5. **User Feedback**
Always send feedback messages:

```java
onComplete: text -> {
    MessageUtil.sendSuccess(player, "Discord updated!");
    // ... save and reopen
}

onCancel: () -> {
    MessageUtil.sendInfo(player, "Input cancelled");
    // ... reopen parent GUI
}
```

---

## Common Input Scenarios

### Scenario 1: Update Profile Field
```java
// Click Discord button in settings
AnvilInputUtil.openDiscordInput(
    plugin,
    player,
    currentDiscord,
    newDiscord -> {
        profile.setDiscord(newDiscord);
        dao.save(profile).thenAccept(success -> {
            MessageUtil.sendSuccess(player, "Discord updated!");
            settingsGUI.open();
        });
    },
    () -> settingsGUI.open()
);
```

### Scenario 2: Collect Multiple Items
```java
// Add previous factions
AnvilInputUtil.openListInput(
    plugin,
    player,
    "Previous Factions",
    10,
    factions -> {
        profile.setPreviousFactions(factions);
        dao.save(profile);
        MessageUtil.sendSuccess(player, "Added " + factions.size() + " factions!");
        settingsGUI.open();
    },
    () -> settingsGUI.open()
);
```

### Scenario 3: Numeric Range Input
```java
// Set skill level (1-10)
AnvilInputUtil.openNumericInput(
    plugin,
    player,
    "PvP Skill Level",
    1,
    10,
    skillLevel -> {
        profile.setPvpSkill(skillLevel);
        dao.save(profile);
        MessageUtil.sendSuccess(player, "PvP skill set to " + skillLevel);
        settingsGUI.open();
    },
    () -> settingsGUI.open()
);
```

### Scenario 4: Yes/No Confirmation
```java
// Delete application
AnvilInputUtil.openConfirmation(
    plugin,
    player,
    "Delete Application",
    "Permanently delete this application?",
    () -> {
        dao.delete(applicationId);
        MessageUtil.sendSuccess(player, "Application deleted");
        applicationsGUI.open();
    },
    () -> applicationsGUI.open()
);
```

---

## Error Handling

### Invalid Input
When validator returns error message:
- Error message sent to player
- Input field cleared or preserved
- Dialog remains open for retry
- Player can close to cancel

### Example Validation
```java
text -> {
    if (text.length() < 3) {
        return "Too short! (min 3 characters)";
    }
    if (!text.matches("[a-zA-Z0-9]+")) {
        return "Only letters and numbers allowed!";
    }
    return null; // Valid
}
```

### Dialog Closure
When player closes dialog:
- `onCancel` callback is invoked
- Should reopen parent GUI
- Should send cancellation message

---

## Threading Considerations

### Main Thread Operations
AnvilGUI operations must be on main thread:
- Opening dialogs
- Updating inventories
- Reopening GUIs

### Async Operations
Database operations should be async:
- Saving profile data
- Loading data
- Queries

### Correct Pattern
```java
// Dialog opened on main thread
AnvilInputUtil.openTextInput(
    plugin,
    player,
    "Title",
    "",
    text -> {
        // This callback is on main thread

        // Async database save
        dao.save(data).thenAccept(success -> {
            // This is async thread

            if (success) {
                // Return to main thread for GUI
                Bukkit.getScheduler().runTask(plugin, () -> {
                    gui.open();
                });
            }
        });
    },
    () -> {
        // Cancel callback - main thread
        gui.open();
    }
);
```

---

## Advanced Features

### Custom Validation Logic
```java
java.util.function.Function<String, String> customValidator = text -> {
    // Complex validation logic
    if (isPlayerBanned(text)) {
        return "That player is banned!";
    }
    if (factionExists(text)) {
        return "Faction name already taken!";
    }
    return null; // Valid
};

AnvilInputUtil.openTextInput(
    plugin, player, "Title", "", customValidator,
    text -> { /* ... */ },
    () -> { /* ... */ }
);
```

### Sequential Inputs
Collect multiple pieces of data:
```java
// Step 1: Get name
AnvilInputUtil.openTextInput(plugin, player, "Name", "", name -> {
    // Step 2: Get age
    AnvilInputUtil.openNumericInput(plugin, player, "Age", 1, 100, age -> {
        // Step 3: Get Discord
        AnvilInputUtil.openDiscordInput(plugin, player, null, discord -> {
            // All data collected
            createProfile(name, age, discord);
        }, cancelCallback);
    }, cancelCallback);
}, cancelCallback);
```

---

## Troubleshooting

### Dialog Doesn't Open
**Cause**: Not on main thread
**Solution**: Use `Bukkit.getScheduler().runTask()`

### Player Can't Type
**Cause**: Using wrong slot
**Solution**: Player must click OUTPUT slot to submit

### Validation Not Working
**Cause**: Validator returning wrong type
**Solution**: Return `String` for error, `null` for valid

### GUI Doesn't Reopen
**Cause**: Not calling cancel callback
**Solution**: Always provide cancel callback that reopens parent GUI

---

## Summary

✅ **Complete AnvilGUI Integration** - All text input needs covered
✅ **Server Theme Styling** - Consistent visual design
✅ **Built-in Validators** - Discord, faction names, numeric
✅ **Flexible API** - Simple to complex input scenarios
✅ **Thread Safe** - Proper main/async thread handling
✅ **Build Success** - Compiles without errors

**Files Created**: 1 utility class (~350 lines)
**Dependencies Added**: AnvilGUI 1.10.8-SNAPSHOT
**Input Methods**: 8 pre-built input types

The AnvilGUI integration provides a clean, native Minecraft interface for all text input needs throughout the plugin!