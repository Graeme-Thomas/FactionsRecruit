package com.gnut.factionsrecruit.listeners;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player quit events
 *
 * Updates session tracking when players leave the server
 */
public class PlayerQuitListener implements Listener {

    private final FactionsRecruit plugin;

    public PlayerQuitListener(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DatabaseManager db = plugin.getDatabaseManager();

        // Update last seen timestamp asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                db.updateLastSeen(player.getUniqueId());
            } catch (Exception e) {
                plugin.getLogger().warning("Error updating last seen for " + player.getName() + ": " + e.getMessage());
            }
        });
    }
}