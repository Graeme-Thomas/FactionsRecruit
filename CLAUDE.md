# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FactionsRecruit is a Minecraft Spigot plugin that provides a comprehensive recruitment system for Factions servers. It features an in-game GUI browser where players can create resumes, browse factions, submit applications, and manage invitations.

## Build Commands

```bash
# Build the plugin JAR
mvn clean package

# The output JAR will be in target/factionsrecruit-1.0-SNAPSHOT.jar
# Copy this to your server's plugins/ directory
```

## Project Architecture

### Core Components

- **FactionsRecruit.java**: Main plugin class that coordinates all subsystems and manages global state
- **DatabaseManager**: Handles all MySQL database operations and connection management
- **ConfigManager**: Manages configuration loading and provides typed access to settings
- **PAPIIntegrationManager**: Integrates with PlaceholderAPI for FactionsUUID data access
- **GuiManager**: Manages GUI state and provides shared GUI utilities

### GUI System Architecture

The plugin uses a modular GUI system where each major interface has its own class:

- **RecruitGUI**: Main browser interface (54-slot inventory) with player/faction toggle
- **ResumeEditorGUI**: Player resume creation/editing interface with validation
- **FactionApplicationEditorGUI**: Faction leaders set recruitment requirements
- **PlayerInfoGUI**: Display detailed player profiles with invitation options
- **PendingApplicationsGUI**: Manage incoming/outgoing applications
- **FilterGUI**: Advanced filtering for search results

### Data Models

- **PlayerResume**: Player profile data (timezone, experience, skills, availability)
- **FactionApplication**: Faction recruitment requirements and status
- **PlayerApplication**: Individual applications from players to factions
- **FactionInvitation**: Invitations from faction leaders to players
- **LoginNotification**: Tracks notification states for login alerts

### Database Schema

The plugin uses 6 main tables for data persistence:
- `player_resumes`: Player profile information
- `faction_applications`: Faction recruitment settings
- `recruitment_requests`: Player applications to factions
- `faction_invitations`: Faction invitations to players
- `edit_cooldowns`: Rate limiting for edits
- `login_notifications`: Login notification flags

## Key Configuration

Main configuration is in `src/main/resources/config.yml`:
- Database connection settings (MySQL required)
- Cooldown timers for applications and resume edits
- Expiry periods for applications and invitations
- UI sound effects and messaging

PlaceholderAPI integration details are documented in `PAPI.md`.

## Development Guidelines

### GUI Development
- All GUIs use 6x9 (54-slot) inventories with consistent border design
- Color scheme: Pink/White/Red gradient borders with interactive content areas
- Use VisualUtils for consistent item creation and formatting
- Follow the detailed UI specifications in `SPEC.md`

### Database Operations
- All database calls should go through DatabaseManager
- Use prepared statements for all queries
- Handle connection failures gracefully
- Implement proper cleanup for expired data

### State Management
- Player-specific temporary data is stored in the main plugin class using UUID maps
- Use proper cleanup when players disconnect
- Maintain pagination state for browse interfaces

### PlaceholderAPI Integration
- Use PAPIIntegrationManager for all faction data access
- Required placeholders: faction name, leader, member count, has_faction status
- See PAPI.md for complete placeholder reference

## Testing and Deployment

This is a Minecraft plugin, so testing requires:
1. A Spigot/Paper server (1.19+)
2. FactionsUUID plugin installed
3. PlaceholderAPI plugin installed
4. MySQL database configured

The plugin generates comprehensive logs during initialization - check console output for component loading status.

## Important Implementation Notes

- The plugin uses Java 8 compatibility for broad server support
- Maven shade plugin bundles MySQL connector and AnvilGUI dependencies
- Automatic cleanup task runs hourly to expire old applications/invitations
- GUI interactions are handled through Bukkit event system
- Configuration supports live reload via admin commands