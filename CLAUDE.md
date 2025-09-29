# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FactionsRecruit is a Minecraft server plugin built with Maven that provides a comprehensive recruitment system for factions. The plugin creates GUI-based interfaces for players to find factions and for faction leaders to recruit new members.

## Build and Development Commands

```bash
# Build the project
mvn clean compile

# Package the plugin JAR
mvn clean package

# Install to local repository
mvn clean install

# Run tests (when implemented)
mvn test
```

## Architecture

### Core Components

- **GUI System**: Located in `src/main/java/com/gnut/factionsrecruit/interfaces/`
  - All GUI classes follow a consistent naming pattern: `*GUI.java`
  - Each GUI represents a different screen in the recruitment workflow
  - Currently only skeleton files exist - implementation needed

- **Visual Utilities**: `src/main/java/com/gnut/factionsrecruit/util/VisualUtils.java`
  - Comprehensive theming system with server-specific white/pink/red color scheme
  - Gradient text effects, small caps conversion, and enhanced item creation
  - Server-themed decorative elements and symbols
  - All GUI elements should use these utilities for consistent styling

- **Database Schema**: `src/main/java/com/gnut/factionsrecruit/databasemanagement/schema.txt`
  - Database: `factions_recruitment`
  - Tables: `player_resume`, `factions_applications`, `invites`, `applications`
  - Handles player profiles, faction recruitment status, and application workflow

### GUI Design System

The UI follows a strict design specification found in `UIDESCRIPTIONS.md`:

- **Theme**: Pink and white glass pane alternating border pattern
- **Layout**: 6-row chest inventory (slots 0-53)
- **Navigation**: All UIs must have close/back button in middle of bottom row (slot 49)
- **Pagination**: Forward/backward arrows when needed (slots 45/53)
- **Color Scheme**: Server theme uses white (#FFFFFF) to pink (#FF69B4) to red (#FF1744) gradients

### GUI Workflow

1. **LandingUI**: Main entry point with options for faction listings, player listings, status, and settings
2. **FactionsRecruitingGUI**: Browse factions looking for members
3. **LookingForFactionsGUI**: Browse players seeking factions
4. **PlayerApplicationsGUI**: Manage incoming/outgoing applications and invitations
5. **ApplicationSettingsGUI**: Configure player recruitment profile
6. **Support GUIs**: OneToTenGUI, TimeZoneSelectionGUI, AvailabilitySelectionGUI

## Development Guidelines

### Visual Consistency
- Always use `VisualUtils` methods for item creation and text formatting
- Follow server theme: white/pink/red gradients with small caps text
- Use `VisualUtils.createServerItem()` for GUI items
- Use `VisualUtils.createCompactServerTitle()` for GUI titles (under 30 chars)

### GUI Implementation Pattern
Each GUI should:
- Extend appropriate Bukkit inventory holder interface
- Implement the exact slot layout specified in UIDESCRIPTIONS.md
- Use pink/white glass panes for borders (slots 0-8, 9,17, 18,26, 27,35, 36,44, 45-53)
- Handle click events for navigation and functionality
- Maintain consistent visual theming

### Database Integration
- Use the schema defined in `schema.txt`
- Player data stored in `player_resume` table with timezone, skills, availability
- Application workflow managed through `invites` and `applications` tables
- Faction recruitment status tracked in `factions_applications` table

## Package Structure

```
com.gnut.factionsrecruit/
├── interfaces/          # All GUI classes
├── util/               # Utilities (VisualUtils, etc.)
└── databasemanagement/ # Database schema and related files
```

## Current State

- VisualUtils is fully implemented with comprehensive theming system
- All GUI classes exist as empty files - need implementation
- Database schema is defined but database integration needs implementation
- UI specifications are detailed in UIDESCRIPTIONS.md

## Next Development Steps

1. Implement database connection and management system
2. Create GUI base classes following the design specifications
3. Implement each GUI according to its slot layout in UIDESCRIPTIONS.md
4. Add event handlers for player interactions
5. Integrate with existing faction plugin APIs
6. Add configuration system for customization