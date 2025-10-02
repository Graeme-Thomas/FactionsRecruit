# FactionsRecruit Plugin

## Project Overview

FactionsRecruit is a Spigot/Bukkit plugin designed to streamline the recruitment process for Factions servers. It provides an intuitive in-game browser interface where players can create and manage resumes, browse factions, submit applications, and handle invitations. The plugin integrates with PlaceholderAPI for dynamic information display and uses a MySQL database for persistent storage of player resumes, faction applications, and recruitment data.

**Key Features:**
*   Player resume creation and management.
*   Faction application management for leaders, including setting requirements.
*   In-game GUI for browsing players and factions.
*   Application and invitation system with configurable timeouts and cooldowns.
*   Login and real-time notification system.
*   PlaceholderAPI integration for FactionsUUID data.
*   Comprehensive database schema for robust data storage.
*   Automated cleanup tasks for expired data.

## Technologies Used

*   **Language:** Java 8+
*   **Build System:** Apache Maven
*   **Minecraft API:** Spigot API (version 1.19+)
*   **Database:** MySQL
*   **Dependencies:**
    *   FactionsUUID (implied by PAPI integration)
    *   PlaceholderAPI
    *   MySQL Connector/J

## Building and Running

This project uses Maven for dependency management and building.

### Prerequisites

*   Java Development Kit (JDK) 8 or higher.
*   Apache Maven.

### Build Instructions

To compile the plugin and package it into a JAR file, navigate to the project's root directory (where `pom.xml` is located) and run the following command:

```bash
mvn clean package
```

This command will:
1.  Clean any previous build artifacts.
2.  Compile the Java source code.
3.  Run tests (if any).
4.  Package the plugin and its dependencies into a single JAR file (due to the `maven-shade-plugin`) in the `target/` directory. The resulting JAR will typically be named `factionsrecruit-1.0-SNAPSHOT.jar`.

### Deployment

1.  After building, locate the `factionsrecruit-1.0-SNAPSHOT.jar` file in the `target/` directory.
2.  Copy this JAR file to the `plugins/` directory of your Spigot/Bukkit server.
3.  Ensure your `config.yml` (generated in the `plugins/FactionsRecruit/` folder after the first run) is correctly configured, especially the MySQL database settings.
4.  Start or restart your Minecraft server.

### Configuration

The plugin's main configuration file is `config.yml`, located in `src/main/resources/`. This file defines:
*   GUI titles and messages.
*   Timeouts for recruitment requests and invitations.
*   Cooldowns for sending invitations.
*   MySQL database connection details.

Example `config.yml` snippet:

```yaml
# FactionsRecruit Configuration

# GUI Settings
gui_title: "&6Faction Recruitment Browser"

# Messages
messages:
  prefix: "&8[&6FactionsRecruit&8] &r"
  # ... other messages ...

# Settings
settings:
  debug_mode: false
  recruitment_request_timeout_seconds: 60 # How long a recruitment request is valid
  invitation_timeout_seconds: 60 # How long an invitation is valid
  recruitment_cooldown_hours: 8 # Cooldown for sending recruitment invitations

# Database Settings
database:
  host: localhost
  port: 3306
  name: factionsrecruit
  username: root
  password: 9342850_sql
```

## Development Conventions

*   **Code Style:** Standard Java conventions.
*   **Plugin Structure:** Follows typical Spigot plugin architecture with a main class extending `JavaPlugin`, command executors, and event listeners.
*   **Database Interaction:** Handled by `DatabaseManager`.
*   **Configuration Management:** Handled by `ConfigManager`.
*   **PlaceholderAPI Integration:** Managed by `PAPIIntegrationManager`.
*   **UI/GUI:** Implemented using Bukkit's inventory API, as detailed in `SPEC.md`.

## Important Files

*   `pom.xml`: Maven project configuration, dependencies, and build plugins.
*   `src/main/resources/plugin.yml`: Plugin metadata (name, version, main class, commands).
*   `src/main/resources/config.yml`: Default plugin configuration.
*   `src/main/java/com/dirtygang/factionsrecruit/FactionsRecruit.java`: The main plugin class, entry point for the plugin.
*   `PAPI.md`: Documentation for PlaceholderAPI placeholders supported by the plugin.
*   `SPEC.md`: Detailed specification of the plugin's features, UI layouts, database schema, and logic. This document is crucial for understanding the plugin's design and functionality.

## Administrative Commands

The plugin provides several administrative commands, typically accessible via `/recruitadmin`:

*   `/recruitadmin reload`: Reloads the plugin's configuration.
*   `/recruitadmin cleanup`: Forces an immediate cleanup of expired data.
*   `/recruitadmin stats`: Displays plugin statistics.
*   `/recruitadmin reset <player>`: Resets a specific player's recruitment data.
*   `/recruitadmin faction <faction> status`: Shows the recruitment status for a given faction.

## Permissions

*   `factionrecruitment.use`: Allows basic access to the `/recruit` command (default for all players).
*   `factionrecruitment.bypass.cooldown`: Bypasses recruitment cooldowns.
*   `factionrecruitment.admin`: Grants access to administrative commands.
*   `factionrecruitment.reload`: Allows reloading the configuration.
