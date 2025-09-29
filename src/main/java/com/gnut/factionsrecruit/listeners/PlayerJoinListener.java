package com.gnut.factionsrecruit.listeners;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handles player join events
 *
 * Creates a player profile in the database if it doesn't exist
 */
public class PlayerJoinListener implements Listener {

    private final FactionsRecruit plugin;

    public PlayerJoinListener(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        DatabaseManager db = plugin.getDatabaseManager();

        // Create player profile asynchronously to avoid blocking login
        db.playerExists(player.getUniqueId()).thenAccept(exists -> {
            if (!exists) {
                // Create new player profile with default values
                db.createPlayerProfile(player.getUniqueId(), player.getName()).thenRun(() -> {
                    plugin.getLogger().info("Created recruitment profile for player: " + player.getName());
                });
            } else {
                // Update last seen timestamp
                db.updateLastSeen(player.getUniqueId());
            }
        });
    }
}