package com.gnut.factionsrecruit.util;

import com.gnut.factionsrecruit.FactionsRecruit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages plugin configuration loading and validation
 */
public class ConfigManager {

    private final FactionsRecruit plugin;
    private FileConfiguration config;

    public ConfigManager(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads and validates the plugin configuration
     *
     * @return true if configuration loaded successfully, false otherwise
     */
    public boolean loadConfig() {
        try {
            // Save default config if not exists
            plugin.saveDefaultConfig();

            // Reload config from disk
            plugin.reloadConfig();
            this.config = plugin.getConfig();

            // Validate required configuration sections
            if (!validateConfig()) {
                plugin.getLogger().severe("Configuration validation failed!");
                return false;
            }

            plugin.getLogger().info("Configuration loaded and validated successfully");
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Error loading configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validates the configuration file for required sections and values
     *
     * @return true if configuration is valid, false otherwise
     */
    private boolean validateConfig() {
        boolean valid = true;

        // Validate database configuration
        if (!config.contains("database")) {
            plugin.getLogger().severe("Missing 'database' section in config.yml");
            valid = false;
        } else {
            if (!config.contains("database.host")) {
                plugin.getLogger().severe("Missing 'database.host' in config.yml");
                valid = false;
            }
            if (!config.contains("database.port")) {
                plugin.getLogger().severe("Missing 'database.port' in config.yml");
                valid = false;
            }
            if (!config.contains("database.database")) {
                plugin.getLogger().severe("Missing 'database.database' in config.yml");
                valid = false;
            }
            if (!config.contains("database.username")) {
                plugin.getLogger().severe("Missing 'database.username' in config.yml");
                valid = false;
            }
            if (!config.contains("database.password")) {
                plugin.getLogger().warning("Missing 'database.password' in config.yml (using empty password)");
            }
        }

        // Validate expiration settings
        if (!config.contains("expiration")) {
            plugin.getLogger().warning("Missing 'expiration' section, using default values");
        }

        // Validate GUI settings
        if (!config.contains("gui")) {
            plugin.getLogger().warning("Missing 'gui' section, using default values");
        }

        // Validate message settings
        if (!config.contains("messages")) {
            plugin.getLogger().warning("Missing 'messages' section, using default values");
        }

        return valid;
    }

    /**
     * Gets the configuration object
     *
     * @return The FileConfiguration instance
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Gets a database configuration value
     *
     * @param key The configuration key under 'database'
     * @param defaultValue The default value if key not found
     * @return The configuration value
     */
    public String getDatabaseConfig(String key, String defaultValue) {
        return config.getString("database." + key, defaultValue);
    }

    /**
     * Gets an integer database configuration value
     *
     * @param key The configuration key under 'database'
     * @param defaultValue The default value if key not found
     * @return The configuration value
     */
    public int getDatabaseConfigInt(String key, int defaultValue) {
        return config.getInt("database." + key, defaultValue);
    }

    /**
     * Gets a message from configuration
     *
     * @param key The message key
     * @param defaultValue The default message if key not found
     * @return The message
     */
    public String getMessage(String key, String defaultValue) {
        return config.getString("messages." + key, defaultValue);
    }

    /**
     * Gets GUI items per page setting
     *
     * @return Number of items per page
     */
    public int getItemsPerPage() {
        return config.getInt("gui.items-per-page", 28);
    }

    /**
     * Gets application expiry time in hours
     *
     * @return Application expiry time in hours
     */
    public int getApplicationExpiryHours() {
        return config.getInt("expiration.application-hours", 168); // 7 days default
    }

    /**
     * Gets invitation expiry time in hours
     *
     * @return Invitation expiry time in hours
     */
    public int getInvitationExpiryHours() {
        return config.getInt("expiration.invitation-hours", 72); // 3 days default
    }

    /**
     * Gets cleanup interval in minutes
     *
     * @return Cleanup interval in minutes
     */
    public long getCleanupIntervalMinutes() {
        return config.getLong("cleanup.interval-minutes", 60); // 1 hour default
    }
}