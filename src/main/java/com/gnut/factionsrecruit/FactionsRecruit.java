package com.gnut.factionsrecruit;

import com.gnut.factionsrecruit.commands.RecruitCommand;
import com.gnut.factionsrecruit.database.DatabaseManager;
import com.gnut.factionsrecruit.integration.PlaceholderAPIIntegration;
import com.gnut.factionsrecruit.listeners.InventoryClickListener;
import com.gnut.factionsrecruit.listeners.PlayerJoinListener;
import com.gnut.factionsrecruit.listeners.PlayerQuitListener;
import com.gnut.factionsrecruit.util.ConfigManager;
import com.gnut.factionsrecruit.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

/**
 * FactionsRecruit - Comprehensive recruitment system for factions
 *
 * This plugin provides GUI-based interfaces for players to find factions
 * and for faction leaders to recruit new members.
 */
public class FactionsRecruit extends JavaPlugin {

    private static FactionsRecruit instance;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;

    // Cleanup task ID for scheduled maintenance
    private int cleanupTaskId = -1;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration manager
        getLogger().info("Loading configuration...");
        this.configManager = new ConfigManager(this);
        if (!configManager.loadConfig()) {
            getLogger().severe("Failed to load configuration! Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize PlaceholderAPI integration
        getLogger().info("Initializing PlaceholderAPI integration...");
        PlaceholderAPIIntegration.initialize();

        // Initialize database manager
        getLogger().info("Initializing database connection...");
        this.databaseManager = new DatabaseManager(this);

        // Initialize database asynchronously to avoid blocking server startup
        databaseManager.initialize().thenAccept(success -> {
            if (success) {
                getLogger().info("Database connection established successfully!");

                // Schedule cleanup tasks for expired applications/invitations
                Bukkit.getScheduler().runTask(this, this::scheduleCleanupTasks);
            } else {
                getLogger().severe("Failed to initialize database! Plugin will be disabled.");
                Bukkit.getScheduler().runTask(this, () ->
                    getServer().getPluginManager().disablePlugin(this)
                );
            }
        }).exceptionally(throwable -> {
            getLogger().severe("Error during database initialization: " + throwable.getMessage());
            throwable.printStackTrace();
            Bukkit.getScheduler().runTask(this, () ->
                getServer().getPluginManager().disablePlugin(this)
            );
            return null;
        });

        // Register commands
        registerCommands();

        // Register event listeners
        registerListeners();

        // Log successful initialization
        getLogger().info(VisualUtils.SmallCaps.convert("FactionsRecruit") + " has been enabled!");
        getLogger().info("Version: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        // Cancel cleanup task
        if (cleanupTaskId != -1) {
            Bukkit.getScheduler().cancelTask(cleanupTaskId);
            getLogger().info("Cancelled cleanup task");
        }

        // Close database connections gracefully
        if (databaseManager != null) {
            getLogger().info("Closing database connections...");
            try {
                databaseManager.shutdown();
                getLogger().info("Database connections closed successfully");
            } catch (Exception e) {
                getLogger().warning("Error while closing database connections: " + e.getMessage());
            }
        }

        // Clear instance reference
        instance = null;

        getLogger().info("FactionsRecruit has been disabled");
    }

    /**
     * Registers all plugin commands
     */
    private void registerCommands() {
        getLogger().info("Registering commands...");

        // Register main /recruit command
        RecruitCommand recruitCommand = new RecruitCommand(this);
        getCommand("recruit").setExecutor(recruitCommand);
        getCommand("recruit").setTabCompleter(recruitCommand);

        getLogger().info("Commands registered successfully");
    }

    /**
     * Registers all event listeners
     */
    private void registerListeners() {
        getLogger().info("Registering event listeners...");

        PluginManager pm = getServer().getPluginManager();

        // Register player join listener (creates player profile if not exists)
        pm.registerEvents(new PlayerJoinListener(this), this);

        // Register player quit listener (updates session tracking)
        pm.registerEvents(new PlayerQuitListener(this), this);

        // Register inventory click listener (handles GUI interactions)
        pm.registerEvents(new InventoryClickListener(this), this);

        getLogger().info("Event listeners registered successfully");
    }

    /**
     * Schedules periodic cleanup tasks for expired applications and invitations
     */
    private void scheduleCleanupTasks() {
        // Get cleanup interval from config (default: 1 hour)
        long cleanupIntervalMinutes = configManager.getConfig().getLong("cleanup.interval-minutes", 60);
        long cleanupIntervalTicks = cleanupIntervalMinutes * 60 * 20; // Convert minutes to ticks

        getLogger().info("Scheduling cleanup task to run every " + cleanupIntervalMinutes + " minutes");

        // Schedule repeating task
        cleanupTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                getLogger().info("Running cleanup task for expired applications and invitations...");

                // Cleanup expired applications (async)
                databaseManager.cleanupExpiredApplications().thenAccept(expiredApps -> {
                    if (expiredApps > 0) {
                        getLogger().info("Cleaned up " + expiredApps + " expired applications");
                    }
                });

                // Cleanup expired invitations (async)
                databaseManager.cleanupExpiredInvitations().thenAccept(expiredInvites -> {
                    if (expiredInvites > 0) {
                        getLogger().info("Cleaned up " + expiredInvites + " expired invitations");
                    }
                });
            } catch (Exception e) {
                getLogger().warning("Error during cleanup task: " + e.getMessage());
                e.printStackTrace();
            }
        }, cleanupIntervalTicks, cleanupIntervalTicks).getTaskId();
    }

    /**
     * Gets the plugin instance
     *
     * @return The plugin instance
     */
    public static FactionsRecruit getInstance() {
        return instance;
    }

    /**
     * Gets the database manager
     *
     * @return The database manager instance
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Gets the configuration manager
     *
     * @return The configuration manager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Reloads the plugin configuration and reconnects to database if needed
     */
    public void reloadPluginConfig() {
        getLogger().info("Reloading plugin configuration...");

        // Reload config
        if (!configManager.loadConfig()) {
            getLogger().warning("Failed to reload configuration!");
            return;
        }

        getLogger().info("Configuration reloaded successfully");
        MessageUtil.sendConsoleMessage("Configuration reloaded", true);
    }
}